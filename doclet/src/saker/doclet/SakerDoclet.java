package saker.doclet;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;

import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.Parameterizable;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.IntersectionType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import com.sun.source.doctree.DeprecatedTree;
import com.sun.source.doctree.DocCommentTree;
import com.sun.source.doctree.DocTree;
import com.sun.source.doctree.DocTree.Kind;
import com.sun.source.doctree.ParamTree;
import com.sun.source.doctree.ReturnTree;
import com.sun.source.doctree.SeeTree;
import com.sun.source.doctree.SinceTree;
import com.sun.source.doctree.ThrowsTree;
import com.sun.source.util.DocTreePath;
import com.sun.source.util.DocTrees;
import com.sun.source.util.TreePath;

import jdk.javadoc.doclet.Doclet;
import jdk.javadoc.doclet.DocletEnvironment;
import jdk.javadoc.doclet.Reporter;
import saker.build.file.path.SakerPath;
import saker.build.file.path.WildcardPath;
import saker.build.thirdparty.org.objectweb.asm.ClassReader;
import saker.build.thirdparty.org.objectweb.asm.ClassVisitor;
import saker.build.thirdparty.org.objectweb.asm.FieldVisitor;
import saker.build.thirdparty.org.objectweb.asm.MethodVisitor;
import saker.build.thirdparty.org.objectweb.asm.Opcodes;
import saker.build.thirdparty.saker.util.ConcatIterable;
import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.thirdparty.saker.util.function.Functionals;

/**
 * The doclet.
 */
public class SakerDoclet implements Doclet {
	private static final String PACKAGE_SUMMARY_HTML_NAME = "index";
	private static final String TEMPLATE_KEYWORD_JAVADOC_BODY = "JAVADOC_BODY";
	private static final String TEMPLATE_KEYWORD_JAVADOC_TITLE = "JAVADOC_TITLE";
	private static final String TEMPLATE_KEYWORD_JAVADOC_HEAD = "JAVADOC_HEAD";
	private static final String TEMPLATE_KEYWORD_JAVADOC_ROOT = "JAVADOC_ROOT";
	private static final String TEMPLATE_KEYWORD_JAVADOC_PAGENAME = "JAVADOC_PAGENAME";
	private static final String[] TEMPLATE_KEYWORDS = { TEMPLATE_KEYWORD_JAVADOC_TITLE, TEMPLATE_KEYWORD_JAVADOC_BODY,
			TEMPLATE_KEYWORD_JAVADOC_ROOT, TEMPLATE_KEYWORD_JAVADOC_HEAD, TEMPLATE_KEYWORD_JAVADOC_PAGENAME };

	private static final SakerPath PATH_INDEX_HTML = SakerPath.valueOf("index.html");
	private static final SakerPath PATH_ALL_CLASSES_HTML = SakerPath.valueOf("all-classes.html");
	private static final SakerPath PATH_CONSTANTS_HTML = SakerPath.valueOf("constants.html");
	private Path templateFile;
	private Path apiJar;
	private SakerPath favicon;
	private Map<ElementKind, SakerPath> kindFavicons = new EnumMap<>(ElementKind.class);
	private Path outputDirectory;

	private String indexTitle = "Javadoc";

	private TemplatedContents template;
	private Map<String, Path> templateEmbedMacros = new LinkedHashMap<>();
	private Map<String, String> templateMacros = new LinkedHashMap<>();

	private Elements elements;
	private Types types;
	private DocTrees trees;

	private NavigableMap<String, PackageDocumentationInfo> docPackages = new TreeMap<>();
	/**
	 * The type documentation infos mapped by their qualified names.
	 */
	private NavigableMap<String, TypeDocumentationInfo> docTypes = new TreeMap<>();
	private Set<VariableElement> constants = new HashSet<>();
	private TypeElement javaLangObjectElement;
	private DeclaredType javaLangObjectType;
	private TypeMirror javaLangThrowableType;
	private TypeMirror javaLangRuntimeExceptionType;
	private TypeMirror javaLangErrorType;
	private TypeMirror javaLangStringType;
	private TypeMirror javaLangAnnotationAnnotationType;
	private Map<WildcardPath, String> packageExternalDocSites = new LinkedHashMap<>();
	{
		packageExternalDocSites.put(WildcardPath.valueOf("java/**"), "https://docs.oracle.com/javase/8/docs/api/");
		packageExternalDocSites.put(WildcardPath.valueOf("javax/**"), "https://docs.oracle.com/javase/8/docs/api/");
	}
	private NavigableSet<WildcardPath> excludeClassWildcards = new TreeSet<>();

	@Override
	public void init(Locale locale, Reporter reporter) {
		System.out.println("SakerDoclet.init()");
	}

	@Override
	public String getName() {
		return getClass().getSimpleName();
	}

