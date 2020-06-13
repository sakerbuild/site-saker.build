package saker.docgen;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.nio.charset.StandardCharsets;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.Supplier;

import org.commonmark.renderer.html.HtmlWriter;

import saker.build.exception.InvalidPathFormatException;
import saker.build.file.path.SakerPath;
import saker.build.file.path.WildcardPath;
import saker.build.file.provider.FileEntry;
import saker.build.file.provider.FileEventListener;
import saker.build.file.provider.FileEventListener.ListenerToken;
import saker.build.file.provider.FileProviderKey;
import saker.build.file.provider.RootFileProviderKey;
import saker.build.file.provider.SakerFileProvider;
import saker.build.file.provider.SakerPathFiles;
import saker.build.runtime.environment.SakerEnvironment;
import saker.build.runtime.params.ExecutionPathConfiguration;
import saker.build.runtime.params.ExecutionScriptConfiguration;
import saker.build.runtime.params.ExecutionScriptConfiguration.ScriptOptionsConfig;
import saker.build.runtime.params.ExecutionScriptConfiguration.ScriptProviderLocation;
import saker.build.scripting.ScriptAccessProvider;
import saker.build.scripting.ScriptParsingFailedException;
import saker.build.scripting.SimpleScriptParsingOptions;
import saker.build.scripting.model.ScriptModellingEngine;
import saker.build.scripting.model.ScriptModellingEnvironment;
import saker.build.scripting.model.ScriptModellingEnvironmentConfiguration;
import saker.build.scripting.model.ScriptStructureOutline;
import saker.build.scripting.model.ScriptSyntaxModel;
import saker.build.scripting.model.ScriptToken;
import saker.build.scripting.model.SimpleScriptToken;
import saker.build.scripting.model.SimpleTokenStyle;
import saker.build.scripting.model.StructureOutlineEntry;
import saker.build.scripting.model.TokenStyle;
import saker.build.scripting.model.info.ExternalScriptInformationProvider;
import saker.build.task.TaskContext;
import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.thirdparty.saker.util.StringUtils;
import saker.build.thirdparty.saker.util.function.LazySupplier;
import saker.build.thirdparty.saker.util.io.ByteSink;
import saker.build.thirdparty.saker.util.io.ByteSource;
import saker.build.thirdparty.saker.util.io.UnsyncByteArrayInputStream;

public class ScriptStyleCSSBuilder implements AutoCloseable {
	private static final SimpleTokenStyle TOKEN_STYLE_UNSTYLED = new SimpleTokenStyle();
	private static final Set<TokenStyle> SINGLE_UNSTYLED_TOKEN = Collections.singleton(TOKEN_STYLE_UNSTYLED);

	private NavigableMap<String, Set<? extends TokenStyle>> classStyles = new TreeMap<>();
	private Map<Set<? extends TokenStyle>, String> styleClasses = new LinkedHashMap<>();

	private String lastCssName = "";

	private Map<String, TokenStyle> lightStyles = new TreeMap<>();
	private Map<String, TokenStyle> darkStyles = new TreeMap<>();

	private String languageBlockClass = null;

	private ScriptAccessProvider builtinScriptAccessProvider;
	private TaskContext taskcontext;

	public ScriptStyleCSSBuilder(TaskContext taskcontext, SakerEnvironment environment) throws Exception {
		this.taskcontext = taskcontext;
		builtinScriptAccessProvider = ExecutionScriptConfiguration.getScriptAccessorProvider(environment,
				ScriptProviderLocation.getBuiltin());
	}

	public void setLanguageBlockClass(String languageBlockClass) {
		this.languageBlockClass = languageBlockClass;
	}

	public boolean isEmpty() {
		return classStyles.isEmpty();
	}

