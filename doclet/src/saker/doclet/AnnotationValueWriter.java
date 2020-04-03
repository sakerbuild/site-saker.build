package saker.doclet;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.AnnotationValueVisitor;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import saker.build.file.path.SakerPath;

public class AnnotationValueWriter implements AnnotationValueVisitor<Void, Appendable> {
	private final SakerDoclet doclet;
	private final SakerPath pagePath;

	public AnnotationValueWriter(SakerDoclet sakerDoclet, SakerPath pagePath) {
		this.doclet = sakerDoclet;
		this.pagePath = pagePath;
	}

	@Override
	public Void visit(AnnotationValue av, Appendable p) {
		return av.accept(this, p);
	}

	private void writeAsConstantExpression(Object v, Appendable p) {
		try {
			p.append(SakerDoclet.escapeHtml(doclet.getElements().getConstantExpression(v)));
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	@Override
	public Void visitBoolean(boolean b, Appendable p) {
		try {
			p.append(DocCommentHtmlWriter.HTML_SPAN_JAVADOC_KEYWORD);
			p.append(Boolean.toString(b));
			p.append("</span>");
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		return null;
	}

	@Override
	public Void visitByte(byte b, Appendable p) {
		writeAsConstantExpression(b, p);
		return null;
	}

	@Override
	public Void visitChar(char c, Appendable p) {
		writeAsConstantExpression(c, p);
		return null;
	}

	@Override
	public Void visitDouble(double d, Appendable p) {
		writeAsConstantExpression(d, p);
		return null;
	}

	@Override
	public Void visitFloat(float f, Appendable p) {
		writeAsConstantExpression(f, p);
		return null;
	}

	@Override
	public Void visitInt(int i, Appendable p) {
		writeAsConstantExpression(i, p);
		return null;
	}

	@Override
	public Void visitLong(long i, Appendable p) {
		writeAsConstantExpression(i, p);
		return null;
	}

	@Override
	public Void visitShort(short s, Appendable p) {
		writeAsConstantExpression(s, p);
		return null;
	}

	@Override
	public Void visitString(String s, Appendable p) {
		writeAsConstantExpression(s, p);
		return null;
	}

	private void writeTypeReference(TypeMirror tm, Appendable out) throws IOException {
		switch (tm.getKind()) {
			case ARRAY: {
				ArrayType at = (ArrayType) tm;
				writeTypeReference(at.getComponentType(), out);
				out.append("[]");
				break;
			}
			case BOOLEAN:
			case BYTE:
			case CHAR:
			case DOUBLE:
			case INT:
			case LONG:
			case SHORT:
			case FLOAT:
			case VOID: {
				out.append(tm.getKind().name().toLowerCase(Locale.ENGLISH));
				break;
			}

			case DECLARED: {
				DeclaredType dt = (DeclaredType) tm;
				TypeElement aselem = (TypeElement) dt.asElement();
				if (doclet.isTypeDocumented(aselem)) {
					out.append(doclet.getAHrefTagForElement(pagePath, aselem));
					out.append(aselem.getSimpleName());
					out.append("</a>");
				} else {
					writeUndocumentedTypeReference(out, aselem);
					System.out.println("Warning: Undocumented type in annotation: " + aselem);
				}
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
			case TYPEVAR:
			case UNION:
			case WILDCARD:
			default: {
				throw new UnsupportedOperationException("Invalid annotation type: " + tm);
			}
		}
	}

	private void writeUndocumentedTypeReference(Appendable out, TypeElement type) throws IOException {
		if (type.getNestingKind() == NestingKind.MEMBER) {
			writeUndocumentedEnclosingTypeReference(out, (TypeElement) type.getEnclosingElement());
			out.append(".<span title=\"Undocumented ");
			SakerDoclet.writeTypeElementKindKeyword(type, out);
			out.append(type.getQualifiedName());
			out.append("\">");
			out.append(type.getSimpleName());
			out.append("</span>");
		} else {
			out.append("<span title=\"Undocumented ");
			SakerDoclet.writeTypeElementKindKeyword(type, out);
			out.append(type.getQualifiedName());
			out.append("\">");
			out.append(type.getQualifiedName());
			out.append("</span>");
		}
	}

	private void writeUndocumentedEnclosingTypeReference(Appendable out, TypeElement type) throws IOException {
		if (doclet.isTypeDocumented(type)) {
			out.append(doclet.getAHrefTagForElement(pagePath, type));
			out.append(type.getSimpleName());
			out.append("</a>");
		} else {
			writeUndocumentedTypeReference(out, type);
		}
	}

	@Override
	public Void visitType(TypeMirror t, Appendable p) {
		try {
			writeTypeReference(t, p);
			p.append("." + DocCommentHtmlWriter.HTML_SPAN_JAVADOC_KEYWORD + "class</span>");
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		return null;
	}

	@Override
	public Void visitEnumConstant(VariableElement c, Appendable p) {
		try {
			DocCommentHtmlWriter.writeTypeMirror(doclet, pagePath, c.getEnclosingElement().asType(), p,
					DocCommentHtmlWriter.TM_LINK_TYPES | DocCommentHtmlWriter.TM_NO_CODE_SPAN_CLASS);
			p.append(".<wbr>");
			p.append(doclet.getAHrefTagForElement(this.pagePath, c));
			p.append(c.getSimpleName());
			p.append("</a>");
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		return null;
	}

	@Override
	public Void visitAnnotation(AnnotationMirror a, Appendable p) {
		try {
			DocCommentHtmlWriter.writeTypeMirror(doclet, pagePath, a.getAnnotationType(), p,
					DocCommentHtmlWriter.TM_LINK_TYPES | DocCommentHtmlWriter.TM_NO_CODE_SPAN_CLASS
							| DocCommentHtmlWriter.TM_INCLUDE_ANNOT_SIGN);
			Map<? extends ExecutableElement, ? extends AnnotationValue> elementvals = a.getElementValues();
			if (!elementvals.isEmpty()) {
				p.append("(<wbr>");
				if (elementvals.size() == 1
						&& elementvals.keySet().iterator().next().getSimpleName().contentEquals("value")) {
					elementvals.values().iterator().next().accept(this, p);
				} else {
					for (Iterator<? extends Entry<? extends ExecutableElement, ? extends AnnotationValue>> it = elementvals
							.entrySet().iterator(); it.hasNext();) {
						Entry<? extends ExecutableElement, ? extends AnnotationValue> entry = it.next();
						ExecutableElement annotmethod = entry.getKey();
						p.append(doclet.getAHrefTagForElement(pagePath, annotmethod));
						p.append(annotmethod.getSimpleName());
						p.append("</a>&nbsp;= ");
						entry.getValue().accept(this, p);
						if (it.hasNext()) {
							p.append(", ");
						}
					}
				}
				p.append(')');
			}
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		return null;
	}

	@Override
	public Void visitArray(List<? extends AnnotationValue> vals, Appendable p) {
		try {
			if (vals.isEmpty()) {
				p.append("{}");
			} else if (vals.size() == 1) {
				vals.get(0).accept(this, p);
			} else {
				p.append("{ ");
				for (Iterator<? extends AnnotationValue> it = vals.iterator(); it.hasNext();) {
					AnnotationValue av = it.next();
					av.accept(this, p);
					if (it.hasNext()) {
						p.append(", ");
					}
				}
				p.append(" }");
			}
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		return null;
	}

	@Override
	public Void visitUnknown(AnnotationValue av, Appendable p) {
		throw new UnsupportedOperationException(av.toString());
	}

}