	@Override
	public Set<? extends Option> getSupportedOptions() {
		return ObjectUtils.newHashSet(new ApiJarOption(), new OutputDirectoryOption(), new TemplateFileOption(),
				new IndexTitleOption(), new FaviconOption(), new DefaultFaviconOption(), new MacroOption(),
				new EmbedMacroOption(), new ExternalDocSiteOption(), new ExcludeClassOption());
	}

	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latest();
	}

	public boolean isAnnotationDocumented(AnnotationMirror am) {
		TypeElement elem = (TypeElement) am.getAnnotationType().asElement();
		if (!isTypeDocumented(elem)) {
			return false;
		}
		return elem.getAnnotation(Documented.class) != null;
	}

	private static class InnerClassInfo {
		String name;
		String outerName;
		String innerName;
		int access;

		public InnerClassInfo(String name, String outerName, String innerName, int access) {
			this.name = name;
			this.outerName = outerName;
			this.innerName = innerName;
			this.access = access;
		}

		@Override
		public String toString() {
			return "InnerClassInfo[" + (name != null ? "name=" + name + ", " : "")
					+ (outerName != null ? "outerName=" + outerName + ", " : "")
					+ (innerName != null ? "innerName=" + innerName + ", " : "") + "access=" + access + "]";
		}
	}

	private PackageDocumentationInfo getPackageInfoForName(String packagename) {
		return docPackages.compute(packagename, (k, v) -> v == null ? new PackageDocumentationInfo(k) : v);
	}

	private TypeDocumentationInfo getTypeInfoForQualifiedName(String internalname, String qname) {
		return docTypes.compute(qname, (k, v) -> {
			if (v == null) {
				return new TypeDocumentationInfo(internalname);
			}
			if (!v.getInternalName().equals(internalname)) {
				throw new IllegalArgumentException(
						"Internal name mismatch: " + internalname + " - " + v.getInternalName() + " on " + qname);
			}
			return v;
		});
	}

	private final class DocumentingClassVisitor extends ClassVisitor {
		private String name;
		private NavigableMap<String, InnerClassInfo> innerClassInfos;
		private NavigableSet<String> methods = new TreeSet<>();
		private NavigableSet<String> fields = new TreeSet<>();
		private String superName;
		private String[] interfaces;

		public DocumentingClassVisitor() {
			super(Opcodes.ASM6);
		}

		@Override
		public void visit(int version, int access, String name, String signature, String superName,
				String[] interfaces) {
			this.superName = superName;
			this.interfaces = interfaces == null ? ObjectUtils.EMPTY_STRING_ARRAY : interfaces.clone();
			this.name = name;

			super.visit(version, access, name, signature, superName, interfaces);
		}

		@Override
		public MethodVisitor visitMethod(int access, String name, String descriptor, String signature,
				String[] exceptions) {
			methods.add(name + descriptor);
			return null;
		}

		@Override
		public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
			fields.add(name);
			return null;
		}

		@Override
		public void visitInnerClass(String name, String outerName, String innerName, int access) {
			if (innerClassInfos == null) {
				innerClassInfos = new TreeMap<>();
			}
			innerClassInfos.put(name, new InnerClassInfo(name, outerName, innerName, access));
			super.visitInnerClass(name, outerName, innerName, access);
		}

		@Override
		public void visitEnd() {
			super.visitEnd();
			String qname = getQualifiedTypeName();
			TypeElement type = elements.getTypeElement(qname);
//			System.out.println("SakerDoclet.DocumentingClassVisitor.visitEnd() " + this.name + " - " + qname + " - " + type);
			TypeDocumentationInfo typeinfo = getTypeInfoForQualifiedName(this.name, qname);
			typeinfo.setElement(type);
			typeinfo.setDocumentedFields(fields);
			typeinfo.setDocumentedMethods(methods);

			PackageElement packageelement = elements.getPackageOf(type.getEnclosingElement());
			PackageDocumentationInfo packageinfo = getPackageInfoForName(packageelement.getQualifiedName().toString());

			if (superName != null) {
				TypeDocumentationInfo superinfo = getTypeInfoForQualifiedName(superName,
						getQualifiedTypeNameFor(superName));
				superinfo.addSubClass(typeinfo);
			}
			for (String itfname : interfaces) {
				TypeDocumentationInfo itfinfo = getTypeInfoForQualifiedName(itfname, getQualifiedTypeNameFor(itfname));
				itfinfo.addInterfaceImplementation(typeinfo);
			}
		}

		private String getQualifiedTypeNameFor(String name) {
			InnerClassInfo ici = innerClassInfos == null ? null : innerClassInfos.get(name);
			String n;
			if (ici != null) {
				n = getQualifiedTypeNameFor(ici.outerName) + "/" + ici.innerName;
			} else {
				n = name;
			}
			if (n.indexOf('$') >= 0) {
				throw new AssertionError("Failed to determine qualified name for: " + name + " as " + n
						+ " innerclasses " + innerClassInfos);
			}
			return n.replace('/', '.');
		}

		private String getQualifiedTypeName() {
			return getQualifiedTypeNameFor(this.name);
		}
	}

	@Override
	public boolean run(DocletEnvironment environment) {
		System.out.println("SakerDoclet.run() excluded " + excludeClassWildcards);
		elements = environment.getElementUtils();
		types = environment.getTypeUtils();
		trees = environment.getDocTrees();

		javaLangObjectElement = elements.getTypeElement("java.lang.Object");
		javaLangObjectType = (DeclaredType) javaLangObjectElement.asType();
		javaLangAnnotationAnnotationType = elements.getTypeElement(Annotation.class.getName()).asType();
		javaLangStringType = elements.getTypeElement("java.lang.String").asType();
		javaLangThrowableType = elements.getTypeElement(Throwable.class.getName()).asType();
		javaLangRuntimeExceptionType = elements.getTypeElement(RuntimeException.class.getName()).asType();
		javaLangErrorType = elements.getTypeElement(Error.class.getName()).asType();

		try {

			NavigableSet<String> templatekeywords = ImmutableUtils.makeImmutableNavigableSet(TEMPLATE_KEYWORDS);
			if (templateFile == null) {
				this.template = new TemplatedContents(ObjectUtils.newLinkedHashSet(TEMPLATE_KEYWORDS));
			} else {
				String templatecontents = new String(Files.readAllBytes(templateFile), StandardCharsets.UTF_8);
				Set<String> resolvedmacros = new TreeSet<>();
				for (Iterator<Entry<String, Path>> it = templateEmbedMacros.entrySet().iterator(); it.hasNext();) {
					Entry<String, Path> entry = it.next();
					String macro = entry.getKey();
					if (!templatecontents.contains(macro)) {
						continue;
					}
					if (!resolvedmacros.add(macro)) {
						throw new IllegalArgumentException("Recursive embed macro: " + macro);
					}
					templatecontents = templatecontents.replace(macro,
							new String(Files.readAllBytes(entry.getValue()), StandardCharsets.UTF_8));
					it = templateEmbedMacros.entrySet().iterator();
				}
				for (Entry<String, String> entry : templateMacros.entrySet()) {
					templatecontents = templatecontents.replace(entry.getKey(), entry.getValue());
				}
				this.template = new TemplatedContents(templatecontents, templatekeywords);
			}

			if (apiJar != null) {
				try (JarFile apijar = new JarFile(apiJar.toFile())) {
					Enumeration<JarEntry> jarentries = apijar.entries();
					while (jarentries.hasMoreElements()) {
						ZipEntry entry = jarentries.nextElement();
						String ename = entry.getName();
						if (!ename.endsWith(".class")) {
							continue;
						}
						try (InputStream entryis = apijar.getInputStream(entry)) {
							ClassReader reader = new ClassReader(entryis);
							reader.accept(new DocumentingClassVisitor(),
									ClassReader.SKIP_CODE | ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
						}
					}
				}
			} else {
				for (Element included : environment.getIncludedElements()) {
					System.out.println("SakerDoclet.run() include " + included);
					switch (included.getKind()) {
						case PACKAGE: {
							PackageElement pe = (PackageElement) included;
							PackageDocumentationInfo packdocinfo = docPackages
									.computeIfAbsent(pe.getQualifiedName().toString(), PackageDocumentationInfo::new);
							break;
						}
						case CLASS:
						case ANNOTATION_TYPE:
						case ENUM:
						case INTERFACE: {
							TypeElement te = (TypeElement) included;
							addNonAPIJarIncludedType(te);
							break;
						}

						case CONSTRUCTOR:
						case ENUM_CONSTANT:
						case EXCEPTION_PARAMETER:
						case FIELD:
						case INSTANCE_INIT:
						case LOCAL_VARIABLE:
						case METHOD:
						case MODULE:
						case OTHER:
						case PARAMETER:
						case RESOURCE_VARIABLE:
						case STATIC_INIT:
						case TYPE_PARAMETER:
						default: {
							break;
						}
					}
				}
			}

			for (Entry<String, PackageDocumentationInfo> entry : docPackages.entrySet()) {
				PackageElement packelem = elements.getPackageElement(entry.getKey());
				PackageDocumentationInfo packinfo = entry.getValue();
				packinfo.setElement(packelem);
				writePackagePage(packinfo);
			}
			for (Entry<String, TypeDocumentationInfo> entry : docTypes.entrySet()) {
				writeTypePage(entry.getValue());
			}
			writeIndexHtml();
			writeAllClassesHtml();
			writeConstantsHtml();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}

		return true;
	}

	private static boolean hasPublicOrProtectedConstructor(TypeElement te) {
		for (Element ee : te.getEnclosedElements()) {
			if (ee.getKind() != ElementKind.CONSTRUCTOR) {
				continue;
			}
			Set<Modifier> modifiers = ee.getModifiers();
			if (modifiers.contains(Modifier.PUBLIC) || modifiers.contains(Modifier.PROTECTED)) {
				return true;
			}
		}
		return false;
	}

	private void addNonAPIJarIncludedType(TypeElement te) {
		Name typeqname = te.getQualifiedName();
		if (getExternalDocSiteLink(typeqname.toString()) != null) {
			//external type
			return;
		}
		if (isClassExplicitlyExcluded(typeqname.toString())) {
			//excluded
			return;
		}
		String internalname = getInternalName(te);
		PackageElement pack = elements.getPackageOf(te);
		PackageDocumentationInfo packdocinfo = docPackages.computeIfAbsent(pack.getQualifiedName().toString(),
				PackageDocumentationInfo::new);
		TypeDocumentationInfo typedocinfoelem = getTypeInfoForQualifiedName(internalname, typeqname.toString());
		if (typedocinfoelem.getElement() == te) {
			//already added
			return;
		}
		typedocinfoelem.setElement(te);

		TypeElement supertype = getSuperClassElement(te);
		if (supertype != null) {
			addNonAPIJarIncludedType(supertype);
			getTypeInfoForQualifiedName(getInternalName(supertype), supertype.getQualifiedName().toString())
					.addSubClass(typedocinfoelem);
		}
		for (TypeMirror itf : te.getInterfaces()) {
			DeclaredType dt = (DeclaredType) itf;
			TypeElement superitf = (TypeElement) dt.asElement();
			addNonAPIJarIncludedType(superitf);
			getTypeInfoForQualifiedName(getInternalName(superitf), superitf.getQualifiedName().toString())
					.addSubClass(typedocinfoelem);
		}

		boolean includeprotected;
		if (te.getModifiers().contains(Modifier.FINAL)) {
			includeprotected = false;
		} else if (!te.getModifiers().contains(Modifier.PUBLIC)) {
			includeprotected = false;
		} else {
			includeprotected = hasPublicOrProtectedConstructor(te);
		}

		NavigableSet<String> docfields = new TreeSet<>();
		NavigableSet<String> docmethods = new TreeSet<>();
		for (Element enclosed : te.getEnclosedElements()) {
			Modifier vis = getVisibilityModifier(enclosed);
			if (vis != Modifier.PUBLIC) {
				if (vis == Modifier.PROTECTED) {
					if (!includeprotected) {
						continue;
					}
				} else {
					//don't include
					continue;
				}
			}
			switch (enclosed.getKind()) {
				case ANNOTATION_TYPE:
				case CLASS:
				case INTERFACE:
				case ENUM: {
					//already present in included elements
//					TypeElement encte = (TypeElement) enclosed;
//					addNonAPIJarIncludedType(encte);
					break;
				}

				case ENUM_CONSTANT:
				case FIELD: {
					VariableElement encve = (VariableElement) enclosed;
					docfields.add(encve.getSimpleName().toString());
					break;
				}
				case CONSTRUCTOR:
				case METHOD: {
					ExecutableElement encee = (ExecutableElement) enclosed;
					docmethods.add(encee.getSimpleName() + getDescriptor(encee));
					break;
				}

				default: {
					break;
				}
			}
		}
		typedocinfoelem.setDocumentedFields(docfields);
		typedocinfoelem.setDocumentedMethods(docmethods);
	}

	private SakerPath getFaviconPath(ElementKind kind) {
		if (kind == null) {
			return favicon;
		}
		return kindFavicons.getOrDefault(kind, favicon);
	}

	private void writePageContents(SakerPath pagepath, StringBuilder out, String title, ElementKind pageelemkind,
			String pagename, TemplatedContents template) throws IOException {
		Files.createDirectories(outputDirectory.resolve(pagepath.getParent().toString()));
		try (BufferedWriter os = Files.newBufferedWriter(outputDirectory.resolve(pagepath.toString()),
				StandardCharsets.UTF_8)) {
			TreeMap<String, String> keywords = new TreeMap<>();
			String rootpath = getDocPagePath(pagepath, SakerPath.EMPTY);

			keywords.put(TEMPLATE_KEYWORD_JAVADOC_TITLE, title);
			keywords.put(TEMPLATE_KEYWORD_JAVADOC_BODY, out.toString());
			keywords.put(TEMPLATE_KEYWORD_JAVADOC_ROOT, rootpath.isEmpty() ? "" : rootpath + "/");
			keywords.put(TEMPLATE_KEYWORD_JAVADOC_PAGENAME, pagename);

			SakerPath faviconpath = getFaviconPath(pageelemkind);
			if (faviconpath != null) {
				keywords.put(TEMPLATE_KEYWORD_JAVADOC_HEAD,
						"<link rel=\"icon\" href=\"" + getDocPagePath(pagepath, faviconpath) + "\">");
			} else {
				keywords.put(TEMPLATE_KEYWORD_JAVADOC_HEAD, "");
			}
			template.writeTo(os, keywords);
		}
	}

	private void collectAllTypeElementsInElement(Element elem, Collection<TypeElement> result) {
		for (Element encelem : elem.getEnclosedElements()) {
			if (isTypeElementKind(encelem.getKind())) {
				if (result.add((TypeElement) encelem)) {
					collectAllTypeElementsInElement(encelem, result);
				}
			}
		}
	}

	private Iterable<TypeElement> collectAllTypeElementsInPackage(PackageElement pack) {
		Set<TypeElement> result = new HashSet<>();
		collectAllTypeElementsInElement(pack, result);
		return result;
	}

	private static Modifier getVisibilityModifier(Element elem) {
		Set<Modifier> mods = elem.getModifiers();
		if (mods.contains(Modifier.PUBLIC)) {
			return Modifier.PUBLIC;
		}
		if (mods.contains(Modifier.PRIVATE)) {
			return Modifier.PRIVATE;
		}
		if (mods.contains(Modifier.PROTECTED)) {
			return Modifier.PROTECTED;
		}
		return null;
	}

	private void writeConstantsHtml() throws IOException {
		SakerPath pagepath = PATH_CONSTANTS_HTML;
		StringBuilder out = new StringBuilder();

		NavigableMap<String, NavigableMap<String, VariableElement>> mappedconstants = new TreeMap<>();

		for (VariableElement ce : constants) {
			TypeElement enctype = (TypeElement) ce.getEnclosingElement();
			mappedconstants.computeIfAbsent(enctype.getQualifiedName().toString(), Functionals.treeMapComputer())
					.put(ce.getSimpleName().toString(), ce);
		}
		out.append("<div class=\"javadoc-index-constants\">");
		for (Entry<String, NavigableMap<String, VariableElement>> typeconstants : mappedconstants.entrySet()) {
			TypeElement typeelem = docTypes.get(typeconstants.getKey()).getElement();
			out.append("<div class=\"javadoc-index-constants-type\">");
			out.append("<table>");

			out.append("<tbody>");
			for (Entry<String, VariableElement> entry : typeconstants.getValue().entrySet()) {
				String constname = entry.getKey();
				VariableElement constelem = entry.getValue();
				Modifier vismodifier = getVisibilityModifier(constelem);

				out.append("<tr class=\"javadoc-index-constants-entry\">");

				out.append("<td class=\"javadoc-index-constants-entry-meta\">");
				if (vismodifier != null) {
					out.append(DocCommentHtmlWriter.HTML_SPAN_JAVADOC_KEYWORD);
					out.append(vismodifier.name().toLowerCase(Locale.ENGLISH));
					out.append("</span>&nbsp;");
				}
				DocCommentHtmlWriter.writeTypeMirror(this, pagepath, constelem.asType(), out,
						DocCommentHtmlWriter.TM_LINK_TYPES);
				out.append("</td>");

				out.append("<td class=\"javadoc-index-constants-entry-name\">");
				out.append(getAHrefTagForElement(pagepath, constelem));
				out.append(constname);
				out.append("</a>");
				out.append("</td>");

				out.append("<td class=\"javadoc-index-constants-entry-value\">");
				out.append(elements.getConstantExpression(constelem.getConstantValue()));
				out.append("</td>");

				out.append("</tr>");
			}
			out.append("</tbody>");
			out.append("<thead>");
			out.append("<tr>");

			out.append("<th class=\"javadoc-th-constant-meta\"></th>");
			out.append("<th class=\"javadoc-th-constant-name\"></th>");
			out.append("<th class=\"javadoc-th-constant-value\"></th>");

			out.append("</tr>");
			out.append("</thead>");

			out.append("<caption>");
			out.append(DocCommentHtmlWriter.HTML_SPAN_CODE);
			out.append(getAHrefTagForElement(pagepath, typeelem));
			out.append(typeelem.getQualifiedName());
			out.append("</a></span>");
			out.append("</caption>");

			out.append("</table>");

			out.append("</div>");
		}
		out.append("</div>");

		writePageContents(pagepath, out, "Constants index", null, "constants", this.template);
	}

	private void writeAllClassesHtml() throws IOException {
		SakerPath pagepath = PATH_ALL_CLASSES_HTML;
		StringBuilder out = new StringBuilder();

		List<TypeDocumentationInfo> classes = new ArrayList<>();
		NavigableSet<Character> initials = new TreeSet<>();
		for (TypeDocumentationInfo typeinfo : docTypes.values()) {
			if (typeinfo.getElement() == null) {
				continue;
			}
			classes.add(typeinfo);
			initials.add(Character.toUpperCase(typeinfo.getElement().getSimpleName().toString().charAt(0)));
		}
		classes.sort((l, r) -> l.getElement().getSimpleName().toString()
				.compareToIgnoreCase(r.getElement().getSimpleName().toString()));

		out.append("<div class=\"javadoc-index-all-classes\">");
		if (!classes.isEmpty()) {
			out.append("<div class=\"javadoc-index-all-classes-lookup\">");
			for (Character initc : initials) {
				out.append("<a href=\"#" + initc + "\">");
				out.append(initc);
				out.append("</a>");
			}
			out.append("</div>");
			char currentinitial = 0;
			for (TypeDocumentationInfo tinfo : classes) {
				TypeElement infoelem = tinfo.getElement();
				String sname = infoelem.getSimpleName().toString();
				char c = Character.toUpperCase(sname.charAt(0));
				if (c != currentinitial) {
					if (currentinitial != 0) {
						out.append("</div>");
					}
					out.append("<div id=\"" + c + "\" class=\"javadoc-index-all-classes-section\">");
					currentinitial = c;
				}
				out.append("<div class=\"" + DocCommentHtmlWriter.HTML_CLASS_CODE_SPAN + " javadoc-index-all-entry\">");
				out.append(getAHrefTagForElement(pagepath, infoelem));
				out.append(sname);
				out.append("</a>");
				if (infoelem.getNestingKind() == NestingKind.MEMBER) {
					TypeElement enclosingtype = (TypeElement) infoelem.getEnclosingElement();
					out.append("<span class=\"javadoc-idx-enclosing-info\">");
					out.append("&nbsp;in ");
					out.append(getAHrefTagForElement(pagepath, enclosingtype));
					out.append(getNestingQualifiedName(enclosingtype));
					out.append("</a>");
					out.append("</span>");
				}
				out.append("</div>");
			}
			if (currentinitial != 0) {
				out.append("</div>");
			}
		}
		out.append("</div>");

		writePageContents(pagepath, out, "Class index", null, "all-classes", this.template);
	}

	private void writeIndexHtml() throws IOException {
		SakerPath pagepath = PATH_INDEX_HTML;
		StringBuilder out = new StringBuilder();

		out.append("<div class=\"javadoc-index-packages javadoc-idx-section\">");
		out.append("<table>");
		{
			out.append("<caption>Packages</caption>");
			out.append("<thead>");
			out.append("<tr>");

			out.append("<th class=\"javadoc-index-packages-th-name\"></th>");
			out.append("<th class=\"javadoc-index-packages-th-short\"></th>");

			out.append("</tr>");
			out.append("</thead>");

			out.append("<tbody>");
			for (Entry<String, PackageDocumentationInfo> entry : docPackages.entrySet()) {
				PackageElement packelem = entry.getValue().getElement();

				out.append("<tr class=\"javadoc-index-package javadoc-idx-member\">");

				out.append("<td class=\"javadoc-index-package-name javadoc-idx-member-meta\">");
				out.append(getAHrefTagForElement(pagepath, packelem));
				out.append(entry.getKey());
				out.append("</a>");
				out.append("</td>");

				out.append("<td class=\"javadoc-index-package-sig javadoc-idx-member-sig\">");
				out.append("<div class=\"javadoc-index-package-short javadoc-idx-member-short\">");
				DocCommentTree doctree = trees.getDocCommentTree(packelem);
				if (doctree != null) {
					DocCommentHtmlWriter writer = new DocCommentHtmlWriter(this, trees, out, trees.getPath(packelem),
							doctree, pagepath);
					for (DocTree fstree : doctree.getFirstSentence()) {
						writer.write(fstree);
					}
				}
				out.append("</div>");
				out.append("</td>");

				out.append("</tr>");
			}
			out.append("</tbody>");
		}
		out.append("</table>");
		out.append("</div>");

		writePageContents(pagepath, out, indexTitle, null, "index", this.template);
	}

	private void writePackagePage(PackageDocumentationInfo packageinfo) throws IOException {
		PackageElement elem = packageinfo.getElement();
		if (elem == null) {
			throw new IllegalArgumentException("Package doesn't exist: " + elem);
		}
		String elemqname = elem.getQualifiedName().toString();
		DocCommentTree doctree = trees.getDocCommentTree(elem);
		SakerPath pagepath = getPagePath(elem);
		StringBuilder out = new StringBuilder();

		out.append("<div class=\"javadoc-package-header\">");
		{
			out.append("<div class=\"javadoc-package-header-pkg\">");
			out.append(DocCommentHtmlWriter.HTML_SPAN_CODE + DocCommentHtmlWriter.HTML_SPAN_JAVADOC_KEYWORD
					+ "package</span> ");

			out.append("<span class=\"javadoc-package-header-name\">");
			out.append(elemqname);
			out.append("</span>");

			out.append("</span>");
			out.append("</div>");
		}
		out.append("</div>");

		NavigableMap<String, TypeElement> exceptiontypes = new TreeMap<>();

		NavigableMap<String, TypeElement> enumtypes = new TreeMap<>();
		NavigableMap<String, TypeElement> classtypes = new TreeMap<>();
		NavigableMap<String, TypeElement> annotationtypes = new TreeMap<>();
		NavigableMap<String, TypeElement> interfacetypes = new TreeMap<>();

		for (TypeElement type : collectAllTypeElementsInPackage(elem)) {
			if (!docTypes.containsKey(type.getQualifiedName().toString())) {
				continue;
			}
			String typesimplename = type.getSimpleName().toString();
			switch (type.getKind()) {
				case ANNOTATION_TYPE: {
					annotationtypes.put(typesimplename, type);
					break;
				}
				case CLASS: {
					if (types.isAssignable(type.asType(), javaLangThrowableType)) {
						exceptiontypes.put(typesimplename, type);
					} else {
						classtypes.put(typesimplename, type);
					}
					break;
				}
				case ENUM: {
					enumtypes.put(typesimplename, type);
					break;
				}
				case INTERFACE: {
					interfacetypes.put(typesimplename, type);
					break;
				}
				default: {
					throw new UnsupportedOperationException(type.getKind().toString());
				}
			}
		}

		out.append("<div class=\"javadoc-package-doc\">");
		if (doctree != null) {
			DocCommentHtmlWriter writer = new DocCommentHtmlWriter(this, trees, out, trees.getPath(elem), doctree,
					pagepath);

			DeprecatedTree deprtree = findDeprecatedTree(doctree, elem);
			writeDeprecatedTagForElement(pagepath, out, new ElementDocTree<DeprecatedTree>(elem, doctree, deprtree),
					elements.isDeprecated(elem), elem);

			out.append("<div class=\"javadoc-package-doc-body\">");
			for (DocTree t : doctree.getFirstSentence()) {
				writer.write(t);
			}
			List<? extends DocTree> body = doctree.getBody();
			if (!ObjectUtils.isNullOrEmpty(body)) {
				out.append(' ');
			}
			for (DocTree t : body) {
				writer.write(t);
			}
			out.append("</div>");
			writeSeeTagDetails(elem, doctree, out, pagepath, elem);
			writeSinceTagDetails(elem, doctree, out, pagepath, elem);
		} else {
			System.out.println("Missing documentation for package: " + packageinfo);
		}
		out.append("</div>");

		out.append("<div class=\"javadoc-package-idx\">");
		writePackageIndexSection(elem, pagepath, out, interfacetypes, "itfs", "itf", "Interfaces");
		writePackageIndexSection(elem, pagepath, out, classtypes, "classes", "class", "Classes");
		writePackageIndexSection(elem, pagepath, out, annotationtypes, "annots", "annot", "Annotations");
		writePackageIndexSection(elem, pagepath, out, enumtypes, "enums", "enum", "Enumerations");
		writePackageIndexSection(elem, pagepath, out, exceptiontypes, "exceptions", "exception", "Exceptions");
		out.append("</div>");

		writePageContents(pagepath, out, elem.getQualifiedName().toString(), ElementKind.PACKAGE, "package-info",
				this.template);
	}

	private void writePackageIndexSection(PackageElement packelem, SakerPath pagepath, Appendable out,
			NavigableMap<String, TypeElement> types, String idxsectioncname, String idxentrycname, String caption)
			throws IOException {
		if (types.isEmpty()) {
			return;
		}
		out.append("<div class=\"javadoc-package-idx-" + idxsectioncname + " javadoc-idx-section\">");
		out.append("<table>");
		out.append("<caption>" + caption + "</caption>");
		out.append("<thead>");
		out.append("<tr>");

		out.append("<th class=\"javadoc-th-type-name\"></th>");
		out.append("<th class=\"javadoc-th-type-details\"></th>");

		out.append("</tr>");
		out.append("</thead>");

		out.append("<tbody>");
		for (Entry<String, TypeElement> entry : types.entrySet()) {
			TypeElement typeelem = entry.getValue();
			DocCommentTree typedoctree = trees.getDocCommentTree(typeelem);

			out.append("<tr class=\"javadoc-package-idx-" + idxentrycname + " javadoc-idx-member\">");

			out.append("<td class=\"javadoc-package-idx-" + idxentrycname + "-name javadoc-idx-member-meta\">");
			out.append(getAHrefTagForElement(pagepath, typeelem));
			out.append(entry.getKey());
			out.append("</a>");
			if (typeelem.getNestingKind() == NestingKind.MEMBER) {
				out.append("<span class=\"javadoc-idx-enclosing-info\">");
				TypeElement encelem = (TypeElement) typeelem.getEnclosingElement();
				String encqname = encelem.getQualifiedName().toString();
				String pqname = packelem.getQualifiedName().toString();
				if (!pqname.isEmpty()) {
					encqname = encqname.substring(pqname.length() + 1);
				}
				out.append("&nbsp;in ");
				out.append(getAHrefTagForElement(pagepath, encelem));
				out.append(encqname);
				out.append("</a>");
				out.append("</span>");
			}
			out.append("</td>");

			out.append("<td class=\"javadoc-package-idx-" + idxentrycname + "-sig javadoc-idx-member-sig\">");
			if (typedoctree != null) {
				out.append("<div class=\"javadoc-package-idx-" + idxentrycname + "-short javadoc-member-short\">");
				writeIndexFirstSentence(pagepath, out, typeelem,
						new LinkedDoc<>(typeelem, typedoctree, LinkedDocRelation.DIRECT));
				out.append("</div>");
			}
			out.append("</td>");

			out.append("</tr>");
		}

		out.append("</tbody>");
		out.append("</table>");
		out.append("</div>");
	}

	private static boolean writeModifiers(Element elem, Appendable out, TypeElement relatedreducemodifier,
			boolean keywordspan) throws IOException {
		Set<Modifier> modifiers = elem.getModifiers();
		if (modifiers.isEmpty()) {
			return false;
		}
		modifiers = EnumSet.copyOf(modifiers);
		ElementKind elemkind = elem.getKind();
		if (elemkind.isInterface()) {
			modifiers.remove(Modifier.ABSTRACT);
			modifiers.remove(Modifier.STATIC);
		} else if (elemkind == ElementKind.ENUM) {
			modifiers.remove(Modifier.ABSTRACT);
			modifiers.remove(Modifier.STATIC);
			//don't write the final modifier for enums, as it is implied
			modifiers.remove(Modifier.FINAL);
		} else if (elemkind == ElementKind.METHOD) {
			if (relatedreducemodifier != null) {
				if (relatedreducemodifier.getKind().isInterface()) {
					if (!modifiers.contains(Modifier.STATIC)) {
						modifiers.remove(Modifier.ABSTRACT);
					}
				}
				if (relatedreducemodifier.getModifiers().contains(Modifier.FINAL)) {
					//we don't need to display the final identifier, if the class is already final, as it is implied
					modifiers.remove(Modifier.FINAL);
				}
				if (relatedreducemodifier.getKind() == ElementKind.ENUM) {
					//no need to display the abstract modifier for enums, as clients cannot extend it.
					modifiers.remove(Modifier.ABSTRACT);
				}
			}
		}
		//to have it sorted by ordinal
		if (modifiers.isEmpty()) {
			return false;
		}
		if (keywordspan) {
			out.append("<span class=\"javadoc-kw\">");
		}
		for (Iterator<Modifier> it = modifiers.iterator(); it.hasNext();) {
			Modifier m = it.next();
			out.append(m.name().toLowerCase(Locale.ENGLISH));
			out.append(' ');
		}
		if (keywordspan) {
			out.append("</span>");
		}
		return true;
	}

	private static boolean writeModifiers(Element elem, Appendable out, TypeElement relatedreducemodifier)
			throws IOException {
		return writeModifiers(elem, out, relatedreducemodifier, true);
	}

	public static String escapeHtml(String s) {
		return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'",
				"&#39;");
	}

	public Types getTypes() {
		return types;
	}

	public Elements getElements() {
		return elements;
	}

	public TypeMirror getJavaLangObjectType() {
		return javaLangObjectType;
	}

	public String getNestingQualifiedName(TypeElement type) {
		if (type.getNestingKind() == NestingKind.MEMBER) {
			PackageElement pack = elements.getPackageOf(type);
			String packqname = pack.getQualifiedName().toString();
			if (packqname.isEmpty()) {
				return type.getQualifiedName().toString();
			}
			return type.getQualifiedName().toString().substring(packqname.length() + 1);
		}
		return type.getSimpleName().toString();
	}

	public String getAHrefTagForElement(SakerPath pagepath, Element element, Map<String, String> additionaltags) {
		StringBuilder out = new StringBuilder();
		out.append("<a href=\"");
		out.append(getDocPageHref(pagepath, element));
		out.append("\" title=\"");
		try {
			writeElementTagTitle(pagepath, element, out);
		} catch (IOException e) {
			throw new AssertionError(e);
		}
		ElementKind elementkind = element.getKind();
		out.append("\"");

		out.append(" data-javadoc-kind=\"");
		out.append(elementkind.name().toLowerCase(Locale.ENGLISH));
		out.append("\"");
		if (isExecutableElementKind(elementkind)) {
			out.append(" data-javadoc-enclosing-kind=\"");
			out.append(element.getEnclosingElement().getKind().name().toLowerCase(Locale.ENGLISH));
			out.append("\"");
		}
		if (!ObjectUtils.isNullOrEmpty(additionaltags)) {
			for (Entry<String, String> entry : additionaltags.entrySet()) {
				out.append(' ');
				out.append(entry.getKey());
				out.append("=\"");
				out.append(entry.getValue());
				out.append("\"");
			}
		}
		out.append(">");
		return out.toString();
	}

	public void writeElementTagTitle(SakerPath pagepath, Element element, Appendable out) throws IOException {
		ElementKind elementkind = element.getKind();
		switch (elementkind) {
			case CONSTRUCTOR: {
				ExecutableElement ee = (ExecutableElement) element;
				if (!isMethodDocumented(ee)) {
					throw new IllegalArgumentException("Trying to acquire link to undocumented element: " + element);
				}
				out.append(getNestingQualifiedName((TypeElement) element.getEnclosingElement()));
				DocCommentHtmlWriter.writeReferenceParameterTypes(this, pagepath, ee, DocCommentHtmlWriter.TM_NO_HTML,
						out);
				break;
			}
			case METHOD: {
				ExecutableElement ee = (ExecutableElement) element;
				if (!isMethodDocumented(ee)) {
					throw new IllegalArgumentException("Trying to acquire link to undocumented element: " + element);
				}
				out.append(getNestingQualifiedName((TypeElement) element.getEnclosingElement()));
				out.append('.');
				out.append(element.getSimpleName());
				DocCommentHtmlWriter.writeReferenceParameterTypes(this, pagepath, ee, DocCommentHtmlWriter.TM_NO_HTML,
						out);
				break;
			}
			case PACKAGE: {
				PackageElement packelem = (PackageElement) element;
				if (!isPackageDocumented(packelem)) {
					throw new IllegalArgumentException("Trying to acquire link to undocumented element: " + element);
				}
				out.append(packelem.getQualifiedName());
				break;
			}
			case FIELD:
			case ENUM_CONSTANT: {
				if (!isFieldDocumented((VariableElement) element)) {
					throw new IllegalArgumentException("Trying to acquire link to undocumented element: " + element);
				}
				out.append(getNestingQualifiedName((TypeElement) element.getEnclosingElement()));
				out.append('.');
				out.append(element.getSimpleName());
				break;
			}
			case ANNOTATION_TYPE:
			case CLASS:
			case ENUM:
			case INTERFACE: {
				TypeElement type = (TypeElement) element;
				if (!isTypeDocumented(type)) {
					throw new IllegalArgumentException("Trying to acquire link to undocumented element: " + element);
				}
				writeTypeElementKindKeyword(type, out);
				out.append(type.getQualifiedName());
				break;
			}
			case TYPE_PARAMETER: {
				TypeParameterElement tpe = (TypeParameterElement) element;
				out.append(tpe.getSimpleName());
				List<? extends TypeMirror> bounds = tpe.getBounds();
				if (!ObjectUtils.isNullOrEmpty(bounds)) {
					if (bounds.size() != 1 || !this.types.isSameType(bounds.get(0), javaLangObjectType)) {
						out.append(" extends ");
						for (Iterator<? extends TypeMirror> it = bounds.iterator(); it.hasNext();) {
							TypeMirror b = it.next();
							DocCommentHtmlWriter.writeTypeMirror(this, pagepath, b, out,
									DocCommentHtmlWriter.TM_NO_HTML);
							if (it.hasNext()) {
								out.append(", ");
							}
						}
					}
				}
				break;
			}
			default: {
				throw new UnsupportedOperationException(elementkind + " - " + element);
			}
		}
	}

	private static boolean isExecutableElementKind(ElementKind elementkind) {
		return elementkind == ElementKind.METHOD || elementkind == ElementKind.CONSTRUCTOR;
	}

	public String getAHrefTagForElement(SakerPath pagepath, Element element) {
		return getAHrefTagForElement(pagepath, element, null);
	}

	private void writeMethodOverrideNoteForDetail(ExecutableElement overriddenmethod, Appendable out,
			SakerPath pagepath) throws IOException {
		out.append("<div class=\"javadoc-overridden-inherited\">");
		out.append("Overridden from: " + DocCommentHtmlWriter.HTML_SPAN_CODE);
		out.append(getAHrefTagForElement(pagepath, overriddenmethod,
				ImmutableUtils.singletonNavigableMap("class", "javadoc-overridden-inherited-type")));
		out.append(overriddenmethod.getEnclosingElement().getSimpleName());
		out.append("</a></span>");
		out.append("</div>");
	}

	private void writeDetailLinkNote(ExecutableElement linkedmethod, Appendable out, SakerPath pagepath)
			throws IOException {
		out.append("<div class=\"javadoc-doc-linked\">");
		out.append("Documentation included from: " + DocCommentHtmlWriter.HTML_SPAN_CODE);
		out.append(getAHrefTagForElement(pagepath, linkedmethod,
				ImmutableUtils.singletonNavigableMap("class", "javadoc-doc-linked-type")));
		out.append(linkedmethod.getEnclosingElement().getSimpleName());
		out.append("</a></span>");
		out.append("</div>");
	}

	@Deprecated
	public void appendInheritedDocumentationNotFoundFirstSentence(ExecutableElement method, Appendable out,
			SakerPath pagepath, List<LinkedDoc<ExecutableElement>> methoddocinfos) throws IOException {
		out.append("<span class=\"javadoc-missing-inherited\">");
		out.append("See overridden method documentation: " + DocCommentHtmlWriter.HTML_SPAN_CODE);
		out.append(getAHrefTagForElement(pagepath, method,
				ImmutableUtils.singletonNavigableMap("class", "javadoc-missing-inherited-type")));
		out.append(method.getEnclosingElement().getSimpleName());
		out.append("</a></span>");
		out.append("</span>");
		//TODO warn?

		System.out.println(
				"Warning: Inherited documentation not found for: " + method.getEnclosingElement() + "." + method);
	}

	public void appendInheritedDocumentationNotFoundDetail(ExecutableElement method, Appendable out, SakerPath pagepath,
			List<LinkedDoc<ExecutableElement>> methoddocinfos) throws IOException {
		appendInheritedDocumentationNotFoundFirstSentence(method, out, pagepath, methoddocinfos);
		//TODO warn?
	}

	public static class ElemDoc {
		private Element element;
		private DocCommentTree commentTree;

		public ElemDoc(Element element, DocCommentTree commentTree) {
			this.element = element;
			this.commentTree = commentTree;
		}

		public Element getElement() {
			return element;
		}

		public DocCommentTree getCommentTree() {
			return commentTree;
		}
	}

	public static class ElementDocTree<DT extends DocTree> extends ElemDoc {
		private DT doc;

		public ElementDocTree(Element element, DocCommentTree commentTree, DT doc) {
			super(element, commentTree);
			this.doc = doc;
		}

		public DT getDocTree() {
			return doc;
		}
	}

	private enum LinkedDocRelation {
		DIRECT,
		INHERITED,
		LINKED;
	}

	public static class LinkedDoc<E extends Element> {
		protected final E element;
		protected final DocCommentTree docTree;
		protected final LinkedDocRelation relation;
		protected final E associatedElement;

		public LinkedDoc(E element, DocCommentTree docTree, LinkedDocRelation relation) {
			this.element = element;
			this.docTree = docTree;
			this.relation = relation;
			this.associatedElement = element;
		}

		public LinkedDoc(E element, DocCommentTree docTree, LinkedDocRelation relation, E associatedElement) {
			this.element = element;
			this.docTree = docTree;
			this.relation = relation;
			this.associatedElement = associatedElement;
		}

		public E getDocElement() {
			return element;
		}

		public DocCommentTree getDocTree() {
			return docTree;
		}

		public LinkedDocRelation getRelation() {
			return relation;
		}

		public E getAssociatedElement() {
			return associatedElement;
		}

		public LinkedDoc<E> replaceRelation(LinkedDocRelation relation) {
			return new LinkedDoc<>(associatedElement, docTree, relation, associatedElement);
		}

		@Override
		public String toString() {
			return "LinkedDoc["
					+ (element != null
							? "element=" + element.getEnclosingElement().getSimpleName() + "." + element + ", "
							: "")
					+ (docTree != null ? "has doc, " : "no doc, ")
					+ (relation != null ? "relation=" + relation + ", " : "") + "]";
		}

	}

	private List<LinkedDoc<ExecutableElement>> getInitialMethodDocCommentTree(ExecutableElement ee) {
		LinkedDoc<ExecutableElement> linked = getDocCommentTreeWalkSeeTags(ee);
		List<LinkedDoc<ExecutableElement>> resultdocs = collectInheritedDocumentations(linked.getAssociatedElement());
		if (resultdocs == null) {
			//no inherited doc
			return Collections.singletonList(linked);
		}

		if (linked.getRelation() != LinkedDocRelation.DIRECT) {
			ListIterator<LinkedDoc<ExecutableElement>> it = resultdocs.listIterator();
			while (it.hasNext()) {
				it.set(it.next().replaceRelation(linked.getRelation()));
			}
		}

		resultdocs.add(0, linked);
		return resultdocs;
	}

	private void collectAllSuperTypesImpl(TypeElement type, Map<String, DeclaredType> result,
			Map<TypeParameterElement, TypeMirror> parammirrors) {
		for (TypeMirror itf : new ConcatIterable<>(
				Arrays.asList(Collections.singleton(type.getSuperclass()), type.getInterfaces()))) {
			if (itf.getKind() != TypeKind.DECLARED) {
				continue;
			}
			DeclaredType dt = (DeclaredType) itf;
			TypeElement itfelem = (TypeElement) dt.asElement();
			String itfname = itfelem.getQualifiedName().toString();
			if (result.containsKey(itfname)) {
				continue;
			}
			List<? extends TypeParameterElement> sttypeparams = itfelem.getTypeParameters();

			TypeMirror[] currenttypeargs;
			if (!ObjectUtils.isNullOrEmpty(sttypeparams)) {
				//the super type has type parameters
				List<? extends TypeMirror> sctypeargs = dt.getTypeArguments();
				if (ObjectUtils.isNullOrEmpty(sctypeargs)) {
					//the super class was declared as raw type
					//ignore
					//empty args
					currenttypeargs = new TypeMirror[0];
				} else {
					if (sctypeargs.size() != sttypeparams.size()) {
						throw new IllegalArgumentException("Type argument sizes mismatch on: "
								+ itfelem.getQualifiedName() + " with " + sttypeparams + " and " + sctypeargs);
					}
					currenttypeargs = new TypeMirror[sttypeparams.size()];
					for (int i = 0; i < currenttypeargs.length; i++) {
						TypeMirror targ = sctypeargs.get(i);
						if (targ.getKind() == TypeKind.TYPEVAR) {
							TypeVariable tv = (TypeVariable) targ;
							TypeMirror substitute = parammirrors.get(tv.asElement());
							if (substitute == null) {
								throw new IllegalArgumentException("Type variable mirror not found for: " + tv);
							}
							currenttypeargs[i] = substitute;
						} else {
							currenttypeargs[i] = targ;
						}
					}
				}
			} else {
				currenttypeargs = new TypeMirror[0];
			}

			result.put(itfname, types.getDeclaredType(itfelem, currenttypeargs));
			if (currenttypeargs.length > 0) {
				for (int i = 0; i < currenttypeargs.length; i++) {
					parammirrors.put(sttypeparams.get(i), currenttypeargs[i]);
				}
			}

			collectAllSuperTypesImpl(itfelem, result, parammirrors);
		}
	}

	private NavigableMap<String, DeclaredType> collectAllSuperTypesAndElement(TypeElement elem) {
		Map<TypeParameterElement, TypeMirror> parammirrors = new HashMap<>();
		DeclaredType elemdt = (DeclaredType) elem.asType();
		Iterator<? extends TypeMirror> targit = elemdt.getTypeArguments().iterator();
		for (TypeParameterElement tpe : elem.getTypeParameters()) {
			parammirrors.put(tpe, targit.next());
		}

		NavigableMap<String, DeclaredType> result = new TreeMap<>();
		result.put(elem.getQualifiedName().toString(), elemdt);
		result.put(Object.class.getCanonicalName(), javaLangObjectType);
		collectAllSuperTypesImpl(elem, result, parammirrors);
		return result;
	}

	private void checkPackageDocumentationExistence(PackageElement elem) {
		if (!isPackageDocumented(elem)) {
			throw new IllegalArgumentException("Package not documented: " + elem);
		}
	}

	public boolean isPackageDocumented(PackageElement elem) {
		return docPackages.containsKey(elem.getQualifiedName().toString());
	}

	private void checkTypeDocumentationExistence(TypeElement type) {
		String qname = type.getQualifiedName().toString();
		if (getExternalDocSiteLink(qname) != null) {
			return;
		}
		TypeDocumentationInfo got = docTypes.get(qname);
		if (got == null || got.getElement() == null) {
			throw new IllegalArgumentException("Documentation referenced type is not documented: " + qname);
		}
	}

	public boolean isTypeDocumented(TypeElement elem) {
		String qname = elem.getQualifiedName().toString();
		if (getExternalDocSiteLink(qname) != null) {
			return true;
		}
		TypeDocumentationInfo docinfo = docTypes.get(qname);
		return docinfo != null && docinfo.getElement() != null;
	}

	public boolean isFieldDocumented(VariableElement field) {
		return isFieldDocumented((TypeElement) field.getEnclosingElement(), field.getSimpleName().toString());
	}

	public boolean isMethodDocumented(ExecutableElement ee) {
		return isMethodDocumented((TypeElement) ee.getEnclosingElement(), ee.getSimpleName() + getDescriptor(ee));
	}

	public boolean isFieldDocumented(TypeElement type, String name) {
		String enctypeqname = type.getQualifiedName().toString();
		if (getExternalDocSiteLink(enctypeqname) != null) {
			return true;
		}
		TypeDocumentationInfo typeinfo = docTypes.get(enctypeqname);
		if (getExternalDocSiteLink(typeinfo.getInternalName()) != null) {
			return true;
		}
		return typeinfo.getElement() != null && typeinfo.isFieldDocumented(name);
	}

	private boolean isMethodDocumented(TypeElement type, String methodnamedescriptor) {
		String enctypeqname = type.getQualifiedName().toString();
		if (getExternalDocSiteLink(enctypeqname) != null) {
			return true;
		}
		TypeDocumentationInfo typeinfo = docTypes.get(enctypeqname);
		if (typeinfo == null) {
			throw new IllegalArgumentException("Type not found: " + enctypeqname);
		}
		if (getExternalDocSiteLink(typeinfo.getInternalName()) != null) {
			return true;
		}
		return typeinfo.getElement() != null && typeinfo.isMethodDocumented(methodnamedescriptor);
	}

	private static List<TypeElement> getEnclosingTypeElements(Element elem) {
		LinkedList<TypeElement> result = new LinkedList<>();
		while (isTypeElementKind((elem = elem.getEnclosingElement()).getKind())) {
			result.addFirst((TypeElement) elem);
		}
		return result;
	}

	private void writeTypePage(TypeDocumentationInfo typeinfo) throws IOException {
		TypeElement elem = typeinfo.getElement();
		if (elem == null) {
			//the element wasnt set, ignore creating the documentation for it.
			if (getExternalDocSiteLink(typeinfo.getInternalName()) == null) {
				System.out.println("Warning: no element for: " + typeinfo.getInternalName());
			}
			return;
		}
		SakerPath pagepath = getPagePath(elem);

		NavigableMap<String, VariableElement> fields = new TreeMap<>();
		NavigableMap<String, VariableElement> enumconstants = new TreeMap<>();
		NavigableMap<String, ExecutableElement> methods = new TreeMap<>();
		NavigableMap<String, ExecutableElement> constructors = new TreeMap<>();

		NavigableMap<String, TypeElement> types = new TreeMap<>();

		NavigableMap<String, List<LinkedDoc<ExecutableElement>>> methoddocs = new TreeMap<>();
		NavigableMap<String, LinkedDoc<ExecutableElement>> constructordocs = new TreeMap<>();

		for (Element enclosed : elem.getEnclosedElements()) {
			switch (enclosed.getKind()) {
				case ANNOTATION_TYPE:
				case CLASS:
				case ENUM:
				case INTERFACE: {
					//will be written in the enclosing loop
					TypeElement enctype = (TypeElement) enclosed;
					if (isTypeDocumented(enctype)) {
						types.put(enclosed.getSimpleName().toString(), enctype);
					}
					break;
				}

				case CONSTRUCTOR: {
					if (typeinfo.isMethodNameDocumented(enclosed.getSimpleName().toString())) {
						String desc = enclosed.getSimpleName() + getDescriptor((ExecutableElement) enclosed);
						if (typeinfo.isMethodDocumented(desc)) {
							constructors.put(desc, (ExecutableElement) enclosed);
							List<LinkedDoc<ExecutableElement>> constructordocslist = getInitialMethodDocCommentTree(
									(ExecutableElement) enclosed);
							if (!ObjectUtils.isNullOrEmpty(constructordocslist)) {
								if (constructordocslist.size() != 1) {
									throw new AssertionError("Multiple constructor documentations found: " + enclosed);
								}
								constructordocs.put(desc, constructordocslist.get(0));
							}
						}
					}
					break;
				}
				case METHOD: {
					if (typeinfo.isMethodNameDocumented(enclosed.getSimpleName().toString())) {
						String desc = enclosed.getSimpleName() + getDescriptor((ExecutableElement) enclosed);
						if (typeinfo.isMethodDocumented(desc)) {
							methods.put(desc, (ExecutableElement) enclosed);
							methoddocs.put(desc, getInitialMethodDocCommentTree((ExecutableElement) enclosed));
						}
					}
					break;
				}
				case ENUM_CONSTANT: {
					String fname = enclosed.getSimpleName().toString();
					if (typeinfo.isFieldDocumented(fname)) {
						enumconstants.put(fname, (VariableElement) enclosed);
					}
					break;
				}
				case FIELD: {
					String fname = enclosed.getSimpleName().toString();
					if (typeinfo.isFieldDocumented(fname)) {
						fields.put(fname, (VariableElement) enclosed);
					}
					break;
				}
				default: {
					break;
				}
			}
		}

		Map<String, ExecutableElement> hierarchyinheritedmethods = new TreeMap<>();
		Iterable<TypeElement> breadthfirstinheritedtypes = collectInheritedTypesBreadthFirst(elem);
		for (TypeElement supertype : breadthfirstinheritedtypes) {
			for (ExecutableElement method : ElementFilter.methodsIn(supertype.getEnclosedElements())) {
				Set<Modifier> modifiers = method.getModifiers();
				if (modifiers.contains(Modifier.STATIC)) {
					continue;
				}
				if (modifiers.contains(Modifier.PRIVATE)) {
					//private methods are not inherited
					continue;
				}
				String namedescr = method.getSimpleName() + getDescriptor(method);
				if (isMethodDocumented(supertype, namedescr)) {
					hierarchyinheritedmethods.putIfAbsent(namedescr, method);
				}
			}
		}
		//remove all methods from inheritance which are declared in elem
		hierarchyinheritedmethods.keySet().removeAll(methods.keySet());

		StringBuilder out = new StringBuilder();

		out.append("<div class=\"javadoc-class-header\">");
		PackageElement elempackage = elements.getPackageOf(elem);
		if (elempackage != null) {
			out.append("<div class=\"javadoc-class-header-pkg\">");
			out.append(DocCommentHtmlWriter.HTML_SPAN_CODE + DocCommentHtmlWriter.HTML_SPAN_JAVADOC_KEYWORD
					+ "package</span> ");

			out.append(getAHrefTagForElement(pagepath, elempackage));
			out.append(elempackage.getQualifiedName());
			out.append("</a>");

			out.append("</span>");
			out.append("</div>");
		}
		out.append("<div class=\"javadoc-class-header-class\">");
		List<AnnotationMirror> typeelemannotations = DocCommentHtmlWriter.filterDocumentedAnnotations(this,
				elem.getAnnotationMirrors());
		//TODO write annotations for other source elements too. params, methods, fields, return types, etc..
		if (!typeelemannotations.isEmpty()) {
			out.append("<div class=\"javadoc-class-header-class-annots\">");
			writeAnnotations(pagepath, out, typeelemannotations);
			out.append("</div>");
		}

		out.append(DocCommentHtmlWriter.HTML_SPAN_JAVADOC_KEYWORD);
		writeModifiers(elem, out, elem, false);
		writeTypeElementKindKeyword(elem, out);
		out.append("</span>");

		if (elem.getNestingKind() == NestingKind.MEMBER) {
			out.append("<span class=\"javadoc-class-header-name-enclosing\">");
			for (TypeElement enc : getEnclosingTypeElements(elem)) {
				out.append(getAHrefTagForElement(pagepath, enc));
				out.append(enc.getSimpleName());
				out.append("</a>.");
			}
			out.append("</span>");
		}
		out.append("<span class=\"javadoc-class-header-name\">");
		out.append(elem.getSimpleName());
		out.append("</span>");
		List<? extends TypeParameterElement> typeparams = elem.getTypeParameters();
		if (!ObjectUtils.isNullOrEmpty(typeparams)) {
			out.append("&lt;<wbr>");
			for (Iterator<? extends TypeParameterElement> it = typeparams.iterator(); it.hasNext();) {
				TypeParameterElement tpe = it.next();
				out.append(DocCommentHtmlWriter.HTML_SPAN_JAVADOC_TYPEPARAM);
				out.append(tpe.getSimpleName());
				out.append("</span>");
				List<? extends TypeMirror> bounds = tpe.getBounds();
				if (!ObjectUtils.isNullOrEmpty(bounds)) {
					if (bounds.size() != 1 || !this.types.isSameType(bounds.get(0), javaLangObjectType)) {
						out.append("&nbsp;" + DocCommentHtmlWriter.HTML_SPAN_JAVADOC_KEYWORD + "extends</span> ");
						for (Iterator<? extends TypeMirror> bit = bounds.iterator(); bit.hasNext();) {
							TypeMirror bound = bit.next();
							DocCommentHtmlWriter.writeTypeMirror(this, pagepath, bound, out,
									DocCommentHtmlWriter.TM_LINK_TYPES);
							if (bit.hasNext()) {
								out.append("&nbsp;&amp; ");
							}
						}
					}
				}
				if (it.hasNext()) {
					out.append(", ");
				}
			}
			out.append("&gt;");
		}
		TypeMirror sc = elem.getSuperclass();
		ElementKind elementkind = elem.getKind();
		if (sc.getKind() != TypeKind.NONE && elementkind == ElementKind.CLASS) {
			//no superclass for enum, annotation, or interface
			if (sc.getKind() != TypeKind.DECLARED || !this.types.isSameType(sc, javaLangObjectType)) {
				//do not write extends Object
				out.append("&nbsp;" + DocCommentHtmlWriter.HTML_SPAN_JAVADOC_KEYWORD + "extends</span> ");
				DocCommentHtmlWriter.writeTypeMirror(this, pagepath, sc, out, DocCommentHtmlWriter.TM_LINK_TYPES);
			}
		}
		List<? extends TypeMirror> itfs = elem.getInterfaces();
		if (elementkind == ElementKind.ANNOTATION_TYPE) {
			itfs = new ArrayList<>(itfs);
			for (Iterator<? extends TypeMirror> it = itfs.iterator(); it.hasNext();) {
				TypeMirror i = it.next();
				if (this.types.isSameType(i, javaLangAnnotationAnnotationType)) {
					it.remove();
					break;
				}
			}
		}
		if (!ObjectUtils.isNullOrEmpty(itfs)) {
			if (elementkind.isInterface()) {
				out.append("&nbsp;" + DocCommentHtmlWriter.HTML_SPAN_JAVADOC_KEYWORD + "extends</span> ");
			} else {
				out.append("&nbsp;" + DocCommentHtmlWriter.HTML_SPAN_JAVADOC_KEYWORD + "implements</span> ");
			}
			for (Iterator<? extends TypeMirror> it = itfs.iterator(); it.hasNext();) {
				TypeMirror itf = it.next();
				DocCommentHtmlWriter.writeTypeMirror(this, pagepath, itf, out, DocCommentHtmlWriter.TM_LINK_TYPES);
				if (it.hasNext()) {
					out.append(", ");
				}
			}
		}

		out.append("</div>");
		out.append("</div>");

		System.out.println(elem.getQualifiedName());

		Map<String, DeclaredType> alltypesinhierarchy = collectAllSuperTypesAndElement(elem);

		if (elem.getSuperclass().getKind() == TypeKind.NONE && elem.getInterfaces().isEmpty()) {
			//don't write super hierarchy
		} else {
			out.append("<div class=\"javadoc-class-super-hierarchy\">");
			writeSuperTypeHierarchy((DeclaredType) elem.asType(), alltypesinhierarchy, out, pagepath);
			out.append("</div>");
		}

		Collection<TypeDocumentationInfo> allsubtypes = getAllSubTypes(typeinfo);
		if (!allsubtypes.isEmpty()) {
			out.append("<div class=\"javadoc-class-subtypes\">");
			out.append("<div class=\"javadoc-class-subtypes-list\">");
			out.append(DocCommentHtmlWriter.HTML_SPAN_CODE);
			for (Iterator<TypeDocumentationInfo> it = allsubtypes.iterator(); it.hasNext();) {
				TypeDocumentationInfo subc = it.next();
				TypeElement subelem = subc.getElement();
				out.append("<span class=\"javadoc-subtype\">");
				DocCommentHtmlWriter.writeTypeMirror(this, pagepath, subelem.asType(), out,
						DocCommentHtmlWriter.TM_LINK_TYPES | DocCommentHtmlWriter.TM_NO_CODE_SPAN_CLASS);
				out.append("</span>");
				if (it.hasNext()) {
					out.append(", ");
				}
			}
			out.append("</span>");
			out.append("</div>");
			out.append("</div>");
		}

//		if (elem.getKind() == ElementKind.CLASS) {
//			Collection<TypeDocumentationInfo> subclasses = getAllSubClasses(typeinfo);
//			if (!subclasses.isEmpty()) {
//				out.append("<div class=\"javadoc-class-subclasses\">");
//				out.append(DocCommentHtmlWriter.HTML_SPAN_CODE);
//				for (Iterator<TypeDocumentationInfo> it = subclasses.iterator(); it.hasNext();) {
//					TypeDocumentationInfo subc = it.next();
//					TypeElement subelem = subc.getElement();
//					DocCommentHtmlWriter.writeTypeMirror(this, pagepath, subelem.asType(), out,
//							DocCommentHtmlWriter.TM_LINK_TYPES | DocCommentHtmlWriter.TM_NO_CODE_SPAN_CLASS);
//					if (it.hasNext()) {
//						out.append(", ");
//					}
//				}
//				out.append("</span>");
//				out.append("</div>");
//			}
//		}
//		if (elem.getKind().isInterface()) {
//			Collection<TypeDocumentationInfo> allitfimplementations = getAllInterfaceImplementations(typeinfo);
//			if (!allitfimplementations.isEmpty()) {
//				out.append("<div class=\"javadoc-class-itf-impls\">");
//				boolean writtenclassheaders = false;
//				for (Iterator<TypeDocumentationInfo> it = allitfimplementations.iterator(); it.hasNext();) {
//					TypeDocumentationInfo itfimpl = it.next();
//					TypeElement itfelem = itfimpl.getElement();
//					if (!itfelem.getKind().isClass()) {
//						continue;
//					}
//					if (!writtenclassheaders) {
//						out.append("<div class=\"javadoc-class-itf-impls-classes\">");
//						out.append(DocCommentHtmlWriter.HTML_SPAN_CODE);
//						writtenclassheaders = true;
//					} else {
//						//there was a previous
//						out.append(", ");
//					}
//					DocCommentHtmlWriter.writeTypeMirror(this, pagepath, itfelem.asType(), out,
//							DocCommentHtmlWriter.TM_LINK_TYPES | DocCommentHtmlWriter.TM_NO_CODE_SPAN_CLASS);
//				}
//				if (writtenclassheaders) {
//					out.append("</span>");
//					out.append("</div>");
//				}
//				boolean writtenitfheaders = false;
//				for (Iterator<TypeDocumentationInfo> it = allitfimplementations.iterator(); it.hasNext();) {
//					TypeDocumentationInfo itfimpl = it.next();
//					TypeElement itfelem = itfimpl.getElement();
//					if (!itfelem.getKind().isInterface()) {
//						continue;
//					}
//					if (!writtenitfheaders) {
//						out.append("<div class=\"javadoc-class-itf-impls-itfs\">");
//						out.append(DocCommentHtmlWriter.HTML_SPAN_CODE);
//						writtenitfheaders = true;
//					} else {
//						//there was a previous
//						out.append(", ");
//					}
//					DocCommentHtmlWriter.writeTypeMirror(this, pagepath, itfelem.asType(), out,
//							DocCommentHtmlWriter.TM_LINK_TYPES | DocCommentHtmlWriter.TM_NO_CODE_SPAN_CLASS);
//				}
//				if (writtenitfheaders) {
//					out.append("</span>");
//					out.append("</div>");
//				}
//				out.append("</div>");
//			}
//		}
		out.append("<div class=\"javadoc-class-doc\">");
		DocCommentTree doctree = trees.getDocCommentTree(elem);
		if (doctree != null) {
			DocCommentHtmlWriter writer = new DocCommentHtmlWriter(this, trees, out, trees.getPath(elem), doctree,
					pagepath);

			DeprecatedTree deprtree = findDeprecatedTree(doctree, elem);
			writeDeprecatedTagForElement(pagepath, out, new ElementDocTree<DeprecatedTree>(elem, doctree, deprtree),
					elements.isDeprecated(elem), elem);

			out.append("<div class=\"javadoc-class-doc-body\">");
			for (DocTree t : doctree.getFirstSentence()) {
				writer.write(t);
			}
			List<? extends DocTree> body = doctree.getBody();
			if (!ObjectUtils.isNullOrEmpty(body)) {
				out.append(' ');
			}
			for (DocTree t : body) {
				writer.write(t);
			}
			out.append("</div>");
			writeTypeParameterDetails(elem, out, doctree, pagepath);
			writeSeeTagDetails(elem, doctree, out, pagepath, elem);
			writeSinceTagDetails(elem, doctree, out, pagepath, elem);
		} else {
			System.out.println("Missing documentation for type: " + elem);
		}
		out.append("</div>");

		out.append("<div class=\"javadoc-class-idx\">");
		{
			if (!types.isEmpty()) {
				out.append("<div class=\"javadoc-class-idx-types javadoc-idx-section\">");
				out.append("<table>");
				out.append("<caption>Nested types</caption>");
				out.append("<thead>");
				out.append("<tr>");

				out.append("<th class=\"javadoc-th-type-meta\"></th>");
				out.append("<th class=\"javadoc-th-type-details\"></th>");

				out.append("</tr>");
				out.append("</thead>");
				out.append("<tbody>");
				for (Entry<String, TypeElement> entry : types.entrySet()) {
					TypeElement nestedtype = entry.getValue();
					DocCommentTree typedoctree = trees.getDocCommentTree(nestedtype);

					out.append("<tr class=\"javadoc-class-idx-type javadoc-idx-member\">");
					out.append("<td class=\"javadoc-class-idx-field-sig javadoc-idx-member-meta\">");
					out.append(DocCommentHtmlWriter.HTML_SPAN_JAVADOC_KEYWORD);
					writeModifiers(nestedtype, out, elem, false);
					writeTypeElementKindKeyword(nestedtype, out);
					out.append("</span>");
					out.append("</td>");

					out.append("<td class=\"javadoc-class-idx-type-sig javadoc-idx-member-sig\">");
					out.append("<div class=\"javadoc-class-idx-type-name javadoc-idx-member-name\">");
					out.append(getAHrefTagForElement(pagepath, nestedtype));
					out.append(entry.getKey());
					out.append("</a>");
					out.append("</div>");
					if (typedoctree != null) {
						out.append("<div class=\"javadoc-class-idx-type-short javadoc-member-short\">");
						writeIndexFirstSentence(pagepath, out, nestedtype,
								new LinkedDoc<>(nestedtype, typedoctree, LinkedDocRelation.DIRECT));
						out.append("</div>");
					}
					out.append("</td>");
					out.append("</tr>");
				}
				out.append("</tbody>");
				out.append("</table>");
				out.append("</div>");
			}

			if (!enumconstants.isEmpty()) {
				out.append("<div class=\"javadoc-class-idx-enumconstants javadoc-idx-section\">");
				out.append("<table>");
				out.append("<caption>Enumeration constants</caption>");
				out.append("<thead>");
				out.append("<tr>");

				out.append("<th class=\"javadoc-th-type-enum\"></th>");

				out.append("</tr>");
				out.append("</thead>");

				out.append("<tbody>");
				for (Entry<String, VariableElement> entry : enumconstants.entrySet()) {
					VariableElement ve = entry.getValue();
					DocCommentTree enumdoctree = trees.getDocCommentTree(ve);

					out.append("<tr class=\"javadoc-class-idx-enum javadoc-idx-member\">");
					out.append("<td class=\"javadoc-class-idx-enum-sig javadoc-idx-member-sig\">");
					out.append("<div class=\"javadoc-class-idx-enum-name javadoc-idx-member-name\">");
					out.append(getAHrefTagForElement(pagepath, ve));
					out.append(ve.getSimpleName());
					out.append("</a>");
					out.append("</div>");
					if (enumdoctree != null) {
						out.append("<div class=\"javadoc-class-idx-enum-short javadoc-member-short\">");
						writeIndexFirstSentence(pagepath, out, ve,
								new LinkedDoc<>(ve, enumdoctree, LinkedDocRelation.DIRECT));
						out.append("</div>");
					}
					out.append("</td>");
					out.append("</tr>");
				}
				out.append("</tbody>");
				out.append("</table>");
				out.append("</div>");
			}

			if (!fields.isEmpty()) {
				out.append("<div class=\"javadoc-class-idx-fields javadoc-idx-section\">");
				out.append("<table>");
				out.append("<caption>Fields</caption>");
				out.append("<thead>");
				out.append("<tr>");

				out.append("<th class=\"javadoc-th-field-meta\"></th>");
				out.append("<th class=\"javadoc-th-field-details\"></th>");

				out.append("</tr>");
				out.append("</thead>");
				out.append("<tbody>");
				for (Entry<String, VariableElement> entry : fields.entrySet()) {
					VariableElement fe = entry.getValue();
					DocCommentTree fielddoctree = trees.getDocCommentTree(fe);

					out.append("<tr class=\"javadoc-class-idx-field javadoc-idx-member\">");
					out.append("<td class=\"javadoc-class-idx-field-sig javadoc-idx-member-meta\">");
					writeModifiers(fe, out, elem);
					DocCommentHtmlWriter.writeTypeMirror(this, pagepath, fe.asType(), out,
							DocCommentHtmlWriter.TM_LINK_TYPES);
					out.append("</td>");
					out.append("<td class=\"javadoc-class-idx-field-sig javadoc-idx-member-sig\">");
					out.append("<div class=\"javadoc-class-idx-field-name javadoc-idx-member-name\">");
					out.append(getAHrefTagForElement(pagepath, fe));
					out.append(fe.getSimpleName());
					out.append("</a>");
					Object constval = fe.getConstantValue();
					if (constval != null) {
						out.append("<span class=\"javadoc-constexpr\"> = <span class=\"javadoc-constval\">");
						out.append(escapeHtml(elements.getConstantExpression(constval)));
						out.append("</span></span>");
						constants.add(fe);
					}
					out.append("</div>");
					if (fielddoctree != null) {
						out.append("<div class=\"javadoc-class-idx-field-short javadoc-member-short\">");
						writeIndexFirstSentence(pagepath, out, fe,
								new LinkedDoc<>(fe, fielddoctree, LinkedDocRelation.DIRECT));
						out.append("</div>");
					}
					out.append("</td>");
					out.append("</tr>");
				}
				out.append("</tbody>");
				out.append("</table>");
				out.append("</div>");
			}

			if (!constructors.isEmpty()) {
				out.append("<div class=\"javadoc-class-idx-constructors javadoc-idx-section\">");
				out.append("<table>");
				out.append("<caption>Constructors</caption>");
				out.append("<thead>");
				out.append("<tr>");

				out.append("<th class=\"javadoc-th-constructor-meta\"></th>");
				out.append("<th class=\"javadoc-th-constructor-details\"></th>");

				out.append("</tr>");
				out.append("</thead>");

				out.append("<tbody>");
				for (Entry<String, ExecutableElement> entry : constructors.entrySet()) {
					ExecutableElement ee = entry.getValue();
					LinkedDoc<ExecutableElement> methoddoctree = constructordocs.get(entry.getKey());

					out.append("<tr class=\"javadoc-class-idx-constructor javadoc-idx-member\">");
					out.append("<td class=\"javadoc-class-idx-constructor-meta javadoc-idx-member-meta\">");
					writeMethodIndexMeta(pagepath, ee, out, false);
					out.append("</td>");
					out.append("<td class=\"javadoc-class-idx-constructor-sig javadoc-idx-member-sig\">");
					out.append("<div class=\"javadoc-class-idx-constructor-name javadoc-idx-member-name\">");
					writeMethodIndexSignature(pagepath, ee, out, false);
					out.append("</div>");
					if (methoddoctree != null) {
						out.append("<div class=\"javadoc-class-idx-constructor-short javadoc-member-short\">");
						writeIndexFirstSentence(pagepath, out, ee, methoddoctree);
						out.append("</div>");
					}
					out.append("</td>");
					out.append("</tr>");
				}
				out.append("</tbody>");
				out.append("</table>");
				out.append("</div>");
			}

			if (!methods.isEmpty()) {
				out.append("<div class=\"javadoc-class-idx-methods javadoc-idx-section\">");
				out.append("<table>");
				out.append("<caption>Methods</caption>");
				out.append("<thead>");
				out.append("<tr>");

				out.append("<th class=\"javadoc-th-method-meta\"></th>");
				out.append("<th class=\"javadoc-th-method-details\"></th>");

				out.append("</tr>");
				out.append("</thead>");

				out.append("<tbody>");
				for (Entry<String, ExecutableElement> entry : methods.entrySet()) {
					ExecutableElement ee = entry.getValue();
					List<LinkedDoc<ExecutableElement>> methoddocinfos = methoddocs.get(entry.getKey());

					out.append("<tr class=\"javadoc-class-idx-method javadoc-idx-member\">");
					out.append("<td class=\"javadoc-class-idx-method-meta javadoc-idx-member-meta\">");
					writeMethodIndexMeta(pagepath, ee, out, false);
					out.append("</td>");
					out.append("<td class=\"javadoc-class-idx-method-sig javadoc-idx-member-sig\">");

					out.append("<div class=\"javadoc-class-idx-method-name javadoc-idx-member-name\">");
					writeMethodIndexSignature(pagepath, ee, out, false);
					out.append("</div>");

					out.append("<div class=\"javadoc-class-idx-method-short javadoc-member-short\">");
					LinkedDoc<ExecutableElement> methoddocinfo = getFirstDocumentedDoc(methoddocinfos);
					if (methoddocinfo != null) {
						writeIndexFirstSentence(pagepath, out, ee, methoddocinfo);
					} else {
						LinkedDoc<ExecutableElement> inheriteddoc = getSecondDoc(methoddocinfos);
						if (inheriteddoc != null) {
							appendInheritedDocumentationNotFoundFirstSentence(inheriteddoc.getDocElement(), out,
									pagepath, methoddocinfos);
						} else if (isEnumValueOfMethod(ee)) {
							out.append("Returns the enum constant of this type with the specified name.");
						} else if (isEnumValuesMethod(ee)) {
							out.append(
									"Returns an array containing the constants of this enum type, in the order they are declared.");
						} else {
							System.out.println("Warning: undocumented: " + ee.getEnclosingElement() + "." + ee + " in "
									+ methoddocinfos);
						}
					}
					out.append("</div>");

					out.append("</td>");
					out.append("</tr>");
				}
				out.append("</tbody>");
				out.append("</table>");
				out.append("</div>");
			}

			if (!hierarchyinheritedmethods.isEmpty()) {
				Map<TypeElement, Map<String, ExecutableElement>> typedinheritedmethods = new HashMap<>();
				for (Entry<String, ExecutableElement> entry : hierarchyinheritedmethods.entrySet()) {
					ExecutableElement inheritedelem = entry.getValue();
					typedinheritedmethods.computeIfAbsent((TypeElement) inheritedelem.getEnclosingElement(),
							Functionals.treeMapComputer()).put(entry.getKey(), inheritedelem);
				}
				if (elementkind.isInterface()) {
					//do not include inherited methods from the Object.class if the type is an interface.
					typedinheritedmethods.remove(javaLangObjectElement);
				}
				if (!typedinheritedmethods.isEmpty()) {
					out.append("<div class=\"javadoc-class-idx-inherited-methods javadoc-idx-section\">");
					out.append("<table>");
					out.append("<caption>Inherited methods</caption>");

					out.append("<tbody>");
					for (TypeElement inhtype : breadthfirstinheritedtypes) {
						Map<String, ExecutableElement> inhmethods = typedinheritedmethods.remove(inhtype);
						if (inhmethods != null) {
							out.append("<tr class=\"javadoc-class-idx-inherited-methods-entry\">");
							out.append("<td>");
							out.append("<div class=\"javadoc-class-idx-inherited-methods-title\">");
							out.append("From: ");
							DeclaredType inherittypemirror = alltypesinhierarchy
									.get(inhtype.getQualifiedName().toString());
							if (inherittypemirror == null) {
								throw new AssertionError("Inherited type not found: " + inhtype + " for " + elem);
							}
							out.append("<span class=\"" + DocCommentHtmlWriter.HTML_CLASS_CODE_SPAN + "\">");
							DocCommentHtmlWriter.writeTypeMirror(this, pagepath, inherittypemirror, out,
									DocCommentHtmlWriter.TM_LINK_TYPES | DocCommentHtmlWriter.TM_NO_CODE_SPAN_CLASS);
							out.append("</span>");
							out.append("</div>");
							out.append("<div class=\"javadoc-class-idx-inherited-methods-list\">");
							out.append(DocCommentHtmlWriter.HTML_SPAN_CODE);
							for (Iterator<Entry<String, ExecutableElement>> it = inhmethods.entrySet().iterator(); it
									.hasNext();) {
								Entry<String, ExecutableElement> entry = it.next();
								ExecutableElement inhee = entry.getValue();
								out.append(getAHrefTagForElement(pagepath, inhee));
								out.append(inhee.getSimpleName());
								out.append("</a>");
								if (it.hasNext()) {
									out.append(", ");
								}
							}
							out.append("</span>");
							out.append("</div>");
							out.append("</td>");
							out.append("</tr>");
						}
					}
					if (!typedinheritedmethods.isEmpty()) {
						throw new AssertionError("Unhandled inherited methods: " + typedinheritedmethods);
					}
					out.append("</tbody>");
					out.append("</table>");
					out.append("</div>");
				}
			}
		}
		out.append("</div>");

		out.append("<div class=\"javadoc-class-details\">");
		{

			if (!enumconstants.isEmpty()) {
				out.append("<div class=\"javadoc-class-details-enumconstants javadoc-details-section\">");
				for (Entry<String, VariableElement> entry : enumconstants.entrySet()) {
					VariableElement ve = entry.getValue();
					DocCommentTree enumdoctree = trees.getDocCommentTree(ve);

					out.append("<div id=\"" + getFieldHtmlIdentifier(ve)
							+ "\" class=\"javadoc-class-detail-enum javadoc-class-detail-member\">");
					out.append("<div class=\"javadoc-class-detail-enum-sig javadoc-member-sig\">");
					writeEnumDetailsSignature(pagepath, out, ve);
					out.append("</div>");
					if (enumdoctree != null) {
						out.append("<div class=\"javadoc-class-detail-enum-detail javadoc-member-detail\">");
						writeFieldEnumConstDetail(pagepath, out, ve, enumdoctree);
						out.append("</div>");
					}
					out.append("</div>");
				}
				out.append("</div>");
			}

			if (!fields.isEmpty()) {
				out.append("<div class=\"javadoc-class-details-fields javadoc-details-section\">");
				for (Entry<String, VariableElement> entry : fields.entrySet()) {
					VariableElement fe = entry.getValue();
					DocCommentTree fielddoctree = trees.getDocCommentTree(fe);

					out.append("<div id=\"" + getFieldHtmlIdentifier(fe)
							+ "\" class=\"javadoc-class-detail-field javadoc-class-detail-member\">");
					out.append("<div class=\"javadoc-class-detail-field-sig javadoc-member-sig\">");
					writeFieldDetailsSignature(elem, pagepath, out, fe);
					out.append("</div>");
					if (fielddoctree != null) {
						out.append("<div class=\"javadoc-class-detail-field-detail javadoc-member-detail\">");
						writeFieldEnumConstDetail(pagepath, out, fe, fielddoctree);
						out.append("</div>");
					}
					out.append("</div>");
				}
				out.append("</div>");
			}

			if (!constructors.isEmpty()) {
				out.append("<div class=\"javadoc-class-details-constructors javadoc-details-section\">");
				for (Entry<String, ExecutableElement> entry : constructors.entrySet()) {
					ExecutableElement ee = entry.getValue();
					LinkedDoc<ExecutableElement> eedoctree = constructordocs.get(entry.getKey());

					out.append("<div id=\"" + getMethodHtmlIdentifier(ee)
							+ "\" class=\"javadoc-class-detail-constructor javadoc-class-detail-member\">");

					out.append("<div class=\"javadoc-class-detail-constructor-sig javadoc-member-sig\">");
					writeMethodConstructorDetailSignature(pagepath, out, ee);
					out.append("</div>");

					if (eedoctree != null) {
						out.append("<div class=\"javadoc-class-detail-constructor-detail javadoc-member-detail\">");
						writeConstructorDetail(pagepath, out, ee, eedoctree);
						out.append("</div>");
					}

					out.append("</div>");
				}
				out.append("</div>");
			}

			if (!methods.isEmpty()) {
				out.append("<div class=\"javadoc-class-details-methods javadoc-details-section\">");
				for (Entry<String, ExecutableElement> entry : methods.entrySet()) {
					ExecutableElement ee = entry.getValue();
					List<LinkedDoc<ExecutableElement>> methoddocinfos = methoddocs.get(entry.getKey());

					out.append("<div id=\"" + getMethodHtmlIdentifier(ee)
							+ "\" class=\"javadoc-class-detail-method javadoc-class-detail-member\">");

					out.append("<div class=\"javadoc-class-detail-method-sig javadoc-member-sig\">");
					writeMethodConstructorDetailSignature(pagepath, out, ee);
					out.append("</div>");

					out.append("<div class=\"javadoc-class-detail-method-detail javadoc-member-detail\">");
					writeMethodDetails(pagepath, out, methoddocinfos, ee);
					out.append("</div>");

					out.append("</div>");
				}
				out.append("</div>");
			}
		}
		out.append("</div>");

		writePageContents(pagepath, out, elem.getSimpleName().toString(), elementkind, elem.getSimpleName().toString(),
				this.template);
	}

	private LinkedDoc<ExecutableElement> getDocCommentTreeWalkSeeTags(ExecutableElement ee) {
		return getDocCommentTreeWalkSeeTags(ee, LinkedDocRelation.DIRECT);
	}

	private Element getSeeTagOnlyRefElement(DocCommentTree dtree, Element ee) {
		if (!ObjectUtils.isNullOrEmpty(dtree.getFirstSentence())) {
			return null;
		}
		if (!ObjectUtils.isNullOrEmpty(dtree.getBody())) {
			return null;
		}
		List<? extends DocTree> btags = dtree.getBlockTags();
		if (btags == null || btags.size() != 1) {
			return null;
		}
		DocTree blocktag = btags.get(0);
		if (blocktag.getKind() != Kind.SEE) {
			return null;
		}
		SeeTree seetree = (SeeTree) blocktag;
		List<? extends DocTree> refs = seetree.getReference();
		if (refs == null || refs.size() != 1) {
			return null;
		}

		//single @see tag in the whole comment
		DocTree firstref = refs.get(0);
		DocTreePath treepath = DocTreePath.getPath(trees.getPath(ee), dtree, firstref);
		Element linkelem = dePseudoize(trees.getElement(treepath));
		if (linkelem == null) {
			throw new IllegalArgumentException("Referenced element not found: " + firstref + " in "
					+ treepath.getTreePath().getCompilationUnit().getSourceFile());
		}
		return linkelem;
	}

	private LinkedDoc<ExecutableElement> getDocCommentTreeWalkSeeTags(ExecutableElement ee,
			LinkedDocRelation selfrelation) {
		LinkedDoc<ExecutableElement> dtlink = getDocCommentTreeWithPseudo(ee, selfrelation);
		DocCommentTree dtree = dtlink.getDocTree();
		if (dtree == null) {
			List<LinkedDoc<ExecutableElement>> inheriteds = collectInheritedDocumentations(ee);
			if (ObjectUtils.isNullOrEmpty(inheriteds)) {
				return dtlink;
			}
			for (LinkedDoc<ExecutableElement> inheriteddoc : inheriteds) {
				if (inheriteddoc.getDocTree() != null) {
					Element seelink = getSeeTagOnlyRefElement(inheriteddoc.getDocTree(), inheriteddoc.getDocElement());
					if (seelink == null) {
						continue;
					}
					if (seelink.getKind() == ee.getKind()) {
						ExecutableElement linkedexec = (ExecutableElement) seelink;
						return getDocCommentTreeWalkSeeTags(linkedexec, LinkedDocRelation.LINKED);
					}
				}
			}
			return dtlink;
		}
		if (!ObjectUtils.isNullOrEmpty(dtree.getFirstSentence()) || !ObjectUtils.isNullOrEmpty(dtree.getBody())) {
			return dtlink;
		}
		List<? extends DocTree> btags = dtree.getBlockTags();
		if (btags == null || btags.size() != 1) {
			return dtlink;
		}
		DocTree blocktag = btags.get(0);
		if (blocktag.getKind() != Kind.SEE) {
			return dtlink;
		}
		SeeTree seetree = (SeeTree) blocktag;
		List<? extends DocTree> refs = seetree.getReference();
		if (refs == null || refs.size() != 1) {
			return dtlink;
		}

		//single @see tag in the whole comment
		DocTree firstref = refs.get(0);
		DocTreePath treepath = DocTreePath.getPath(trees.getPath(ee), dtree, firstref);
		Element linkelem = trees.getElement(treepath);
		if (linkelem == null) {
			throw new IllegalArgumentException("Referenced element not found: " + firstref + " in "
					+ treepath.getTreePath().getCompilationUnit().getSourceFile());
		}
		if (linkelem.getKind() == ee.getKind()) {
			ExecutableElement linkedexec = (ExecutableElement) linkelem;
			return getDocCommentTreeWalkSeeTags(linkedexec, LinkedDocRelation.LINKED);
		}
		throw new IllegalArgumentException("Different linked doc kind: " + linkelem.getKind() + " for " + ee + " in "
				+ treepath.getTreePath().getCompilationUnit().getSourceFile());
	}

	private void writeFieldDetailsSignature(TypeElement elem, SakerPath pagepath, Appendable out, VariableElement fe)
			throws IOException {
		List<AnnotationMirror> annots = DocCommentHtmlWriter.filterDocumentedAnnotations(this,
				fe.getAnnotationMirrors());
		if (!ObjectUtils.isNullOrEmpty(annots)) {
			out.append("<div class=\"javadoc-field-annots javadoc-member-annots\">");
			writeAnnotations(pagepath, out, annots);
			out.append("</div>");
		}
		writeModifiers(fe, out, elem);
		DocCommentHtmlWriter.writeTypeMirror(this, pagepath, fe.asType(), out, DocCommentHtmlWriter.TM_LINK_TYPES);
		out.append(' ');

		out.append(getAHrefTagForElement(pagepath, fe, ImmutableUtils.singletonMap("class", "javadoc-sig-name")));
		out.append(fe.getSimpleName());
		out.append("</a>");
		Object constval = fe.getConstantValue();
		if (constval != null) {
			out.append("<span class=\"javadoc-constexpr\"> = <span class=\"javadoc-constval\">");
			out.append(escapeHtml(elements.getConstantExpression(constval)));
			out.append("</span></span>");
		}
	}

	private void writeEnumDetailsSignature(SakerPath pagepath, Appendable out, VariableElement ve) throws IOException {
		List<AnnotationMirror> annots = DocCommentHtmlWriter.filterDocumentedAnnotations(this,
				ve.getAnnotationMirrors());
		if (!ObjectUtils.isNullOrEmpty(annots)) {
			out.append("<div class=\"javadoc-enum-annots javadoc-member-annots\">");
			writeAnnotations(pagepath, out, annots);
			out.append("</div>");
		}

		out.append(getAHrefTagForElement(pagepath, ve, ImmutableUtils.singletonMap("class", "javadoc-sig-name")));
		out.append(ve.getSimpleName());
		out.append("</a>");
	}

	public void writeAnnotations(SakerPath pagepath, Appendable out,
			List<? extends AnnotationMirror> typeelemannotations) throws IOException {
		AnnotationValueWriter writer = new AnnotationValueWriter(this, pagepath);
		for (AnnotationMirror am : typeelemannotations) {
			out.append("<div class=\"javadoc-annot\">");
			writer.visitAnnotation(am, out);
			out.append("</div>");
		}
	}

	private void writeSuperTypeHierarchyImpl(DeclaredType type, Map<String, DeclaredType> alltypesinhierarchy,
			Appendable out, SakerPath pagepath, Set<String> writtentypes) throws IOException {
		TypeElement elem = (TypeElement) type.asElement();
		String typeqname = elem.getQualifiedName().toString();
		if (!writtentypes.add(typeqname)) {
			//type was already written
			return;
		}
		TypeMirror sc = elem.getSuperclass();
		List<? extends TypeMirror> interfaces = elem.getInterfaces();
		DeclaredType elemdeclaredtype = alltypesinhierarchy.get(typeqname);
		if (elemdeclaredtype == null) {
			throw new AssertionError("Inherited type not found: " + elem + " in " + alltypesinhierarchy);
		}

		out.append("<div class=\"javadoc-class-super-hierarchy-entry\">");
		DocCommentHtmlWriter.writeTypeMirror(this, pagepath, elemdeclaredtype, out, DocCommentHtmlWriter.TM_LINK_TYPES
				| DocCommentHtmlWriter.TM_FULLY_QUALIFIED | DocCommentHtmlWriter.TM_PARAM_TYPES_SIMPLE);
		if (sc.getKind() != TypeKind.NONE) {
			writeSuperTypeHierarchyImpl((DeclaredType) sc, alltypesinhierarchy, out, pagepath, writtentypes);
		}
		for (TypeMirror itf : interfaces) {
			writeSuperTypeHierarchyImpl((DeclaredType) itf, alltypesinhierarchy, out, pagepath, writtentypes);
		}
		out.append("</div>");
	}

	private void writeSuperTypeHierarchy(DeclaredType type, Map<String, DeclaredType> alltypesinhierarchy,
			Appendable out, SakerPath pagepath) throws IOException {
		writeSuperTypeHierarchyImpl(type, alltypesinhierarchy, out, pagepath, new TreeSet<>());
	}

	public static void writeTypeElementKindKeyword(TypeElement elem, Appendable out) throws IOException {
		switch (elem.getKind()) {
			case CLASS: {
				out.append("class ");
				break;
			}
			case ENUM: {
				out.append("enum ");
				break;
			}
			case ANNOTATION_TYPE: {
				out.append("@interface ");
				break;
			}
			case INTERFACE: {
				out.append("interface ");
				break;
			}
			default: {
				throw new UnsupportedOperationException(elem.getKind().toString());
			}
		}
	}

	private boolean isEnumValueOfMethod(ExecutableElement ee) {
		if (!ee.getSimpleName().contentEquals("valueOf")) {
			return false;
		}
		if (!ee.getModifiers().contains(Modifier.STATIC)) {
			return false;
		}
		Element enclosing = ee.getEnclosingElement();
		if (enclosing.getKind() != ElementKind.ENUM) {
			return false;
		}
		List<? extends VariableElement> params = ee.getParameters();
		if (params.size() != 1) {
			return false;
		}
		if (!types.isSameType(params.get(0).asType(), javaLangStringType)) {
			return false;
		}
		if (!types.isSameType(ee.getReturnType(), enclosing.asType())) {
			return false;
		}
		return true;
	}

	private boolean isEnumValuesMethod(ExecutableElement ee) {
		if (!ee.getSimpleName().contentEquals("values")) {
			return false;
		}
		if (!ee.getModifiers().contains(Modifier.STATIC)) {
			return false;
		}
		Element enclosing = ee.getEnclosingElement();
		if (enclosing.getKind() != ElementKind.ENUM) {
			return false;
		}
		if (!ee.getParameters().isEmpty()) {
			return false;
		}
		TypeMirror rettype = ee.getReturnType();
		if (rettype.getKind() != TypeKind.ARRAY) {
			return false;
		}
		ArrayType at = (ArrayType) rettype;
		if (!types.isSameType(at.getComponentType(), enclosing.asType())) {
			return false;
		}
		return true;
	}

	private void writeFieldEnumConstDetail(SakerPath pagepath, Appendable out, VariableElement fe,
			DocCommentTree fielddoctree) throws IOException {
		DeprecatedTree deprtree = findDeprecatedTree(fielddoctree, fe);
		writeDeprecatedTagForElement(pagepath, out, new ElementDocTree<>(fe, fielddoctree, deprtree),
				elements.isDeprecated(fe), fe);

		DocCommentHtmlWriter writer = new DocCommentHtmlWriter(this, trees, out, trees.getPath(fe), fielddoctree,
				pagepath);
		out.append("<div class=\"javadoc-detail-body\">");
		for (DocTree t : fielddoctree.getFirstSentence()) {
			writer.write(t);
		}
		List<? extends DocTree> body = fielddoctree.getBody();
		if (!ObjectUtils.isNullOrEmpty(body)) {
			out.append(' ');
		}
		for (DocTree t : body) {
			writer.write(t);
		}
		out.append("</div>");
		writeSeeTagDetails(fe, fielddoctree, out, pagepath, fe);
		writeSinceTagDetails(fe, fielddoctree, out, pagepath, fe);
	}

	private void writeConstructorDetail(SakerPath pagepath, Appendable out, ExecutableElement constructor,
			LinkedDoc<ExecutableElement> doc) throws IOException {
		ExecutableElement ee = doc.getDocElement();
		DocCommentTree eedoctree = doc.getDocTree();
		if (eedoctree == null) {
			System.out.println("Warning: Constructor documentation is missing: " + constructor);
			return;
		}

		if (doc.getRelation() == LinkedDocRelation.LINKED) {
			writeDetailLinkNote(doc.getAssociatedElement(), out, pagepath);
		}

		DeprecatedTree deprtree = findDeprecatedTree(eedoctree, ee);
		writeDeprecatedTagForElement(pagepath, out, new ElementDocTree<>(ee, eedoctree, deprtree),
				elements.isDeprecated(ee), ee);

		DocCommentHtmlWriter writer = new DocCommentHtmlWriter(this, trees, out, trees.getPath(ee), eedoctree,
				pagepath);
		out.append("<div class=\"javadoc-detail-body\">");
		for (DocTree t : eedoctree.getFirstSentence()) {
			writer.write(t);
		}
		//append a space so the sentences don't run into each other
		List<? extends DocTree> body = eedoctree.getBody();
		if (!ObjectUtils.isNullOrEmpty(body)) {
			out.append(' ');
		}
		for (DocTree t : body) {
			writer.write(t);
		}
		out.append("</div>");
		//TODO block tags
		writeParameterMethodDetails(ee, doc, out, pagepath);
		writeThrowsMethodDetails(Collections.singletonList(doc), out, pagepath, ee);
		writeSeeTagDetails(ee, eedoctree, out, pagepath, ee);
		writeSinceTagDetails(ee, eedoctree, out, pagepath, ee);
	}

	private static LinkedDoc<ExecutableElement> getSecondDoc(List<LinkedDoc<ExecutableElement>> docinfos) {
		if (docinfos.size() > 1) {
			return docinfos.get(1);
		}
		return null;
	}

	public static LinkedDoc<ExecutableElement> getFirstDocumentedDoc(List<LinkedDoc<ExecutableElement>> docinfos) {
		if (docinfos == null) {
			return null;
		}
		for (LinkedDoc<ExecutableElement> d : docinfos) {
			if (d.getDocTree() != null) {
				return d;
			}
		}
		return null;
	}

	private static ElementDocTree<DeprecatedTree> getDeprecatedTree(List<LinkedDoc<ExecutableElement>> methoddocinfos) {
		for (LinkedDoc<ExecutableElement> docinfo : methoddocinfos) {
			DocCommentTree dt = docinfo.getDocTree();
			if (dt == null) {
				continue;
			}
			DeprecatedTree result = null;
			for (DocTree bt : dt.getBlockTags()) {
				if (bt.getKind() != Kind.DEPRECATED) {
					continue;
				}
				if (result != null) {
					throw new IllegalArgumentException("Multiple @deprecated tag in: "
							+ docinfo.getDocElement().getEnclosingElement() + "." + docinfo.getDocElement());
				}
				result = (DeprecatedTree) bt;
			}
			if (result != null) {
				return new ElementDocTree<>(docinfo.getDocElement(), dt, result);
			}
		}
		return null;
	}

	private boolean isDeprecated(List<LinkedDoc<ExecutableElement>> methoddocinfos) {
		for (LinkedDoc<ExecutableElement> docinfo : methoddocinfos) {
			if (elements.isDeprecated(docinfo.getDocElement())) {
				return true;
			}
		}
		return false;
	}

	private void writeMethodDetails(SakerPath pagepath, Appendable out,
			List<LinkedDoc<ExecutableElement>> methoddocinfos, ExecutableElement ee) throws IOException {
		ElementDocTree<DeprecatedTree> deprtree = getDeprecatedTree(methoddocinfos);
		boolean elemdeprecated = isDeprecated(methoddocinfos);
		writeDeprecatedTagForElement(pagepath, out, deprtree, elemdeprecated, ee);

		LinkedDoc<ExecutableElement> firstdocumented = getFirstDocumentedDoc(methoddocinfos);
		if (firstdocumented != null) {
			switch (firstdocumented.getRelation()) {
				case INHERITED: {
					//TODO it was inherited from somewhere, note that
					writeMethodOverrideNoteForDetail(firstdocumented.getAssociatedElement(), out, pagepath);
					break;
				}
				case LINKED: {
					writeDetailLinkNote(firstdocumented.getAssociatedElement(), out, pagepath);
					break;
				}
				default: {
					break;
				}
			}
			//TODO note about the copying of doc
			DocCommentTree methoddoctree = firstdocumented.getDocTree();
			DocCommentHtmlWriter writer = new DocCommentHtmlWriter(this, trees, out,
					trees.getPath(firstdocumented.getDocElement()), methoddoctree, pagepath);
			writer.setAutoLinkRelativeElement(ee);
			out.append("<div class=\"javadoc-detail-body\">");
			for (DocTree t : methoddoctree.getFirstSentence()) {
				writer.write(t);
			}
			List<? extends DocTree> body = methoddoctree.getBody();
			if (!ObjectUtils.isNullOrEmpty(body)) {
				out.append(' ');
			}
			for (DocTree t : body) {
				writer.write(t);
			}
			out.append("</div>");
			writeTypeParameterDetails(firstdocumented.getDocElement(), out, methoddoctree, pagepath);
			writeParameterMethodDetails(ee, firstdocumented, out, pagepath);
		} else {
			//there is no documentation on the element on any of its inherited elements
			LinkedDoc<ExecutableElement> inheritedfrom = getSecondDoc(methoddocinfos);
			if (inheritedfrom != null) {
				appendInheritedDocumentationNotFoundDetail(inheritedfrom.getDocElement(), out, pagepath,
						methoddocinfos);
				return;
			}
			if (isEnumValueOfMethod(ee)) {
				out.append("Returns the enum constant of this type with the specified name. "
						+ "The string must match <i>exactly</i> an identifier used to declare an enum constant in this type. "
						+ "(Extraneous whitespace characters are not permitted.)");

				Map<TypeMirror, String> throwdeclarationhtmls = new LinkedHashMap<>();
				throwdeclarationhtmls.put(elements.getTypeElement(IllegalArgumentException.class.getName()).asType(),
						"If this enum type has no constant with the specified name.");
				throwdeclarationhtmls.put(elements.getTypeElement(NullPointerException.class.getName()).asType(),
						"If the argument is <code>null</code>.");
				writeSimpleThrowsMethodDetails(throwdeclarationhtmls, out, pagepath);

				writeSimpleReturnsMethodDetails("The enum constant with the specified name.", out);
				return;
			}
			if (isEnumValuesMethod(ee)) {
				String enclosingname = ee.getEnclosingElement().getSimpleName().toString();
				out.append(
						"Returns an array containing the constants of this enum type, in the order they are declared. "
								+ "This method may be used to iterate over the constants as follows:" + "<pre>for ("
								+ enclosingname + " c : " + enclosingname + ".values())\r\n"
								+ "    System.out.println(c);</pre>");
				writeSimpleReturnsMethodDetails(
						"An array containing the constants of this enum type, in the order they are declared.", out);
				return;
			}
			//TODO no documentation
			return;
		}
		writeReturnsMethodDetails(methoddocinfos, out, pagepath, ee);
		writeThrowsMethodDetails(methoddocinfos, out, pagepath, ee);
		writeSeeTagDetails(firstdocumented.getDocElement(), firstdocumented.getDocTree(), out, pagepath, ee);
		writeSinceTagDetails(firstdocumented.getDocElement(), firstdocumented.getDocTree(), out, pagepath, ee);
	}

	private void writeDeprecatedTagForElement(SakerPath pagepath, Appendable out,
			ElementDocTree<DeprecatedTree> deprtree, boolean elemdeprecated, Element elem) throws IOException {
		if (elemdeprecated) {
			if (deprtree == null || deprtree.getDocTree() == null) {
				System.out.println(
						"Warning: No @deprecated documentation found for: " + elem.getEnclosingElement() + "." + elem);
			} else {
				//write the deprecated
				DocCommentHtmlWriter writer = new DocCommentHtmlWriter(this, trees, out,
						trees.getPath(deprtree.getElement()), deprtree.getCommentTree(), pagepath);
				writer.setAutoLinkRelativeElement(elem);
				out.append("<div class=\"javadoc-detail-deprecated\">");
				for (DocTree dt : deprtree.getDocTree().getBody()) {
					writer.write(dt);
				}
				out.append("</div>");
			}
		} else {
			//the elem is not deprecated
			if (deprtree != null && deprtree.getDocTree() != null) {
				throw new IllegalArgumentException(
						"@deprecated found on non-deprecated element: " + elem.getEnclosingElement() + "." + elem);
			}
		}
	}

	private static DeprecatedTree findDeprecatedTree(DocCommentTree doctree, Element elem) {
		if (doctree == null) {
			return null;
		}
		List<? extends DocTree> blocktags = doctree.getBlockTags();
		if (ObjectUtils.isNullOrEmpty(blocktags)) {
			return null;
		}
		DeprecatedTree rettree = null;
		for (DocTree bt : blocktags) {
			if (bt.getKind() != Kind.DEPRECATED) {
				continue;
			}
			if (rettree != null) {
				throw new IllegalArgumentException(
						"Multiple @deprecated on " + elem.getEnclosingElement() + "." + elem);
			}
			rettree = (DeprecatedTree) bt;
		}
		return rettree;
	}

	private static ReturnTree findReturnTree(DocCommentTree doctree, ExecutableElement elem) {
		ReturnTree rettree = null;
		for (DocTree bt : doctree.getBlockTags()) {
			if (bt.getKind() != Kind.RETURN) {
				continue;
			}
			if (rettree != null) {
				throw new IllegalArgumentException("Multiple @return on " + elem.getEnclosingElement() + "." + elem);
			}
			rettree = (ReturnTree) bt;
		}
		return rettree;
	}

	private static ElementDocTree<ReturnTree> findReturnTree(List<LinkedDoc<ExecutableElement>> methoddocinfos) {
		for (LinkedDoc<ExecutableElement> docinfo : methoddocinfos) {
			DocCommentTree dt = docinfo.getDocTree();
			if (dt != null) {
				ReturnTree rt = findReturnTree(dt, docinfo.getDocElement());
				if (rt != null) {
					return new ElementDocTree<>(docinfo.getDocElement(), dt, rt);
				}
			}
		}
		return null;
	}

	private boolean isInheritedFromJavaLib(List<LinkedDoc<ExecutableElement>> methoddocinfos) {
		for (LinkedDoc<ExecutableElement> docinfo : methoddocinfos) {
			String packname = elements.getPackageOf(docinfo.getDocElement().getEnclosingElement()).getQualifiedName()
					.toString();
			if (getExternalDocSiteLink(packname) != null) {
				return true;
			}
		}
		return false;
	}

	private ElementDocTree<ThrowsTree> findThrowsDoc(List<LinkedDoc<ExecutableElement>> methoddocinfos,
			TypeMirror exctype) {
		for (LinkedDoc<ExecutableElement> docinfo : methoddocinfos) {
			DocCommentTree dt = docinfo.getDocTree();
			if (dt != null) {
				for (DocTree bt : dt.getBlockTags()) {
					DocTree.Kind btkind = bt.getKind();
					if (btkind != Kind.THROWS && btkind != Kind.EXCEPTION) {
						continue;
					}
					ThrowsTree tt = (ThrowsTree) bt;
					Element excelem = dePseudoize(trees.getElement(DocTreePath.getPath(
							this.trees.getPath(docinfo.getDocElement()), docinfo.getDocTree(), tt.getExceptionName())));
					if (excelem == null) {
						continue;
					}
					if (types.isSameType(exctype, excelem.asType())) {
						return new ElementDocTree<ThrowsTree>(docinfo.getDocElement(), docinfo.getDocTree(), tt);
					}
				}
			}
		}
		return null;
	}

	private void writeSinceTagDetails(Element element, DocCommentTree doccomment, Appendable out, SakerPath pagepath,
			Element documentedelement) throws IOException {
		boolean hdr = false;
		DocCommentHtmlWriter writer = null;
		for (DocTree bt : doccomment.getBlockTags()) {
			if (bt.getKind() != Kind.SINCE) {
				continue;
			}
			SinceTree st = (SinceTree) bt;
			if (!hdr) {
				out.append("<div class=\"javadoc-detail-since javadoc-blocktag-section\">");
				writer = new DocCommentHtmlWriter(this, trees, out, trees.getPath(element), doccomment, pagepath);
				writer.setAutoLinkRelativeElement(documentedelement);
				writer.setAutoLinkRelativeNoLinkSplit(true);
				hdr = true;
			}
			out.append("<div class=\"javadoc-detail-since-entry\">");
			for (DocTree dt : st.getBody()) {
				writer.write(dt);
			}
			out.append("</div>");
		}
		if (hdr) {
			out.append("</div>");
		}
	}

	private void writeSeeTagDetails(Element element, DocCommentTree doccomment, Appendable out, SakerPath pagepath,
			Element documentedelement) throws IOException {
		boolean hdr = false;
		DocCommentHtmlWriter writer = null;
		for (DocTree bt : doccomment.getBlockTags()) {
			if (bt.getKind() != Kind.SEE) {
				continue;
			}
			SeeTree st = (SeeTree) bt;
			if (!hdr) {
				out.append("<div class=\"javadoc-detail-see javadoc-blocktag-section\">");
				writer = new DocCommentHtmlWriter(this, trees, out, trees.getPath(element), doccomment, pagepath);
				writer.setAutoLinkRelativeElement(documentedelement);
				writer.setAutoLinkRelativeNoLinkSplit(true);
				hdr = true;
			}
			out.append("<div class=\"javadoc-detail-see-entry\">");
			for (DocTree reft : st.getReference()) {
				writer.write(reft);
			}
			out.append("</div>");
		}
		if (hdr) {
			out.append("</div>");
		}
	}

	private void writeThrowsMethodDetails(List<LinkedDoc<ExecutableElement>> methoddocinfos, Appendable out,
			SakerPath pagePath, ExecutableElement elem) throws IOException {
		List<? extends TypeMirror> throwtypes = elem.getThrownTypes();
		@SuppressWarnings({ "unchecked", "rawtypes" })
		ElementDocTree<ThrowsTree>[] throwstrees = new ElementDocTree[throwtypes.size()];
		LinkedDoc<ExecutableElement> firstmethoddoc = methoddocinfos.get(0);
		DocCommentTree directdoctree = firstmethoddoc.getDocTree();
		if (directdoctree != null) {
			ExecutableElement firstmethodelem = firstmethoddoc.getDocElement();
			finder:
			for (DocTree bt : directdoctree.getBlockTags()) {
				if (bt.getKind() != Kind.THROWS) {
					continue;
				}
				ThrowsTree tt = (ThrowsTree) bt;
				Element excelem = dePseudoize(trees.getElement(DocTreePath.getPath(this.trees.getPath(firstmethodelem),
						firstmethoddoc.getDocTree(), tt.getExceptionName())));
				if (excelem == null) {
					throw new IllegalArgumentException("Exception type not found: " + tt.getExceptionName() + " on "
							+ firstmethodelem.getEnclosingElement() + "." + firstmethodelem);
				}
				TypeMirror exctype = excelem.asType();
				for (int i = 0; i < throwstrees.length; i++) {
					if (types.isSameType(exctype, throwtypes.get(i))) {
						if (throwstrees[i] != null) {
							throw new IllegalArgumentException(
									"Multiple throws declaration of: " + tt + " and " + throwstrees[i] + " on "
											+ firstmethodelem.getEnclosingElement() + "." + firstmethodelem);
						}
						throwstrees[i] = new ElementDocTree<ThrowsTree>(firstmethodelem, directdoctree, tt);
						continue finder;
					}
				}
				System.out.println("Warning: Undeclared @throws: " + tt + " on " + firstmethodelem.getEnclosingElement()
						+ "." + firstmethodelem);
			}
		}
		for (int i = 0; i < throwstrees.length; i++) {
			if (throwstrees[i] == null) {
				throwstrees[i] = findThrowsDoc(methoddocinfos, throwtypes.get(i));
				if (throwstrees[i] == null) {
					if (!isInheritedFromJavaLib(methoddocinfos)) {
						//only throw if the documentation should be available
						System.out.println("Warning: Missing exception documentation of: " + throwtypes.get(i) + " on "
								+ elem.getEnclosingElement() + "." + elem);
					}
				}
			}
		}
		if (throwstrees.length > 0) {
			out.append("<div class=\"javadoc-method-detail-throwdeclarations javadoc-blocktag-section\">");
			for (int i = 0; i < throwstrees.length; i++) {
				TypeMirror throwntm = throwtypes.get(i);
				boolean checkedexception = !isUncheckedExceptionType(throwntm);
				out.append("<div class=\"javadoc-method-detail-throw\""
						+ (checkedexception ? " data-javadoc-exception-checked" : "") + ">");
				out.append("<span class=\"javadoc-method-detail-throw-name\">");
				DocCommentHtmlWriter.writeTypeMirror(this, pagePath, throwntm, out, DocCommentHtmlWriter.TM_LINK_TYPES);
				out.append("</span>");
				out.append("<span class=\"javadoc-method-detail-throw-detail\">");
				ElementDocTree<ThrowsTree> tt = throwstrees[i];
				if (tt != null) {
					DocCommentHtmlWriter writer = new DocCommentHtmlWriter(this, trees, out,
							trees.getPath(tt.getElement()), tt.getCommentTree(), pagePath);
					for (DocTree desct : tt.getDocTree().getDescription()) {
						writer.write(desct);
					}
				}
				out.append("</span>");
				out.append("</div>");
			}
			out.append("</div>");
		}
	}

	private boolean isUncheckedExceptionType(TypeMirror tm) {
		return types.isAssignable(tm, javaLangRuntimeExceptionType) || types.isAssignable(tm, javaLangErrorType);
	}

	private void writeSimpleThrowsMethodDetails(Map<TypeMirror, String> throwdeclarationhtmls, Appendable out,
			SakerPath pagePath) throws IOException {
		out.append("<div class=\"javadoc-method-detail-throwdeclarations javadoc-blocktag-section\">");
		for (Entry<TypeMirror, String> entry : throwdeclarationhtmls.entrySet()) {
			out.append("<div class=\"javadoc-method-detail-throw\">");
			out.append("<span class=\"javadoc-method-detail-throw-name\">");
			DocCommentHtmlWriter.writeTypeMirror(this, pagePath, entry.getKey(), out,
					DocCommentHtmlWriter.TM_LINK_TYPES);
			out.append("</span>");
			out.append("<span class=\"javadoc-method-detail-throw-detail\">");
			out.append(entry.getValue());
			out.append("</span>");
			out.append("</div>");
		}
		out.append("</div>");
	}

	private void writeReturnsMethodDetails(List<LinkedDoc<ExecutableElement>> methoddocinfos, Appendable out,
			SakerPath pagePath, ExecutableElement elem) throws IOException {
		ElementDocTree<ReturnTree> rettree = findReturnTree(methoddocinfos);
		if (elem.getReturnType().getKind() == TypeKind.VOID) {
			if (rettree != null) {
				throw new IllegalArgumentException("@return on void method " + elem.getEnclosingElement() + "." + elem);
			}
			return;
		}
		if (rettree == null) {
			if (isInheritedFromJavaLib(methoddocinfos)) {
				//inherited doc is from java packages
				//TODO write a link to it?
				return;
			}
			System.out.println("Warning: Missing @return on method " + elem.getEnclosingElement() + "." + elem + " in "
					+ methoddocinfos);
			//to signal the absence with empty div
			out.append("<div class=\"javadoc-method-detail-return javadoc-blocktag-section\">");
			out.append("</div>");
			return;
		}
		out.append("<div class=\"javadoc-method-detail-return javadoc-blocktag-section\">");
		out.append("<div class=\"javadoc-method-detail-return-body\">");
		DocCommentHtmlWriter writer = new DocCommentHtmlWriter(this, trees, out, trees.getPath(rettree.getElement()),
				rettree.getCommentTree(), pagePath);
		for (DocTree desct : rettree.getDocTree().getDescription()) {
			writer.write(desct);
		}
		out.append("</div>");
		out.append("</div>");
	}

	private void writeSimpleReturnsMethodDetails(String html, Appendable out) throws IOException {
		out.append("<div class=\"javadoc-method-detail-return javadoc-blocktag-section\">");
		out.append(html);
		out.append("</div>");
	}

	private void writeParameterMethodDetails(ExecutableElement elem, LinkedDoc<ExecutableElement> doc, Appendable out,
			SakerPath pagePath) throws IOException {
		DocCommentTree methoddoctree = doc.getDocTree();
		ExecutableElement docelem = doc.getDocElement();
		List<? extends VariableElement> params = elem.getParameters();
		int paramcount = params.size();
		String[] nameswitch = new String[paramcount];
		if (elem != docelem) {
			List<? extends VariableElement> docelemparams = docelem.getParameters();
			if (paramcount != docelemparams.size()) {
				throw new IllegalArgumentException("Documentation parameter count mismatch: " + elem + " - " + docelem);
			}
			for (int i = 0; i < nameswitch.length; i++) {
				nameswitch[i] = docelemparams.get(i).getSimpleName().toString();
			}
		} else {
			for (int i = 0; i < nameswitch.length; i++) {
				nameswitch[i] = params.get(i).getSimpleName().toString();
			}
		}

		//TODO inherit method paramter documentation if missing?
		Map<String, ParamTree> paramdocs = new TreeMap<>();
		for (DocTree bt : methoddoctree.getBlockTags()) {
			if (bt.getKind() != Kind.PARAM) {
				continue;
			}
			ParamTree pt = (ParamTree) bt;
			if (pt.isTypeParameter()) {
				continue;
			}
			ParamTree prev = paramdocs.put(pt.getName().getName().toString(), pt);
			if (prev != null) {
				throw new IllegalArgumentException("Multiple parameter documentation definition: "
						+ pt.getName().getName() + " for " + elem.getEnclosingElement() + "." + elem);
			}
		}
		if (!paramdocs.isEmpty()) {
			out.append("<div class=\"javadoc-method-detail-params javadoc-blocktag-section\">");
			DocCommentHtmlWriter writer = new DocCommentHtmlWriter(this, trees, out, trees.getPath(docelem),
					methoddoctree, pagePath);

			for (int i = 0; i < params.size(); i++) {
				VariableElement pe = params.get(i);
				String name = pe.getSimpleName().toString();
				ParamTree pt = paramdocs.remove(nameswitch[i]);
				if (pt == null) {
					String warnmsg = "Missing parameter documentation: " + name + " for " + elem.getEnclosingElement()
							+ "." + elem + " found: " + paramdocs.keySet();
//					if (elem.getKind() == ElementKind.CONSTRUCTOR) {
//						//XXX handle constructor missing parameter? 
//						System.out.println("Warning: " + warnmsg);
//						continue;
//					}
					throw new IllegalArgumentException(warnmsg);
				}
				out.append("<div class=\"javadoc-method-detail-param\">");
				out.append("<span class=\"javadoc-method-detail-param-name\">");
				out.append(name);
				out.append("</span>");
				out.append("<span class=\"javadoc-method-detail-param-detail\">");
				for (DocTree desct : pt.getDescription()) {
					writer.write(desct);
				}
				out.append("</span>");
				out.append("</div>");
			}
			out.append("</div>");
		}
		if (!paramdocs.isEmpty()) {
			throw new IllegalArgumentException("Extraneous parameter documentation: " + paramdocs + " on "
					+ elem.getEnclosingElement() + "." + elem);
		}
	}

