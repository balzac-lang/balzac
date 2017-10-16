package it.unica.tcs.lib;

import org.bitcoinj.script.Script;

abstract public class InputScript extends ScriptBuilder2<InputScript> {

	public InputScript() {
		super();
	}

	public InputScript(Script script) {
		super(script);
	}
}
