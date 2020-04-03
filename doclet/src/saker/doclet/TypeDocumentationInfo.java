package saker.doclet;

import java.util.HashSet;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.lang.model.element.TypeElement;

public class TypeDocumentationInfo {
	private final String internalName;
	private TypeElement element;
	/**
	 * The method name and descriptor concatenated.
	 */
	private NavigableSet<String> documentedMethods = new TreeSet<>();
	/**
	 * The field names.
	 */
	private NavigableSet<String> documentedFields = new TreeSet<>();

	private Set<TypeDocumentationInfo> subClasses = new HashSet<>();
	private Set<TypeDocumentationInfo> interfaceImplementations = new HashSet<>();

	public TypeDocumentationInfo(String internalName) {
		this.internalName = internalName;
	}

	public TypeDocumentationInfo(String internalName, TypeElement element) {
		this.internalName = internalName;
		this.element = element;
	}

	public String getInternalName() {
		return internalName;
	}

	public void addSubClass(TypeDocumentationInfo info) {
		this.subClasses.add(info);
	}

	public void addInterfaceImplementation(TypeDocumentationInfo info) {
		this.interfaceImplementations.add(info);
	}

	public Set<TypeDocumentationInfo> getSubClasses() {
		return subClasses;
	}

	public Set<TypeDocumentationInfo> getInterfaceImplementations() {
		return interfaceImplementations;
	}

	public TypeElement getElement() {
		return element;
	}

	public void setElement(TypeElement element) {
		if (element == null) {
			throw new IllegalArgumentException("null element: " + internalName);
		}
		if (this.element != null) {
			throw new IllegalStateException("Type is already set for: " + this.element.getQualifiedName());
		}
		this.element = element;
	}

	public void setDocumentedFields(NavigableSet<String> documentedFields) {
		this.documentedFields = documentedFields;
	}

	public void setDocumentedMethods(NavigableSet<String> documentedMethods) {
		this.documentedMethods = documentedMethods;
	}

	public NavigableSet<String> getDocumentedFields() {
		return documentedFields;
	}

	public NavigableSet<String> getDocumentedMethods() {
		return documentedMethods;
	}

	public boolean isMethodNameDocumented(String name) {
		String check = name + "(";
		SortedSet<String> tail = documentedMethods.tailSet(check);
		if (tail.isEmpty()) {
			return false;
		}
		String f = tail.first();
		return f.startsWith(check);
	}

	public boolean isMethodDocumented(String methodnamedescriptor) {
		return documentedMethods.contains(methodnamedescriptor);
	}

	public boolean isFieldDocumented(String name) {
		return documentedFields.contains(name);
	}

}
