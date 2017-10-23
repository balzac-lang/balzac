package it.unica.tcs.lib;

import org.bitcoinj.script.Script;

public class ScriptBuilder2 extends AbstractScriptBuilderWithVar<ScriptBuilder2> {

	private static final long serialVersionUID = 1L;

	public ScriptBuilder2() {
		super();
	}

	public ScriptBuilder2(Script script) {
		super(script);
	}

	public ScriptBuilder2(String serializedScript) {
		super(serializedScript);
	}

}
