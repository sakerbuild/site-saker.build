package saker.doclet;

public interface ErrorReporter {
	public default void error(String message) {
		error(message, null);
	}

	public default void error(Throwable cause) {
		error(null, cause);
	}

	public void error(String message, Throwable cause);
}