	public String getCssFileContents() {
		StringBuilder cssBuilder = new StringBuilder();
		if (!lightStyles.isEmpty()) {
			//cssBuilder.append("@media not all and (prefers-color-scheme: dark) {\n");
			writeStyles(cssBuilder, lightStyles);
			//cssBuilder.append("}\n");
		}
		if (!darkStyles.isEmpty()) {
			cssBuilder.append("@media all and (prefers-color-scheme: dark) {\n");
			cssBuilder.append(StringUtils.toStringJoin(
					ObjectUtils.isNullOrEmpty(languageBlockClass) ? "." : "." + languageBlockClass + " .",
					ObjectUtils.isNullOrEmpty(languageBlockClass) ? ", ." : ", ." + languageBlockClass + " .",
					lightStyles.keySet(), null));
			cssBuilder.append(
					"{ color:unset; background-color:unset; font-weight:unset; font-style:unset; text-decoration:unset; }\n");
			writeStyles(cssBuilder, darkStyles);
			cssBuilder.append("}\n");
		}
		return cssBuilder.toString();
	}

	private void writeStyles(StringBuilder cssBuilder, Map<String, TokenStyle> styles) {
		for (Entry<String, TokenStyle> entry : styles.entrySet()) {
			if (!ObjectUtils.isNullOrEmpty(languageBlockClass)) {
				cssBuilder.append('.');
				cssBuilder.append(languageBlockClass);
				cssBuilder.append(' ');
			}
			cssBuilder.append('.');
			cssBuilder.append(entry.getKey());
			cssBuilder.append('{');
			appendCssStyle(entry.getValue(), cssBuilder);
			cssBuilder.append("}\n");
		}
	}

	public ProcessedScript process(String scriptcontents) throws IOException, ScriptParsingFailedException {
		byte[] bytecontents = scriptcontents.getBytes(StandardCharsets.UTF_8);

		ExecutionScriptConfiguration scriptconfig = ExecutionScriptConfiguration.builder()
				.addConfig(WildcardPath.valueOf("/**"),
						new ScriptOptionsConfig(Collections.emptyNavigableMap(), ScriptProviderLocation.getBuiltin()))
				.build();
		ExecutionPathConfiguration pathconfig = ExecutionPathConfiguration.builder(SakerPath.valueOf("/"))
				.addAllRoots(new SingleFileSakerFileProvider("/", "script.build", bytecontents)).build();
		SakerPath scriptpath = SakerPath.valueOf("/script.build");
		try (StyleBuildingModellingEnvironment modellingenv = new StyleBuildingModellingEnvironment(scriptpath,
				bytecontents, scriptconfig, pathconfig)) {
			modellingenv.init(builtinScriptAccessProvider);
			ScriptSyntaxModel model = modellingenv.getModel(scriptpath);
			model.createModel(null);
			Iterable<? extends ScriptToken> tokens = model.getTokens(0, Integer.MAX_VALUE);
			List<ScriptToken> tokenslist = new ArrayList<>();
			for (ScriptToken t : tokens) {
				tokenslist.add(new SimpleScriptToken(t.getOffset(), t.getLength(), t.getType()));
			}
			Map<String, Set<? extends TokenStyle>> styles = model.getTokenStyles();
			ScriptStructureOutline outline = model.getStructureOutline();
			return new ProcessedScript(tokenslist, styles, scriptcontents, outline);
		}
	}

	@Override
	public void close() throws IOException {
	}

	public interface TaskLinkHrefProvider {
		public String getTaskLink(String taskid);
	}

	private static StructureOutlineEntry findTaskEntryForParameterOffsetInOutline(
			List<? extends StructureOutlineEntry> entries, int offset) {
		for (StructureOutlineEntry entry : entries) {
			if (offset >= entry.getOffset() && offset <= entry.getOffset() + entry.getLength()) {
				if ("saker.script.task".equals(entry.getSchemaIdentifier())) {
					for (StructureOutlineEntry c : entry.getChildren()) {
						if ("saker.script.task.parameter".equals(c.getSchemaIdentifier())) {
							if (offset >= c.getOffset() && offset <= c.getOffset() + c.getLength()) {
								return entry;
							}
						}
					}
				}
				StructureOutlineEntry tn = findTaskEntryForParameterOffsetInOutline(entry.getChildren(), offset);
				if (tn != null) {
					return tn;
				}
			}
		}
		return null;
	}

