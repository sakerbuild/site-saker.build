package saker.docgen;

public interface PlaceholderCollection {
	public default String getBody() {
		return "<!-- DOC-BODY -->";
	}

	public default String getNavigation() {
		return "<!-- DOC-NAV -->";
	}

	public default String getTitle() {
		return "<!-- DOC-TITLE -->";
	}

	public default String getHeadingsNavigation() {
		return "<!-- DOC-HEADINGS-NAV -->";
	}

	public default String getJavaScriptLanguagesArray() {
		return "<!-- DOC-JS-LANGUAGES -->";
	}

	public default String getPreviousSection() {
		return "<!-- DOC-PREV-SECTION -->";
	}

	public default String getNextSection() {
		return "<!-- DOC-NEXT-SECTION -->";
	}

	public default String getLanguageScripts() {
		return "<!-- DOC-LANGUAGE-SCRIPTS -->";
	}

	public default String getPathToRootDir() {
		return "<!-- DOC-PATH-TO-ROOT-DIR -->";
	}
}