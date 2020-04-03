package saker.nest.taskdoc;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import saker.build.file.ByteArraySakerFile;
import saker.build.file.SakerDirectory;
import saker.build.file.SakerFile;
import saker.build.file.path.SakerPath;
import saker.build.file.path.WildcardPath;
import saker.build.file.provider.SakerPathFiles;
import saker.build.runtime.execution.ExecutionContext;
import saker.build.runtime.execution.SakerLog;
import saker.build.scripting.model.FormattedTextContent;
import saker.build.scripting.model.info.TypeInformationKind;
import saker.build.task.ParameterizableTask;
import saker.build.task.TaskContext;
import saker.build.task.TaskFactory;
import saker.build.task.TaskName;
import saker.build.task.utils.TaskLookupExecutionProperty;
import saker.build.task.utils.annot.SakerInput;
import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.thirdparty.saker.util.ReflectUtils;
import saker.build.thirdparty.saker.util.StringUtils;
import saker.build.thirdparty.saker.util.classloader.ClassLoaderUtil;
import saker.build.thirdparty.saker.util.io.ByteArrayRegion;
import saker.build.thirdparty.saker.util.io.UnsyncByteArrayOutputStream;
import saker.nest.NestRepository;
import saker.nest.bundle.BundleIdentifier;
import saker.nest.bundle.BundleInformation;
import saker.nest.bundle.BundleKey;
import saker.nest.bundle.NestBundleClassLoader;
import saker.nest.bundle.lookup.BundleInformationLookupResult;
import saker.nest.scriptinfo.reflection.annot.NestFieldInformation;
import saker.nest.scriptinfo.reflection.annot.NestInformation;
import saker.nest.scriptinfo.reflection.annot.NestParameterInformation;
import saker.nest.scriptinfo.reflection.annot.NestTaskInformation;
import saker.nest.scriptinfo.reflection.annot.NestTypeInformation;
import saker.nest.scriptinfo.reflection.annot.NestTypeUsage;
import saker.nest.utils.FrontendTaskFactory;

public class TaskDocumentationTaskFactory extends FrontendTaskFactory<Object> {
	private static final long serialVersionUID = 1L;

	public static final String TASK_NAME = "saker.taskdoc.gen";

	private static final String TYPES_DOC_DIR_NAME = "types";

	public static final String TEMPLATE_TASK_NAME = "TASK_NAME";
	public static final String TEMPLATE_TASK_DOCROOT = "TASK_DOCROOT";
	public static final String TEMPLATE_TASK_DOCBODY = "TASK_DOCBODY";
	public static final String TEMPLATE_TASK_PAGENAME = "TASK_PAGENAME";
	private static final NavigableSet<String> TEMPLATE_KEYWORDS = ImmutableUtils.makeImmutableNavigableSet(
			new String[] { TEMPLATE_TASK_NAME, TEMPLATE_TASK_DOCROOT, TEMPLATE_TASK_DOCBODY, TEMPLATE_TASK_PAGENAME });

	private static final NestFieldInformation[] EMPTY_NEST_FIELD_INFORMATIONS = new NestFieldInformation[0];
	private static final NestTypeUsage[] EMPTY_NEST_TYPE_USAGES = new NestTypeUsage[0];

