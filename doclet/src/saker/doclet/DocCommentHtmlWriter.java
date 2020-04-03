package saker.doclet;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import javax.lang.model.AnnotatedConstruct;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.WildcardType;

import com.sun.source.doctree.AttributeTree;
import com.sun.source.doctree.AuthorTree;
import com.sun.source.doctree.CommentTree;
import com.sun.source.doctree.DeprecatedTree;
import com.sun.source.doctree.DocCommentTree;
import com.sun.source.doctree.DocRootTree;
import com.sun.source.doctree.DocTree;
import com.sun.source.doctree.DocTreeVisitor;
import com.sun.source.doctree.EndElementTree;
import com.sun.source.doctree.EntityTree;
import com.sun.source.doctree.ErroneousTree;
import com.sun.source.doctree.HiddenTree;
import com.sun.source.doctree.IdentifierTree;
import com.sun.source.doctree.IndexTree;
import com.sun.source.doctree.InheritDocTree;
import com.sun.source.doctree.LinkTree;
import com.sun.source.doctree.LiteralTree;
import com.sun.source.doctree.ParamTree;
import com.sun.source.doctree.ProvidesTree;
import com.sun.source.doctree.ReferenceTree;
import com.sun.source.doctree.ReturnTree;
import com.sun.source.doctree.SeeTree;
import com.sun.source.doctree.SerialDataTree;
import com.sun.source.doctree.SerialFieldTree;
import com.sun.source.doctree.SerialTree;
import com.sun.source.doctree.SinceTree;
import com.sun.source.doctree.StartElementTree;
import com.sun.source.doctree.TextTree;
import com.sun.source.doctree.ThrowsTree;
import com.sun.source.doctree.UnknownBlockTagTree;
import com.sun.source.doctree.UnknownInlineTagTree;
import com.sun.source.doctree.UsesTree;
import com.sun.source.doctree.ValueTree;
import com.sun.source.doctree.VersionTree;
import com.sun.source.util.DocTreePath;
import com.sun.source.util.DocTrees;
import com.sun.source.util.TreePath;

import saker.build.file.path.SakerPath;
import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.doclet.SakerDoclet.LinkedDoc;

public class DocCommentHtmlWriter implements DocTreeVisitor<Void, Void> {
	public static final String HTML_CLASS_JAVADOC_KEYWORD = "javadoc-kw";
	public static final String HTML_CLASS_CODE_SPAN = "javadoc-code-ref";
	public static final String HTML_CLASS_JAVADOC_TYPEPARAM = "javadoc-tp";
	public static final String HTML_SPAN_CODE = "<span class=\"" + HTML_CLASS_CODE_SPAN + "\">";
	public static final String HTML_SPAN_JAVADOC_KEYWORD = "<span class=\"" + HTML_CLASS_JAVADOC_KEYWORD + "\">";
	public static final String HTML_SPAN_JAVADOC_TYPEPARAM = "<span class=\"" + HTML_CLASS_JAVADOC_TYPEPARAM + "\">";

	public static final Set<String> JAVA_KEYWORDS = ImmutableUtils.makeImmutableNavigableSet(new String[] { "abstract",
			"assert", "boolean", "break", "byte", "case", "catch", "char", "class", "continue", "default", "do",
			"double", "else", "enum", "extends", "final", "finally", "float", "for", "if", "implements", "import",
			"instanceof", "int", "interface", "long", "native", "new", "null", "package", "private", "protected",
			"public", "return", "short", "static", "strictfp", "super", "switch", "synchronized", "this", "throw",
			"throws", "transient", "try", "void", "volatile", "while", });

	private SakerDoclet doclet;
	private String docRoot = "";
	private DocTrees trees;
	private Appendable out;
	private TreePath treePath;
	private DocCommentTree commentTree;
	private SakerPath pagePath;

	private boolean firstSentenceOnly;
	private Element autoLinkRelativeElement;
	private boolean autoLinkRelativeNoLinkSplit;

