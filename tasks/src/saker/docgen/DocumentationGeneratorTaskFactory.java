package saker.docgen;

import java.io.Externalizable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import org.commonmark.Extension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.Code;
import org.commonmark.node.CustomBlock;
import org.commonmark.node.FencedCodeBlock;
import org.commonmark.node.Heading;
import org.commonmark.node.Image;
import org.commonmark.node.Link;
import org.commonmark.node.Node;
import org.commonmark.node.Text;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.CoreHtmlNodeRenderer;
import org.commonmark.renderer.html.HtmlNodeRendererContext;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.renderer.html.HtmlWriter;

import saker.build.exception.InvalidPathFormatException;
import saker.build.file.ByteArraySakerFile;
import saker.build.file.DelegateSakerFile;
import saker.build.file.SakerDirectory;
import saker.build.file.SakerFile;
import saker.build.file.path.SakerPath;
import saker.build.file.path.WildcardPath;
import saker.build.file.provider.SakerPathFiles;
import saker.build.runtime.execution.ExecutionContext;
import saker.build.runtime.execution.SakerLog;
import saker.build.scripting.ScriptParsingFailedException;
import saker.build.task.ParameterizableTask;
import saker.build.task.Task;
import saker.build.task.TaskContext;
import saker.build.task.TaskFactory;
import saker.build.task.TaskName;
import saker.build.task.exception.TaskExecutionFailedException;
import saker.build.task.utils.SimpleStructuredObjectTaskResult;
import saker.build.task.utils.annot.SakerInput;
import saker.build.task.utils.dependencies.EqualityTaskOutputChangeDetector;
import saker.build.task.utils.dependencies.RecursiveIgnoreCaseExtensionFileCollectionStrategy;
import saker.build.task.utils.dependencies.WildcardFileCollectionStrategy;
import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.thirdparty.saker.util.StringUtils;
import saker.build.thirdparty.saker.util.function.Functionals;
import saker.build.thirdparty.saker.util.function.LazySupplier;
import saker.build.thirdparty.saker.util.io.FileUtils;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.docgen.ScriptStyleCSSBuilder.ProcessedScript;
import saker.url.UrlTitleTaskFactory;

public class DocumentationGeneratorTaskFactory implements TaskFactory<Object>, Externalizable {
	private static final long serialVersionUID = 1L;

	public static final String TASK_NAME = "saker.site.doc.gen";

	private static final Pattern PATTERN_COMMA_SPLIT = Pattern.compile("[,]+");
	private static final int DEFAULT_CODE_BLOCK_LENGTH_WARNING = 80;

	private static final String LINK_ROOT_SITE_ROOT = "root:";
	private static final String LINK_ROOT_RAW = "raw:/";

	private interface NavigationSection {
		public String getTitle();
	}

	private interface MarkdownNavigationSection extends NavigationSection {
		public SakerPath getMarkdownPath();
	}

	private static class LinkNavigationSection implements NavigationSection {
		private Link link;
		private String title;
		private SakerPath linkedPath;

		public LinkNavigationSection(Link link, String title, SakerPath linkedpath) {
			this.link = link;
			this.title = title;
			this.linkedPath = linkedpath;
		}

		public SakerPath getLinkedOutputPath() {
			return linkedPath;
		}

		@Override
		public String getTitle() {
			return title;
		}

		public Link getLink() {
			return link;
		}

		@Override
		public String toString() {
			return "LinkNavigationSection[" + (link != null ? "link=" + link + ", " : "")
					+ (title != null ? "title=" + title : "") + "]";
		}

	}

	private static class MarkdownSectionReference implements MarkdownNavigationSection {
		private SakerPath markdownPath;
		private String title;

		public MarkdownSectionReference(SakerPath markdownPath, String title) {
			this.markdownPath = markdownPath;
			this.title = title;
		}

		@Override
		public SakerPath getMarkdownPath() {
			return markdownPath;
		}

		@Override
		public String getTitle() {
			return title;
		}

		@Override
		public String toString() {
			return "SubSectionReference[" + (markdownPath != null ? "markdownPath=" + markdownPath + ", " : "")
					+ (title != null ? "title=" + title : "") + "]";
		}
	}

	private final static class RootMarkdownSection {
		private String title;
		private List<NavigationSection> sections;

		public RootMarkdownSection(String title, List<NavigationSection> sections) {
			this.title = title;
			this.sections = sections;
		}

		public String getTitle() {
			return title;
		}

		public List<NavigationSection> getSections() {
			return sections;
		}
	}

	private final static class RootMarkdownVisitor extends AbstractVisitor {
		private NavigableMap<SakerPath, ParsedMarkdown> relativeParsedMarkdowns;
		private List<RootMarkdownSection> rootSections = new ArrayList<>();
		private RootMarkdownSection currentSection = null;
		private Parser parser;
		private SakerPath rootMarkdownDirectory;

		private Set<SakerPath> linkedDocResources = new TreeSet<>();
		private SiteGenerationState state;
		private SakerPath rootOutputDirectory;

		public RootMarkdownVisitor(Parser parser, SakerPath rootMarkdownDirectory, SiteGenerationState state,
				SakerPath rootOutputDirectory) {
			this.parser = parser;
			this.relativeParsedMarkdowns = state.relativeParsedMarkdowns;
			this.rootMarkdownDirectory = rootMarkdownDirectory;
			this.state = state;
			this.rootOutputDirectory = rootOutputDirectory;
		}

		public Set<SakerPath> getLinkedDocumentationResources() {
			return linkedDocResources;
		}

		@Override
		public void visit(FencedCodeBlock fencedCodeBlock) {
			String info = fencedCodeBlock.getInfo();
			if (ObjectUtils.isNullOrEmpty(info)) {
				throw new IllegalArgumentException("No title specified for root section.");
			}
			currentSection = new RootMarkdownSection(info, new ArrayList<>());
			rootSections.add(currentSection);
			String literal = fencedCodeBlock.getLiteral();
			Node parsed = parser.parse(literal);
			parsed.accept(this);
			super.visit(fencedCodeBlock);
			currentSection = null;
		}

		@Override
		public void visit(Link link) {
			Node fc = link.getFirstChild();
			if (fc != null && fc == link.getLastChild() && fc instanceof Text) {
				if (currentSection == null) {
					currentSection = new RootMarkdownSection(null, new ArrayList<>());
					rootSections.add(currentSection);
				}
				String linktext = ((Text) fc).getLiteral();
				switch (linktext) {
					case "!section": {
						SakerPath destpath = SakerPath.valueOf(rootMarkdownDirectory, link.getDestination());
						String title = link.getTitle();
						ParsedMarkdown markdown = relativeParsedMarkdowns.get(destpath);
						if (markdown == null) {
							throw new IllegalArgumentException("Markdown not found: " + destpath);
						}
						currentSection.sections.add(new MarkdownSectionReference(destpath, title));
						link.unlink();
						return;
					}
					default: {
						break;
					}
				}
			}
			if (fc != null) {
				TextCollectorVisitor textvis = new TextCollectorVisitor();
				fc.accept(textvis);
				String title = textvis.getText();
				if (title.isEmpty()) {
					throw new IllegalArgumentException("Empty title root link refrence: " + link);
				}
				if (currentSection == null) {
					currentSection = new RootMarkdownSection(null, new ArrayList<>());
					rootSections.add(currentSection);
				}
				SakerPath destpath = SakerPath.valueOf(link.getDestination());
				SakerPath linkedpath;
				if (SakerPath.ROOT_SLASH.equals(destpath.getRoot())) {
					linkedpath = state.outputDirectoryPath.resolve(destpath.forcedRelative());
				} else if (LINK_ROOT_SITE_ROOT.equals(destpath.getRoot())) {
					linkedpath = rootOutputDirectory.resolve(destpath.forcedRelative());
				} else {
					linkedpath = state.outputDirectoryPath.resolve(destpath);
				}
				linkedDocResources.add(linkedpath);
				currentSection.sections.add(new LinkNavigationSection(link, title, linkedpath));
			} else {
				throw new IllegalArgumentException("Illegal root link refrence: " + link);
			}
			//TODO
			super.visit(link);
		}

		public List<RootMarkdownSection> getRootSections() {
			return rootSections;
		}
	}

	private static Map<String, String> parseLinkTitleAttributes(String title) {
		if (title == null) {
			return Collections.emptyMap();
		}
		LinkedHashMap<String, String> result = new LinkedHashMap<>();
		String[] split = PATTERN_COMMA_SPLIT.split(title);
		for (String s : split) {
			s = s.trim();
			int idx = s.indexOf(':');
			if (idx < 0) {
				result.put(s, null);
			} else {
				result.put(s.substring(0, idx).trim(), s.substring(idx + 1).trim());
			}
		}
		return result;
	}

	private final static class TextCollectorVisitor extends AbstractVisitor {
		private StringBuilder sb = new StringBuilder();

		@Override
		public void visit(Heading heading) {
			super.visit(heading);
		}

		@Override
		public void visit(Text text) {
			sb.append(text.getLiteral());
			super.visit(text);
		}

		@Override
		public void visit(Code code) {
			sb.append(code.getLiteral());
			super.visit(code);
		}

		@Override
		public void visit(Link link) {
			super.visit(link);
		}

		public String getText() {
			return sb.toString();
		}
	}

	private static void startUrlTitleTask(TaskContext taskcontext, String destination) {
		if (isTitleAutoRetrieved(destination)) {
			UrlTitleTaskFactory task = new UrlTitleTaskFactory(destination);
			taskcontext.startTask(task, task, null);
		}
	}

	private static boolean isTitleAutoRetrieved(String destination) {
		return destination.startsWith("http:") || destination.startsWith("https:");
	}

	private final static class PreprocessingVisitor extends AbstractVisitor {
		private SakerPath currentMarkdownRelativePath;
		private SakerPath currentMarkdownAbsolutePath;
		private SakerPath markdownDirectoryPath;
		private NavigableMap<String, SakerPath> resourceRoots;

		private Heading titleHeading;
		private String titleHeadingText;

		private Map<Node, String> nodeAnchors = new LinkedHashMap<>();
		private Map<String, String> anchorTitles = new TreeMap<>();

		private List<NavigationSection> subSections = new ArrayList<>();
		private TaskContext taskContext;
		private SimpleSiteInfo siteinfo;

		public PreprocessingVisitor(TaskContext taskContext, SakerPath currentMarkdownRelativePath,
				SakerPath currentMarkdownAbsolutePath, SakerPath markdownDirectoryPath,
				NavigableMap<String, SakerPath> resourceRoots, SimpleSiteInfo siteinfo) {
			this.taskContext = taskContext;
			this.currentMarkdownRelativePath = currentMarkdownRelativePath;
			this.currentMarkdownAbsolutePath = currentMarkdownAbsolutePath;
			this.markdownDirectoryPath = markdownDirectoryPath;
			this.resourceRoots = resourceRoots;
			this.siteinfo = siteinfo;
		}