	@Override
	public ParameterizableTask<? extends Object> createTask(ExecutionContext executioncontext) {
		return new ParameterizableTask<Object>() {

			@SakerInput(value = { "", "Task", "Tasks" })
			public Collection<TaskName> taskNameOption;
			@SakerInput(value = { "Bundle", "Bundles" })
			public Collection<Object> bundleOption;

			@SakerInput(value = "Template", required = true)
			public SakerPath templateOption;

			@SakerInput("Output")
			public SakerPath outputPathOption;
			@SakerInput("EmbedMacros")
			public Map<String, SakerPath> embedMacrosOption = Collections.emptyMap();
			@SakerInput("Macros")
			public Map<String, String> macrosOption = Collections.emptyMap();

			@Override
			public Object run(TaskContext taskcontext) throws Exception {
				Map<String, SakerPath> embedmacros = ImmutableUtils.makeImmutableLinkedHashMap(embedMacrosOption);
				List<Object> bundleinputs = ImmutableUtils.makeImmutableList(bundleOption);
				NavigableSet<TaskName> tasks = ObjectUtils.newTreeSet(taskNameOption);
				Map<String, String> macros = ImmutableUtils.makeImmutableLinkedHashMap(macrosOption);

				SakerFile templatefile = taskcontext.getTaskUtilities().resolveAtPath(templateOption);
				if (templatefile == null) {
					throw new FileNotFoundException("Template file not found: " + templateOption);
				}
				taskcontext.getTaskUtilities().reportInputFileDependency(null, templatefile);
				String templatefilecontents = templatefile.getContent();
				Set<String> resolvedmacros = new TreeSet<>();
				for (Iterator<Entry<String, SakerPath>> it = embedmacros.entrySet().iterator(); it.hasNext();) {
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
					it = embedmacros.entrySet().iterator();
				}
				for (Entry<String, String> entry : macros.entrySet()) {
					templatefilecontents = templatefilecontents.replace(entry.getKey(), entry.getValue());
				}
				TemplatedContents template = new TemplatedContents(templatefilecontents, TEMPLATE_KEYWORDS);

				SakerDirectory bd = SakerPathFiles.requireBuildDirectory(taskcontext).getDirectoryCreate(TASK_NAME);
				SakerDirectory outputdir = taskcontext.getTaskUtilities().resolveDirectoryAtRelativePathCreate(bd,
						outputPathOption);
				outputdir.clear();

				if (bundleinputs != null) {
					NestBundleClassLoader cl = (NestBundleClassLoader) this.getClass().getClassLoader();
					for (Object bundlein : bundleinputs) {
						if (bundlein == null) {
							continue;
						}
						BundleInformation bundleinfo;
						//TODO should install dependencies on the bundle info
						if (bundlein instanceof BundleKey) {
							BundleKey bk = (BundleKey) bundlein;
							bundleinfo = cl.getBundleStorageConfiguration()
									.getBundleStorageViewForKey(bk.getStorageViewKey())
									.getBundleInformation(bk.getBundleIdentifier());
						} else {
							BundleIdentifier bid;
							if (bundlein instanceof BundleIdentifier) {
								bid = (BundleIdentifier) bundlein;
							} else {
								bid = BundleIdentifier.valueOf(bundlein.toString());
							}
							BundleInformationLookupResult lookupresult = cl.getBundleStorageConfiguration()
									.getBundleLookup().lookupBundleInformation(bid);
							bundleinfo = lookupresult.getBundleInformation();
						}
						tasks.addAll(bundleinfo.getTaskClassNames().keySet());
					}
				}

				TypeReferencesTracker reftypes = new TypeReferencesTracker();
				NavigableMap<TaskName, Class<? extends TaskFactory<?>>> doctasks = new TreeMap<>();

				for (TaskName tn : tasks) {
					TaskFactory<?> taskfactory = taskcontext.getTaskUtilities()
							.getReportExecutionDependency(new TaskLookupExecutionProperty(tn, null));
					@SuppressWarnings("unchecked")
					Class<? extends TaskFactory<?>> taskclass = (Class<? extends TaskFactory<?>>) taskfactory
							.getClass();

					doctasks.put(tn, taskclass);

					SakerDirectory taskoutdir = outputdir;

					SakerPath pagepath = getTaskDocPagePath(tn);

					ByteArrayRegion pagebytes = getTaskDocPageContents(template, tn, taskclass, reftypes, pagepath);
					ByteArraySakerFile taskoutfile = new ByteArraySakerFile(pagepath.getFileName(), pagebytes);
					taskoutdir.add(taskoutfile);
					taskcontext.getTaskUtilities().reportOutputFileDependency(null, taskoutfile);
				}

				Set<? extends Class<?>> refclasses = reftypes.getTypes();
				if (!refclasses.isEmpty()) {
					Set<Class<?>> documentedtypes = new HashSet<>();
					SakerDirectory typesoutdir = outputdir.getDirectoryCreate(TYPES_DOC_DIR_NAME);
					while (true) {
						if (documentedtypes.size() == refclasses.size()) {
							break;
						}
						for (Class<?> type : refclasses) {
							if (!documentedtypes.add(type)) {
								//already documented
								continue;
							}
							SakerPath pagepath = reftypes.getRootRelativePath(type);
							ByteArrayRegion pagebytes = getTypeDocPageContents(template, type, reftypes, pagepath);
							ByteArraySakerFile typeoutfile = new ByteArraySakerFile(pagepath.getFileName(), pagebytes);
							typesoutdir.add(typeoutfile);
							taskcontext.getTaskUtilities().reportOutputFileDependency(null, typeoutfile);
						}
						refclasses = reftypes.getTypes();
					}
				}

				SakerPath indexpagepath = SakerPath.valueOf("index.html");
				ByteArrayRegion indexcontents = getIndexDocPageContents(template, indexpagepath, reftypes, doctasks,
						refclasses);
				ByteArraySakerFile indexfile = new ByteArraySakerFile(indexpagepath.getFileName(), indexcontents);
				outputdir.add(indexfile);
				taskcontext.getTaskUtilities().reportOutputFileDependency(null, indexfile);

				//TODO generate index file
				outputdir.synchronize();

				SimpleTaskDocumentationTaskOutput result = new SimpleTaskDocumentationTaskOutput();
				result.setOutputPath(outputdir.getSakerPath());
				return result;
			}

		};
	}

	private static ByteArrayRegion getTypeDocPageContents(TemplatedContents template, Class<?> typeclass,
			TypeReferencesTracker reftypes, SakerPath pagepath) throws IOException {
		Map<String, String> templatekeywords = new TreeMap<>();
		templatekeywords.put(TEMPLATE_TASK_NAME, getReferencedTypeSimpleName(typeclass));
		templatekeywords.put(TEMPLATE_TASK_DOCROOT, getDocRootTemplateString(pagepath));

		StringBuilder body = new StringBuilder();
		generateTypeBody(pagepath, typeclass, body, reftypes);
		templatekeywords.put(TEMPLATE_TASK_DOCBODY, body.toString());
		templatekeywords.put(TEMPLATE_TASK_PAGENAME, getReferencedTypeSimpleName(typeclass));

		UnsyncByteArrayOutputStream baos = new UnsyncByteArrayOutputStream();
		try (OutputStreamWriter out = new OutputStreamWriter(baos, StandardCharsets.UTF_8)) {
			template.writeTo(out, templatekeywords);
		}
		return baos.toByteArrayRegion();
	}

	private static String getDocRootTemplateString(SakerPath pagepath) {
		String str = getRelativePathToPage(pagepath, SakerPath.EMPTY).toString();
		if (str.isEmpty()) {
			return "";
		}
		return str + "/";
	}

	private static SakerPath getTaskDocPagePath(TaskName tn) {
		return SakerPath.valueOf(tn.toString() + ".html");
	}

