package it.unica.tcs.lib;

import org.bitcoinj.script.Script;

abstract public class InputScript extends ScriptBuilder2 {

	private static final long serialVersionUID = 1L;

	public InputScript() {
		super();
	}

	public InputScript(Script script) {
		super(script);
	}

	public InputScript(String serializedScript) {
		super(serializedScript);
	}
}
