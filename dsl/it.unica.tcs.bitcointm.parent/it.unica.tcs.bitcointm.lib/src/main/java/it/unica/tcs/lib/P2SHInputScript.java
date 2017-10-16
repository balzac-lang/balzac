/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.lib;

import org.bitcoinj.script.Script;
import static com.google.common.base.Preconditions.checkState;

public class P2SHInputScript extends InputScriptImpl {

	private final P2SHOutputScript redeemScript;
	
	public P2SHInputScript(P2SHOutputScript redeemScript) {
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
		return super.isReady() && redeemScript.isReady();
	}

	public P2SHOutputScript getRedeemScript() {
		return redeemScript;
	}
}
