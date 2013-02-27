package jini.intro;

public class InvalidLongException extends Exception {

	private String message;

	InvalidLongException(String s) {
		message = s;
	}

	public String getMessage() {
		return message;
	}
}