//	private void writeParameterMethodDetails(ExecutableElement elem, Appendable out, DocCommentTree methoddoctree,
//			SakerPath pagePath) throws IOException {
//		//TODO inherit method paramter documentation if missing?
//		Map<String, ParamTree> paramdocs = new TreeMap<>();
//		for (DocTree bt : methoddoctree.getBlockTags()) {
//			if (bt.getKind() != Kind.PARAM) {
//				continue;
//			}
//			ParamTree pt = (ParamTree) bt;
//			if (pt.isTypeParameter()) {
//				continue;
//			}
//			ParamTree prev = paramdocs.put(pt.getName().getName().toString(), pt);
//			if (prev != null) {
//				throw new IllegalArgumentException("Multiple parameter documentation definition: "
//						+ pt.getName().getName() + " for " + elem.getEnclosingElement() + "." + elem);
//			}
//		}
//		if (!paramdocs.isEmpty()) {
//			out.append("<div class=\"javadoc-method-detail-params javadoc-blocktag-section\">");
//			DocCommentHtmlWriter writer = new DocCommentHtmlWriter(this, trees, out, trees.getPath(elem), methoddoctree,
//					pagePath);
//			for (VariableElement pe : elem.getParameters()) {
//				String name = pe.getSimpleName().toString();
//				ParamTree pt = paramdocs.remove(name);
//				if (pt == null) {
//					String warnmsg = "Missing parameter documentation: " + name + " for " + elem.getEnclosingElement()
//							+ "." + elem + " found: " + paramdocs.keySet();
////					if (elem.getKind() == ElementKind.CONSTRUCTOR) {
////						//XXX handle constructor missing parameter? 
////						System.out.println("Warning: " + warnmsg);
////						continue;
////					}
//					throw new IllegalArgumentException(warnmsg);
//				}
//				out.append("<div class=\"javadoc-method-detail-param\">");
//				out.append("<span class=\"javadoc-method-detail-param-name\">");
//				out.append(name);
//				out.append("</span>");
//				out.append("<span class=\"javadoc-method-detail-param-detail\">");
//				for (DocTree desct : pt.getDescription()) {
//					writer.write(desct);
//				}
//				out.append("</span>");
//				out.append("</div>");
//			}
//			out.append("</div>");
//		}
//		if (!paramdocs.isEmpty()) {
//			throw new IllegalArgumentException("Extraneous parameter documentation: " + paramdocs + " on "
//					+ elem.getEnclosingElement() + "." + elem);
//		}
//	}

	private void writeTypeParameterDetails(Parameterizable elem, Appendable out, DocCommentTree methoddoctree,
			SakerPath pagePath) throws IOException {
		Map<String, ParamTree> paramdocs = new TreeMap<>();
		for (DocTree bt : methoddoctree.getBlockTags()) {
			if (bt.getKind() != Kind.PARAM) {
				continue;
			}
			ParamTree pt = (ParamTree) bt;
			if (!pt.isTypeParameter()) {
				continue;
			}
			ParamTree prev = paramdocs.put(pt.getName().getName().toString(), pt);
			if (prev != null) {
				throw new IllegalArgumentException("Multiple type parameter documentation definition: "
						+ pt.getName().getName() + " for " + elem.getEnclosingElement() + "." + elem);
			}
		}
		if (!paramdocs.isEmpty()) {
			out.append("<div class=\"javadoc-detail-typeparams javadoc-blocktag-section\">");
			DocCommentHtmlWriter writer = new DocCommentHtmlWriter(this, trees, out, trees.getPath(elem), methoddoctree,
					pagePath);
			for (TypeParameterElement tpe : elem.getTypeParameters()) {
				String name = tpe.getSimpleName().toString();
				ParamTree pt = paramdocs.remove(name);
				if (pt == null) {
					throw new IllegalArgumentException("Missing type parameter documentation: " + name + " for "
							+ elem.getEnclosingElement() + "." + elem);
				}
				out.append("<div class=\"javadoc-detail-typeparam\">");
				out.append("<span class=\"javadoc-detail-typeparam-name\">");
				out.append(name);
				out.append("</span>");
				out.append("<span class=\"javadoc-detail-typeparam-detail\">");
				for (DocTree desct : pt.getDescription()) {
					writer.write(desct);
				}
				out.append("</span>");
				out.append("</div>");
			}
			out.append("</div>");
		}
		if (!paramdocs.isEmpty()) {
			throw new IllegalArgumentException("Extraneous type parameter documentation: " + paramdocs + " on "
					+ elem.getEnclosingElement() + "." + elem);
		}
	}

	private void writeMethodConstructorDetailSignature(SakerPath pagepath, Appendable out, ExecutableElement ee)
			throws IOException {
		List<AnnotationMirror> annots = DocCommentHtmlWriter.filterDocumentedAnnotations(this,
				ee.getAnnotationMirrors());
		if (!ObjectUtils.isNullOrEmpty(annots)) {
			out.append("<div class=\"javadoc-method-annots javadoc-member-annots\">");
			writeAnnotations(pagepath, out, annots);
			out.append("</div>");
		}
		if (writeMethodIndexMeta(pagepath, ee, out, true)) {
			out.append(' ');
		}
		writeMethodIndexSignature(pagepath, ee, out, true);
		List<? extends TypeMirror> thrown = ee.getThrownTypes();
		if (!thrown.isEmpty()) {
			out.append("<span class=\"javadoc-method-throws\">");
			out.append("<span class=\"" + DocCommentHtmlWriter.HTML_CLASS_JAVADOC_KEYWORD + "\">&nbsp;throws</span> ");
			out.append("<span class=\"javadoc-method-throws-entries\">");
			for (Iterator<? extends TypeMirror> it = thrown.iterator(); it.hasNext();) {
				TypeMirror tm = it.next();
				out.append("<span class=\"javadoc-method-exc\">");
				DocCommentHtmlWriter.writeTypeMirror(this, pagepath, tm, out,
						DocCommentHtmlWriter.TM_LINK_TYPES | DocCommentHtmlWriter.TM_WITH_DOCUMENTED_ANNOTATIONS);
				if (it.hasNext()) {
					out.append(", ");
				}
				out.append("</span>");
			}
			out.append("</span>");
			out.append("</span>");
		}
	}

	private void collectAllSubClasses(TypeDocumentationInfo typeinfo, Map<String, TypeDocumentationInfo> result) {
		for (TypeDocumentationInfo sc : typeinfo.getSubClasses()) {
			TypeElement scelem = sc.getElement();
			if (scelem == null) {
				continue;
			}
			TypeDocumentationInfo prev = result.putIfAbsent(scelem.getQualifiedName().toString(), sc);
			if (prev != null) {
				continue;
			}
			collectAllSubClasses(sc, result);
		}
	}

	private Collection<TypeDocumentationInfo> getAllSubClasses(TypeDocumentationInfo typeinfo) {
		Map<String, TypeDocumentationInfo> result = new TreeMap<>();
		collectAllSubClasses(typeinfo, result);
		return result.values();
	}

	private void collectAllInterfaceImplementations(TypeDocumentationInfo typeinfo,
			Map<String, TypeDocumentationInfo> result) {
		for (TypeDocumentationInfo sc : typeinfo.getInterfaceImplementations()) {
			TypeElement scelem = sc.getElement();
			if (scelem == null) {
				continue;
			}
			TypeDocumentationInfo prev = result.putIfAbsent(scelem.getQualifiedName().toString(), sc);
			if (prev != null) {
				continue;
			}
			collectAllInterfaceImplementations(sc, result);
		}
		collectAllSubClasses(typeinfo, result);
	}

	private Collection<TypeDocumentationInfo> getAllInterfaceImplementations(TypeDocumentationInfo typeinfo) {
		Map<String, TypeDocumentationInfo> result = new TreeMap<>();
		collectAllInterfaceImplementations(typeinfo, result);
		return result.values();
	}

	private Collection<TypeDocumentationInfo> getAllSubTypes(TypeDocumentationInfo typeinfo) {
		Map<String, TypeDocumentationInfo> result = new TreeMap<>();
		collectAllInterfaceImplementations(typeinfo, result);
		collectAllSubClasses(typeinfo, result);
		return result.values();
	}

	private boolean writeMethodIndexMeta(SakerPath pagepath, ExecutableElement elem, Appendable out,
			boolean fullmodifiers) throws IOException {
		boolean result = false;
		result |= writeModifiers(elem, out, fullmodifiers ? null : (TypeElement) elem.getEnclosingElement());
		List<? extends TypeParameterElement> tparams = elem.getTypeParameters();
		if (!ObjectUtils.isNullOrEmpty(tparams)) {
			out.append("&lt;<wbr>");
			for (Iterator<? extends TypeParameterElement> it = tparams.iterator(); it.hasNext();) {
				TypeParameterElement tpe = it.next();
				out.append(DocCommentHtmlWriter.HTML_SPAN_JAVADOC_TYPEPARAM);
				out.append(tpe.getSimpleName());
				out.append("</span>");
				List<? extends TypeMirror> bounds = tpe.getBounds();
				if (!ObjectUtils.isNullOrEmpty(bounds)) {
					if (bounds.size() != 1 || !getTypes().isSameType(bounds.get(0), getJavaLangObjectType())) {
						out.append("&nbsp;" + DocCommentHtmlWriter.HTML_SPAN_JAVADOC_KEYWORD + "extends</span> ");
						for (Iterator<? extends TypeMirror> bit = bounds.iterator(); bit.hasNext();) {
							TypeMirror bound = bit.next();
							DocCommentHtmlWriter.writeTypeMirror(this, pagepath, bound, out,
									DocCommentHtmlWriter.TM_LINK_TYPES);
							if (bit.hasNext()) {
								out.append("&nbsp;&amp; ");
							}
						}
					}
				}
				if (it.hasNext()) {
					out.append(", ");
				}
			}
			out.append("&gt; ");
			result = true;
		}
		if (elem.getKind() == ElementKind.METHOD) {
			DocCommentHtmlWriter.writeTypeMirror(this, pagepath, elem.getReturnType(), out,
					DocCommentHtmlWriter.TM_LINK_TYPES);
			result = true;
		}
		return result;
	}

	private void writeMethodIndexSignature(SakerPath pagepath, ExecutableElement elem, Appendable out,
			boolean annotations) throws IOException {
		Name methodname;
		if (elem.getKind() == ElementKind.CONSTRUCTOR) {
			methodname = elem.getEnclosingElement().getSimpleName();
		} else {
			methodname = elem.getSimpleName();
		}
		out.append(getAHrefTagForElement(pagepath, elem, ImmutableUtils.singletonMap("class", "javadoc-sig-name")));
		out.append(methodname);
		out.append("</a>");
		DocCommentHtmlWriter.writeReferenceParameterTypes(this, pagepath, elem,
				DocCommentHtmlWriter.TM_LINK_TYPES | DocCommentHtmlWriter.TM_PARAMETER_NAMES
						| DocCommentHtmlWriter.TM_SPAN_PARAMETER_NAMES
						| (annotations ? DocCommentHtmlWriter.TM_WITH_DOCUMENTED_ANNOTATIONS : 0),
				out);
		AnnotationValue defval = elem.getDefaultValue();
		if (defval != null) {
			out.append("&nbsp;" + DocCommentHtmlWriter.HTML_SPAN_JAVADOC_KEYWORD + "default</span> ");
			defval.accept(new AnnotationValueWriter(this, pagepath), out);
		}
	}

	private void writeIndexFirstSentence(SakerPath pagepath, Appendable sb, TreePath treepath, LinkedDoc<?> linkeddoc,
			Element elem) throws IOException {
		DocCommentTree doctree = linkeddoc.getDocTree();
		if (doctree == null) {
			return;
		}
		DocCommentHtmlWriter writer = new DocCommentHtmlWriter(this, trees, sb, treepath, doctree, pagepath);
		writer.setFirstSentenceOnly(true);
		DeprecatedTree deprtree = findDeprecatedTree(doctree, elem);
		if (deprtree != null) {
			sb.append("<div class=\"javadoc-idx-deprecated\">");
			for (DocTree t : deprtree.getBody()) {
				writer.write(t);
			}
			sb.append("</div>");
			return;
		}
		for (DocTree t : doctree.getFirstSentence()) {
			writer.write(t);
		}
	}

	private void writeIndexFirstSentence(SakerPath pagepath, Appendable sb, Element elem, LinkedDoc<?> methoddoctree)
			throws IOException {
		TreePath treepath = trees.getPath(elem);
		if (treepath == null) {
			throw new AssertionError("Tree path for: " + elem + " is null");
		}
		writeIndexFirstSentence(pagepath, sb, treepath, methoddoctree, elem);
	}

	private static void collectInheritedTypesBreadthFirstImpl(TypeElement type, Set<TypeElement> result) {
		TypeElement sc = getSuperClassElement(type);
		if (sc != null) {
			result.add(sc);
		}
		for (TypeMirror itf : type.getInterfaces()) {
			DeclaredType dt = (DeclaredType) itf;
			TypeElement itftype = (TypeElement) dt.asElement();
			result.add(itftype);
		}
	}

	private Collection<TypeElement> collectInheritedTypesBreadthFirst(TypeElement type) {
		LinkedHashSet<TypeElement> result = new LinkedHashSet<>();
		Set<TypeElement> nadded = new LinkedHashSet<>();
		Set<TypeElement> nadded2 = new LinkedHashSet<>();

		collectInheritedTypesBreadthFirstImpl(type, nadded);
		while (!nadded.isEmpty()) {
			nadded2.clear();
			for (TypeElement added : nadded) {
				if (result.add(added)) {
					nadded2.add(added);
				}
			}
			nadded.clear();
			for (TypeElement te : nadded2) {
				collectInheritedTypesBreadthFirstImpl(te, nadded);
			}
		}
		result.add(javaLangObjectElement);
		return result;
	}

	@SuppressWarnings("unchecked")
	private <E extends Element> LinkedDoc<E> getDocCommentTreeWithPseudo(E elem, LinkedDocRelation relation) {
		DocCommentTree doctree = trees.getDocCommentTree(elem);
		if (doctree != null) {
			return new LinkedDoc<>(elem, doctree, relation);
		}
		Element pseudo = pseudoize(elem);
		if (pseudo == null) {
			return new LinkedDoc<>(elem, null, relation);
		}
		doctree = trees.getDocCommentTree(pseudo);
		if (doctree != null) {
			return new LinkedDoc<>((E) pseudo, doctree, relation, elem);
		}
		return new LinkedDoc<>(elem, null, relation);
	}

	private LinkedDoc<ExecutableElement> getInheritedDocumentation(ExecutableElement elem, TypeElement type) {
		TypeElement enclosingtype = (TypeElement) elem.getEnclosingElement();
		for (Element encelem : type.getEnclosedElements()) {
			if (encelem.getKind() != ElementKind.METHOD) {
				continue;
			}
			ExecutableElement encee = (ExecutableElement) encelem;
			if (elements.overrides(elem, encee, enclosingtype)) {
				DocCommentTree doctree = trees.getDocCommentTree(encee);
				ExecutableElement docowner = encee;
				if (doctree == null) {
					Element pseudoelem = pseudoize(encee);
					if (pseudoelem != null) {
						doctree = trees.getDocCommentTree(pseudoelem);
						if (doctree != null) {
							docowner = (ExecutableElement) pseudoelem;
						}
					}
				}
				return new LinkedDoc<>(docowner, doctree, LinkedDocRelation.INHERITED, encee);
			}
		}
		return null;
	}

