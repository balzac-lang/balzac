/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.lib;

import org.bitcoinj.script.Script;

public class InputScriptImpl extends InputScript {

	public InputScriptImpl() {
		super();
	}

	public InputScriptImpl(Script script) {
		super(script);
	}

}