		@Override
		public void visit(Link link) {
			Node fc = link.getFirstChild();
			String linkdest = link.getDestination();
			if (linkdest.startsWith(LINK_ROOT_RAW)) {
				super.visit(link);
				return;
			}
			String title = link.getTitle();
			if (title == null) {
				startUrlTitleTask(taskContext, linkdest);
			}
			if (fc != null && fc == link.getLastChild() && fc instanceof Text) {
				Text linktextnode = (Text) fc;
				String linktext = linktextnode.getLiteral();
				switch (linktext) {
					case "!section": {
						SakerPath destpath = SakerPath.valueOf(linkdest);
						if (destpath.isRelative()) {
							destpath = currentMarkdownRelativePath.getParent().resolve(destpath);
						} else if (SakerPath.ROOT_SLASH.equals(destpath.getRoot())) {
							destpath = destpath.forcedRelative();
						} else {
							throw new IllegalArgumentException(
									"Invalid section path:" + destpath + " in " + currentMarkdownRelativePath);
						}
						subSections.add(new MarkdownSectionReference(destpath, link.getTitle()));
						link.unlink();
						return;
					}
					case "!macro": {
						Entry<String, String> macro = null;
						for (Entry<String, String> entry : siteinfo.macros) {
							if (linkdest.equals(entry.getKey())) {
								macro = entry;
								break;
							}
						}
						if (macro == null) {
							throw new IllegalArgumentException("Macro not found: " + linkdest);
						}
						link.prependChild(new Text(macro.getValue()));
						link.unlink();
						break;
					}
					case "!embedcode": {

						SakerPath destpath = currentMarkdownRelativePath.getParent()
								.resolve(SakerPath.valueOf(linkdest));
						EmbedCustomBlock embedblock = new EmbedCustomBlock(destpath);

						Map<String, String> attrs = parseLinkTitleAttributes(link.getTitle());
						//TODO support subtitle

						embedblock.setLanguage(attrs.get("language"));
						embedblock.setIncludeRaw(Boolean.parseBoolean(attrs.get("include-raw")));
						embedblock.setTrimLineWhiteSpace(Boolean.parseBoolean(attrs.get("trim-line-whitespace")));
						String rangeattr = attrs.get("range");
						String rangemarkerstart = attrs.get("range-marker-start");
						String rangemarkerend = attrs.get("range-marker-end");
						if (rangeattr != null) {
							int idx = rangeattr.indexOf(':');
							if (idx < 0) {
								throw new IllegalArgumentException("Invalid embed range format: " + rangeattr);
							}
							String start = rangeattr.substring(0, idx);
							if (!start.isEmpty()) {
								if (!ObjectUtils.isNullOrEmpty(rangemarkerstart)) {
									throw new IllegalArgumentException(
											"Embed range marker start and start line specified.");
								}
								embedblock.setRangeStart(Integer.parseInt(start));
							}
							String end = rangeattr.substring(idx + 1);
							if (!end.isEmpty()) {
								if (!ObjectUtils.isNullOrEmpty(rangemarkerend)) {
									throw new IllegalArgumentException(
											"Embed range marker end and end line specified.");
								}
								embedblock.setRangeEnd(Integer.parseInt(end));
							}
						}
						embedblock.setRangeMarkerStart(rangemarkerstart);
						embedblock.setRangeMarkerEnd(rangemarkerend);
						embedblock.setIncludeStartMarker(Boolean.parseBoolean(attrs.get("marker-start-include")));
						embedblock.setIncludeEndMarker(Boolean.parseBoolean(attrs.get("marker-end-include")));
						link.insertBefore(embedblock);
						link.unlink();
						return;
					}
					case "!include": {
						SakerPath destpath = SakerPath.valueOf(linkdest);

						if (destpath.isRelative()) {
							destpath = currentMarkdownAbsolutePath.getParent().resolve(destpath);
						} else if (SakerPath.ROOT_SLASH.equals(destpath.getRoot())) {
							destpath = markdownDirectoryPath.resolve(destpath.forcedRelative());
						} else if (LINK_ROOT_SITE_ROOT.equals(destpath.getRoot())) {
							throw new UnsupportedOperationException("unsupported " + LINK_ROOT_SITE_ROOT
									+ " !include root in " + currentMarkdownRelativePath);
						} else {
							SakerPath resrootpath = resourceRoots.get(destpath.getRoot());
							if (resrootpath != null) {
								destpath = resrootpath.resolve(destpath.forcedRelative());
							} else {
								throw new IllegalArgumentException(
										"Invalid include path:" + destpath + " in " + currentMarkdownRelativePath);
							}
						}

						link.insertBefore(new IncludeCustomBlock(destpath));
						link.unlink();
						break;
					}
					case "!tableofcontents": {
						link.insertBefore(new TableOfContentsCustomBlock());
						link.unlink();
						break;
					}
					default: {
						break;
					}
				}
			}
			super.visit(link);
		}

		@Override
		public void visit(Heading heading) {
			TextCollectorVisitor textvis = new TextCollectorVisitor();
			heading.accept(textvis);
			String headingtext = textvis.getText();
			String anchorid = generateHeadingAnchorId(headingtext);

			String originalanchor = anchorid;
			int loopc = 1;
			for (; (nodeAnchors.putIfAbsent(heading, anchorid)) != null; ++loopc) {
				anchorid = originalanchor + "-" + loopc;
			}
			anchorTitles.put(anchorid, headingtext);

			if (titleHeading == null || heading.getLevel() < titleHeading.getLevel()) {
				titleHeading = heading;
				titleHeadingText = headingtext;
			}
			super.visit(heading);
		}

		@Override
		public void visit(FencedCodeBlock fencedCodeBlock) {
			String codeliteral = fencedCodeBlock.getLiteral();
			warnCodeLineLengths(codeliteral);
			super.visit(fencedCodeBlock);
		}

		public String getTitleHeadingText() {
			return titleHeadingText;
		}

		public List<NavigationSection> getSubSections() {
			return subSections;
		}

	}

	private static void warnCodeLineLengths(String codeliteral) {
		warnCodeLineLengths(codeliteral, DEFAULT_CODE_BLOCK_LENGTH_WARNING);
	}

	private static void warnCodeLineLengths(String codeliteral, int maxlen) {
		int idx = 0;
		int end = codeliteral.length();
		warnCodeLineLengths(codeliteral, maxlen, idx, end);
	}

	private static void warnCodeLineLengths(String codeliteral, int maxlen, int start, int end) {
		while (start < end) {
			int nlidx = codeliteral.indexOf('\n', start);
			if (nlidx < 0) {
				nlidx = end;
			}
			if (nlidx - start > maxlen) {
				SakerLog.warning().println("Code block line is longer than " + maxlen + " characters: "
						+ codeliteral.substring(start, nlidx));
			}
			start = nlidx + 1;
		}
	}

	private static class ContentHeading {
		private ContentHeading parent;
		private Heading heading;
		private List<ContentHeading> children = new ArrayList<>();
		private String title;

		public ContentHeading(Heading heading) {
			this.heading = heading;
		}

		public String getTitle() {
			return title;
		}

		public Heading getHeading() {
			return heading;
		}

		public String getIdAttribute(ParsedMarkdown markdown) {
			return markdown.getHeadingAnchor(heading);
		}

		public List<ContentHeading> getChildren() {
			return children;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public void add(ContentHeading ch) {
			children.add(ch);
			ch.parent = this;
		}

		public ContentHeading getParent() {
			return parent;
		}

		@Override
		public String toString() {
			return "ContentHeading[" + (title != null ? "title=" + title : "") + "]";
		}

	}

	private final static class ValidatingVisitor extends AbstractVisitor {
		private NavigableMap<SakerPath, ParsedMarkdown> relativeParsedMarkdowns;
		private NavigableMap<SakerPath, ParsedMarkdown> rootRelativeOutputMarkdowns;
		private ParsedMarkdown currentMarkdown;

		private List<ContentHeading> headings = new ArrayList<>();
		private ContentHeading currentHeading = null;

		private Set<SakerPath> linkedDocResources = new TreeSet<>();
		private Set<SakerPath> linkedRootResources = new TreeSet<>();
		private String docRootPath;
		private SakerPath rootOutputDirPath;

		public ValidatingVisitor(String docrootpath, ParsedMarkdown currentmarkdown,
				NavigableMap<SakerPath, ParsedMarkdown> relativeparsedmarkdowns, SakerPath rootoutputdirpath,
				NavigableMap<SakerPath, ParsedMarkdown> rootRelativeOutputMarkdowns) {
			this.docRootPath = docrootpath;
			this.currentMarkdown = currentmarkdown;
			this.relativeParsedMarkdowns = relativeparsedmarkdowns;
			this.rootOutputDirPath = rootoutputdirpath;
			this.rootRelativeOutputMarkdowns = rootRelativeOutputMarkdowns;
		}

		public Set<SakerPath> getLinkedDocumentationResources() {
			return linkedDocResources;
		}

		public Set<SakerPath> getLinkedRootResources() {
			return linkedRootResources;
		}

		@Override
		public void visit(Heading heading) {
			TextCollectorVisitor textvisitor = new TextCollectorVisitor();
			heading.accept(textvisitor);
			String title = textvisitor.getText();
			int level = heading.getLevel();
			if (level == 1) {
				ContentHeading ch = new ContentHeading(heading);
				ch.setTitle(title);
				headings.add(ch);
				currentHeading = ch;
			} else {
				while (currentHeading != null && level <= currentHeading.getHeading().getLevel()) {
					ContentHeading parent = currentHeading.getParent();
					currentHeading = parent;
				}
				ContentHeading ch = new ContentHeading(heading);
				ch.setTitle(title);
				if (currentHeading == null) {
					headings.add(ch);
					currentHeading = ch;
				} else {
					currentHeading.add(ch);
					currentHeading = ch;
				}
			}
			super.visit(heading);
		}

		@Override
		public void visit(CustomBlock node) {
			if (node instanceof EmbedCustomBlock) {
				EmbedCustomBlock embed = (EmbedCustomBlock) node;
				String lang = embed.getLanguage();
				addLanguage(lang);
			} else if (node instanceof IncludeCustomBlock) {
				//nothing to do
			} else if (node instanceof TableOfContentsCustomBlock) {
				//nothing to do
			} else {
				super.visit(node);
			}
		}

		@Override
		public void visit(FencedCodeBlock fencedCodeBlock) {
			super.visit(fencedCodeBlock);
			String lang = getFencedCodeBlockLanguage(fencedCodeBlock);
			addLanguage(lang);
		}

		private void addLanguage(String lang) {
			if (!ObjectUtils.isNullOrEmpty(lang)) {
				lang = lang.toLowerCase(Locale.ENGLISH);
				if (!"sakerscript".equals(lang)) {
					currentMarkdown.getCodeLanguages().add(lang);
				}
			}
		}

		@Override
		public void visit(Image image) {
			String linkpath = image.getDestination();
			try {
				SakerPath destpath = SakerPath.valueOf(linkpath);

				if (SakerPath.ROOT_SLASH.equals(destpath.getRoot())) {
					SakerPath relfilepath = destpath.forcedRelative();
					linkedDocResources.add(relfilepath);
					if (docRootPath != null) {
						image.setDestination(docRootPath + relfilepath.toString());
					}
				} else if (LINK_ROOT_SITE_ROOT.equals(destpath.getRoot())) {
					SakerPath relfilepath = destpath.forcedRelative();
					linkedRootResources.add(relfilepath);
					image.setDestination(getRelativePathString(currentMarkdown.getAbsoluteOutputPath(),
							rootOutputDirPath.resolve(relfilepath)));
				} else if (destpath.isRelative()) {
					linkedDocResources.add(currentMarkdown.getRelativePath().getParent().resolve(destpath));
				}

			} catch (InvalidPathFormatException e) {
				//the destination might not be parseable
			}
			super.visit(image);
		}