	private static ByteArrayRegion getIndexDocPageContents(TemplatedContents template, SakerPath pagepath,
			TypeReferencesTracker reftypes, NavigableMap<TaskName, Class<? extends TaskFactory<?>>> doctasks,
			Set<? extends Class<?>> refclasses) throws IOException {
		StringBuilder body = new StringBuilder();
		body.append("<div class=\"taskdoc-index\">");
		if (!doctasks.isEmpty()) {
			body.append("<div class=\"taskdoc-idx-section\">");
			body.append("<table>");
			body.append("<caption>Build tasks</caption>");

			body.append("<tbody>");
			for (Entry<TaskName, Class<? extends TaskFactory<?>>> entry : doctasks.entrySet()) {
				TaskName tn = entry.getKey();
				Class<? extends TaskFactory<?>> taskclass = entry.getValue();

				body.append("<tr class=\"taskdoc-task-idx\">");
				String escapedname = escapeHtml(tn.toString() + "()");

				body.append("<td class=\"taskdoc-task-idx-meta\"><a class=\"taskdoc-task-idx-name\" href=\""
						+ getRelativePathToPage(pagepath, getTaskDocPagePath(tn)) + "\" title=\"Build task "
						+ escapedname + "\">");
				body.append(escapedname);
				body.append("</a></td>");

				body.append("<td class=\"taskdoc-task-idx-summary\">");
				int l = body.length();
				writeSignatureInformation(taskclass.getAnnotationsByType(NestInformation.class), body);
				if (body.length() == l) {
					SakerLog.warning().println("No index information for task: " + taskclass);
				}
				body.append("</td>");

				body.append("</tr>");
			}
			body.append("</tbody>");

			body.append("</table>");
			body.append("</div>");
		}
		if (!refclasses.isEmpty()) {
			List<? extends Class<?>> sortedrefclasses = ObjectUtils.newArrayList(refclasses);
			sortedrefclasses.sort((l, r) -> getReferencedTypeSimpleName(l).compareTo(getReferencedTypeSimpleName(r)));

			body.append("<div class=\"taskdoc-idx-section\">");
			body.append("<table>");
			body.append("<caption>Types</caption>");

			body.append("<tbody>");
			for (Class<?> refc : sortedrefclasses) {
				String sname = getReferencedTypeSimpleName(refc);

				body.append("<tr class=\"taskdoc-type-idx\">");
				String escapedname = escapeHtml(sname);

				body.append("<td class=\"taskdoc-type-idx-meta\"><a class=\"taskdoc-type-idx-name\" href=\""
						+ getRelativePathToPage(pagepath, reftypes.getRootRelativePath(refc)) + "\" title=\"Type "
						+ escapedname + "\">");
				body.append(escapedname);
				body.append("</a></td>");

				body.append("<td class=\"taskdoc-type-idx-summary\">");
				int l = body.length();
				writeSignatureInformation(refc.getAnnotationsByType(NestInformation.class), body);
				if (body.length() == l) {
					SakerLog.warning().println("No index information for type: " + refc);
				}
				body.append("</td>");

				body.append("</tr>");
			}
			body.append("</tbody>");

			body.append("</table>");
			body.append("</div>");
		}
		body.append("</div>");

		Map<String, String> templatekeywords = new TreeMap<>();
		templatekeywords.put(TEMPLATE_TASK_NAME, "Doc Index");
		templatekeywords.put(TEMPLATE_TASK_DOCROOT, getDocRootTemplateString(pagepath));

		templatekeywords.put(TEMPLATE_TASK_DOCBODY, body.toString());
		templatekeywords.put(TEMPLATE_TASK_PAGENAME, "index");
		UnsyncByteArrayOutputStream baos = new UnsyncByteArrayOutputStream();
		try (OutputStreamWriter out = new OutputStreamWriter(baos, StandardCharsets.UTF_8)) {
			template.writeTo(out, templatekeywords);
		}
		return baos.toByteArrayRegion();
	}

	private static ByteArrayRegion getTaskDocPageContents(TemplatedContents template, TaskName tn,
			Class<? extends TaskFactory<?>> taskclass, TypeReferencesTracker reftypes, SakerPath pagepath)
			throws IOException {
		Map<String, String> templatekeywords = new TreeMap<>();
		templatekeywords.put(TEMPLATE_TASK_NAME, tn.toString() + "()");
		templatekeywords.put(TEMPLATE_TASK_DOCROOT, getDocRootTemplateString(pagepath));
		StringBuilder body = new StringBuilder();
		generateTaskBody(pagepath, taskclass, tn, body, reftypes);

		templatekeywords.put(TEMPLATE_TASK_DOCBODY, body.toString());
		templatekeywords.put(TEMPLATE_TASK_PAGENAME, tn.toString());
		UnsyncByteArrayOutputStream baos = new UnsyncByteArrayOutputStream();
		try (OutputStreamWriter out = new OutputStreamWriter(baos, StandardCharsets.UTF_8)) {
			template.writeTo(out, templatekeywords);
		}
		return baos.toByteArrayRegion();
	}

	protected static SakerPath getRelativePathToPage(SakerPath pagepath, SakerPath targetpath) {
		return pagepath.getParent().relativize(targetpath);
	}

