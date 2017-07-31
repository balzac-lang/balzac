package it.unica.tcs.generator;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.bitcoinj.script.Script;

public class ScriptCompilationResult extends CompilationResult<ImmutablePair<Script, String>> {

	
	public ScriptCompilationResult(Script script, String str) {
		super(new ImmutablePair<>(script, str));
	}
	
}