		@Override
		public void visit(Link link) {
			String linkdest = link.getDestination();
			if (linkdest.startsWith(LINK_ROOT_RAW)) {
				link.setDestination(linkdest.substring(LINK_ROOT_RAW.length()));
				super.visit(link);
				return;
			}
			try {
				String linkpath = linkdest;
				String linkappend = "";
				String anchor = null;
				int hashmarkidx = linkpath.lastIndexOf('#');
				if (hashmarkidx >= 0) {
					linkappend = linkpath.substring(hashmarkidx);
					linkpath = linkpath.substring(0, hashmarkidx);
					anchor = linkappend.substring(1);
				}
				SakerPath destpath = SakerPath.valueOf(linkpath);
				ParsedMarkdown referenced = null;
				if (destpath.isRelative()) {
					if (SakerPath.EMPTY.equals(destpath)) {
						referenced = currentMarkdown;
					} else {
						SakerPath resolvedpath = currentMarkdown.getRelativePath().getParent().resolve(destpath);
						referenced = relativeParsedMarkdowns.get(resolvedpath);
					}
				} else if (SakerPath.ROOT_SLASH.equals(destpath.getRoot())) {
					referenced = relativeParsedMarkdowns.get(destpath.forcedRelative());
				} else if (LINK_ROOT_SITE_ROOT.equals(destpath.getRoot())) {
					referenced = rootRelativeOutputMarkdowns.get(destpath.forcedRelative());
				}
				if (referenced != null) {
					String headingtitle;
					if (anchor != null) {
						//there was an anchor defined
						String anchortitle = referenced.getAnchorTitle(anchor);
						if (anchortitle == null) {
							throw new IllegalArgumentException("Anchor not found: " + anchor + " in " + linkdest
									+ " for " + currentMarkdown.getAbsolutePath() + " available: "
									+ referenced.getAnchors());
						}
						headingtitle = anchortitle;
					} else {
						headingtitle = referenced.getHeadingTitle();
					}
					link.setDestination(getRelativePathString(currentMarkdown.getAbsoluteOutputPath(),
							referenced.getAbsoluteOutputPath()) + linkappend);
					if (link.getTitle() == null) {
						link.setTitle(headingtitle);
					}
					if (link.getFirstChild() == null) {
						if (headingtitle == null) {
							throw new IllegalArgumentException(
									"Referenced markdown doesn't have a title. " + referenced.getRelativePath());
						}
						link.appendChild(new Text(headingtitle));
					}
				} else {
					if (StringUtils.endsWithIgnoreCase(linkpath, ".md")) {
						throw new IllegalArgumentException("Markdown not found at: " + linkdest + " declared in "
								+ currentMarkdown.getAbsolutePath());
					}
					//if we're linking a file that is not a markdown, but a valid resource, make sure that file is included
					if (SakerPath.ROOT_SLASH.equals(destpath.getRoot())) {
						SakerPath relfilepath = destpath.forcedRelative();
						linkedDocResources.add(relfilepath);
						if (link.getFirstChild() == null) {
							link.appendChild(new Text(destpath.getFileName()));
						}
					} else if (LINK_ROOT_SITE_ROOT.equals(destpath.getRoot())) {
						SakerPath relfilepath = destpath.forcedRelative();
						linkedRootResources.add(relfilepath);
						if (link.getFirstChild() == null) {
							link.appendChild(new Text(destpath.getFileName()));
						}
						link.setDestination(getRelativePathString(currentMarkdown.getAbsoluteOutputPath(),
								rootOutputDirPath.resolve(relfilepath)));
					} else if (destpath.isRelative()) {
						linkedDocResources.add(currentMarkdown.getRelativePath().getParent().resolve(destpath));
						if (link.getFirstChild() == null) {
							link.appendChild(new Text(destpath.getFileName()));
						}
					}
//					if (link.getTitle() == null) {
//						link.setTitle(destpath.getFileName());
//					}
				}
				//else some other root we dont handle
			} catch (InvalidPathFormatException e) {
				//the destination might not be parseable
			}
			String destination = link.getDestination();
			if (destination.startsWith("/")) {
				if (docRootPath != null) {
					link.setDestination(docRootPath + destination.substring(1));
				}
			}
			super.visit(link);
		}

		public List<ContentHeading> getHeadings() {
			return headings;
		}
	}

	private static class ParsedMarkdown {
		private Node node;
		private String headingTitle;
		private SakerPath relativePath;
		private SakerPath absolutePath;
		private SakerPath absoluteOutputPath;
		private List<NavigationSection> subSections;
		private List<ContentHeading> headings;
		private Map<Node, String> nodeAnchors;
		private Map<String, String> anchorTitles;
		private Set<String> codeLanguages = new TreeSet<>();

		public ParsedMarkdown(Node node, String headingTitle, SakerPath absolutepath, SakerPath relativepath,
				List<NavigationSection> subsections, SakerPath absoluteOutputPath) {
			this.absolutePath = absolutepath;
			this.relativePath = relativepath;
			this.node = node;
			this.headingTitle = headingTitle;
			this.subSections = subsections;
			this.absoluteOutputPath = absoluteOutputPath;
		}

		public Set<String> getCodeLanguages() {
			return codeLanguages;
		}

		public String getHeadingTitle() {
			return headingTitle;
		}

		public SakerPath getRelativePath() {
			return relativePath;
		}

		public SakerPath getAbsolutePath() {
			return absolutePath;
		}

		public SakerPath getAbsoluteOutputPath() {
			return absoluteOutputPath;
		}

		public Node getNode() {
			return node;
		}

		public List<NavigationSection> getSubSections() {
			return subSections;
		}

		public void setContentHeadings(List<ContentHeading> headings) {
			this.headings = headings;
		}

		public List<ContentHeading> getContentHeadings() {
			return headings;
		}

		public Map<Node, String> getNodeAnchors() {
			return nodeAnchors;
		}

		public void setNodeAnchors(Map<Node, String> nodeAnchors) {
			this.nodeAnchors = nodeAnchors;
		}

		public void setAnchorTitles(Map<String, String> anchorTitles) {
			this.anchorTitles = anchorTitles;
		}

		public String getHeadingAnchor(Heading heading) {
			return nodeAnchors.get(heading);
		}

//		public Heading getAnchorHeading(String anchorid) {
//			return anchorHeadings.get(anchorid);
//		}

		public String getAnchorTitle(String anchorid) {
			return anchorTitles.get(anchorid);
		}

		public Set<String> getAnchors() {
			return anchorTitles.keySet();
		}
	}

	private static String getFencedCodeBlockLanguage(FencedCodeBlock fencedCodeBlock) {
		String result = fencedCodeBlock.getInfo();
		if (result == null) {
			return null;
		}
		result = result.trim();
		int spaceidx = result.indexOf(' ');
		if (spaceidx > 0) {
			result = result.substring(0, spaceidx);
		}
		return result;
	}

	private static Map<SakerPath, Integer> collectSectionIndices(List<RootMarkdownSection> rootmarkdowns,
			Map<SakerPath, ParsedMarkdown> markdowns, List<ParsedMarkdown> indexresults) {
		TreeMap<SakerPath, Integer> result = new TreeMap<>();

		int idx = 0;
		for (RootMarkdownSection rootmd : rootmarkdowns) {
			idx = collectSectionIndices(result, rootmd.getSections(), markdowns, indexresults, idx);
		}
		return result;
	}

	private static int collectSectionIndices(TreeMap<SakerPath, Integer> treeMap, List<NavigationSection> sections,
			Map<SakerPath, ParsedMarkdown> markdowns, List<ParsedMarkdown> indexresults, int idx) {
		for (NavigationSection section : sections) {
			if (!(section instanceof MarkdownNavigationSection)) {
				continue;
			}
			MarkdownNavigationSection mdsection = (MarkdownNavigationSection) section;
			ParsedMarkdown pmd = markdowns.get(mdsection.getMarkdownPath());
			if (pmd == null) {
				throw new IllegalArgumentException("Markdown not found: " + mdsection.getMarkdownPath());
			}
			if (pmd.getHeadingTitle() != null) {
				Integer prev = treeMap.put(mdsection.getMarkdownPath(), idx++);
				if (prev != null) {
					throw new IllegalArgumentException(
							"Multiple occurrence of markdown: " + mdsection.getMarkdownPath());
				}
				indexresults.add(pmd);
			}
			idx = collectSectionIndices(treeMap, pmd.getSubSections(), markdowns, indexresults, idx);
		}
		return idx;
	}

	private static final class AnchoringHtmlNodeRendererContext implements HtmlNodeRendererContext {
		private final HtmlNodeRendererContext subject;
		private final Map<Node, String> nodeAnchors;

		public AnchoringHtmlNodeRendererContext(HtmlNodeRendererContext subject, Map<Node, String> nodeAnchors) {
			this.subject = subject;
			this.nodeAnchors = nodeAnchors;
		}

		@Override
		public String encodeUrl(String url) {
			return subject.encodeUrl(url);
		}

		@Override
		public Map<String, String> extendAttributes(Node node, String tagName, Map<String, String> attributes) {
			String anch = nodeAnchors.get(node);
			if (anch != null) {
				TreeMap<String, String> result = new TreeMap<>(attributes);
				result.putIfAbsent("id", anch);
				return result;
			}
			return subject.extendAttributes(node, tagName, attributes);
		}

		@Override
		public HtmlWriter getWriter() {
			return subject.getWriter();
		}

		@Override
		public String getSoftbreak() {
			return subject.getSoftbreak();
		}

		@Override
		public void render(Node node) {
			subject.render(node);
		}

		@Override
		public boolean shouldEscapeHtml() {
			return subject.shouldEscapeHtml();
		}

	}

	private static final class HtmlMarkdownRenderer extends CoreHtmlNodeRenderer {
		private final HtmlWriter html;
		private final TaskContext taskContext;
		private final SakerDirectory docDirectory;
		private final SakerDirectory outputDirectory;
		private final String rootDomain;
		private final ParsedMarkdown markdown;

		private final ScriptStyleCSSBuilder scriptStyleCssBuilder;
		private final Function<String, String> taskLinkFunction;
		private NavigableMap<SakerPath, ParsedMarkdown> relativeParsedMarkdowns;

		public HtmlMarkdownRenderer(ParsedMarkdown markdown, HtmlNodeRendererContext context, TaskContext taskcontext,
				SakerDirectory docDirectory, SakerDirectory outputDirectory, String rootDomain,
				ScriptStyleCSSBuilder scriptStyleCssBuilder, Function<String, String> taskLinkFunction,
				NavigableMap<SakerPath, ParsedMarkdown> relativeParsedMarkdowns) {
			super(new AnchoringHtmlNodeRendererContext(context, markdown.getNodeAnchors()));
			this.markdown = markdown;
			this.taskContext = taskcontext;
			this.docDirectory = docDirectory;
			this.outputDirectory = outputDirectory;
			this.rootDomain = rootDomain;
			this.scriptStyleCssBuilder = scriptStyleCssBuilder;
			this.taskLinkFunction = taskLinkFunction;
			this.relativeParsedMarkdowns = relativeParsedMarkdowns;
			this.html = context.getWriter();
		}

		@Override
		public Set<Class<? extends Node>> getNodeTypes() {
			Set<Class<? extends Node>> result = new LinkedHashSet<>(super.getNodeTypes());
			result.add(EmbedCustomBlock.class);
			result.add(IncludeCustomBlock.class);
			result.add(TableOfContentsCustomBlock.class);
			return result;
		}

		@Override
		public void visit(FencedCodeBlock fencedCodeBlock) {
			String language = getFencedCodeBlockLanguage(fencedCodeBlock);
			String blockinfo = fencedCodeBlock.getInfo();

			Map<String, String> codeattrs = new LinkedHashMap<>();
			String classattr;
			if (!ObjectUtils.isNullOrEmpty(language)) {
				classattr = "language-" + blockinfo;
			} else {
				classattr = blockinfo;
			}
			if (!ObjectUtils.isNullOrEmpty(classattr)) {
				codeattrs.put("class", classattr);
			}

			html.line();
			Map<String, String> preattrs = Collections.singletonMap("class", "doc-code-block");
			html.tag("pre", preattrs);
			html.tag("code", codeattrs);
			ProcessedScript sakerscript = null;
			String codeliteral = fencedCodeBlock.getLiteral();
			if ("sakerscript".equals(language)) {
				try {
					sakerscript = scriptStyleCssBuilder.process(codeliteral);
				} catch (IOException | ScriptParsingFailedException e) {
					//XXX reify
					throw new RuntimeException("Failed to parse script in: " + markdown.getAbsolutePath(), e);
				}
			}
			if (sakerscript != null) {
				sakerscript.write(html, 0, codeliteral.length(), taskLinkFunction);
			} else {
				html.text(codeliteral);
			}
			html.tag("/code");
			html.tag("/pre");
			html.line();
		}

