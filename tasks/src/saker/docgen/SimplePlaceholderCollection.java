package saker.docgen;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class SimplePlaceholderCollection implements PlaceholderCollection, Externalizable {
	private static final long serialVersionUID = 1L;

	private String body;
	private String navigation;
	private String title;
	private String javaScriptLanguagesArray;
	private String headingsNavigation;
	private String previousSection;
	private String nextSection;
	private String languageScripts;

	/**
	 * For {@link Externalizable}.
	 */
	public SimplePlaceholderCollection() {
	}

	public SimplePlaceholderCollection(PlaceholderCollection copy) {
		this.body = copy.getBody();
		this.navigation = copy.getNavigation();
		this.title = copy.getTitle();
		this.javaScriptLanguagesArray = copy.getJavaScriptLanguagesArray();
		this.headingsNavigation = copy.getHeadingsNavigation();
		this.previousSection = copy.getPreviousSection();
		this.nextSection = copy.getNextSection();
		this.languageScripts = copy.getLanguageScripts();
	}

	@Override
	public String getBody() {
		return body;
	}

	@Override
	public String getNavigation() {
		return navigation;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public String getHeadingsNavigation() {
		return headingsNavigation;
	}

	@Override
	public String getJavaScriptLanguagesArray() {
		return javaScriptLanguagesArray;
	}

	@Override
	public String getPreviousSection() {
		return previousSection;
	}

	@Override
	public String getNextSection() {
		return nextSection;
	}

	@Override
	public String getLanguageScripts() {
		return languageScripts;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((body == null) ? 0 : body.hashCode());
		result = prime * result + ((headingsNavigation == null) ? 0 : headingsNavigation.hashCode());
		result = prime * result + ((javaScriptLanguagesArray == null) ? 0 : javaScriptLanguagesArray.hashCode());
		result = prime * result + ((languageScripts == null) ? 0 : languageScripts.hashCode());
		result = prime * result + ((navigation == null) ? 0 : navigation.hashCode());
		result = prime * result + ((nextSection == null) ? 0 : nextSection.hashCode());
		result = prime * result + ((previousSection == null) ? 0 : previousSection.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
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
		SimplePlaceholderCollection other = (SimplePlaceholderCollection) obj;
		if (body == null) {
			if (other.body != null)
				return false;
		} else if (!body.equals(other.body))
			return false;
		if (headingsNavigation == null) {
			if (other.headingsNavigation != null)
				return false;
		} else if (!headingsNavigation.equals(other.headingsNavigation))
			return false;
		if (javaScriptLanguagesArray == null) {
			if (other.javaScriptLanguagesArray != null)
				return false;
		} else if (!javaScriptLanguagesArray.equals(other.javaScriptLanguagesArray))
			return false;
		if (languageScripts == null) {
			if (other.languageScripts != null)
				return false;
		} else if (!languageScripts.equals(other.languageScripts))
			return false;
		if (navigation == null) {
			if (other.navigation != null)
				return false;
		} else if (!navigation.equals(other.navigation))
			return false;
		if (nextSection == null) {
			if (other.nextSection != null)
				return false;
		} else if (!nextSection.equals(other.nextSection))
			return false;
		if (previousSection == null) {
			if (other.previousSection != null)
				return false;
		} else if (!previousSection.equals(other.previousSection))
			return false;
		if (title == null) {
			if (other.title != null)
				return false;
		} else if (!title.equals(other.title))
			return false;
		return true;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(body);
		out.writeObject(navigation);
		out.writeObject(title);
		out.writeObject(headingsNavigation);
		out.writeObject(javaScriptLanguagesArray);
		out.writeObject(previousSection);
		out.writeObject(nextSection);
		out.writeObject(languageScripts);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		body = (String) in.readObject();
		navigation = (String) in.readObject();
		title = (String) in.readObject();
		headingsNavigation = (String) in.readObject();
		javaScriptLanguagesArray = (String) in.readObject();
		previousSection = (String) in.readObject();
		nextSection = (String) in.readObject();
		languageScripts = (String) in.readObject();
	}

}