	protected static void generateTypeBody(SakerPath pagepath, Class<?> type, StringBuilder out,
			TypeReferencesTracker doctypes) {
		NestTypeInformation tinfo = type.getAnnotation(NestTypeInformation.class);
		NestTypeUsage[] relatedtypes = tinfo == null ? EMPTY_NEST_TYPE_USAGES : tinfo.relatedTypes();
		NestFieldInformation[] enums = tinfo == null ? EMPTY_NEST_FIELD_INFORMATIONS : tinfo.enumValues();
		String escapedtypename = escapeHtml(getReferencedTypeSimpleName(type));

		out.append("<div class=\"typedoc-header\">");
		out.append("<div class=\"typedoc-typename\"><a href=\"\" title=\"Type " + escapedtypename + "\">");
		out.append(escapedtypename);
		out.append("</a></div>");

		BundleIdentifier bundleid = getBundleIdentifierOfType(type);
		if (bundleid != null) {
			String escapedbundleid = escapeHtml(bundleid.toString());

			out.append("<div class=\"typedoc-container-bundle\">");
			//TODO href to repository package site
			out.append("In bundle: <a title=\"Package " + escapedbundleid + "\">");
			out.append(escapedbundleid);
			out.append("</a>");
			out.append("</div>");
		}

		if (relatedtypes.length > 0) {
			out.append("<div class=\"taskdoc-relatedtypes\">");
			out.append("<div class=\"taskdoc-relatedtypes-title\">Related types: </div>");
			for (NestTypeUsage reltype : relatedtypes) {
				out.append("<div class=\"taskdoc-relatedtype\">");
				writeTypeReferenceInformation(pagepath, out, reltype, doctypes);
				out.append("</div>");
			}
			out.append("</div>");
		}

		out.append("</div>");

		writeInformationDiv(type.getAnnotationsByType(NestInformation.class), out, "typedoc-info", false, false);

		List<TypeEnumDetails> enuminfos = new ArrayList<>();
		{
			Object[] enconstants = type.getEnumConstants();
			boolean hasenums = !ObjectUtils.isNullOrEmpty(enums) || !ObjectUtils.isNullOrEmpty(enconstants);
			if (hasenums) {
				Set<String> hadenums = new TreeSet<>();
				for (NestFieldInformation enf : enums) {
					String enname = enf.value();
					if (enname.isEmpty()) {
						continue;
					}
					if (!hadenums.add(enname)) {
						continue;
					}
					enuminfos.add(new TypeEnumDetails(enname, enf.info(), enf.deprecated()));
				}
				if (enconstants != null) {
					for (Object enobj : enconstants) {
						Enum<?> en = (Enum<?>) enobj;
						String enumname = en.name();
						if (!hadenums.add(enumname)) {
							//already documented the enum
							continue;
						}
						Field enfield = ReflectUtils.getDeclaredFieldAssert(type, enumname);
						enuminfos.add(new TypeEnumDetails(enumname, enfield.getAnnotationsByType(NestInformation.class),
								enfield.isAnnotationPresent(Deprecated.class)));
					}
				}
				enuminfos.sort((l, r) -> {
					int cmp = StringUtils.compareStringsNullFirst(l.enname, r.enname);
					if (cmp != 0) {
						return cmp;
					}
					return 0;
				});
			}
		}
		List<NestFieldInformation> sortedfieldinfos = new ArrayList<>(
				ImmutableUtils.makeImmutableList(type.getAnnotationsByType(NestFieldInformation.class)));
		sortedfieldinfos.sort((l, r) -> {
			int cmp = StringUtils.compareStringsNullFirst(l.value(), r.value());
			if (cmp != 0) {
				return cmp;
			}
			return 0;
		});

		boolean hasdetails = !enuminfos.isEmpty() || !sortedfieldinfos.isEmpty();

		if (hasdetails) {
			out.append("<div class=\"typedoc-idx\">");
			out.append("<div class=\"typedoc-idx-title\">Summary</div>");
			if (!enuminfos.isEmpty()) {
				out.append("<div class=\"typedoc-idx-section typedoc-idx-enums\">");
				out.append("<table>");
				out.append("<caption>Enumeration values</caption>");

				out.append("<tbody>");

				for (TypeEnumDetails endet : enuminfos) {
					writeEnumIndexRow(endet.enname, endet.infos, endet.deprecated, out);
				}
				out.append("</tbody>");

				out.append("</table>");
				out.append("</div>");
			}

			if (!ObjectUtils.isNullOrEmpty(sortedfieldinfos)) {
				out.append("<div class=\"typedoc-idx-section typedoc-idx-fields\">");
				out.append("<table>");
				out.append("<caption>Fields</caption>");

				out.append("<tbody>");
				for (NestFieldInformation f : sortedfieldinfos) {
					writeFieldIndexRow(f, out);
				}
				out.append("</tbody>");

				out.append("</table>");
				out.append("</div>");
			}

			out.append("</div>");

			if (!enuminfos.isEmpty()) {
				out.append("<div class=\"typedoc-details-enums\">");
				out.append("<div class=\"typedoc-details-title\">Enumeration values</div>");
				for (TypeEnumDetails endet : enuminfos) {
					writeEnumDetailsDiv(endet.enname, endet.infos, endet.deprecated, out);
				}
				out.append("</div>");
			}

			if (!ObjectUtils.isNullOrEmpty(sortedfieldinfos)) {
				out.append("<div class=\"typedoc-details-fields\">");
				out.append("<div class=\"typedoc-details-title\">Fields</div>");
				for (NestFieldInformation f : sortedfieldinfos) {
					writeFieldDetailsDiv(f, doctypes, pagepath, out);
				}
				out.append("</div>");
			}
		}
	}

	private static BundleIdentifier getBundleIdentifierOfType(Class<?> type) {
		ClassLoader cl = type.getClassLoader();
		if (!(cl instanceof NestBundleClassLoader)) {
			return null;
		}
		return ((NestBundleClassLoader) cl).getBundle().getBundleIdentifier().withoutMetaQualifiers();
	}

	private static class TypeEnumDetails {
		String enname;
		NestInformation[] infos;
		boolean deprecated;

		public TypeEnumDetails(String enname, NestInformation[] infos, boolean deprecated) {
			this.enname = enname;
			this.infos = infos;
			this.deprecated = deprecated;
		}
	}

