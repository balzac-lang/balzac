package it.unica.tcs.generator;

abstract public class CompilationResult<T> {

	protected final T output;
	
	public CompilationResult(T output) {
		this.output = output;
	}
	
	public T getOutput() {
		return output;
	}
}
