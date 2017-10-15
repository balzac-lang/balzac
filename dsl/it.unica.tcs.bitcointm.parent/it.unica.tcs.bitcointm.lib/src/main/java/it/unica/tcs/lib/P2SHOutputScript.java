/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.lib;

import static com.google.common.base.Preconditions.checkState;

import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;

public class P2SHOutputScript extends ScriptBuilder2 {

	public P2SHOutputScript(ScriptBuilder2 redeemScript) {
		super.append(redeemScript);
	}
	
	@Override
	public Script build() {
		checkState(isReady(), "redeemScript is not ready");
		return ScriptBuilder.createP2SHOutputScript(super.build());
	}
}