	private static void writeEnumIndexRow(String enname, NestInformation[] infos, boolean deprecated,
			StringBuilder out) {
		out.append("<tr class=\"taskdoc-idx-member\"");
		if (deprecated) {
			out.append(" taskdoc-deprecated");
		}
		out.append(">");
		String escapedname = escapeHtml(enname);

		out.append("<td><a class=\"typedoc-idx-enum-name\" href=\"#en-" + escapedname + "\" title=\"Enum " + escapedname
				+ "\">");
		out.append(escapedname);
		out.append("</a></td>");

		out.append("<td>");
		writeSignatureInformation(infos, out);
		out.append("</td>");

		out.append("</tr>");
	}

	private static void writeEnumDetailsDiv(String enname, NestInformation[] infos, boolean deprecated,
			StringBuilder out) {
		String escapedname = escapeHtml(enname);
		out.append("<div id=\"en-" + escapedname + "\" class=\"typedoc-detail-member typedoc-detail-enum\"");
		if (deprecated) {
			out.append(" taskdoc-deprecated");
		}
		out.append(">");

		out.append("<div class=\"typedoc-detail-enum-name\"><a href=\"#en-" + escapedname + "\" title=\"Enum "
				+ escapedname + "\">");
		out.append(escapedname);
		out.append("</a></div>");

		writeInformationDiv(infos, out, "typedoc-detail-enum-detail", deprecated, false);

		out.append("</div>");
	}

	private static void writeFieldIndexRow(NestFieldInformation finfo, StringBuilder out) {
		String name = finfo.value();
		if (name.isEmpty()) {
			//don't include
			return;
		}
		boolean deprecated = finfo.deprecated();
		NestInformation[] infos = finfo.info();
		out.append("<tr class=\"taskdoc-idx-member\"");
		if (deprecated) {
			out.append(" taskdoc-deprecated");
		}
		out.append(">");
		String escapedname = escapeHtml(name);

		out.append("<td><a class=\"typedoc-idx-field-name\" href=\"#f-" + escapedname + "\" title=\"Field "
				+ escapedname + "\">");
		out.append(escapedname);
		out.append("</a></td>");

		out.append("<td>");
		writeSignatureInformation(infos, out);
		out.append("</td>");

		out.append("</tr>");
	}

	private static void writeFieldDetailsDiv(NestFieldInformation finfo, TypeReferencesTracker doctypes,
			SakerPath pagepath, StringBuilder out) {
		String name = finfo.value();
		if (name.isEmpty()) {
			//don't include
			return;
		}
		String escapedname = escapeHtml(name);
		boolean deprecated = finfo.deprecated();
		NestInformation[] infos = finfo.info();
		out.append("<div id=\"f-" + escapedname + "\" class=\"typedoc-detail-member typedoc-detail-field\"");
		if (deprecated) {
			out.append(" taskdoc-deprecated");
		}
		out.append(">");

		out.append("<div class=\"typedoc-detail-param-header-sig\">");
		out.append("<div class=\"typedoc-detail-field-name\"><a href=\"#f-" + escapedname + "\" title=\"Field "
				+ escapedname + "\">");
		out.append(escapedname);
		out.append("</a></div>");

		NestTypeUsage ftype = finfo.type();
		out.append("<div class=\"typedoc-detail-field-type\">");
		writeTypeReferenceInformation(pagepath, out, ftype, doctypes);
		out.append("</div>");

		out.append("</div>");

		writeInformationDiv(infos, out, "typedoc-detail-field-detail", deprecated, false);

		out.append("</div>");
	}