//	private ExecutableElement getOverriddenMethod(ExecutableElement elem, TypeElement type) {
//		for (Element encelem : type.getEnclosedElements()) {
//			if (encelem.getKind() != ElementKind.METHOD) {
//				continue;
//			}
//			ExecutableElement encee = (ExecutableElement) encelem;
//			if (elements.overrides(elem, encee, (TypeElement) elem.getEnclosingElement())) {
//				return encee;
//			}
//		}
//		return null;
//	}

	public List<LinkedDoc<ExecutableElement>> collectInheritedDocumentations(Element elem) {
		if (elem.getKind() != ElementKind.METHOD || elem.getModifiers().contains(Modifier.STATIC)) {
			return null;
		}
		TypeElement type = (TypeElement) elem.getEnclosingElement();
		Iterable<TypeElement> allinheritedtypes = collectInheritedTypesBreadthFirst(type);
		List<LinkedDoc<ExecutableElement>> result = new ArrayList<>();
		for (TypeElement inheritedtype : allinheritedtypes) {
			LinkedDoc<ExecutableElement> mdoc = getInheritedDocumentation((ExecutableElement) elem, inheritedtype);
			if (mdoc != null) {
				result.add(mdoc);
			}
		}

		return result;
	}

	public static TypeElement getSuperClassElement(TypeElement elem) {
		TypeMirror sc = elem.getSuperclass();
		if (sc.getKind() == TypeKind.NONE) {
			return null;
		}
		return (TypeElement) ((DeclaredType) sc).asElement();
	}

	public SakerPath getPagePath(TypeElement elem) {
		checkNotPseudoElement(elem);
		String packagestr = elements.getPackageOf(elem).getQualifiedName().toString();
		String qname = elem.getQualifiedName().toString();
		checkTypeDocumentationExistence(elem);

		if (!packagestr.isEmpty()) {
			qname = qname.substring(packagestr.length() + 1);
		}
		return SakerPath.valueOf(packagestr.replace('.', '/') + "/" + qname + ".html");
	}

	public SakerPath getPagePath(PackageElement elem) {
		checkNotPseudoElement(elem);
		checkPackageDocumentationExistence(elem);
		return SakerPath.valueOf(
				elem.getQualifiedName().toString().replace('.', '/') + "/" + PACKAGE_SUMMARY_HTML_NAME + ".html");
	}

	public SakerPath getPagePath(Element elem) {
		switch (elem.getKind()) {
			case ANNOTATION_TYPE:
			case CLASS:
			case ENUM:
			case INTERFACE: {
				return getPagePath((TypeElement) elem);
			}
			case PACKAGE: {
				return getPagePath((PackageElement) elem);
			}
			case CONSTRUCTOR:
			case METHOD:
			case ENUM_CONSTANT:
			case TYPE_PARAMETER:
			case FIELD: {
				return getPagePath(elem.getEnclosingElement());
			}

			case EXCEPTION_PARAMETER:
			case INSTANCE_INIT:
			case LOCAL_VARIABLE:
			case MODULE:
			case OTHER:
			case PARAMETER:
			case RESOURCE_VARIABLE:
			case STATIC_INIT:
			default: {
				throw new UnsupportedOperationException(elem.getKind().toString() + " - " + elem);
			}
		}
	}

	public static boolean isTypeElementKind(ElementKind kind) {
		return kind.isClass() || kind.isInterface();
	}

	public static TypeElement getTypeElementOf(Element element) {
		while (element != null) {
			ElementKind kind = element.getKind();
			if (isTypeElementKind(kind)) {
				return (TypeElement) element;
			}
			element = element.getEnclosingElement();
		}
		return null;
	}

	public static String escapeHref(String s) {
		//TODO implement
		return s;
	}

	private String getMethodParametersId(ExecutableElement ee) {
		try {
			StringBuilder sb = new StringBuilder();
			for (Iterator<? extends VariableElement> it = ee.getParameters().iterator(); it.hasNext();) {
				VariableElement ve = it.next();
				DocCommentHtmlWriter.writeTypeMirror(this, null, ve.asType(), sb,
						DocCommentHtmlWriter.TM_FULLY_QUALIFIED | DocCommentHtmlWriter.TM_NO_HTML
								| DocCommentHtmlWriter.TM_NO_TYPE_ARGS
								| (ee.isVarArgs() && !it.hasNext() ? DocCommentHtmlWriter.TM_LAST_VARARG : 0));
				if (it.hasNext()) {
					sb.append(",");
				}
			}
			return sb.toString();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private static final Pattern PATTERN_IDENTIFIER_REPLACER = Pattern.compile("[^a-zA-Z\\_:.0-9-]");

	private void appendHtmlDocletMethodAnchorIdType(TypeMirror tm, StringBuilder sb) {
		switch (tm.getKind()) {
			case ARRAY: {
				ArrayType at = (ArrayType) tm;
				appendHtmlDocletMethodAnchorIdType(at.getComponentType(), sb);
				sb.append(":A");
				break;
			}
			case BOOLEAN:
			case BYTE:
			case CHAR:
			case DOUBLE:
			case FLOAT:
			case INT:
			case LONG:
			case SHORT:
			case VOID: {
				sb.append(tm.getKind().name().toLowerCase(Locale.ENGLISH));
				break;
			}
			case DECLARED: {
				DeclaredType dt = (DeclaredType) tm;
				TypeElement elem = (TypeElement) dt.asElement();
				sb.append(elem.getQualifiedName());
				break;
			}
			case TYPEVAR: {
				TypeVariable tv = (TypeVariable) tm;
				sb.append(tv.asElement().getSimpleName());
				break;
			}
			case ERROR:
			case EXECUTABLE:
			case INTERSECTION:
			case MODULE:
			case NONE:
			case NULL:
			case OTHER:
			case PACKAGE:
			case UNION:
			case WILDCARD:
			default: {
				throw new UnsupportedOperationException(tm.getKind() + " - " + tm);
			}
		}
	}

	public static String getDocPagePath(SakerPath pagepath, SakerPath targetpath) {
		if (pagepath.equals(targetpath)) {
			//self reference
			return "";
		}
		SakerPath relpath = pagepath.getParent().relativize(targetpath);
		return relpath.toString();
	}

	private void checkNotPseudoElement(Element elem) {
		PackageElement pe = elements.getPackageOf(elem);
		if (pe.getQualifiedName().toString().startsWith("pseudo.")) {
			throw new AssertionError(elem);
		}
	}

	public Element pseudoize(Element elem) {
		if (elem == null) {
			return null;
		}
		return elem.accept(new ElementVisitor<Element, Void>() {
			@Override
			public Element visit(Element e, Void p) {
				return e.accept(this, p);
			}

			@Override
			public Element visitPackage(PackageElement e, Void p) {
				String qname = e.getQualifiedName().toString();
				if (qname.startsWith("pseudo.")) {
					return null;
				}
				return elements.getPackageElement("pseudo." + qname);
			}

			@Override
			public Element visitType(TypeElement e, Void p) {
				String qname = e.getQualifiedName().toString();
				if (qname.startsWith("pseudo.")) {
					return null;
				}
				return elements.getTypeElement("pseudo." + qname);
			}

			@Override
			public Element visitVariable(VariableElement e, Void p) {
				Element eenclosing = e.getEnclosingElement();
				Element nenclosing = pseudoize(eenclosing);
				if (nenclosing == null) {
					return null;
				}

				for (Element ence : nenclosing.getEnclosedElements()) {
					if (!ence.getSimpleName().contentEquals(e.getSimpleName())) {
						continue;
					}
					if (ence.getKind() == e.getKind()) {
						return ence;
					}
				}
				throw new AssertionError(e);
			}

			@Override
			public Element visitExecutable(ExecutableElement e, Void p) {
				Element eenclosing = e.getEnclosingElement();
				Element nenclosing = pseudoize(eenclosing);
				if (nenclosing == null) {
					return null;
				}
				String edescriptor = getDescriptor(e);
				for (Element ence : nenclosing.getEnclosedElements()) {
					if (!ence.getSimpleName().contentEquals(e.getSimpleName())) {
						continue;
					}
					if (ence.getKind() != e.getKind()) {
						continue;
					}
					if (edescriptor.equals(getDescriptor((ExecutableElement) ence).replace("Lpseudo/", "L"))) {
						return ence;
					}
				}
				throw new AssertionError(e);
			}

			@Override
			public Element visitTypeParameter(TypeParameterElement e, Void p) {
				Element eenclosing = e.getEnclosingElement();
				Element nenclosing = pseudoize(eenclosing);
				if (nenclosing == null) {
					return null;
				}
				for (TypeParameterElement tpe : ((Parameterizable) nenclosing).getTypeParameters()) {
					if (!tpe.getSimpleName().contentEquals(e.getSimpleName())) {
						continue;
					}
					return tpe;
				}
				throw new AssertionError(e);
			}

			@Override
			public Element visitUnknown(Element e, Void p) {
				throw new AssertionError(e);
			}
		}, null);
	}

	public Element dePseudoize(Element elem) {
		if (elem == null) {
			return null;
		}
		return elem.accept(new ElementVisitor<Element, Void>() {
			@Override
			public Element visit(Element e, Void p) {
				return e.accept(this, p);
			}

			@Override
			public Element visitPackage(PackageElement e, Void p) {
				String qname = e.getQualifiedName().toString();
				if (!qname.startsWith("pseudo.")) {
					return e;
				}
				return elements.getPackageElement(qname.substring("pseudo.".length()));
			}

			@Override
			public Element visitType(TypeElement e, Void p) {
				String qname = e.getQualifiedName().toString();
				if (!qname.startsWith("pseudo.")) {
					return e;
				}
				return elements.getTypeElement(qname.substring("pseudo.".length()));
			}

			@Override
			public Element visitVariable(VariableElement e, Void p) {
				Element eenclosing = e.getEnclosingElement();
				Element nenclosing = dePseudoize(eenclosing);
				if (eenclosing == nenclosing) {
					return e;
				}

				for (Element ence : nenclosing.getEnclosedElements()) {
					if (!ence.getSimpleName().contentEquals(e.getSimpleName())) {
						continue;
					}
					if (ence.getKind() == e.getKind()) {
						return ence;
					}
				}
				throw new AssertionError(e);
			}

			@Override
			public Element visitExecutable(ExecutableElement e, Void p) {
				Element eenclosing = e.getEnclosingElement();
				Element nenclosing = dePseudoize(eenclosing);
				if (eenclosing == nenclosing) {
					return e;
				}
				String edescriptor = getDescriptor(e);
				for (Element ence : nenclosing.getEnclosedElements()) {
					if (!ence.getSimpleName().contentEquals(e.getSimpleName())) {
						continue;
					}
					if (ence.getKind() != e.getKind()) {
						continue;
					}
					if (edescriptor.equals(getDescriptor((ExecutableElement) ence))) {
						return ence;
					}
				}
				throw new AssertionError(e);
			}

			@Override
			public Element visitTypeParameter(TypeParameterElement e, Void p) {
				Element eenclosing = e.getEnclosingElement();
				Element nenclosing = dePseudoize(eenclosing);
				if (eenclosing == nenclosing) {
					return e;
				}
				for (TypeParameterElement tpe : ((Parameterizable) nenclosing).getTypeParameters()) {
					if (!tpe.getSimpleName().contentEquals(e.getSimpleName())) {
						continue;
					}
					return tpe;
				}
				throw new AssertionError(e);
			}

			@Override
			public Element visitUnknown(Element e, Void p) {
				throw new AssertionError(e);
			}
		}, null);
	}

	//the argument may be internal name as well
	private String getExternalDocSiteLink(String qualifiedorinternalname) {
		SakerPath wctestpath = SakerPath.valueOf(qualifiedorinternalname.replace('.', '/'));
		for (Entry<WildcardPath, String> entry : packageExternalDocSites.entrySet()) {
			if (entry.getKey().includes(wctestpath)) {
				return entry.getValue();
			}
		}
		return null;
	}

	private boolean isClassExplicitlyExcluded(String qualifiedorinternalname) {
		SakerPath wctestpath = SakerPath.valueOf(qualifiedorinternalname.replace('.', '/'));
		for (WildcardPath wc : excludeClassWildcards) {
			if (wc.includes(wctestpath)) {
				return true;
			}
		}
		return false;
	}

	public String getDocPageHref(SakerPath pagepath, Element element) {
		checkNotPseudoElement(element);
		PackageElement packelem = elements.getPackageOf(element);
		String packagename = packelem.getQualifiedName().toString();
		ElementKind elementkind = element.getKind();
		if (!docPackages.containsKey(packagename)) {
			String extlink = getExternalDocSiteLink(packagename);
			if (extlink == null) {
				throw new IllegalArgumentException("Undocumented package: " + packelem);
			}
			StringBuilder sb = new StringBuilder();
			sb.append(extlink);
			appendLinkExternalLocationRelativeHref(element, packagename, elementkind, sb);
			return escapeHref(sb.toString());
		}
		SakerPath elementtargetpath = getPagePath(element);
		SakerPath relpath = pagepath.getParent().relativize(elementtargetpath);
		String appendid = "";
		switch (elementkind) {
			case ANNOTATION_TYPE:
			case CLASS:
			case ENUM:
			case INTERFACE: {
				//top of the page
				//only append the "#" if we're linking from the same page, else it will be present in cross-page links, which is unnecessary
				if (pagepath.equals(elementtargetpath)) {
					appendid = "#";
				}
				break;
			}
			case METHOD:
			case CONSTRUCTOR: {
				appendid = "#" + getMethodHtmlIdentifier((ExecutableElement) element);
				break;
			}
			case ENUM_CONSTANT:
			case FIELD: {
				appendid = "#" + getFieldHtmlIdentifier((VariableElement) element);
				break;
			}
			case EXCEPTION_PARAMETER:
			case INSTANCE_INIT:
			case LOCAL_VARIABLE:
			case MODULE:
			case OTHER:
			case PACKAGE:
			case PARAMETER:
			case RESOURCE_VARIABLE:
			case STATIC_INIT:
			case TYPE_PARAMETER:
			default: {
				break;
			}
		}
		if (relpath.getNameCount() == 1 && relpath.getName(0).equals(pagepath.getFileName())) {
			return escapeHref(appendid);
		}
		return escapeHref(relpath.toString() + appendid);
	}

	private void appendLinkExternalLocationRelativeHref(Element element, String packagename, ElementKind elementkind,
			StringBuilder sb) {
		sb.append(packagename.replace('.', '/'));
		sb.append('/');
		String appendid = "";
		if (elementkind == ElementKind.PACKAGE) {
			sb.append(PACKAGE_SUMMARY_HTML_NAME);
			sb.append(".html");
		} else {
			TypeElement telem = getTypeElementOf(element);
			if (packagename.isEmpty()) {
				sb.append(telem.getQualifiedName());
				sb.append(".html");
			} else {
				sb.append(telem.getQualifiedName().toString().substring(packagename.length() + 1));
				sb.append(".html");
			}
			if (elementkind == ElementKind.CONSTRUCTOR || elementkind == ElementKind.METHOD) {
				// see HtmlDocWriter.getName(String)
				// see HtmlDocletWriter.getAnchor(ExecutableElement, boolean)

				ExecutableElement ee = (ExecutableElement) element;
				String methodname = elementkind == ElementKind.CONSTRUCTOR
						? ee.getEnclosingElement().getSimpleName().toString()
						: ee.getSimpleName().toString();
				List<? extends VariableElement> methodparams = ee.getParameters();
				if (methodparams.isEmpty()) {
					appendid = "#" + methodname + "--";
				} else {
					StringBuilder appendidsb = new StringBuilder();
					appendidsb.append('#');
					appendidsb.append(methodname);
					//opening (
					appendidsb.append('-');
					for (Iterator<? extends VariableElement> it = methodparams.iterator(); it.hasNext();) {
						VariableElement pe = it.next();
						TypeMirror paramtype = pe.asType();
						appendHtmlDocletMethodAnchorIdType(paramtype, appendidsb);
						if (it.hasNext()) {
							//comma ,
							appendidsb.append('-');
						}
					}
					//closing )
					appendidsb.append('-');
					appendid = appendidsb.toString();
				}
			} else if (elementkind.isField()) {
				appendid = "#" + element.getSimpleName();
			}
		}
		sb.append(appendid);
	}

	private Name getFieldHtmlIdentifier(VariableElement element) {
		return element.getSimpleName();
	}

	private String getMethodHtmlIdentifier(ExecutableElement element) {
		Name name;
		switch (element.getKind()) {
			case CONSTRUCTOR: {
				name = element.getEnclosingElement().getSimpleName();
				break;
			}
			case METHOD: {
				name = element.getSimpleName();
				break;
			}
			default: {
				throw new UnsupportedOperationException(element.getKind().toString());
			}
		}
		return PATTERN_IDENTIFIER_REPLACER.matcher((name + "(" + getMethodParametersId(element) + ")")).replaceAll("-");
	}

	private String getDescriptor(TypeMirror tm) {
		TypeKind kind = tm.getKind();
		switch (kind) {
			case ARRAY: {
				return "[" + getDescriptor(((ArrayType) tm).getComponentType());
			}
			case BOOLEAN: {
				return "Z";
			}
			case BYTE: {
				return "B";
			}
			case CHAR: {
				return "C";
			}
			case DOUBLE: {
				return "D";
			}
			case FLOAT: {
				return "F";
			}
			case INT: {
				return "I";
			}
			case LONG: {
				return "J";
			}
			case SHORT: {
				return "S";
			}
			case DECLARED: {
				TypeElement elem = (TypeElement) ((DeclaredType) tm).asElement();
				return "L" + getInternalName(elem) + ";";
			}
			case VOID: {
				return "V";
			}
			case EXECUTABLE: {
				ExecutableType et = (ExecutableType) tm;
				List<? extends TypeMirror> params = et.getParameterTypes();
				StringBuilder sb = new StringBuilder();
				sb.append('(');
				sb.append(')');
				for (TypeMirror ptm : params) {
					sb.append(getDescriptor(ptm));
				}
				sb.append(getDescriptor(et.getReturnType()));
				return sb.toString();
			}
			case TYPEVAR: {
				TypeVariable tv = (TypeVariable) tm;
				TypeParameterElement elem = (TypeParameterElement) tv.asElement();
				List<? extends TypeMirror> bounds = elem.getBounds();
				if (bounds.isEmpty()) {
					return "Ljava/lang/Object;";
				}
				return getDescriptor(bounds.get(0));
			}
			case ERROR:
			case INTERSECTION:
			case UNION:
			case WILDCARD:
			default: {
				throw new IllegalArgumentException(kind.toString());
			}
		}
	}

	private String getInternalName(TypeMirror tm) {
		TypeKind kind = tm.getKind();
		switch (kind) {
			case DECLARED: {
				TypeElement elem = (TypeElement) ((DeclaredType) tm).asElement();
				return getInternalName(elem);
			}
			case TYPEVAR: {
				TypeVariable tv = (TypeVariable) tm;
				return getInternalName(tv.getUpperBound());
			}
			case INTERSECTION: {
				IntersectionType is = (IntersectionType) tm;
				List<? extends TypeMirror> bounds = is.getBounds();
				if (bounds == null || bounds.isEmpty()) {
					return null;
				}
				return getInternalName(bounds.get(0));
			}
			default: {
				throw new IllegalArgumentException(kind.toString());
			}
		}
	}

	private String getDescriptor(ExecutableElement ee) {
		return getDescriptor(ee, getImplicitInnerClassConstructorParameters(ee));
	}

	private String getDescriptor(ExecutableElement ee, List<TypeElement> implicitparameters) {
		List<? extends VariableElement> params = ee.getParameters();
		StringBuilder sb = new StringBuilder();
		sb.append('(');
		for (TypeElement impp : implicitparameters) {
			sb.append(getDescriptor(impp.asType()));
		}
		for (VariableElement ptm : params) {
			sb.append(getDescriptor(ptm.asType()));
		}
		sb.append(')');
		sb.append(getDescriptor(ee.getReturnType()));
		return sb.toString();
	}

	private static List<TypeElement> getImplicitInnerClassConstructorParameters(ExecutableElement ee) {
		if (ee.getKind() != ElementKind.CONSTRUCTOR) {
			return Collections.emptyList();
		}
		TypeElement type = (TypeElement) ee.getEnclosingElement();
		if (type.getKind() != ElementKind.CLASS) {
			//only add enclosing ref for class constructors
			return Collections.emptyList();
		}

		Element enclosingelement = type.getEnclosingElement();
		if (enclosingelement == null || enclosingelement.getKind() != ElementKind.CLASS
				|| type.getModifiers().contains(Modifier.STATIC)) {
			return Collections.emptyList();
		}
		return Collections.singletonList((TypeElement) enclosingelement);
	}

	private String getInternalName(TypeElement type) {
		return elements.getBinaryName(type).toString().replace('.', '/');
	}

	private class ApiJarOption implements Option {
		@Override
		public int getArgumentCount() {
			return 1;
		}

		@Override
		public String getDescription() {
			return "The API JAR to specify the included elements of the documentation.";
		}

		@Override
		public Kind getKind() {
			return Kind.STANDARD;
		}

		@Override
		public List<String> getNames() {
			return Arrays.asList("-apijar");
		}

		@Override
		public String getParameters() {
			return "<jarpath>";
		}

		@Override
		public boolean process(String option, List<String> arguments) {
			SakerDoclet.this.apiJar = Paths.get(arguments.get(0));
			return true;
		}
	}

	private class DefaultFaviconOption implements Option {
		@Override
		public int getArgumentCount() {
			return 1;
		}

		@Override
		public String getDescription() {
			return "The default favicon path of the generated html files.";
		}

		@Override
		public Kind getKind() {
			return Kind.STANDARD;
		}

		@Override
		public List<String> getNames() {
			return Arrays.asList("-favicon-default");
		}

		@Override
		public String getParameters() {
			return "<path>";
		}

		@Override
		public boolean process(String option, List<String> arguments) {
			SakerDoclet.this.favicon = SakerPath.valueOf(arguments.get(0));
			return true;
		}
	}

	private class FaviconOption implements Option {
		@Override
		public int getArgumentCount() {
			return 2;
		}

		@Override
		public String getDescription() {
			return "The favicon path for a given doc kind of the generated html files.";
		}

		@Override
		public Kind getKind() {
			return Kind.STANDARD;
		}

		@Override
		public List<String> getNames() {
			return Arrays.asList("-favicon");
		}

		@Override
		public String getParameters() {
			return "<kind> <path>";
		}

		@Override
		public boolean process(String option, List<String> arguments) {
			ElementKind elemkind = ElementKind.valueOf(arguments.get(0).toUpperCase(Locale.ENGLISH));
			SakerPath iconpath = SakerPath.valueOf(arguments.get(1));
			SakerPath prev = kindFavicons.putIfAbsent(elemkind, iconpath);
			if (prev != null) {
				throw new IllegalArgumentException("Multiple favicon for kind: " + elemkind);
			}
			return true;
		}
	}

	private class OutputDirectoryOption implements Option {
		@Override
		public int getArgumentCount() {
			return 1;
		}

		@Override
		public String getDescription() {
			return "The documentation directory output location.";
		}

		@Override
		public Kind getKind() {
			return Kind.STANDARD;
		}

		@Override
		public List<String> getNames() {
			return Arrays.asList("-d");
		}

		@Override
		public String getParameters() {
			return "<directory>";
		}

		@Override
		public boolean process(String option, List<String> arguments) {
			SakerDoclet.this.outputDirectory = Paths.get(arguments.get(0));
			return true;
		}
	}

	private class TemplateFileOption implements Option {
		@Override
		public int getArgumentCount() {
			return 1;
		}

		@Override
		public String getDescription() {
			return "The template file for the generated HTMLs.";
		}

		@Override
		public Kind getKind() {
			return Kind.STANDARD;
		}

		@Override
		public List<String> getNames() {
			return Arrays.asList("-template");
		}

		@Override
		public String getParameters() {
			return "<file path>";
		}

		@Override
		public boolean process(String option, List<String> arguments) {
			SakerDoclet.this.templateFile = Paths.get(arguments.get(0));
			return true;
		}
	}

	private class IndexTitleOption implements Option {
		@Override
		public int getArgumentCount() {
			return 1;
		}

		@Override
		public String getDescription() {
			return "The title of index.html.";
		}

		@Override
		public Kind getKind() {
			return Kind.STANDARD;
		}

		@Override
		public List<String> getNames() {
			return Arrays.asList("-index-title");
		}

		@Override
		public String getParameters() {
			return "<title>";
		}

		@Override
		public boolean process(String option, List<String> arguments) {
			SakerDoclet.this.indexTitle = arguments.get(0);
			return true;
		}
	}

	private class ExternalDocSiteOption implements Option {
		@Override
		public int getArgumentCount() {
			return 2;
		}

		@Override
		public String getDescription() {
			return "External doc site specification. The doc site should end with a slash '/'.";
		}

		@Override
		public Kind getKind() {
			return Kind.STANDARD;
		}

		@Override
		public List<String> getNames() {
			return Arrays.asList("-external-doc");
		}

		@Override
		public String getParameters() {
			return "<package-wildcard> <doc-site>";
		}

		@Override
		public boolean process(String option, List<String> arguments) {
			String sitearg = arguments.get(1);
			SakerDoclet.this.packageExternalDocSites.put(WildcardPath.valueOf(arguments.get(0).replace('.', '/')),
					"NULL".equals(sitearg) ? null : sitearg);
			return true;
		}
	}

	private class MacroOption implements Option {
		@Override
		public int getArgumentCount() {
			return 2;
		}

		@Override
		public String getDescription() {
			return "Substitution macro for the template file.";
		}

		@Override
		public Kind getKind() {
			return Kind.STANDARD;
		}

		@Override
		public List<String> getNames() {
			return Arrays.asList("-macro");
		}

		@Override
		public String getParameters() {
			return "<macro-name> <macro-value>";
		}

		@Override
		public boolean process(String option, List<String> arguments) {
			SakerDoclet.this.templateMacros.put(arguments.get(0), arguments.get(1));
			return true;
		}
	}

	private class EmbedMacroOption implements Option {
		@Override
		public int getArgumentCount() {
			return 2;
		}

		@Override
		public String getDescription() {
			return "Substitution file embedding macro for the template file.";
		}

		@Override
		public Kind getKind() {
			return Kind.STANDARD;
		}

		@Override
		public List<String> getNames() {
			return Arrays.asList("-embed-macro");
		}

		@Override
		public String getParameters() {
			return "<macro-name> <file-path>";
		}

		@Override
		public boolean process(String option, List<String> arguments) {
			SakerDoclet.this.templateEmbedMacros.put(arguments.get(0), Paths.get(arguments.get(1)));
			return true;
		}
	}

	private class ExcludeClassOption implements Option {
		@Override
		public int getArgumentCount() {
			return 1;
		}

		@Override
		public String getDescription() {
			return "Excludes the classes matching the specified wildcard. Can be passed multiple times.";
		}

		@Override
		public Kind getKind() {
			return Kind.STANDARD;
		}

		@Override
		public List<String> getNames() {
			return Arrays.asList("-exclude-class");
		}

		@Override
		public String getParameters() {
			return "<class-name-wildcard>";
		}

		@Override
		public boolean process(String option, List<String> arguments) {
			SakerDoclet.this.excludeClassWildcards.add(WildcardPath.valueOf(arguments.get(0).replace('.', '/')));
			return true;
		}
	}

}
