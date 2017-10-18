package it.unica.tcs.lib;

import org.bitcoinj.script.Script;

abstract public class OutputScript extends ScriptBuilder2 {

	private static final long serialVersionUID = 1L;

	public OutputScript() {
		super();
	}

	public OutputScript(Script script) {
		super(script);
	}

	public OutputScript(String serializedScript) {
		super(serializedScript);
	}

	abstract public boolean isP2SH();
	abstract public boolean isP2PKH();
	abstract public boolean isOP_RETURN();
	
}
