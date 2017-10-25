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
	
	abstract public boolean isP2SH();
	
	public String getType() {
		if (isP2SH())
			return "P2SH";
		else
			return "STANDARD";
	}
}