	protected static void generateTaskBody(SakerPath pagepath, Class<? extends TaskFactory<?>> taskclass, TaskName tn,
			StringBuilder out, TypeReferencesTracker doctypes) {
		BundleIdentifier bundleid = getBundleIdentifierOfType(taskclass);
		NestTaskInformation taskinfo = taskclass.getAnnotation(NestTaskInformation.class);
		NestParameterInformation[] paraminfos = taskclass.getAnnotationsByType(NestParameterInformation.class);
		NestInformation[] infos = taskclass.getAnnotationsByType(NestInformation.class);
		NestTypeUsage rettype = taskinfo == null ? null : taskinfo.returnType();

		List<NestParameterInformation> sortedparaminfos = new ArrayList<>(ImmutableUtils.makeImmutableList(paraminfos));
		sortedparaminfos.sort((l, r) -> {
			int cmp = StringUtils.compareStringsNullFirst(getParameterInfoUseName(l), getParameterInfoUseName(r));
			if (cmp != 0) {
				return cmp;
			}
			return 0;
		});

		out.append("<div class=\"taskdoc-header\">");

		String tasknameparens = escapeHtml(tn.toString() + "()");

		out.append("<div class=\"taskdoc-header-sig\">");
		out.append("<div class=\"taskdoc-taskname\"><a href=\"\" title=\"Build task " + tasknameparens + "\">");
		out.append(tasknameparens);
		out.append("</a></div>");
		if (rettype != null) {
			out.append("<div class=\"taskdoc-return-type\">");
			writeTypeReferenceInformation(pagepath, out, rettype, doctypes);
			out.append("</div>");
		}
		out.append("</div>");

		if (bundleid != null) {
			String escapedbundleid = escapeHtml(bundleid.toString());
			out.append("<div class=\"taskdoc-container-bundle\">");
			//TODO href to repository package site
			out.append("In bundle: <a title=\"Bundle " + escapedbundleid + "\">");
			out.append(escapedbundleid);
			out.append("</a>");
			out.append("</div>");
		}
		out.append("</div>");

		writeInformationDiv(infos, out, "taskdoc-info", false, false);

		if (!ObjectUtils.isNullOrEmpty(sortedparaminfos)) {
			out.append("<div class=\"taskdoc-idx\">");
			out.append("<div class=\"taskdoc-idx-title\">Summary</div>");
			out.append("<div class=\"taskdoc-idx-section taskdoc-idx-parameters\">");
			out.append("<table>");
			out.append("<caption>Parameters</caption>");

			out.append("<tbody>");
			for (NestParameterInformation pinfo : sortedparaminfos) {
				out.append("<tr class=\"taskdoc-idx-member\"");
				if (pinfo.required()) {
					out.append(" taskdoc-required");
				}
				if (pinfo.deprecated()) {
					out.append(" taskdoc-deprecated");
				}
				out.append(">");
				String escapedparamname = escapeHtml(getParameterInfoUseName(pinfo));

				out.append("<td class=\"taskdoc-idx-param-meta\"><a class=\"taskdoc-idx-paramname\" href=\"#" + escapedparamname
						+ "\" title=\"Task parameter " + escapedparamname + "\">");
				out.append(escapedparamname);
				out.append("</a></td>");

				out.append("<td class=\"taskdoc-idx-param-summary\">");
				if (pinfo.deprecated()) {
					out.append("<div class=\"taskdoc-deprecated-parameter\">Deprecated. </div>");
				}
				if (pinfo.required()) {
					out.append("<div class=\"taskdoc-required-parameter\">Required parameter. </div>");
				}
				writeSignatureInformation(pinfo.info(), out);
				out.append("</td>");

				out.append("</tr>");
			}
			out.append("</tbody>");

			out.append("</table>");
			out.append("</div>");
			out.append("</div>");

			out.append("<div class=\"taskdoc-details-parameters\">");
			out.append("<div class=\"taskdoc-details-title\">Parameters</div>");
			for (NestParameterInformation pinfo : sortedparaminfos) {
				String escapedparamname = escapeHtml(getParameterInfoUseName(pinfo));
				out.append("<div id=\"");
				out.append(escapedparamname);
				out.append("\" class=\"taskdoc-detail-param\"");
				if (pinfo.required()) {
					out.append(" taskdoc-required");
				}
				if (pinfo.deprecated()) {
					out.append(" taskdoc-deprecated");
				}
				out.append(">");

				out.append("<div class=\"taskdoc-detail-param-header\">");

				out.append("<div class=\"taskdoc-detail-param-header-sig\">");
				out.append("<div class=\"taskdoc-detail-param-header-name\">");
				out.append("<a href=\"#" + escapedparamname + "\" title=\"Task parameter " + escapedparamname + "\">");
				out.append(escapedparamname);
				out.append("</a>");

				out.append("</div>");

				out.append("<div class=\"taskdoc-detail-param-header-type\">");
				writeTypeReferenceInformation(pagepath, out, pinfo.type(), doctypes);
				out.append("</div>");
				out.append("</div>");

				Iterator<String> aliasit = Arrays.stream(pinfo.aliases()).map(TaskDocumentationTaskFactory::escapeHtml)
						.filter(s -> !escapedparamname.equals(s)).iterator();
				if (aliasit.hasNext()) {
					out.append("<div class=\"taskdoc-detail-param-header-aliases\">Aliases: ");
					do {
						String escapedaliasname = aliasit.next();
						out.append("<div class=\"taskdoc-detail-param-header-alias\">");
						if (escapedaliasname.isEmpty()) {
							//TODO link to unnamed param doc
							out.append("<a title=\"Unnamed parameter\">&lt;unnamed&gt;</a>");
						} else {
							out.append(escapedaliasname);
						}
						out.append("</div>");
					} while (aliasit.hasNext());
					out.append("</div>");
				}

				out.append("</div>");

				writeInformationDiv(pinfo.info(), out, "taskdoc-detail-member taskdoc-detail-param-detail",
						pinfo.deprecated(), pinfo.required());

				out.append("</div>");
			}
			out.append("</div>");
		}

		System.out.println("TaskDocumentationTaskFactory " + tn + " -> " + taskinfo);
	}

	private static final Class<?>[] ELEMENT_TYPES_UNSPECIFIED;
	static {
		ELEMENT_TYPES_UNSPECIFIED = (Class<?>[]) ReflectUtils.getMethodAssert(NestTypeUsage.class, "elementTypes")
				.getDefaultValue();
	}

