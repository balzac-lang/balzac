/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.compiler;

import java.util.HashMap;

import it.unica.tcs.bitcoinTM.Parameter;

/**
 * Everything that is relevant to achieve the compilation.
 */
public class Context {
	public AltStack altstack = new AltStack();
}

class AltStack extends HashMap<Parameter, AltStackEntry>{
	private static final long serialVersionUID = 1L;
}

class AltStackEntry {
	public final Integer position;
	public final Integer occurrences;
	
	public AltStackEntry(Integer position, Integer occurrences) {
		this.position = position;
		this.occurrences = occurrences;
	}
	
	public static AltStackEntry of(Integer position, Integer occurrences) {
		return new AltStackEntry(position, occurrences);
	}
}
