package saker.doclet;

import javax.lang.model.element.PackageElement;

public class PackageDocumentationInfo {
	private String packageName;
	private PackageElement element;

	public PackageDocumentationInfo(String packageName) {
		this.packageName = packageName;
	}

	public void setElement(PackageElement element) {
		this.element = element;
	}

	public PackageElement getElement() {
		return element;
	}

	public String getPackageName() {
		return packageName;
	}

	@Override
	public String toString() {
		return "PackageDocumentationInfo[" + (packageName != null ? "packageName=" + packageName : "") + "]";
	}

}