	private static void writeTypeReferenceInformation(SakerPath pagepath, StringBuilder out, NestTypeUsage typeusage,
			TypeReferencesTracker doctypes) {
		Class<?> val = typeusage.value();
		Class<?>[] elemtypes = typeusage.elementTypes();
		String kind = typeusage.kind();
		if ("".equals(kind)) {
			NestTypeInformation valtinfo = val.getAnnotation(NestTypeInformation.class);
			if (valtinfo != null) {
				kind = valtinfo.kind();
			}
		}
		if (Arrays.equals(elemtypes, ELEMENT_TYPES_UNSPECIFIED)) {
			NestTypeInformation valtinfo = val.getAnnotation(NestTypeInformation.class);
			if (valtinfo != null) {
				valtinfo.elementTypes();
				//TODO handle elem types
			}
		}
		if (val == Object.class && Arrays.equals(elemtypes, ELEMENT_TYPES_UNSPECIFIED) && kind.isEmpty()) {
			//no type specified
			return;
		}
		if (TypeInformationKind.COLLECTION.equalsIgnoreCase(kind)
				|| (kind.isEmpty() && Collection.class.isAssignableFrom(val))) {
			if (elemtypes.length != 1 || Arrays.equals(elemtypes, ELEMENT_TYPES_UNSPECIFIED)) {
				out.append("Collection");
				return;
			}
			Class<?> eltype = elemtypes[0];
			out.append("Collection of ");
			addDocTypeAppendAStartTag(pagepath, out, doctypes, eltype);
			out.append(getReferencedTypeSimpleName(eltype));
			out.append("</a>");
			return;
		}
		if (TypeInformationKind.MAP.equalsIgnoreCase(kind) || (kind.isEmpty() && Map.class.isAssignableFrom(val))) {
			if (elemtypes.length != 2 || Arrays.equals(elemtypes, ELEMENT_TYPES_UNSPECIFIED)) {
				out.append("Map");
				return;
			}
			out.append("Map of ");
			addDocTypeAppendAStartTag(pagepath, out, doctypes, elemtypes[0]);
			out.append(getReferencedTypeSimpleName(elemtypes[0]));
			out.append("</a>");
			out.append(" : ");
			addDocTypeAppendAStartTag(pagepath, out, doctypes, elemtypes[1]);
			out.append(getReferencedTypeSimpleName(elemtypes[1]));
			out.append("</a>");
			return;
		}
		if (TypeInformationKind.ENVIRONMENT_USER_PARAMETER.equalsIgnoreCase(kind)) {
			//TODO doc href
			out.append("<a>Environment user parameter</a>");
			return;
		}
		if (TypeInformationKind.EXECUTION_USER_PARAMETER.equalsIgnoreCase(kind)) {
			//TODO doc href
			out.append("<a>Execution user parameter</a>");
			return;
		}
		if (TypeInformationKind.WILDCARD_PATH.equalsIgnoreCase(kind)) {
			appendSakerBuildDocAStartTag(pagepath, out, WildcardPath.class);
			out.append("Wildcard path</a>");
			return;
		}
		if (TypeInformationKind.BUILD_SCRIPT_PATH.equalsIgnoreCase(kind)) {
			//TODO doc href
			out.append("<a>Build target</a>");
			return;
		}
		if (TypeInformationKind.BOOLEAN.equalsIgnoreCase(kind) || boolean.class == val || Boolean.class == val) {
			appendOracleDocAStartTag(out, Boolean.class);
			out.append("Boolean</a>");
			return;
		}
		if (TypeInformationKind.VOID.equalsIgnoreCase(kind) || void.class == val || Void.class == val) {
			out.append("Void");
			return;
		}
		if (TypeInformationKind.NUMBER.equalsIgnoreCase(kind) || Number.class == val) {
			appendOracleDocAStartTag(out, Number.class);
			out.append("Number</a>");
			return;
		}
		if (TypeInformationKind.FILE_PATH.equalsIgnoreCase(kind)) {
			appendSakerBuildDocAStartTag(pagepath, out, SakerPath.class);
			out.append("File path</a>");
			return;
		}
		if (TypeInformationKind.DIRECTORY_PATH.equalsIgnoreCase(kind)) {
			appendSakerBuildDocAStartTag(pagepath, out, SakerPath.class);
			out.append("Directory path</a>");
			return;
		}
		if (TypeInformationKind.PATH.equalsIgnoreCase(kind)) {
			appendSakerBuildDocAStartTag(pagepath, out, SakerPath.class);
			out.append("Path</a>");
			return;
		}
		if (val == Object.class) {
			//cant really determine what the type is. just ignore
			return;
		}
		if (val.isPrimitive()) {
			Class<?> unprimitivized = ReflectUtils.unprimitivize(val);
			appendOracleDocAStartTag(out, unprimitivized);
			out.append(unprimitivized.getSimpleName());
			out.append("</a>");
			return;
		}

		addDocTypeAppendAStartTag(pagepath, out, doctypes, val);
		out.append(getReferencedTypeSimpleName(val));
		out.append("</a>");
	}

	private static void appendOracleDocAStartTag(StringBuilder out, Class<?> type) {
		out.append("<a href=\"");
		out.append(getOracleJavadocLink(type));
		out.append("\" title=\"");
		out.append(type.getSimpleName());
		out.append("\">");
	}

	private static void appendSakerBuildDocAStartTag(SakerPath pagepath, StringBuilder out, Class<?> type) {
		out.append("<a href=\"");
		out.append(getSakerBuildJavadocLink(pagepath, type));
		out.append("\" title=\"");
		out.append(type.getSimpleName());
		out.append("\">");
	}

	private static void appendNestDocAStartTag(SakerPath pagepath, StringBuilder out, Class<?> type) {
		out.append("<a href=\"");
		out.append(getNestJavadocLink(pagepath, type));
		out.append("\" title=\"");
		out.append(type.getSimpleName());
		out.append("\">");
	}

	private static void addDocTypeAppendAStartTag(SakerPath pagepath, StringBuilder out, TypeReferencesTracker doctypes,
			Class<?> eltype) {
		eltype = ReflectUtils.unprimitivize(eltype);
		try {
			Class<?> fc = Class.forName(eltype.getName(), false, ClassLoaderUtil.getPlatformClassLoaderParent());
			if (fc == eltype) {
				appendOracleDocAStartTag(out, eltype);
				return;
			}
		} catch (ClassNotFoundException e) {
		}
		try {
			Class<?> fc = Class.forName(eltype.getName(), false, SakerPath.class.getClassLoader());
			if (fc == eltype) {
				appendSakerBuildDocAStartTag(pagepath, out, eltype);
				return;
			}
		} catch (ClassNotFoundException e) {
		}
		try {
			Class<?> fc = Class.forName(eltype.getName(), false, NestRepository.class.getClassLoader());
			if (fc == eltype) {
				appendNestDocAStartTag(pagepath, out, eltype);
				return;
			}
		} catch (ClassNotFoundException e) {
		}
		doctypes.add(eltype);
		out.append("<a href=\"" + getRelativePathToPage(pagepath, doctypes.getRootRelativePath(eltype))
				+ "\" title=\"Type " + getReferencedTypeSimpleName(eltype) + "\">");
	}

