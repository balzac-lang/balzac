/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.lib;

/*
 * Output internal representation (not visible outside)
 */
public class Output {
	public final OutputScript script;
	public final long value;
	
	private Output(OutputScript script, long value) {
		this.script = script;
		this.value = value;
	}
	
	public static Output of(OutputScript script, long value) {
		return new Output(script,value);
	}
}