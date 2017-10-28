/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.lib;

import org.bitcoinj.script.Script;
import static com.google.common.base.Preconditions.checkState;

public class P2SHInputScript extends InputScriptImpl {

	private static final long serialVersionUID = 1L;

	private final P2SHOutputScript redeemScript;
	
	public P2SHInputScript(P2SHOutputScript redeemScript) {
		this.redeemScript = redeemScript;
	}
	
	@Override
	public Script build() {
		checkState(redeemScript.isReady(), "redeemScript is not ready");
		return new ScriptBuilder2().append(this).data(redeemScript.build().getProgram()).build();
	}
	
	@Override
	public boolean isReady() {
		return super.isReady() && redeemScript.isReady();
	}

	public P2SHOutputScript getRedeemScript() {
		return redeemScript;
	}
	
	@Override
	public boolean isP2SH() {
		return true;
	}
	
	@Override
	public String toString() {
		return super.toString()+" <"+redeemScript.toString()+">";
	}
}