	private static String getSakerBuildJavadocLink(SakerPath pagepath, Class<?> eltype) {
		return getRelativePathToPage(pagepath,
				SakerPath.valueOf("../../saker.build/javadoc/" + eltype.getName().replace('.', '/') + ".html"))
						.toString();
	}

	private static String getNestJavadocLink(SakerPath pagepath, Class<?> eltype) {
		return getRelativePathToPage(pagepath,
				SakerPath.valueOf("../../saker.nest/javadoc/" + eltype.getName().replace('.', '/') + ".html"))
						.toString();
	}

	private static String getOracleJavadocLink(Class<?> eltype) {
		String oraclejavadoclink = "https://docs.oracle.com/javase/8/docs/api/"
				+ eltype.getCanonicalName().replace('.', '/') + ".html";
		return oraclejavadoclink;
	}

	private static String getReferencedTypeSimpleName(Class<?> c) {
		NestTypeInformation tinfo = c.getAnnotation(NestTypeInformation.class);
		if (tinfo == null) {
			return c.getSimpleName();
		}
		String qname = tinfo.qualifiedName();
		if (qname.isEmpty()) {
			return c.getSimpleName();
		}
		return qname.substring(qname.lastIndexOf('.') + 1);
	}

	public static String escapeHtml(String s) {
		return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'",
				"&#39;");
	}

	private static String getParameterInfoUseName(NestParameterInformation info) {
		String val = info.value();
		if (!val.isEmpty()) {
			return val;
		}
		for (String al : info.aliases()) {
			if (!al.isEmpty()) {
				return al;
			}
		}
		return val;
	}

	private static void writeSignatureInformation(NestInformation[] infos, StringBuilder out) {
		NestInformation winfo = getWriteInformation(infos);
		String firstsentence = getFirstSentence(winfo);
		if (firstsentence != null) {
			out.append(escapeHtml(firstsentence));
		}
	}

	private static String getFirstSentence(NestInformation info) {
		if (info == null) {
			return null;
		}
		return getFirstSentence(info.value());
	}

	private static String getFirstSentence(String str) {
		if (ObjectUtils.isNullOrEmpty(str)) {
			return null;
		}
		BreakIterator breakit = BreakIterator.getSentenceInstance(Locale.ENGLISH);
		breakit.setText(str);
		int n = breakit.next();
		if (n == BreakIterator.DONE) {
			return null;
		}
		return str.substring(0, n);
	}

	private static NestInformation getWriteInformation(NestInformation[] infos) {
		if (ObjectUtils.isNullOrEmpty(infos)) {
			return null;
		}
		NestInformation html = getInformationWithFormat(infos, FormattedTextContent.FORMAT_HTML);
		if (html != null) {
			return html;
		}
		NestInformation markdown = getInformationWithFormat(infos, FormattedTextContent.FORMAT_MARKDOWN);
		if (markdown != null) {
			return markdown;
		}
		NestInformation plaintext = getInformationWithFormat(infos, FormattedTextContent.FORMAT_PLAINTEXT);
		if (plaintext == null) {
			//choose an arbitrary
			return infos[0];
		}
		return plaintext;
	}

	private static void writeInformationDiv(NestInformation[] infos, StringBuilder out, String divcname,
			boolean deprecated, boolean requiredparameter) {
		NestInformation winfo = getWriteInformation(infos);
		out.append("<div class=\"" + divcname + "\"");
		if (winfo != null) {
			out.append(" taskdoc-info-format=\"");
			out.append(escapeHtml(winfo.format()));
			out.append('\"');
		}
		out.append('>');
		if (deprecated) {
			out.append("<div class=\"taskdoc-deprecated-parameter\">Deprecated. </div>");
		}
		if (requiredparameter) {
			out.append("<div class=\"taskdoc-required-parameter\">Required parameter.</div>");
		}
		if (winfo != null) {
			if (FormattedTextContent.FORMAT_MARKDOWN.equals(winfo.format())) {
				Parser parser = Parser.builder().build();
				Node document = parser.parse(winfo.value());
				HtmlRenderer renderer = HtmlRenderer.builder().escapeHtml(true).build();
				renderer.render(document);
			} else {
				out.append(escapeHtml(winfo.value()));
			}
		}
		out.append("</div>");
	}

	private static NestInformation getInformationWithFormat(NestInformation[] infos, String format) {
		for (NestInformation i : infos) {
			if (i.format().equals(format)) {
				return i;
			}
		}
		return null;
	}

	private static class TypeReferencesTracker {
		private Map<Class<?>, String> typeTypeIds = new HashMap<>();
		private Map<String, Class<?>> typeIdTypes = new TreeMap<>();

		public Set<? extends Class<?>> getTypes() {
			return ImmutableUtils.makeImmutableHashSet(typeTypeIds.keySet());
		}

		public String getTypeId(Class<?> c) {
			return typeTypeIds.get(c);
		}

		public void add(Class<?> c) {
			String sname = getReferencedTypeSimpleName(c);
			Class<?> prev = typeIdTypes.putIfAbsent(sname, c);
			if (prev == null) {
				typeTypeIds.put(c, sname);
				return;
			}
			if (prev == c) {
				//already present
				return;
			}
			int i = 2;
			while (true) {
				String nid = sname + "_" + i;
				prev = typeIdTypes.putIfAbsent(nid, c);
				if (prev == null) {
					typeTypeIds.put(c, nid);
					return;
				}
				if (prev == c) {
					return;
				}
				//continue
				++i;
			}
		}

		public SakerPath getRootRelativePath(Class<?> c) {
			return SakerPath.valueOf(TYPES_DOC_DIR_NAME + "/" + getTypeDocHtmlFileName(c));
		}

		public String getTypeDocHtmlFileName(Class<?> c) {
			return escapeHtml(getTypeId(c)) + ".html";
		}
	}
}
