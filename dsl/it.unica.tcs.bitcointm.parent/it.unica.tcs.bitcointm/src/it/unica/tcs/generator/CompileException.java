package it.unica.tcs.generator;

public class CompileException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public CompileException(){
		super("Unknown compile error");
	}

	public CompileException(String message, Throwable cause) {
		super(message, cause);
	}

	public CompileException(String message) {
		super(message);
	}

	public CompileException(Throwable cause) {
		super(cause);
	}
}
