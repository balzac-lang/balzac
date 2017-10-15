/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.lib;

import org.bitcoinj.script.Script;
import static com.google.common.base.Preconditions.checkState;

public class P2SHInputScript extends ScriptBuilder2 {

	// TODO: make it final!
	private ScriptBuilder2 redeemScript;
	
	public P2SHInputScript(ScriptBuilder2 redeemScript) {
		this.redeemScript = redeemScript;
	}
	
	@Override
	public Script build() {
		checkState(redeemScript.isReady(), "redeemScript is not ready");
		super.data(redeemScript.build().getProgram());
		return super.build();
	}
	
	@Override
	public boolean isReady() {
		return this.isReady() && redeemScript.isReady();
	}

	public ScriptBuilder2 getRedeemScript() {
		return redeemScript;
	}
	
	public void setRedeemScript(ScriptBuilder2 redeemScript) {
		this.redeemScript = redeemScript;
	}

}