		@Override
		public void visit(Heading heading) {
			String htag = "h" + heading.getLevel();
			html.line();
			Map<String, String> tagattrs = new LinkedHashMap<>();
			String anchor = markdown.getHeadingAnchor(heading);
			if (anchor != null) {
				tagattrs.put("id", anchor);
			}
			html.tag(htag, tagattrs);
			visitChildren(heading);
			if (!ObjectUtils.isNullOrEmpty(anchor)) {
				Map<String, String> anchorlinkattrs = new TreeMap<>();
				anchorlinkattrs.put("href", "#" + anchor);
				anchorlinkattrs.put("class", "doc-heading-anchor");
				html.tag("a", anchorlinkattrs);
				html.tag("/a");
			}
			html.tag('/' + htag);
			html.line();
		}

		@Override
		public void visit(CustomBlock node) {
			if (node instanceof EmbedCustomBlock) {
				EmbedCustomBlock embed = (EmbedCustomBlock) node;
				visit(embed);
			} else if (node instanceof IncludeCustomBlock) {
				IncludeCustomBlock include = (IncludeCustomBlock) node;
				visit(include);
			} else if (node instanceof TableOfContentsCustomBlock) {
				TableOfContentsCustomBlock tableofcontents = (TableOfContentsCustomBlock) node;
				visit(tableofcontents);
			} else {
				super.visit(node);
			}
		}

		@Override
		public void visit(Link link) {
			if (link.getTitle() == null) {
				String destination = link.getDestination();
				if (isTitleAutoRetrieved(destination)) {
					try {
						String title = (String) taskContext.getTaskResult(new UrlTitleTaskFactory(destination));
						if (!ObjectUtils.isNullOrEmpty(title)) {
							link.setTitle(title);
						}
					} catch (TaskExecutionFailedException e) {
						SakerLog.warning().println("Failed to retrieve title for: " + destination + " (" + e + ")");
					}
				}
			}
			super.visit(link);
		}

		private void visit(TableOfContentsCustomBlock tableofcontents) {
			List<NavigationSection> sections = this.markdown.getSubSections();
			html.tag("div", Collections.singletonMap("class", "doc-table-of-contents"));
			appendTableOfContents(html, sections);
			html.tag("/div");
		}

		private void visit(IncludeCustomBlock include) {
			SakerPath filepath = include.getPath();
			SakerFile file = taskContext.getTaskUtilities().resolveFileAtPath(filepath);
			if (file == null) {
				throw new IllegalArgumentException("Include file not found: " + filepath);
			}

			String content;
			try {
				content = file.getContent();
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
			html.raw(content);

			taskContext.getTaskUtilities().reportInputFileDependency(null, file);
		}

		private void visit(EmbedCustomBlock embed) {
			SakerPath filepath = embed.getPath();
			SakerFile file = SakerPathFiles.resolveAtRelativePath(docDirectory, filepath);
			if (file == null) {
				throw new IllegalArgumentException("Embed file not found: " + filepath);
			}
			taskContext.getTaskUtilities().reportInputFileDependency(null, file);
			boolean includeraw = embed.isIncludeRaw();
			if (includeraw) {
				DelegateSakerFile delegatefile = new DelegateSakerFile(file);
				SakerPathFiles.resolveDirectoryAtRelativePathCreate(outputDirectory, filepath.getParent())
						.add(delegatefile);
				taskContext.getTaskUtilities().reportOutputFileDependency(null, delegatefile);
			}

			String language = embed.getLanguage();
			Map<String, String> codeattrs = new LinkedHashMap<>();
			if (language != null) {
				codeattrs.put("class", "language-" + language);
			}
			if (includeraw) {
				codeattrs.put("data-raw-file", rootDomain + "/" + filepath.toString());
			}

			String content;
			try {
				content = file.getContent();
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}

			ProcessedScript sakerscript = null;
			if ("sakerscript".equals(language)) {
				try {
					sakerscript = scriptStyleCssBuilder.process(content);
				} catch (IOException | ScriptParsingFailedException e) {
					//XXX reify
					throw new RuntimeException("Failed to parse script at: " + file.getSakerPath(), e);
				}
			}

			html.line();
			html.tag("pre");
			html.tag("code", codeattrs);

			Integer rangestart = embed.getRangeStart();
			Integer rangeend = embed.getRangeEnd();
			String startmarker = embed.getRangeMarkerStart();
			String endmarker = embed.getRangeMarkerEnd();

			String text = content;
			int contentstartindex = 0;
			int contentendindex = content.length();
			if (rangestart != null || rangeend != null || startmarker != null || endmarker != null) {
				int[] linemap = StringUtils.getLineIndexMap(content);

				int startmarkerindex = 0;

				if (rangestart == null) {
					if (startmarker != null) {
						if (!startmarker.isEmpty()) {
							int idx = content.indexOf(startmarker);
							if (idx < 0) {
								throw new IllegalArgumentException("Embed start marker not found in file: " + filepath);
							}
							startmarkerindex = idx;
							if (embed.isIncludeStartMarker()) {
								//get the line index, convert to 1 based index
								rangestart = StringUtils.getLineIndex(linemap, idx) + 1;
							} else {
								//get the line index, convert to 1 based index, and the first line should not include the marker
								rangestart = StringUtils.getLineIndex(linemap, idx) + 1 + 1;
							}
						}
					}
				}
				if (rangeend == null) {
					if (endmarker != null) {
						if (!endmarker.isEmpty()) {
							int idx = content.indexOf(endmarker, startmarkerindex);
							if (idx < 0) {
								throw new IllegalArgumentException("Embed end marker not found in file: " + filepath);
							}
							if (embed.isIncludeEndMarker()) {
								//get the line index, convert to 1 based index, the line with the marker should be included
								rangeend = StringUtils.getLineIndex(linemap, idx) + 1;
							} else {
								//get the line index, convert to 1 based index, the line with the marker should not be included
								rangeend = StringUtils.getLineIndex(linemap, idx) + 1 - 1;
							}
						}
					}
				}

				int startline;
				int endline;

				if (rangestart == null) {
					startline = 0;
				} else {
					if (rangestart < 0) {
						startline = linemap.length + rangestart;
					} else {
						startline = rangestart - 1;
					}
				}
				if (rangeend == null) {
					endline = linemap.length;
				} else {
					if (rangeend < 0) {
						endline = linemap.length + rangeend;
					} else {
						endline = Math.min(rangeend, linemap.length);
					}
				}
				if (startline > endline) {
					throw new IllegalArgumentException("Embed range is negative. " + startline + ":" + endline);
				}

				if (startline >= 0 || endline >= 0) {
					if (startline >= linemap.length) {
						throw new IndexOutOfBoundsException(
								"Line start range out of range: " + rangestart + " for count: " + linemap.length);
					}
					int startidx = startline >= 0 ? linemap[startline] : 0;
					int endidx = endline >= 0 && endline < linemap.length ? linemap[endline] : content.length();
					contentstartindex = startidx;
					contentendindex = endidx;
				}
			}
			if (sakerscript != null) {
				if (embed.isTrimLineWhiteSpace()) {
					//TODO handle whitespace trimming for scripting language
				}
				sakerscript.write(html, contentstartindex, contentendindex, taskLinkFunction);
			} else {
				text = content.substring(contentstartindex, contentendindex);
				if (embed.isTrimLineWhiteSpace()) {
					String leadingws = getLeadingWhiteSpace(text);
					text = trimLineWhiteSpace(text, leadingws);
				}
				html.text(text);
			}
			warnCodeLineLengths(content, DEFAULT_CODE_BLOCK_LENGTH_WARNING, contentstartindex, contentendindex);

			html.tag("/code");
			html.tag("/pre");
			html.line();
		}

		private void appendTableOfContents(HtmlWriter html, List<NavigationSection> sections) {
			if (sections.isEmpty()) {
				return;
			}
			html.tag("ul");
			for (NavigationSection section : sections) {
				if (section instanceof MarkdownNavigationSection) {
					MarkdownNavigationSection mdsection = (MarkdownNavigationSection) section;
					SakerPath sectionpath = mdsection.getMarkdownPath();
					ParsedMarkdown submarkdown = relativeParsedMarkdowns.get(sectionpath);
					if (submarkdown == null) {
						throw new IllegalArgumentException("Section markdown not found at path: " + sectionpath);
					}
					String title = section.getTitle();
					if (title == null) {
						title = submarkdown.getHeadingTitle();
					}
					if (title == null) {
						continue;
					}
					html.tag("li");
					String linkatag = "<a href=\""
							+ getRelativePathString(this.markdown.getAbsoluteOutputPath(),
									submarkdown.getAbsoluteOutputPath())
							+ "\" title=\"" + title + "\">" + title + "</a>";
					html.raw(linkatag);

					appendTableOfContents(html, submarkdown.getSubSections());
					html.tag("/li");
					continue;
				}
				if (section instanceof LinkNavigationSection) {
					LinkNavigationSection lsection = (LinkNavigationSection) section;
					html.tag("li");

					html.raw(
							"<a href=\""
									+ getRelativePathString(this.markdown.getAbsoluteOutputPath(),
											lsection.getLinkedOutputPath())
									+ "\" title=\"" + lsection.getTitle() + "\">");
					Node fc = lsection.getLink().getFirstChild();
					if (fc != null) {
						do {
							this.render(fc);
							fc = fc.getNext();
						} while (fc != null);
					} else {
						html.text(lsection.getTitle());
					}
					html.tag("/a");
					html.tag("/li");
					continue;
				}
			}
			html.tag("/ul");
		}

		private static String trimLineWhiteSpace(String text, String leadingws) {
			if (leadingws.isEmpty()) {
				return text;
			}
			StringBuilder trimmedsb = new StringBuilder();
			int len = text.length();
			for (int i = 0; i < len; i++) {
				//start of a line
				char c = text.charAt(i);
				if (c == '\n' || c == '\r') {
					//we dont handle line endings.
					trimmedsb.append(c);
					continue;
				}
				if (!text.startsWith(leadingws, i)) {
					//found a line that doesnt start with the given whitespace
					return text;
				}
				i += leadingws.length();
				while (i < len) {
					char c2 = text.charAt(i++);
					trimmedsb.append(c2);
					if (c2 == '\r' || c2 == '\n') {
						//decrease i as it is incremented in the outer loop
						break;
					}
				}
			}
			return trimmedsb.toString();
		}
	}

	private static String getLeadingWhiteSpace(String cs) {
		int len = cs.length();
		int substrstart = 0;
		for (int i = 0; i < len; i++) {
			char c = cs.charAt(i);
			if (c != ' ' && c != '\t') {
				if (c == '\r' || c == '\n') {
					//don't consider any empty starting lines to be part of the leading whitespace
					substrstart = i + 1;
					continue;
				}
				return cs.substring(substrstart, i);
			}
		}
		return "";
	}

