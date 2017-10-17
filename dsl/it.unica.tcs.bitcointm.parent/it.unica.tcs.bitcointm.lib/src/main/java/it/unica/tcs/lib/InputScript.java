package it.unica.tcs.lib;

import org.bitcoinj.script.Script;

abstract public class InputScript extends ScriptBuilder2 {

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