	public class ProcessedScript {
		private List<ScriptToken> tokensList;
		private Map<String, Set<? extends TokenStyle>> styles;
		private String contents;
		private ScriptStructureOutline outline;

		public ProcessedScript(List<ScriptToken> tokenslist, Map<String, Set<? extends TokenStyle>> styles,
				String scriptcontents, ScriptStructureOutline outline) {
			this.tokensList = tokenslist;
			this.styles = styles;
			this.contents = scriptcontents;
			this.outline = outline;
		}

		public void write(HtmlWriter writer, int startoffset, int endoffset) {
			write(writer, startoffset, endoffset, null);
		}

		public void write(HtmlWriter writer, int startoffset, int endoffset,
				TaskLinkHrefProvider tasklinkhreffunction) {
			String lastclass = null;
			for (ScriptToken token : tokensList) {
				if (token.getOffset() >= endoffset) {
					break;
				}
				if (token.isEmpty() || token.getEndOffset() < startoffset) {
					//token empty or not in range
					continue;
				}

				int tokenstartoffset = Math.max(startoffset, token.getOffset());
				int tokenendoffset = Math.min(token.getEndOffset(), endoffset);
				String text = contents.substring(tokenstartoffset, tokenendoffset);
				if ("__TOKEN__".equals(text) || "\"__TOKEN__\"".equals(text)) {
					//don't need this
					continue;
				}

				String title = null;
				String link = null;
				switch (token.getType()) {
					case "task_identifier": {
						if (tasklinkhreffunction != null) {
							//link the task if appropriate
							link = tasklinkhreffunction.getTaskLink(text);
							title = "Task: " + text + "()";
						}
						break;
					}
					case "param_name_content": {
						StructureOutlineEntry taskoutlineentry = findTaskEntryForParameterOffsetInOutline(
								outline.getRootEntries(), token.getOffset() + token.getLength() / 2);
						if (taskoutlineentry != null) {
							String taskname = contents.substring(taskoutlineentry.getSelectionOffset(),
									taskoutlineentry.getSelectionOffset() + taskoutlineentry.getSelectionLength());
							String tlink = tasklinkhreffunction.getTaskLink(taskname);
							if (tlink != null) {
								title = "Parameter " + text + " of task " + taskname + "()";
								link = tlink + "#" + text;
							}
						}
						break;
					}
					default:
						break;
				}

				Set<? extends TokenStyle> tokenstyles;
				if (isAllWhitespace(text)) {
					tokenstyles = Collections.emptySet();
				} else {
					tokenstyles = styles.get(token.getType());
				}
				if (ObjectUtils.isNullOrEmpty(tokenstyles) || SINGLE_UNSTYLED_TOKEN.equals(tokenstyles)) {
					if (lastclass != null) {
						writer.tag("/span");
						lastclass = null;
					}
					writeTextWithLink(writer, text, link, title);
				} else {
					String styleclassname;
					String foundstylecname = styleClasses.get(tokenstyles);
					if (foundstylecname != null) {
						styleclassname = foundstylecname;
					} else {
						String nextcname = nextShortName(lastCssName);
						styleclassname = nextcname;
						lastCssName = nextcname;
						for (TokenStyle ts : tokenstyles) {
							int theme = ts.getStyle() & TokenStyle.THEME_MASK;
							styleClasses.put(tokenstyles, nextcname);
							classStyles.put(nextcname, tokenstyles);
							switch (theme) {
								case TokenStyle.THEME_DARK: {
									darkStyles.put(nextcname, ts);
									break;
								}
								case TokenStyle.THEME_LIGHT:
								default: {
									lightStyles.put(nextcname, ts);
									break;
								}
							}
						}
//						if (writeclassstyles.size() > 1) {
//							//do not use the default theme
//							writeclassstyles.remove("");
//						}
//						if (writeclassstyles.size() == 1 && TOKEN_STYLE_UNSTYLED.equals(writeclassstyles.get(""))) {
//							styleclassname = null;
//						} else {
//							for (Entry<String, TokenStyle> entry : writeclassstyles.entrySet()) {
//								if (!entry.getKey().isEmpty()) {
//									cssBuilder.append('.');
//									cssBuilder.append(entry.getKey());
//									cssBuilder.append(' ');
//								}
//								cssBuilder.append('.');
//								cssBuilder.append(nextcname);
//								cssBuilder.append('{');
//								appendCssStyle(entry.getValue(), cssBuilder);
//								cssBuilder.append("}\n");
//							}
//							styleclassname = nextcname;
//
//							styleClasses.put(tokenstyles, nextcname);
//							classStyles.put(nextcname, tokenstyles);
//							lastCssName = nextcname;
//						}
					}

					if (styleclassname == null) {
						if (lastclass != null) {
							writer.tag("/span");
							lastclass = null;
						}
						writeTextWithLink(writer, text, link, title);
					} else {
						if (styleclassname.equals(lastclass)) {
							writeTextWithLink(writer, text, link, title);
						} else {
							if (lastclass != null) {
								writer.tag("/span");
							}
							lastclass = styleclassname;
							Map<String, String> attrs = new TreeMap<>();
							attrs.put("class", styleclassname);
							writer.tag("span", attrs);
							writeTextWithLink(writer, text, link, title);
						}
					}
				}
			}
			if (lastclass != null) {
				writer.tag("/span");
				lastclass = null;
			}
		}