	private static String generateHeadingAnchorId(String title) {
		if (title.isEmpty()) {
			return "_";
		}
		StringBuilder sb = new StringBuilder();
		int len = title.length();
		for (int i = 0; i < len; i++) {
			char c = title.charAt(i);
			if (c >= 'A' && c <= 'Z') {
				//to lower case
				sb.append((char) (c - 'A' + 'a'));
				continue;
			}
			if (c == ' ') {
				sb.append('-');
				continue;
			}
			if ((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '_' || c == '-') {
				sb.append(c);
			} else {
				sb.append('_');
			}
		}
		return sb.toString();
	}

	private static final List<Extension> MARKDOWN_EXTENSIONS = ImmutableUtils
			.asUnmodifiableArrayList(TablesExtension.create());

	private static class SimpleSiteInfo implements Externalizable {
		private static final long serialVersionUID = 1L;

		private SakerPath markdownDirectory;
		private SakerPath outputDirectory;

		private SimplePlaceholderCollection placeholders;

		private Set<SimpleIncludeOption> includes;

		private SakerPath rootMarkdownPath;
		private Map<TaskName, SakerPath> taskLinkPaths;

		private List<Entry<String, String>> macros;

		private List<Entry<String, SakerPath>> embedMacros;

		private Map<WildcardPath, SakerPath> templateFiles;

		/**
		 * For {@link Externalizable}.
		 */
		public SimpleSiteInfo() {
		}

		public SimpleSiteInfo(SakerPath directory, SakerPath outputDirectory,
				Map<WildcardPath, SakerPath> templatefiles, SimplePlaceholderCollection placeholders,
				Set<SimpleIncludeOption> includes, SakerPath rootMarkdownPath, Map<TaskName, SakerPath> taskLinkPaths,
				List<Entry<String, String>> macros, List<Entry<String, SakerPath>> embedmacros) {
			this.markdownDirectory = directory;
			this.outputDirectory = outputDirectory;
			this.templateFiles = templatefiles;
			this.placeholders = placeholders;
			this.includes = includes;
			this.rootMarkdownPath = rootMarkdownPath;
			this.taskLinkPaths = taskLinkPaths;
			this.macros = ObjectUtils.cloneArrayList(macros, ImmutableUtils::makeImmutableMapEntry);
			this.embedMacros = ObjectUtils.cloneArrayList(embedmacros, ImmutableUtils::makeImmutableMapEntry);
		}

		@Override
		public void writeExternal(ObjectOutput out) throws IOException {
			out.writeObject(markdownDirectory);
			out.writeObject(outputDirectory);
			out.writeObject(placeholders);
			out.writeObject(rootMarkdownPath);
			SerialUtils.writeExternalCollection(out, includes);
			SerialUtils.writeExternalMap(out, taskLinkPaths);
			SerialUtils.writeExternalMap(out, templateFiles);
			SerialUtils.writeExternalCollection(out, macros);
			SerialUtils.writeExternalCollection(out, embedMacros);
		}

		@Override
		public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
			markdownDirectory = (SakerPath) in.readObject();
			outputDirectory = (SakerPath) in.readObject();
			placeholders = (SimplePlaceholderCollection) in.readObject();
			rootMarkdownPath = (SakerPath) in.readObject();
			includes = SerialUtils.readExternalImmutableLinkedHashSet(in);
			taskLinkPaths = SerialUtils.readExternalImmutableNavigableMap(in);
			templateFiles = SerialUtils.readExternalImmutableLinkedHashMap(in);
			macros = SerialUtils.readExternalImmutableList(in);
			embedMacros = SerialUtils.readExternalImmutableList(in);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((embedMacros == null) ? 0 : embedMacros.hashCode());
			result = prime * result + ((includes == null) ? 0 : includes.hashCode());
			result = prime * result + ((macros == null) ? 0 : macros.hashCode());
			result = prime * result + ((markdownDirectory == null) ? 0 : markdownDirectory.hashCode());
			result = prime * result + ((outputDirectory == null) ? 0 : outputDirectory.hashCode());
			result = prime * result + ((placeholders == null) ? 0 : placeholders.hashCode());
			result = prime * result + ((rootMarkdownPath == null) ? 0 : rootMarkdownPath.hashCode());
			result = prime * result + ((taskLinkPaths == null) ? 0 : taskLinkPaths.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			SimpleSiteInfo other = (SimpleSiteInfo) obj;
			if (embedMacros == null) {
				if (other.embedMacros != null)
					return false;
			} else if (!embedMacros.equals(other.embedMacros))
				return false;
			if (includes == null) {
				if (other.includes != null)
					return false;
			} else if (!includes.equals(other.includes))
				return false;
			if (macros == null) {
				if (other.macros != null)
					return false;
			} else if (!macros.equals(other.macros))
				return false;
			if (markdownDirectory == null) {
				if (other.markdownDirectory != null)
					return false;
			} else if (!markdownDirectory.equals(other.markdownDirectory))
				return false;
			if (outputDirectory == null) {
				if (other.outputDirectory != null)
					return false;
			} else if (!outputDirectory.equals(other.outputDirectory))
				return false;
			if (placeholders == null) {
				if (other.placeholders != null)
					return false;
			} else if (!placeholders.equals(other.placeholders))
				return false;
			if (rootMarkdownPath == null) {
				if (other.rootMarkdownPath != null)
					return false;
			} else if (!rootMarkdownPath.equals(other.rootMarkdownPath))
				return false;
			if (taskLinkPaths == null) {
				if (other.taskLinkPaths != null)
					return false;
			} else if (!taskLinkPaths.equals(other.taskLinkPaths))
				return false;
			return true;
		}

	}

	private static final class SiteGenerationState {

		/**
		 * Absolute.
		 */
		public SakerPath markdownDirectoryPath;
		public SakerDirectory markdownSakerDirectory;
		public NavigableMap<SakerPath, SakerFile> markdownFiles;
		public SakerDirectory outputSakerDirectory;
		public SakerPath outputDirectoryPath;
		public SakerFile rootMarkdownFile;
		public Map<WildcardPath, ProcessedTemplate> templateFiles;
		public NavigableSet<SakerPath> includeRelativePaths;
		public NavigableMap<SakerPath, ParsedMarkdown> relativeParsedMarkdowns = new TreeMap<>();

		public ProcessedTemplate getTemplate(SakerPath markdownpath) {
			for (Entry<WildcardPath, ProcessedTemplate> entry : templateFiles.entrySet()) {
				if (entry.getKey().includes(markdownpath)) {
					return entry.getValue();
				}
			}
			throw new IllegalArgumentException("No template found for markdown: " + markdownpath);
		}
	}

	public static final class WorkerTaskFactory
			implements TaskFactory<DocGeneratorTaskOutput>, Task<DocGeneratorTaskOutput>, Externalizable {

		private static final long serialVersionUID = 1L;

		private SakerPath outputDirectory;

		private SakerPath cssDirectory;

		private List<SimpleSiteInfo> siteInfos;

		private NavigableMap<String, SakerPath> resourceRoots;

		private Set<WildcardPath> allowedMissingResources = new TreeSet<>();

		/**
		 * For {@link Externalizable}.
		 */
		public WorkerTaskFactory() {
		}

		public WorkerTaskFactory(SakerPath outputDirectory, SakerPath cssDirectory, SimpleSiteInfo siteInfo) {
			this.outputDirectory = outputDirectory;
			this.cssDirectory = cssDirectory;
			this.siteInfos = Collections.singletonList(siteInfo);
		}

		public WorkerTaskFactory(SakerPath outputDirectory, SakerPath cssDirectory,
				NavigableMap<String, SakerPath> resourceroots, List<SimpleSiteInfo> siteInfos) {
			this.outputDirectory = outputDirectory;
			this.cssDirectory = cssDirectory;
			this.resourceRoots = resourceroots;
			this.siteInfos = siteInfos;
		}

		@Override
		public Task<? extends DocGeneratorTaskOutput> createTask(ExecutionContext executioncontext) {
			return this;
		}

		@Override
		public DocGeneratorTaskOutput run(TaskContext taskcontext) throws Exception {
			taskcontext.setStandardOutDisplayIdentifier(TASK_NAME);

			final SakerDirectory mainoutputdir;
			if (outputDirectory.isRelative()) {
				mainoutputdir = taskcontext.getTaskUtilities().resolveDirectoryAtRelativePathCreate(
						taskcontext.getTaskBuildDirectory().getDirectoryCreate(TASK_NAME), outputDirectory);
			} else {
				mainoutputdir = taskcontext.getTaskUtilities().resolveDirectoryAtAbsolutePathCreate(outputDirectory);
			}

			if (siteInfos.isEmpty()) {
				throw new IllegalArgumentException("no sites.");
			}

			SakerPath csspath = mainoutputdir.getSakerPath().resolve(cssDirectory).resolve("sakerscript.css");

			mainoutputdir.clear();
			SakerPath mainoutputdirpath = mainoutputdir.getSakerPath();

			NavigableSet<SakerPath> includeabsoluteoutpaths = new TreeSet<>();

			Map<SimpleSiteInfo, SiteGenerationState> generationstates = new LinkedHashMap<>();
			for (SimpleSiteInfo siteInfo : siteInfos) {
				SiteGenerationState state = new SiteGenerationState();

				generationstates.put(siteInfo, state);

				state.markdownDirectoryPath = SakerPathFiles.toAbsolutePath(taskcontext, siteInfo.markdownDirectory);
				state.markdownSakerDirectory = SakerPathFiles.resolveDirectoryAtPath(taskcontext,
						state.markdownDirectoryPath);

				state.markdownFiles = new TreeMap<>(
						taskcontext.getTaskUtilities().collectFilesReportInputFileAndAdditionDependency(null,
								RecursiveIgnoreCaseExtensionFileCollectionStrategy.create(state.markdownDirectoryPath,
										".md")));
				if (siteInfo.outputDirectory != null) {
					state.outputSakerDirectory = taskcontext.getTaskUtilities()
							.resolveDirectoryAtRelativePathCreate(mainoutputdir, siteInfo.outputDirectory);
				} else {
					state.outputSakerDirectory = mainoutputdir;
				}
				state.outputDirectoryPath = state.outputSakerDirectory.getSakerPath();

				SakerPath rootmarkdownpath = state.markdownDirectoryPath.resolve(siteInfo.rootMarkdownPath);
				state.rootMarkdownFile = state.markdownFiles.remove(rootmarkdownpath);
				if (state.rootMarkdownFile == null) {
					throw new IllegalArgumentException(siteInfo.rootMarkdownPath + " root markdown file not found");
				}
				taskcontext.getTaskUtilities().reportInputFileDependency(null, state.rootMarkdownFile);

				System.out.println("Markdowns: ");
				state.markdownFiles.keySet()
						.forEach(p -> System.out.println("    " + SakerPathFiles.toRelativeString(p)));

				state.templateFiles = new LinkedHashMap<>();
				for (Entry<WildcardPath, SakerPath> templateentry : siteInfo.templateFiles.entrySet()) {
					SakerFile tfile = SakerPathFiles.resolveAtPath(taskcontext, templateentry.getValue());
					if (tfile == null) {
						throw new FileNotFoundException("Template file not found: " + templateentry.getValue());
					}
					taskcontext.getTaskUtilities().reportInputFileDependency(null, tfile);

					String templatefilecontents = tfile.getContent();
					Set<String> resolvedmacros = new TreeSet<>();
					for (Iterator<Entry<String, SakerPath>> it = siteInfo.embedMacros.iterator(); it.hasNext();) {
						Entry<String, SakerPath> entry = it.next();
						String macro = entry.getKey();
						if (!templatefilecontents.contains(macro)) {
							continue;
						}
						if (!resolvedmacros.add(macro)) {
							throw new IllegalArgumentException("Recursive embed macro: " + macro);
						}
						SakerFile embedfile = taskcontext.getTaskUtilities().resolveFileAtPath(entry.getValue());
						if (embedfile == null) {
							throw new FileNotFoundException("Embed file not found: " + entry.getValue());
						}
						taskcontext.getTaskUtilities().reportInputFileDependency(null, embedfile);
						templatefilecontents = templatefilecontents.replace(macro, embedfile.getContent());
						it = siteInfo.embedMacros.iterator();
					}
					state.templateFiles.put(templateentry.getKey(),
							new ProcessedTemplate(templatefilecontents, siteInfo.placeholders, siteInfo.macros));
				}

				state.includeRelativePaths = new TreeSet<>();

				for (SimpleIncludeOption include : siteInfo.includes) {
					SakerPath incdir = include.getDirectory();
					SakerPath targetdir = include.getTargetDirectory();
					NavigableMap<SakerPath, SakerFile> includefiles = taskcontext.getTaskUtilities()
							.collectFilesReportInputFileAndAdditionDependency(null,
									WildcardFileCollectionStrategy.create(incdir, include.getResources()));
					for (Entry<SakerPath, SakerFile> entry : includefiles.entrySet()) {
						SakerFile f = entry.getValue();
						if (f instanceof SakerDirectory) {
							continue;
						}
						SakerPath relpath = incdir.relativize(entry.getKey());
						SakerPath outpath = targetdir.resolve(relpath);
						if (!state.includeRelativePaths.add(outpath)) {
							//already added
							continue;
						}
						if (!includeabsoluteoutpaths.add(state.outputDirectoryPath.resolve(outpath))) {
							//already added
							continue;
						}
						SakerDirectory fdir = taskcontext.getTaskUtilities()
								.resolveDirectoryAtRelativePathCreate(state.outputSakerDirectory, outpath.getParent());
						DelegateSakerFile outf = new DelegateSakerFile(f);
						fdir.add(outf);
						taskcontext.getTaskUtilities().reportOutputFileDependency(null, outf);
					}
				}
			}

			Parser parser = Parser.builder().extensions(MARKDOWN_EXTENSIONS).build();

			NavigableMap<SakerPath, ParsedMarkdown> rootrelativeoutputmarkdowns = new TreeMap<>();

			for (Entry<SimpleSiteInfo, SiteGenerationState> siteentry : generationstates.entrySet()) {
				SiteGenerationState state = siteentry.getValue();

				SakerPath markdowndirectory = state.markdownDirectoryPath;
				SakerPath outputdirabsolutepath = state.outputDirectoryPath;

				for (Entry<SakerPath, SakerFile> entry : state.markdownFiles.entrySet()) {
					SakerPath path = entry.getKey();
					SakerFile file = entry.getValue();
					SakerPath relpath = markdowndirectory.relativize(path);
					SakerPath pageoutputpath = outputdirabsolutepath.resolve(relpath.getParent())
							.resolve(FileUtils.changeExtension(relpath.getFileName(), "html"));
					ParsedMarkdown parsedmarkdown = parseMarkup(taskcontext, parser, file, relpath, path,
							pageoutputpath, state.markdownDirectoryPath, siteentry.getKey());
					state.relativeParsedMarkdowns.put(relpath, parsedmarkdown);

					SakerPath rootrelative = mainoutputdirpath.relativize(parsedmarkdown.getAbsoluteOutputPath());
					rootrelativeoutputmarkdowns.put(rootrelative, parsedmarkdown);
				}
			}

			try (ScriptStyleCSSBuilder cssbuilder = new ScriptStyleCSSBuilder(taskcontext,
					taskcontext.getExecutionContext().getEnvironment())) {
				cssbuilder.setLanguageBlockClass("language-sakerscript");
				for (Entry<SimpleSiteInfo, SiteGenerationState> siteentry : generationstates.entrySet()) {
					SimpleSiteInfo siteInfo = siteentry.getKey();
					SiteGenerationState state = siteentry.getValue();
					SakerPath markdowndirectory = state.markdownDirectoryPath;
					SakerDirectory docmdirectory = state.markdownSakerDirectory;
					SakerPath outputdirabsolutepath = state.outputDirectoryPath;

					RootMarkdownVisitor rootvisitor = new RootMarkdownVisitor(parser,
							siteInfo.rootMarkdownPath.getParent(), state, mainoutputdirpath);
					parseMarkupNode(parser, state.rootMarkdownFile, Collections.emptyList()).accept(rootvisitor);

					Set<SakerPath> rootlinkedresources = rootvisitor.getLinkedDocumentationResources();
					for (SakerPath rootlinkres : rootlinkedresources) {
						if (!includeabsoluteoutpaths.contains(rootlinkres)) {
							includeabsoluteoutpaths.forEach(System.out::println);
							throw new IllegalArgumentException("Root linked resource not found: " + rootlinkres);
						}
					}

					NavigableSet<SakerPath> linkeddocresources = new TreeSet<>();
					NavigableSet<SakerPath> linkeddocrootresources = new TreeSet<>();

					for (Entry<SakerPath, ParsedMarkdown> entry : state.relativeParsedMarkdowns.entrySet()) {
						//validate
						SakerPath relpath = entry.getKey();
						String docrootpath = getDocRootPath(markdowndirectory, relpath);
						ParsedMarkdown parsedmarkdown = entry.getValue();
						ValidatingVisitor validatingvisitor = new ValidatingVisitor(docrootpath, parsedmarkdown,
								state.relativeParsedMarkdowns, mainoutputdirpath, rootrelativeoutputmarkdowns);
//						if (parsedmarkdown.getHeadingTitle() == null) {
//							throw new IllegalArgumentException("No heading found in markdown: " + entry.getKey());
//						}
						parsedmarkdown.getNode().accept(validatingvisitor);
						parsedmarkdown.setContentHeadings(validatingvisitor.getHeadings());
						linkeddocresources.addAll(validatingvisitor.getLinkedDocumentationResources());
						linkeddocrootresources.addAll(validatingvisitor.getLinkedRootResources());
					}

					List<ParsedMarkdown> indexmarkdowns = new ArrayList<>();
					Map<SakerPath, Integer> markdownindices = collectSectionIndices(rootvisitor.rootSections,
							state.relativeParsedMarkdowns, indexmarkdowns);

					for (Entry<SakerPath, ParsedMarkdown> entry : state.relativeParsedMarkdowns.entrySet()) {
						ParsedMarkdown parsedmarkdown = entry.getValue();
						SakerPath relpath = entry.getKey();

						Set<String> codelanguages = parsedmarkdown.getCodeLanguages();

						Integer mdindex = markdownindices.get(relpath);

						String docrootpath = getDocRootPath(markdowndirectory, relpath);
						SakerPath pageoutputpath = outputdirabsolutepath.resolve(relpath);

						Function<String, String> tasklinkfunction = ti -> {
							TaskName tn;
							try {
								tn = TaskName.valueOf(ti);
							} catch (IllegalArgumentException e) {
								return null;
							}
							SakerPath linkedpath = siteInfo.taskLinkPaths.get(tn);
							if (linkedpath == null) {
								return null;
							}
							SakerPath linkpath = SakerPath.valueOf(docrootpath + linkedpath + "/" + tn + ".html");
							//TODO add to linked resources and check for existence
							return linkpath.toString();
						};

						Map<PlaceholderType, Supplier<? extends CharSequence>> placeholdercontents = new EnumMap<>(
								PlaceholderType.class);
						placeholdercontents.put(PlaceholderType.BODY, LazySupplier.of(() -> {
							StringBuilder sb = new StringBuilder();
							sb.append("<div class=\"doc-content\">");
							HtmlRenderer.builder().extensions(MARKDOWN_EXTENSIONS)
									.nodeRendererFactory(context -> new HtmlMarkdownRenderer(parsedmarkdown, context,
											taskcontext, docmdirectory, state.outputSakerDirectory, docrootpath,
											cssbuilder, tasklinkfunction, state.relativeParsedMarkdowns))
									.build().render(parsedmarkdown.getNode(), sb);
							if (!cssbuilder.isEmpty()) {
								sb.append("<link href=\"" + getRelativePathString(pageoutputpath, csspath)
										+ "\"  rel=\"stylesheet\" type=\"text/css\" />");
							}
							sb.append("</div>");
							return sb.toString();
						}));
						placeholdercontents.put(PlaceholderType.NAVIGATION, LazySupplier.of(() -> {
							StringBuilder sb = new StringBuilder();
							buildNavigationList(sb, state.relativeParsedMarkdowns, rootvisitor, parsedmarkdown,
									docrootpath);
							return sb.toString();
						}));
						placeholdercontents.put(PlaceholderType.TITLE, parsedmarkdown::getHeadingTitle);
						placeholdercontents.put(PlaceholderType.JS_LANGUAGES_ARRAY, LazySupplier.of(() -> {
							if (codelanguages.isEmpty()) {
								return "[]";
							}
							StringBuilder sb = new StringBuilder();
							sb.append("[");
							for (Iterator<String> it = codelanguages.iterator();;) {
								String lang = it.next();
								sb.append('\"');
								sb.append(lang);
								sb.append('\"');
								if (!it.hasNext()) {
									break;
								}
								sb.append(", ");
							}
							sb.append("]");
							return sb.toString();
						}));
						placeholdercontents.put(PlaceholderType.PATH_TO_ROOT_DIR, LazySupplier.of(() -> {
							return docrootpath;
						}));
						List<ContentHeading> contentheadings = parsedmarkdown.getContentHeadings();
						if (hasMoreThanOneContentHeading(contentheadings)) {
							placeholdercontents.put(PlaceholderType.HEADINGS_NAVIGATION, LazySupplier.of(() -> {
								StringBuilder sb = new StringBuilder();
								buildHeadingsNav(sb, contentheadings, parsedmarkdown);
								return sb.toString();
							}));
						} else {
							placeholdercontents.put(PlaceholderType.HEADINGS_NAVIGATION, Functionals.valSupplier(""));
						}
						if (mdindex != null) {
							if (mdindex > 0) {
								ParsedMarkdown section = indexmarkdowns.get(mdindex - 1);
								placeholdercontents.put(PlaceholderType.PREV_SECTION, LazySupplier.of(() -> {
									return createEndLinkSectionHtml(section, "doc-prev-section", docrootpath, false);
								}));
							}
							if (mdindex + 1 < indexmarkdowns.size()) {
								ParsedMarkdown section = indexmarkdowns.get(mdindex + 1);
								placeholdercontents.put(PlaceholderType.NEXT_SECTION, LazySupplier.of(() -> {
									return createEndLinkSectionHtml(section, "doc-next-section", docrootpath, true);
								}));
							}
						}
						if (!codelanguages.isEmpty()) {
							//XXX the languages should be statically highlighted instead of loading the scripts user side
							placeholdercontents.put(PlaceholderType.LANGUAGE_SCRIPTS, LazySupplier.of(() -> {
								StringBuilder sb = new StringBuilder();
								//TODO the hardcoded hljs script location is bad.
								sb.append("<link rel=\"stylesheet\" href=\"" + docrootpath + "../res/hljs.css\">");
								sb.append(
										"<script src=\"https://cdn.jsdelivr.net/gh/highlightjs/cdn-release@9.15.8/build/highlight.min.js\" integrity=\"sha256-js+I1fdbke/DJrW2qXQlrw7VUEqmdeFeOW37UC0bEiU=\" crossorigin=\"anonymous\"></script>");
								for (String lang : codelanguages) {
									sb.append("<script src=\"");
									sb.append(
											"https://cdn.jsdelivr.net/gh/highlightjs/cdn-release@9.13.1/build/languages/"
													+ lang + ".min.js");
									sb.append("\"></script>");
								}
								return sb.toString();
							}));
						}
						ProcessedTemplate template = state.getTemplate(relpath);
						byte[] renderedbytes = template.process(placeholdercontents).getBytes(StandardCharsets.UTF_8);

						String htmlfilename = FileUtils.changeExtension(relpath.getFileName(), "html");
						SakerDirectory finaloutdir = taskcontext.getTaskUtilities()
								.resolveDirectoryAtPathCreate(state.outputSakerDirectory, relpath.getParent());
						ByteArraySakerFile outfile = new ByteArraySakerFile(htmlfilename, renderedbytes);
						finaloutdir.add(outfile);
						taskcontext.getTaskUtilities().reportOutputFileDependency(null, outfile);
					}

					for (SakerPath linkedrespath : linkeddocresources) {
						if (state.includeRelativePaths.contains(linkedrespath)) {
							continue;
						}
						SakerFile resfile = taskcontext.getTaskUtilities().resolveFileAtRelativePath(docmdirectory,
								linkedrespath);
						if (resfile == null) {
							if (isAllowedMissingResource(linkedrespath)) {
								continue;
							}
							throw new NoSuchFileException(linkedrespath.toString(), null,
									"Linked resource file not found in site: " + state.markdownDirectoryPath);
						}
						taskcontext.getTaskUtilities().reportInputFileDependency(null, resfile);
						DelegateSakerFile addedfile = new DelegateSakerFile(resfile);
						SakerFile prev = taskcontext.getTaskUtilities().resolveDirectoryAtRelativePathCreate(
								state.outputSakerDirectory, linkedrespath.getParent()).addIfAbsent(addedfile);
						if (prev == null) {
							taskcontext.getTaskUtilities().reportOutputFileDependency(null, addedfile);
						}
					}
					for (SakerPath linkedrespath : linkeddocrootresources) {
						SakerPath abspath = mainoutputdirpath.resolve(linkedrespath);
						if (includeabsoluteoutpaths.contains(abspath)) {
							continue;
						}
						if (isAllowedMissingResource(linkedrespath)) {
							continue;
						}
						throw new NoSuchFileException(linkedrespath.toString(), null,
								"Linked root resource file not found in site: " + state.markdownDirectoryPath);
					}
				}
				if (!cssbuilder.isEmpty()) {
					SakerDirectory cssoutdir = taskcontext.getTaskUtilities()
							.resolveDirectoryAtRelativePathCreate(mainoutputdir, cssDirectory);
					ByteArraySakerFile cssoutfile = new ByteArraySakerFile(csspath.getFileName(),
							cssbuilder.getCssFileContents().getBytes(StandardCharsets.UTF_8));
					cssoutdir.add(cssoutfile);
					taskcontext.getTaskUtilities().reportOutputFileDependency(null, cssoutfile);
				}
			}
			mainoutputdir.synchronize();
			return new SimpleDocGeneratorTaskOutput(mainoutputdir.getSakerPath());
		}

		private boolean isAllowedMissingResource(SakerPath path) {
			if (allowedMissingResources != null) {
				for (WildcardPath wc : allowedMissingResources) {
					if (wc.includes(path)) {
						return true;
					}
				}
			}
			return false;
		}

		private static boolean hasMoreThanOneContentHeading(List<ContentHeading> contentheadings) {
			int size = contentheadings.size();
			if (size != 1) {
				//if 0, then no
				return size > 1;
			}
			ContentHeading first = contentheadings.get(0);
			if (first.getChildren().isEmpty()) {
				return false;
			}
			return true;
		}

		private static String getDocRootPath(SakerPath directory, SakerPath relpath) {
			SakerPath relativetodocroot = directory.resolve(relpath).getParent().relativize(directory);
			String docrootpath = SakerPath.EMPTY.equals(relativetodocroot) ? "" : (relativetodocroot + "/");
			return docrootpath;
		}

		private static String createEndLinkSectionHtml(ParsedMarkdown section, String linkdivclassname,
				String docrootpath, boolean next) {
			StringBuilder sb = new StringBuilder();
			sb.append("<a class=\"");
			sb.append(linkdivclassname);
			String headingtitle = section.getHeadingTitle();
			if (headingtitle == null) {
				throw new AssertionError("no heading title.");
			}
			sb.append(
					"\" href=\"" + docrootpath + FileUtils.changeExtension(section.getRelativePath().toString(), "html")
							+ "\" title=\"" + headingtitle + "\">");
			if (!next) {
				sb.append("<svg class=\"doc-section-arrow\" viewBox=\"0 0 24 24\">\r\n"
						+ "    <path fill=\"currentColor\" d=\"M20,11V13H8L13.5,18.5L12.08,19.92L4.16,12L12.08,4.08L13.5,5.5L8,11H20Z\" />\r\n"
						+ "</svg>");
			}
			sb.append(headingtitle);
			if (next) {
				sb.append("<svg class=\"doc-section-arrow\" viewBox=\"0 0 24 24\">\r\n"
						+ "    <path fill=\"currentColor\" d=\"M4,11V13H16L10.5,18.5L11.92,19.92L19.84,12L11.92,4.08L10.5,5.5L16,11H4Z\" />\r\n"
						+ "</svg>");
			}
			sb.append("</a>");
			return sb.toString();
		}

		private void buildHeadingsNav(StringBuilder sb, List<ContentHeading> headings, ParsedMarkdown currentmarkdown) {
			sb.append("<div class=\"doc-headings-nav\">");
			buildHeadingsNavImpl(sb, headings, currentmarkdown, "  ");
			sb.append("</div>");
		}

		private void buildHeadingsNavImpl(StringBuilder sb, List<ContentHeading> headings,
				ParsedMarkdown currentmarkdown, String indent) {
			if (headings.isEmpty()) {
				return;
			}
			sb.append(indent);
			sb.append("<ul>");
			String thisindent = indent + "  ";
			for (ContentHeading ch : headings) {
				String idattr = ch.getIdAttribute(currentmarkdown);
				if (idattr == null) {
					continue;
				}
				sb.append(thisindent);
				sb.append("<li>");
				sb.append("<a");
				sb.append(" href=\"#" + idattr + "\" title=\"" + ch.getTitle() + "\"");
				sb.append(">");
				sb.append(ch.getTitle());
				sb.append("</a>");
				buildHeadingsNavImpl(sb, ch.getChildren(), currentmarkdown, thisindent + "  ");
				sb.append(thisindent);
				sb.append("</li>");
			}
			sb.append(indent);
			sb.append("</ul>");
		}

		private void buildNavigationList(StringBuilder sb, NavigableMap<SakerPath, ParsedMarkdown> markdowns,
				RootMarkdownVisitor rootinfo, ParsedMarkdown currentmarkdown, String docrootpath) {
			sb.append("<div class=\"doc-nav\">");
			int[] collapseidcounter = new int[1];
			for (RootMarkdownSection section : rootinfo.getRootSections()) {
				String title = section.getTitle();
				sb.append("  <div class=\"doc-section\">");
				if (title != null) {
					sb.append("    <p class=\"section-title\">");
					sb.append(title);
					sb.append("</p>");
				}
				buildNaviationSubSectionsList(sb, markdowns, section.getSections(), currentmarkdown, docrootpath,
						collapseidcounter);
				sb.append("  </div>");
			}
			sb.append("</div>");
		}

		private void buildNaviationSubSectionsList(StringBuilder sb, NavigableMap<SakerPath, ParsedMarkdown> markdowns,
				List<NavigationSection> sections, ParsedMarkdown currentmarkdown, String docrootpath,
				int[] collapseidcounter) {
			if (sections.isEmpty()) {
				return;
			}
			sb.append("<ul>");
			for (NavigationSection section : sections) {
				if (section instanceof MarkdownNavigationSection) {
					MarkdownNavigationSection mdsection = (MarkdownNavigationSection) section;
					SakerPath sectionpath = mdsection.getMarkdownPath();
					ParsedMarkdown markdown = markdowns.get(sectionpath);
					if (markdown == null) {
						throw new IllegalArgumentException("Section markdown not found at path: " + sectionpath);
					}
					String title = section.getTitle();
					if (title == null) {
						title = markdown.getHeadingTitle();
					}
					if (title == null) {
						continue;
					}
					buildNavigationSectionList(sb, markdowns, markdown, title, currentmarkdown, docrootpath,
							collapseidcounter);
					continue;
				}
				if (section instanceof LinkNavigationSection) {
					LinkNavigationSection lsection = (LinkNavigationSection) section;
					sb.append("<li>");
					sb.append(
							"<a href=\""
									+ getRelativePathString(currentmarkdown.getAbsoluteOutputPath(),
											lsection.getLinkedOutputPath())
									+ "\" title=\"" + lsection.getTitle() + "\">");
					Node fc = lsection.getLink().getFirstChild();
					if (fc != null) {
						HtmlRenderer htmlrenderer = HtmlRenderer.builder().build();
						do {
							htmlrenderer.render(fc, sb);
							fc = fc.getNext();
						} while (fc != null);
					} else {
						sb.append(lsection.getTitle());
					}
					sb.append("</a>");
					sb.append("</li>");
					continue;
				}
				throw new UnsupportedOperationException(section + "");
			}
			sb.append("</ul>");
		}

		private static boolean isActiveSectionIn(ParsedMarkdown currentmarkdown, List<NavigationSection> sections,
				NavigableMap<SakerPath, ParsedMarkdown> markdowns) {
			for (NavigationSection s : sections) {
				if (s instanceof MarkdownNavigationSection) {
					ParsedMarkdown markdown = markdowns.get(((MarkdownNavigationSection) s).getMarkdownPath());
					if (markdown == currentmarkdown) {
						return true;
					}
					if (isActiveSectionIn(currentmarkdown, markdown.getSubSections(), markdowns)) {
						return true;
					}
				}
			}
			return false;
		}

		private void buildNavigationSectionList(StringBuilder sb, NavigableMap<SakerPath, ParsedMarkdown> markdowns,
				ParsedMarkdown markdown, String title, ParsedMarkdown currentmarkdown, String docrootpath,
				int[] collapseidcounter) {
			List<NavigationSection> subsections = markdown.getSubSections();

			sb.append("<li");
			boolean activemarkdown = markdown == currentmarkdown;
			boolean activeparent = activemarkdown || isActiveSectionIn(currentmarkdown, subsections, markdowns);
			if (activemarkdown || activeparent) {
				sb.append(" class=\"");
				if (activemarkdown) {
					sb.append("doc-active");
				}
				if (activeparent) {
					sb.append(" doc-active-parent");
				}
				sb.append("\"");
			}
			sb.append(">");
			String linkatag = "<a class=\"doc-nav-section-link\" href=\""
					+ getRelativePathString(currentmarkdown.getAbsoluteOutputPath(), markdown.getAbsoluteOutputPath())
					+ "\" title=\"" + title + "\">" + title + "</a>";
			if (!subsections.isEmpty()) {
				String inputid = "_doc_collapse_id_" + (collapseidcounter[0]++);

				sb.append("<label class=\"doc-nav-collapse\" for=\"");
				sb.append(inputid);
				sb.append("\">");
				sb.append(linkatag);
				sb.append("</label>");

				sb.append("<input style=\"display:none;\" id=\"");
				sb.append(inputid);
				sb.append("\" type=\"checkbox\"");
				if (activeparent || activemarkdown) {
					sb.append(" checked");
				}
				sb.append("/>");

				sb.append("<label class=\"doc-nav-section-arrow\" for=\"" + inputid + "\"></label>");

				buildNaviationSubSectionsList(sb, markdowns, subsections, currentmarkdown, docrootpath,
						collapseidcounter);
			} else {
				sb.append(linkatag);
			}
			sb.append("</li>");
		}

		private ParsedMarkdown parseMarkup(TaskContext taskcontext, Parser parser, SakerFile file,
				SakerPath relativepath, SakerPath absolutepath, SakerPath pageoutputpath,
				SakerPath markdownDirectoryPath, SimpleSiteInfo siteinfo) throws IOException {
			Node node = parseMarkupNode(parser, file, siteinfo.macros);
			PreprocessingVisitor preprocessingvisitor = new PreprocessingVisitor(taskcontext, relativepath,
					absolutepath, markdownDirectoryPath, resourceRoots, siteinfo);
			node.accept(preprocessingvisitor);
			ParsedMarkdown result = new ParsedMarkdown(node, preprocessingvisitor.getTitleHeadingText(), absolutepath,
					relativepath, preprocessingvisitor.getSubSections(), pageoutputpath);
			result.setNodeAnchors(preprocessingvisitor.nodeAnchors);
			result.setAnchorTitles(preprocessingvisitor.anchorTitles);
			return result;
		}

		private static Node parseMarkupNode(Parser parser, SakerFile file,
				Iterable<? extends Entry<? extends String, ? extends String>> macros) throws IOException {
			String contents = file.getContent();
			for (Entry<? extends String, ? extends String> m : macros) {
				contents = contents.replace("#MACRO_" + m.getKey(), m.getValue());
			}
			return parser.parse(contents);
		}

		@Override
		public void writeExternal(ObjectOutput out) throws IOException {
			out.writeObject(outputDirectory);
			out.writeObject(cssDirectory);
			SerialUtils.writeExternalCollection(out, siteInfos);
			SerialUtils.writeExternalMap(out, resourceRoots);
			SerialUtils.writeExternalCollection(out, allowedMissingResources);
		}

		@Override
		public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
			outputDirectory = (SakerPath) in.readObject();
			cssDirectory = (SakerPath) in.readObject();
			siteInfos = SerialUtils.readExternalImmutableList(in);
			resourceRoots = SerialUtils.readExternalImmutableNavigableMap(in);
			allowedMissingResources = SerialUtils.readExternalImmutableNavigableSet(in);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((allowedMissingResources == null) ? 0 : allowedMissingResources.hashCode());
			result = prime * result + ((cssDirectory == null) ? 0 : cssDirectory.hashCode());
			result = prime * result + ((outputDirectory == null) ? 0 : outputDirectory.hashCode());
			result = prime * result + ((resourceRoots == null) ? 0 : resourceRoots.hashCode());
			result = prime * result + ((siteInfos == null) ? 0 : siteInfos.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			WorkerTaskFactory other = (WorkerTaskFactory) obj;
			if (allowedMissingResources == null) {
				if (other.allowedMissingResources != null)
					return false;
			} else if (!allowedMissingResources.equals(other.allowedMissingResources))
				return false;
			if (cssDirectory == null) {
				if (other.cssDirectory != null)
					return false;
			} else if (!cssDirectory.equals(other.cssDirectory))
				return false;
			if (outputDirectory == null) {
				if (other.outputDirectory != null)
					return false;
			} else if (!outputDirectory.equals(other.outputDirectory))
				return false;
			if (resourceRoots == null) {
				if (other.resourceRoots != null)
					return false;
			} else if (!resourceRoots.equals(other.resourceRoots))
				return false;
			if (siteInfos == null) {
				if (other.siteInfos != null)
					return false;
			} else if (!siteInfos.equals(other.siteInfos))
				return false;
			return true;
		}

	}

	public interface IncludeTaskOption {
		public SakerPath getDirectory();

		public WildcardPath getResources();

		public SakerPath getTargetDirectory();

		public static IncludeTaskOption valueOf(String str) {
			return valueOf(WildcardPath.valueOf(str));
		}

		public static IncludeTaskOption valueOf(SakerPath path) {
			return valueOf(WildcardPath.valueOf(path));
		}

		public static IncludeTaskOption valueOf(WildcardPath path) {
			return new IncludeTaskOption() {
				@Override
				public SakerPath getTargetDirectory() {
					return null;
				}

				@Override
				public WildcardPath getResources() {
					return path;
				}

				@Override
				public SakerPath getDirectory() {
					return SakerPath.EMPTY;
				}
			};
		}
	}

	private static class SimpleIncludeOption implements Externalizable {
		private static final long serialVersionUID = 1L;

		private SakerPath directory;
		private WildcardPath resources;
		private SakerPath targetDirectory;

		/**
		 * For {@link Externalizable}.
		 */
		public SimpleIncludeOption() {
		}

		public SimpleIncludeOption(SakerPath directory, WildcardPath resources, SakerPath targetDirectory) {
			this.directory = directory;
			this.resources = resources;
			this.targetDirectory = targetDirectory;
		}

		public SakerPath getDirectory() {
			return directory;
		}

		public WildcardPath getResources() {
			return resources;
		}

		public SakerPath getTargetDirectory() {
			return targetDirectory;
		}

		@Override
		public void writeExternal(ObjectOutput out) throws IOException {
			out.writeObject(directory);
			out.writeObject(resources);
			out.writeObject(targetDirectory);
		}

		@Override
		public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
			directory = (SakerPath) in.readObject();
			resources = (WildcardPath) in.readObject();
			targetDirectory = (SakerPath) in.readObject();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((directory == null) ? 0 : directory.hashCode());
			result = prime * result + ((resources == null) ? 0 : resources.hashCode());
			result = prime * result + ((targetDirectory == null) ? 0 : targetDirectory.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			SimpleIncludeOption other = (SimpleIncludeOption) obj;
			if (directory == null) {
				if (other.directory != null)
					return false;
			} else if (!directory.equals(other.directory))
				return false;
			if (resources == null) {
				if (other.resources != null)
					return false;
			} else if (!resources.equals(other.resources))
				return false;
			if (targetDirectory == null) {
				if (other.targetDirectory != null)
					return false;
			} else if (!targetDirectory.equals(other.targetDirectory))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "SimpleIncludeOption[" + (directory != null ? "directory=" + directory + ", " : "")
					+ (resources != null ? "resources=" + resources + ", " : "")
					+ (targetDirectory != null ? "targetDirectory=" + targetDirectory : "") + "]";
		}
	}

	public interface SiteTaskOption {
		public SakerPath getDirectory();

		public SakerPath getSiteOutputDirectory();

		public Map<WildcardPath, SakerPath> getTemplateFiles();

		public SakerPath getRootMarkdown();

		public Map<TaskName, SakerPath> getTaskLinkPaths();

		public Map<String, String> getMacros();

		public Collection<IncludeTaskOption> getInclude();

		public Map<String, SakerPath> getEmbedMacros();
	}

	private static final class TaskImplementation implements ParameterizableTask<Object> {
		private static final SimplePlaceholderCollection DEFAULTS_PLACEHOLDER_COLLECTION = new SimplePlaceholderCollection(
				new PlaceholderCollection() {
					//returns the defaults
				});

		private static final WildcardPath ALL_WILDCARD = WildcardPath.valueOf("**");

		@SakerInput("OutputDirectory")
		public SakerPath outputDirectoryOption;
		@SakerInput("CSSDirectory")
		public SakerPath cssDirectoryOption = SakerPath.valueOf("css");
		@SakerInput("ResourceRoots")
		public Map<String, SakerPath> resourceRootsOption = Collections.emptyNavigableMap();
		@SakerInput(value = { "Site", "Sites" }, required = true)
		public Collection<SiteTaskOption> sitesOption;

		@SakerInput(value = "AllowMissingResources")
		public Collection<WildcardPath> allowedMissingResourcesOption = Collections.emptyNavigableSet();

		@Override
		public Object run(TaskContext taskcontext) throws Exception {
			if (outputDirectoryOption == null) {
				outputDirectoryOption = SakerPath.valueOf("default");
			}

			SakerPath workdirpath = taskcontext.getTaskWorkingDirectoryPath();
			List<SimpleSiteInfo> sites = new ArrayList<>();
			NavigableMap<String, SakerPath> resroots = new TreeMap<>();
			for (Entry<String, SakerPath> entry : resourceRootsOption.entrySet()) {
				String normalizedroot = SakerPath.normalizeRoot(entry.getKey());
				if ("root:".equals(normalizedroot) || "http:".equals(normalizedroot) || "https:".equals(normalizedroot)
						|| "/".equals(normalizedroot)) {
					throw new IllegalArgumentException("Invalid resource root: " + entry);
				}
				resroots.put(normalizedroot, workdirpath.tryResolve(entry.getValue()));
			}

			DocWorkerTaskIdentifier workertaskid = new DocWorkerTaskIdentifier(outputDirectoryOption);
			for (SiteTaskOption site : sitesOption) {
				SakerPath siteoutputdir = ObjectUtils.nullDefault(site.getSiteOutputDirectory(), SakerPath.EMPTY);
				Collection<SimpleIncludeOption> includes = new LinkedHashSet<>();
				Collection<IncludeTaskOption> siteincludes = site.getInclude();
				if (!ObjectUtils.isNullOrEmpty(siteincludes)) {
					for (IncludeTaskOption incto : siteincludes) {
						SakerPath incdir = workdirpath
								.tryResolve(ObjectUtils.nullDefault(incto.getDirectory(), workdirpath));
						includes.add(new SimpleIncludeOption(incdir,
								ObjectUtils.nullDefault(incto.getResources(), ALL_WILDCARD),
								ObjectUtils.nullDefault(incto.getTargetDirectory(), SakerPath.EMPTY)));
					}
				}

				Map<TaskName, SakerPath> sitetasklinkpaths = ImmutableUtils
						.makeImmutableNavigableMap(site.getTaskLinkPaths());

				Map<WildcardPath, SakerPath> templatefiles = ImmutableUtils
						.makeImmutableLinkedHashMap(site.getTemplateFiles());
				if (ObjectUtils.isNullOrEmpty(templatefiles)) {
					throw new IllegalArgumentException("No template files defined.");
				}
				Map<String, String> sitemacros = ImmutableUtils.makeImmutableLinkedHashMap(site.getMacros());
				if (sitemacros == null) {
					sitemacros = Collections.emptyMap();
				}
				Map<String, SakerPath> embedmacros = ImmutableUtils.makeImmutableLinkedHashMap(site.getEmbedMacros());
				if (embedmacros == null) {
					embedmacros = Collections.emptyMap();
				}
				sites.add(new SimpleSiteInfo(site.getDirectory(), siteoutputdir, templatefiles,
						DEFAULTS_PLACEHOLDER_COLLECTION, ImmutableUtils.makeImmutableLinkedHashSet(includes),
						site.getRootMarkdown(), sitetasklinkpaths,
						ImmutableUtils.makeImmutableList(sitemacros.entrySet()),
						ImmutableUtils.makeImmutableList(embedmacros.entrySet())));
			}

			WorkerTaskFactory workertaskfactory = new WorkerTaskFactory(outputDirectoryOption, cssDirectoryOption,
					resroots, sites);
			workertaskfactory.allowedMissingResources = new TreeSet<>();
			if (allowedMissingResourcesOption != null) {
				for (WildcardPath wp : allowedMissingResourcesOption) {
					if (wp == null) {
						continue;
					}
					workertaskfactory.allowedMissingResources.add(wp);
				}
			}

			taskcontext.getTaskUtilities().startTask(workertaskid, workertaskfactory);
			SimpleStructuredObjectTaskResult result = new SimpleStructuredObjectTaskResult(workertaskid);
			taskcontext.reportSelfTaskOutputChangeDetector(new EqualityTaskOutputChangeDetector(result));
			return result;
		}
	}

	@Override
	public Task<? extends Object> createTask(ExecutionContext executionContext) {
		return new TaskImplementation();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
	}

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return ObjectUtils.isSameClass(this, obj);
	}

	private static String getRelativePathString(SakerPath pagepath, SakerPath targetpath) {
		SakerPath relpath = pagepath.getParent().relativize(targetpath);
		return relpath.toString();
	}
}
