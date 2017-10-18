/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.lib;

import java.io.Serializable;

/*
 * Output internal representation (not visible outside)
 */
public class Output implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private final OutputScript script;
	private final long value;
	
	private Output(OutputScript script, long value) {
		this.script = script;
		this.value = value;
	}
	
	public static Output of(OutputScript script, long value) {
		return new Output(script,value);
	}

	public OutputScript getScript() {
		return script;
	}

	public long getValue() {
		return value;
	}
}