		private void writeTextWithLink(HtmlWriter writer, String text, String link, String title) {
			if (link != null) {
				Map<String, String> attrmap = new TreeMap<>();
				attrmap.put("href", link);
				if (title != null) {
					attrmap.put("title", title);
				}
				writer.tag("a", attrmap);
				writer.text(text);
				writer.tag("/a");
			} else {
				writer.text(text);
			}
		}
	}

	private static boolean isAllWhitespace(String str) {
		int len = str.length();
		for (int i = 0; i < len; i++) {
			char c = str.charAt(i);
			if (!Character.isWhitespace(c)) {
				return false;
			}
		}
		return true;
	}

	private static String nextShortName(String cname) {
		if (StringUtils.isConsistsOnlyChar(cname, 'z')) {
			return StringUtils.repeatCharacter('a', cname.length() + 1);
		}
		char[] chars = cname.toCharArray();
		for (int i = cname.length() - 1; i >= 0; i--) {
			char c = chars[i];
			if (c < 'z') {
				chars[i] = (char) (c + 1);
				break;
			}
			chars[i] = 'a';
		}
		return new String(chars);
	}

	private static void appendCssStyle(TokenStyle style, StringBuilder sb) {
		int foreg = style.getForegroundColor();
		int backg = style.getBackgroundColor();
		if (foreg != TokenStyle.COLOR_UNSPECIFIED) {
			sb.append("color:");
			appendRGBAColorFunction(sb, foreg);
			sb.append(';');
		}
		if (backg != TokenStyle.COLOR_UNSPECIFIED) {
			sb.append("background-color:");
			appendRGBAColorFunction(sb, foreg);
			sb.append(';');
		}
		int textstyle = style.getStyle();
		if (((textstyle & TokenStyle.STYLE_BOLD) == TokenStyle.STYLE_BOLD)) {
			sb.append("font-weight:bold;");
		}
		if (((textstyle & TokenStyle.STYLE_ITALIC) == TokenStyle.STYLE_ITALIC)) {
			sb.append("font-style:italic;");
		}
		if (((textstyle & TokenStyle.STYLE_STRIKETHROUGH) == TokenStyle.STYLE_STRIKETHROUGH)) {
			if (((textstyle & TokenStyle.STYLE_UNDERLINE) == TokenStyle.STYLE_UNDERLINE)) {
				sb.append("text-decoration:underline line-through;");
			} else {
				sb.append("text-decoration:line-through;");
			}
		} else {
			if (((textstyle & TokenStyle.STYLE_UNDERLINE) == TokenStyle.STYLE_UNDERLINE)) {
				sb.append("text-decoration:underline;");
			}
		}
	}