	public DocCommentHtmlWriter(SakerDoclet doclet, DocTrees trees, Appendable out, TreePath treepath,
			DocCommentTree commenttree, SakerPath pagePath) {
		Objects.requireNonNull(treepath, "tree path");
		this.doclet = doclet;
		this.trees = trees;
		this.out = out;
		this.treePath = treepath;
		this.commentTree = commenttree;
		this.pagePath = pagePath;
	}

	public void write(DocTree tree) {
		try {
			tree.accept(this, null);
		} catch (Exception e) {
			e.addSuppressed(new RuntimeException("Failed to write for " + pagePath));
			throw e;
		}
	}

	public void setDocRoot(String docRoot) {
		this.docRoot = docRoot;
	}

	public void setFirstSentenceOnly(boolean firstSentenceOnly) {
		this.firstSentenceOnly = firstSentenceOnly;
	}

	public void setAutoLinkRelativeElement(Element autoLinkRelativeElement) {
		this.autoLinkRelativeElement = autoLinkRelativeElement;
	}

	public void setAutoLinkRelativeNoLinkSplit(boolean autoLinkRelativeNoLinkSplit) {
		this.autoLinkRelativeNoLinkSplit = autoLinkRelativeNoLinkSplit;
	}

	@Override
	public Void visitAttribute(AttributeTree node, Void p) {
		try {
			out.append(node.getName());
			out.append('=');
			String quote;
			switch (node.getValueKind()) {
				case DOUBLE: {
					quote = "\"";
					break;
				}
				case EMPTY: {
					out.append("\"\"");
					return null;
				}
				case SINGLE: {
					quote = "\'";
					break;
				}
				case UNQUOTED: {
					quote = "\"";
					break;
				}
				default: {
					throw new UnsupportedOperationException(node.getValueKind().toString());
				}
			}
			out.append(quote);
			List<? extends DocTree> v = node.getValue();
			if (!ObjectUtils.isNullOrEmpty(v)) {
				for (DocTree t : v) {
					write(t);
				}
			}
			out.append(quote);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		return null;
	}

	@Override
	public Void visitAuthor(AuthorTree node, Void p) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Void visitComment(CommentTree node, Void p) {
		return null;
	}

	@Override
	public Void visitDeprecated(DeprecatedTree node, Void p) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Void visitDocComment(DocCommentTree node, Void p) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Void visitDocRoot(DocRootTree node, Void p) {
		try {
			out.append(docRoot);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		return null;
	}

	@Override
	public Void visitEndElement(EndElementTree node, Void p) {
		try {
			out.append("</");
			out.append(node.getName());
			out.append(">");
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		return null;
	}

	@Override
	public Void visitEntity(EntityTree node, Void p) {
		try {
			out.append('&');
			out.append(node.getName());
			out.append(';');
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		return null;
	}

	@Override
	public Void visitErroneous(ErroneousTree node, Void p) {
		System.out.println(node.getDiagnostic());
		System.out.println(trees.getSourcePositions().getStartPosition(treePath.getCompilationUnit(), commentTree, node)
				+ " - " + trees.getSourcePositions().getEndPosition(treePath.getCompilationUnit(), commentTree, node));
		throw new IllegalArgumentException(
				"Erroneous tree: " + node + " in " + this.treePath.getCompilationUnit().getSourceFile());
	}

	@Override
	public Void visitHidden(HiddenTree node, Void p) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Void visitIdentifier(IdentifierTree node, Void p) {
		try {
			out.append(node.getName());
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		return null;
	}

	@Override
	public Void visitIndex(IndexTree node, Void p) {
		//TODO visitIndex is this relevant?
		throw new UnsupportedOperationException();
	}

	@Override
	public Void visitInheritDoc(InheritDocTree node, Void p) {
		try {
			Element documentedelem = doclet.dePseudoize(trees.getElement(treePath));
			List<LinkedDoc<ExecutableElement>> inheriteddocs = doclet
					.collectInheritedDocumentations((ExecutableElement) documentedelem);
			if (inheriteddocs != null) {
				LinkedDoc<ExecutableElement> inheritdocinfo = SakerDoclet.getFirstDocumentedDoc(inheriteddocs);
				if (inheritdocinfo == null) {
					//TODO handle better
					doclet.appendInheritedDocumentationNotFoundFirstSentence(inheriteddocs.get(0).getDocElement(), out,
							pagePath, inheriteddocs);
					return null;
				}
				ExecutableElement inheritelem = inheritdocinfo.getDocElement();
				DocCommentTree doccomment = inheritdocinfo.getDocTree();
				DocCommentHtmlWriter writer = new DocCommentHtmlWriter(doclet, trees, out, trees.getPath(inheritelem),
						doccomment, pagePath);
				writer.setFirstSentenceOnly(this.firstSentenceOnly);
				for (DocTree dt : doccomment.getFirstSentence()) {
					writer.write(dt);
				}
				if (!firstSentenceOnly) {
					for (DocTree dt : doccomment.getBody()) {
						writer.write(dt);
					}
				}
				return null;
			}
			throw new IllegalArgumentException("Inherited documentation not found for: "
					+ documentedelem.getEnclosingElement() + "." + documentedelem);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private static List<Element> getEnclosingElementPath(Element e) {
		LinkedList<Element> result = new LinkedList<>();
		while (e != null) {
			if (e.getKind() == ElementKind.MODULE) {
				break;
			}
			result.addFirst(e);
			e = e.getEnclosingElement();
		}
		return result;
	}

	private List<Element> getRelativeAutoLinkElementPath(Element linkedelement) {
		Element documentedelement = autoLinkRelativeElement == null ? doclet.dePseudoize(trees.getElement(treePath))
				: autoLinkRelativeElement;
		List<Element> searchenclosings = getEnclosingElementPath(linkedelement.getEnclosingElement());
		List<Element> docenclosings = getEnclosingElementPath(documentedelement);
		LinkedList<Element> result = new LinkedList<>();
		Iterator<Element> searchit = searchenclosings.iterator();
		Iterator<Element> docit = docenclosings.iterator();
		while (searchit.hasNext()) {
			Element sn = searchit.next();
			if (!docit.hasNext()) {
				result.add(sn);
			} else {
				Element dn = docit.next();
				if (dn.equals(sn)) {
					continue;
				}
				result.add(sn);
				while (searchit.hasNext()) {
					sn = searchit.next();
					result.add(sn);
				}
				break;
			}
		}
		return result;
	}

	private void writeRelativePath(List<Element> relpath, boolean linkelements) throws IOException {
		if (relpath.isEmpty()) {
			return;
		}
		for (Element e : relpath) {
			switch (e.getKind()) {
				case PACKAGE: {
					//don't write package
					continue;
				}
				case ANNOTATION_TYPE:
				case CLASS:
				case ENUM:
				case INTERFACE: {
					if (linkelements) {
						writeRefTagStartToElement(e);
						out.append(((TypeElement) e).getSimpleName());
						out.append("</a>");
					} else {
						out.append(((TypeElement) e).getSimpleName());
					}
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
					throw new UnsupportedOperationException(e.getKind().toString() + " - " + e);
				}
			}
			out.append('.');
		}
	}

	public static final int TM_NONE = 0;
	public static final int TM_LAST_VARARG = 1 << 1;
	public static final int TM_LINK_TYPES = 1 << 2;
	public static final int TM_FULLY_QUALIFIED = 1 << 3;
	public static final int TM_PARAMETER_NAMES = 1 << 4;
	public static final int TM_PARAM_TYPES_SIMPLE = 1 << 5;
	public static final int TM_NO_CODE_SPAN_CLASS = 1 << 6;
	public static final int TM_NO_HTML = 1 << 7;
	public static final int TM_NO_TYPE_ARGS = 1 << 8;
	public static final int TM_SPAN_PARAMETER_NAMES = 1 << 9;
	public static final int TM_WITH_DOCUMENTED_ANNOTATIONS = 1 << 10;
	public static final int TM_INCLUDE_ANNOT_SIGN = 1 << 11;

	public static void writeReferenceParameterTypes(SakerDoclet doclet, SakerPath pagePath, ExecutableElement ee,
			int flags, Appendable out) throws IOException {
		List<? extends VariableElement> params = ee.getParameters();
		if (((flags & TM_NO_CODE_SPAN_CLASS) != TM_NO_CODE_SPAN_CLASS) && ((flags & TM_NO_HTML) != TM_NO_HTML)) {
			out.append(HTML_SPAN_CODE);
		}
		out.append('(');
		if (!params.isEmpty()) {
			if (((flags & TM_NO_HTML) != TM_NO_HTML)) {
				out.append("<wbr>");
			}
			for (Iterator<? extends VariableElement> it = params.iterator(); it.hasNext();) {
				VariableElement pe = it.next();
				TypeMirror type = pe.asType();
				if (((flags & TM_NO_HTML) != TM_NO_HTML)
						&& ((flags & TM_SPAN_PARAMETER_NAMES) == TM_SPAN_PARAMETER_NAMES)) {
					out.append("<span class=\"javadoc-method-param\">");
				}
				writeAnnotationsForAnnotatedConstruct(doclet, pagePath, pe, out, flags);
				writeTypeMirrorImpl(doclet, pagePath, type, out,
						flags | (!it.hasNext() && ee.isVarArgs() ? TM_LAST_VARARG : 0));
				if (((flags & TM_PARAMETER_NAMES) == TM_PARAMETER_NAMES)) {
					out.append(' ');
					out.append(pe.getSimpleName());
				}
				if (it.hasNext()) {
					out.append(", ");
				}
				if (((flags & TM_NO_HTML) != TM_NO_HTML)
						&& ((flags & TM_SPAN_PARAMETER_NAMES) == TM_SPAN_PARAMETER_NAMES)) {
					out.append("</span>");
				}
			}
		}
		out.append(')');
		if (((flags & TM_NO_CODE_SPAN_CLASS) != TM_NO_CODE_SPAN_CLASS) && ((flags & TM_NO_HTML) != TM_NO_HTML)) {
			out.append("</span>");
		}
	}

	public static void writeTypeMirror(SakerDoclet doclet, SakerPath pagePath, TypeMirror type, Appendable out,
			int flags) throws IOException {
		if (((flags & TM_NO_CODE_SPAN_CLASS) != TM_NO_CODE_SPAN_CLASS) && ((flags & TM_NO_HTML) != TM_NO_HTML)) {
			out.append(HTML_SPAN_CODE);
		}
		writeTypeMirrorImpl(doclet, pagePath, type, out, flags);
		if (((flags & TM_NO_CODE_SPAN_CLASS) != TM_NO_CODE_SPAN_CLASS) && ((flags & TM_NO_HTML) != TM_NO_HTML)) {
			out.append("</span>");
		}
	}

	/**
	 * @return Sorted by simple name of the annotation
	 */
	public static List<AnnotationMirror> filterDocumentedAnnotations(SakerDoclet doclet,
			List<? extends AnnotationMirror> ams) {
		if (ams.isEmpty()) {
			return Collections.emptyList();
		}
		List<AnnotationMirror> result = new ArrayList<>();
		for (AnnotationMirror am : ams) {
			if (!doclet.isAnnotationDocumented(am)) {
				continue;
			}
			result.add(am);
		}
		result.sort((l, r) -> l.getAnnotationType().asElement().getSimpleName().toString()
				.compareTo(r.getAnnotationType().asElement().getSimpleName().toString()));
		return result;
	}

	private static void writeTypeMirrorImpl(SakerDoclet doclet, SakerPath pagePath, TypeMirror type, Appendable out,
			int flags) throws IOException {
		writeAnnotationsForAnnotatedConstruct(doclet, pagePath, type, out, flags);
		TypeKind kind = type.getKind();
		switch (kind) {
			case ARRAY: {
				writeTypeMirrorImpl(doclet, pagePath, ((ArrayType) type).getComponentType(), out,
						flags & ~TM_LAST_VARARG);
				if (((flags & TM_LAST_VARARG) == TM_LAST_VARARG)) {
					out.append("...");
				} else {
					out.append("[]");
				}
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
				if (((flags & TM_NO_HTML) != TM_NO_HTML)) {
					out.append(HTML_SPAN_JAVADOC_KEYWORD);
				}
				out.append(kind.name().toLowerCase(Locale.ENGLISH));
				if (((flags & TM_NO_HTML) != TM_NO_HTML)) {
					out.append("</span>");
				}
				break;
			}

			case DECLARED: {
				DeclaredType dt = (DeclaredType) type;
				TypeMirror enc = dt.getEnclosingType();
				TypeElement aselem = (TypeElement) dt.asElement();
				if (enc.getKind() != TypeKind.NONE) {
					writeTypeMirrorImpl(doclet, pagePath, enc, out, flags);
					out.append(".");
					if (((flags & TM_NO_HTML) != TM_NO_HTML)) {
						out.append("<wbr>");
					}
				}
				String name = (((flags & TM_FULLY_QUALIFIED) == TM_FULLY_QUALIFIED) ? aselem.getQualifiedName()
						: aselem.getSimpleName()).toString();
				if (((flags & TM_INCLUDE_ANNOT_SIGN) == TM_INCLUDE_ANNOT_SIGN)
						&& aselem.getKind() == ElementKind.ANNOTATION_TYPE) {
					name = '@' + name;
				}
				if (((flags & TM_LINK_TYPES) == TM_LINK_TYPES) && ((flags & TM_NO_HTML) != TM_NO_HTML)) {
					out.append(doclet.getAHrefTagForElement(pagePath, aselem));
					out.append(name);
					out.append("</a>");
				} else {
					out.append(name);
				}
				if (((flags & TM_NO_TYPE_ARGS) != TM_NO_TYPE_ARGS)) {
					List<? extends TypeMirror> args = dt.getTypeArguments();
					if (!ObjectUtils.isNullOrEmpty(args)) {
						int pflags = flags;
						if (((flags & TM_PARAM_TYPES_SIMPLE) == TM_PARAM_TYPES_SIMPLE)) {
							pflags = pflags & ~TM_FULLY_QUALIFIED;
						}
						if (((flags & TM_NO_HTML) != TM_NO_HTML)) {
							out.append("&lt;<wbr>");
						} else {
							out.append("<");
						}
						for (Iterator<? extends TypeMirror> it = args.iterator(); it.hasNext();) {
							TypeMirror arg = it.next();
							writeTypeMirrorImpl(doclet, pagePath, arg, out, pflags);
							if (it.hasNext()) {
								out.append(", ");
							}
						}
						if (((flags & TM_NO_HTML) != TM_NO_HTML)) {
							out.append("&gt;");
						} else {
							out.append(">");
						}
					}
				}
				break;
			}
			case TYPEVAR: {
				TypeVariable tv = (TypeVariable) type;
				TypeParameterElement telem = (TypeParameterElement) tv.asElement();

				if (((flags & TM_NO_HTML) != TM_NO_HTML)) {
					out.append("<span class=\"" + HTML_CLASS_JAVADOC_TYPEPARAM + "\" title=\"");
					writeTypeParameterTagTitle(doclet, pagePath, telem, out);
					out.append("\">");
				}
				out.append(telem.getSimpleName());
				if (((flags & TM_NO_HTML) != TM_NO_HTML)) {
					out.append("</span>");
				}
				break;
			}
			case WILDCARD: {
				out.append('?');
				WildcardType wt = (WildcardType) type;
				TypeMirror sup = wt.getSuperBound();
				if (sup != null) {
					if (((flags & TM_NO_HTML) != TM_NO_HTML)) {
						out.append("&nbsp;");
						out.append(HTML_SPAN_JAVADOC_KEYWORD);
					} else {
						out.append(' ');
					}
					out.append("super");
					if (((flags & TM_NO_HTML) != TM_NO_HTML)) {
						out.append("</span>");
					}
					out.append(' ');
					writeTypeMirrorImpl(doclet, pagePath, sup, out, flags);
				}
				TypeMirror ext = wt.getExtendsBound();
				if (ext != null) {
					if (((flags & TM_NO_HTML) != TM_NO_HTML)) {
						out.append("&nbsp;");
						out.append(HTML_SPAN_JAVADOC_KEYWORD);
					} else {
						out.append(' ');
					}
					out.append("extends");
					if (((flags & TM_NO_HTML) != TM_NO_HTML)) {
						out.append("</span>");
					}
					out.append(' ');
					writeTypeMirrorImpl(doclet, pagePath, ext, out, flags);
				}
				break;
			}
			case ERROR:
			case INTERSECTION:
			case NONE:
			case NULL:
			case OTHER:
			case PACKAGE:
			case UNION:
			case MODULE:
			default: {
				throw new UnsupportedOperationException(kind + " - " + type);
			}
		}
	}

	public static void writeTypeParameterTagTitle(SakerDoclet doclet, SakerPath pagePath, TypeParameterElement telem,
			Appendable out) throws IOException {
		doclet.writeElementTagTitle(pagePath, telem, out);
		out.append(" of ");
		doclet.writeElementTagTitle(pagePath, telem.getGenericElement(), out);
	}

	private static void writeAnnotationsForAnnotatedConstruct(SakerDoclet doclet, SakerPath pagePath,
			AnnotatedConstruct type, Appendable out, int flags) throws IOException {
		if (((flags & TM_WITH_DOCUMENTED_ANNOTATIONS) == TM_WITH_DOCUMENTED_ANNOTATIONS)) {
			List<? extends AnnotationMirror> annots = filterDocumentedAnnotations(doclet, type.getAnnotationMirrors());
			if (!ObjectUtils.isNullOrEmpty(annots)) {
				AnnotationValueWriter writer = new AnnotationValueWriter(doclet, pagePath);
				for (AnnotationMirror am : annots) {
					if (((flags & TM_NO_HTML) != TM_NO_HTML)) {
						out.append("<span class=\"javadoc-annot\">");
					}
					writer.visitAnnotation(am, out);
					if (((flags & TM_NO_HTML) != TM_NO_HTML)) {
						out.append("</span>");
					}
				}
			}
		}
	}

	private void visitLinkImpl(LinkTree node, Void p) throws IOException {
		List<? extends DocTree> label = node.getLabel();
		boolean codelinking = "link".equals(node.getTagName());

		ReferenceTree reftree = node.getReference();
		if (reftree == null) {
			throw new IllegalArgumentException(
					"Null reference for link: " + node + " in " + this.treePath.getCompilationUnit().getSourceFile());
		}
		visitReferenceLink(label, codelinking, reftree);
	}

	private void visitReferenceLink(List<? extends DocTree> label, boolean codelinking, ReferenceTree reftree)
			throws IOException {
		DocTreePath treepath = DocTreePath.getPath(treePath, commentTree, reftree);
		Element linkelem = doclet.dePseudoize(trees.getElement(treepath));
		if (linkelem == null) {
			throw new IllegalArgumentException("Referenced element not found: " + reftree + " in "
					+ this.treePath.getCompilationUnit().getSourceFile());
		}

		if (codelinking) {
			out.append(HTML_SPAN_CODE);
			if (autoLinkRelativeNoLinkSplit) {
				writeRefTagStartToElement(linkelem);
			}
			if (label.isEmpty()) {
				List<Element> relpath = getRelativeAutoLinkElementPath(linkelem);
				writeRelativePath(relpath, !autoLinkRelativeNoLinkSplit);
			}
		}
		if (!codelinking || !autoLinkRelativeNoLinkSplit) {
			writeRefTagStartToElement(linkelem);
		}
		if (label.isEmpty()) {
			switch (linkelem.getKind()) {
				case ANNOTATION_TYPE:
				case INTERFACE:
				case ENUM:
				case CLASS: {
					out.append(linkelem.getSimpleName());
					break;
				}
				case FIELD:
				case ENUM_CONSTANT: {
					out.append(linkelem.getSimpleName());
					break;
				}
				case CONSTRUCTOR: {
					out.append(linkelem.getEnclosingElement().getSimpleName());
					ExecutableElement ee = (ExecutableElement) linkelem;
					writeReferenceParameterTypes(doclet, pagePath, ee, TM_NONE, out);
					break;
				}
				case METHOD: {
					out.append(linkelem.getSimpleName());
					ExecutableElement ee = (ExecutableElement) linkelem;
					writeReferenceParameterTypes(doclet, pagePath, ee, TM_NONE, out);
					break;
				}
				case PACKAGE: {
					out.append(((PackageElement) linkelem).getQualifiedName());
					break;
				}
				default: {
					throw new UnsupportedOperationException(linkelem.getKind().toString());
				}
			}
		} else {
			for (DocTree t : label) {
				write(t);
			}
		}
		out.append("</a>");
		if (codelinking) {
			out.append("</span>");
		}
	}

	private void writeRefTagStartToElement(Element linkelem) throws IOException {
		out.append(doclet.getAHrefTagForElement(pagePath, linkelem));
	}

	@Override
	public Void visitLink(LinkTree node, Void p) {
		try {
			visitLinkImpl(node, p);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		return null;
	}

	@Override
	public Void visitLiteral(LiteralTree node, Void p) {
		try {
			switch (node.getTagName()) {
				case "literal": {
					write(node.getBody());
					break;
				}
				case "code": {
					out.append("<code>");
					write(node.getBody());
					out.append("</code>");
					break;
				}
				default: {
					throw new UnsupportedOperationException(node.getTagName());
				}
			}
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		return null;
	}

	@Override
	public Void visitParam(ParamTree node, Void p) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Void visitProvides(ProvidesTree node, Void p) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Void visitReference(ReferenceTree node, Void p) {
		try {
			visitReferenceLink(Collections.emptyList(), true, node);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		return null;
	}

	@Override
	public Void visitReturn(ReturnTree node, Void p) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Void visitSee(SeeTree node, Void p) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Void visitSerial(SerialTree node, Void p) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Void visitSerialData(SerialDataTree node, Void p) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Void visitSerialField(SerialFieldTree node, Void p) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Void visitSince(SinceTree node, Void p) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Void visitStartElement(StartElementTree node, Void p) {
		try {
			out.append("<");
			out.append(node.getName());
			List<? extends DocTree> attributes = node.getAttributes();
			if (!ObjectUtils.isNullOrEmpty(attributes)) {
				out.append(' ');
				for (Iterator<? extends DocTree> it = attributes.iterator(); it.hasNext();) {
					DocTree attr = it.next();
					write(attr);
					if (it.hasNext()) {
						out.append(' ');
					}
				}
			}
			if (node.isSelfClosing()) {
				out.append("/>");
			} else {
				out.append(">");
			}
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		return null;
	}

	@Override
	public Void visitText(TextTree node, Void p) {
		try {
			out.append(node.getBody());
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		return null;
	}

	@Override
	public Void visitThrows(ThrowsTree node, Void p) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Void visitUnknownBlockTag(UnknownBlockTagTree node, Void p) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Void visitUnknownInlineTag(UnknownInlineTagTree node, Void p) {
		throw new UnsupportedOperationException(node.getTagName());
	}

	@Override
	public Void visitUses(UsesTree node, Void p) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Void visitValue(ValueTree node, Void p) {
		ReferenceTree reftree = node.getReference();
		DocTreePath treepath = DocTreePath.getPath(treePath, commentTree, reftree);
		Element linkelem = doclet.dePseudoize(trees.getElement(treepath));
		if (linkelem == null) {
			throw new IllegalArgumentException("Referenced element not found: " + reftree + " in "
					+ this.treePath.getCompilationUnit().getSourceFile());
		}
		if (linkelem.getKind() != ElementKind.FIELD) {
			throw new IllegalArgumentException(
					"Invalid @value reference kind: " + linkelem.getKind() + " - " + linkelem);
		}
		VariableElement ve = (VariableElement) linkelem;
		Object cval = ve.getConstantValue();
		if (cval == null) {
			throw new IllegalArgumentException("@value reference is not constant value: " + linkelem);
		}
		try {
			String valstr = doclet.getElements().getConstantExpression(cval);

			boolean documented = doclet.isFieldDocumented(ve);
			out.append(HTML_SPAN_CODE);
			if (documented) {
				out.append(doclet.getAHrefTagForElement(pagePath, ve));
			}
			out.append(valstr);
			if (documented) {
				out.append("</a>");
			}
			out.append("</span>");
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		return null;
	}

	@Override
	public Void visitVersion(VersionTree node, Void p) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Void visitOther(DocTree node, Void p) {
		throw new UnsupportedOperationException(node.getKind().toString());
	}

}