	private static void appendHexByte(int val, StringBuilder sb) {
		if (val <= 0x0f) {
			sb.append('0');
		} else {
			int higher = val >> 4;
			if (higher <= 9) {
				sb.append((char) ('0' + higher));
			} else {
				sb.append((char) ('a' + higher - 10));
			}
		}
		int lower = val & 0xf;
		if (lower <= 9) {
			sb.append((char) ('0' + lower));
		} else {
			sb.append((char) ('a' + lower - 10));
		}
	}

	private static void appendRGBAColorFunction(StringBuilder sb, int color) {
		int a = (color >>> 24) & 0xFF;
		int r = (color >>> 16) & 0xFF;
		int g = (color >>> 8) & 0xFF;
		int b = (color) & 0xFF;

		if (a != 255) {
			sb.append("rgba(");
		} else {
			sb.append('#');
			appendHexByte(r, sb);
			appendHexByte(g, sb);
			appendHexByte(b, sb);
			return;
		}
		sb.append(r);
		sb.append(',');
		sb.append(g);
		sb.append(',');
		sb.append(b);
		if (a != 255) {
			sb.append(',');
			sb.append(String.format(Locale.US, "%.2f", a / 255f));
		}
		sb.append(")");
	}

	private static class StyleBuildingModellingEnvironment implements ScriptModellingEnvironment {
		private SakerPath scriptPath;
		private byte[] scriptContents;
		private ScriptModellingEngine engine;
		private Supplier<ScriptSyntaxModel> modelSupplier;
		private ExecutionScriptConfiguration scriptConfiguration;
		private ExecutionPathConfiguration pathConfiguration;

		public StyleBuildingModellingEnvironment(SakerPath scriptPath, byte[] scriptContents,
				ExecutionScriptConfiguration scriptConfiguration, ExecutionPathConfiguration pathConfiguration) {
			this.scriptPath = scriptPath;
			this.scriptContents = scriptContents;
			this.scriptConfiguration = scriptConfiguration;
			this.pathConfiguration = pathConfiguration;
		}

		public void init(ScriptAccessProvider builtinScriptAccessProvider) {
			engine = builtinScriptAccessProvider.createModellingEngine(this);
			modelSupplier = LazySupplier.of(() -> {
				return engine.createModel(new SimpleScriptParsingOptions(scriptPath),
						() -> new UnsyncByteArrayInputStream(scriptContents));
			});
		}

		@Override
		public NavigableSet<SakerPath> getTrackedScriptPaths() {
			return ImmutableUtils.singletonNavigableSet(scriptPath);
		}

		@Override
		public ScriptSyntaxModel getModel(SakerPath scriptpath) throws InvalidPathFormatException {
			SakerPathFiles.requireAbsolutePath(scriptpath);
			if (scriptpath.equals(this.scriptPath)) {
				return modelSupplier.get();
			}
			return null;
		}

		@Override
		public ScriptModellingEnvironmentConfiguration getConfiguration() {
			return new ScriptModellingEnvironmentConfiguration() {

				@Override
				public ExecutionScriptConfiguration getScriptConfiguration() {
					return scriptConfiguration;
				}

				@Override
				public ExecutionPathConfiguration getPathConfiguration() {
					return pathConfiguration;
				}

				@Override
				public Collection<? extends ExternalScriptInformationProvider> getExternalScriptInformationProviders() {
					return Collections.emptySet();
				}

				@Override
				public Set<? extends WildcardPath> getExcludedScriptPaths() {
					return Collections.emptyNavigableSet();
				}

				@Override
				public Map<String, String> getUserParameters() {
					return Collections.emptyNavigableMap();
				}
			};
		}

		@Override
		public void close() throws IOException {
			engine.close();
		}
	}

	private static class SimpleRootFileProviderKey implements RootFileProviderKey, Externalizable {
		private static final long serialVersionUID = 1L;

		private UUID uuid;

		/**
		 * For {@link Externalizable}.
		 */
		public SimpleRootFileProviderKey() {
		}

		public SimpleRootFileProviderKey(UUID uuid) {
			this.uuid = uuid;
		}

		@Override
		public UUID getUUID() {
			return uuid;
		}

		@Override
		public void writeExternal(ObjectOutput out) throws IOException {
			out.writeObject(uuid);
		}

		@Override
		public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
			uuid = (UUID) in.readObject();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
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
			SimpleRootFileProviderKey other = (SimpleRootFileProviderKey) obj;
			if (uuid == null) {
				if (other.uuid != null)
					return false;
			} else if (!uuid.equals(other.uuid))
				return false;
			return true;
		}

	}

	private interface SetGetRoots {
		public Set<String> getRoots() throws IOException;
	}

	private static class SingleFileSakerFileProvider implements SakerFileProvider, SetGetRoots {
		private String root;
		private String fileName;
		private byte[] contents;
		private FileProviderKey providerKey = new SimpleRootFileProviderKey(UUID.randomUUID());

		public SingleFileSakerFileProvider(String root, String fileName, byte[] contents) {
			this.root = root;
			this.fileName = fileName;
			this.contents = contents;
		}

		@Override
		public NavigableSet<String> getRoots() throws IOException {
			return ImmutableUtils.singletonNavigableSet(root);
		}

		@Override
		public FileProviderKey getProviderKey() {
			return providerKey;
		}

		@Override
		public NavigableMap<String, ? extends FileEntry> getDirectoryEntries(SakerPath path) throws IOException {
			if (path.getNameCount() == 0 && root.equals(path.getRoot())) {
				return ImmutableUtils.singletonNavigableMap(fileName,
						new FileEntry(FileEntry.TYPE_FILE, contents.length, FileTime.fromMillis(500)));
			}
			throw new IOException("not directory.");
		}

		@Override
		public NavigableMap<SakerPath, ? extends FileEntry> getDirectoryEntriesRecursively(SakerPath path)
				throws IOException {
			if (path.getNameCount() == 0 && root.equals(path.getRoot())) {
				return ImmutableUtils.singletonNavigableMap(SakerPath.valueOf(fileName),
						new FileEntry(FileEntry.TYPE_FILE, contents.length, FileTime.fromMillis(500)));
			}
			throw new IOException("not directory.");
		}

		@Override
		public FileEntry getFileAttributes(SakerPath path, LinkOption... linkoptions) throws IOException {
			if (root.equals(path.getRoot()) && path.getNameCount() == 1 && path.getName(0).equals(fileName)) {
				return new FileEntry(FileEntry.TYPE_FILE, contents.length, FileTime.fromMillis(500));
			}
			throw new IOException("not exists.");
		}

		@Override
		public void setLastModifiedMillis(SakerPath path, long millis) throws IOException {
			throw new IOException("unsupported");
		}

		@Override
		public void createDirectories(SakerPath path) throws IOException, FileAlreadyExistsException {
			throw new IOException("unsupported");
		}

		@Override
		public void deleteRecursively(SakerPath path) throws IOException {
			throw new IOException("unsupported");
		}

		@Override
		public void delete(SakerPath path) throws IOException, DirectoryNotEmptyException {
			throw new IOException("unsupported");
		}

		@Override
		public ByteSource openInput(SakerPath path, OpenOption... openoptions) throws IOException {
			if (path.getNameCount() == 0 && root.equals(path.getRoot())) {
				return new UnsyncByteArrayInputStream(contents);
			}
			throw new IOException("not exists.");
		}

		@Override
		public ByteSink openOutput(SakerPath path, OpenOption... openoptions) throws IOException {
			throw new IOException("unsupported");
		}

		@Override
		public int ensureWriteRequest(SakerPath path, int filetype, int operationflag)
				throws IOException, IllegalArgumentException {
			throw new IOException("unsupported");
		}

		@Override
		public void moveFile(SakerPath source, SakerPath target, CopyOption... copyoptions) throws IOException {
			throw new IOException("unsupported");
		}

		@Override
		public ListenerToken addFileEventListener(SakerPath directory, FileEventListener listener) throws IOException {
			//just ignore. no modifications are done, therefore listeners are not called
			return new ListenerToken() {
				@Override
				public void removeListener() {
					//ignore
				}
			};
		}

	}
}
