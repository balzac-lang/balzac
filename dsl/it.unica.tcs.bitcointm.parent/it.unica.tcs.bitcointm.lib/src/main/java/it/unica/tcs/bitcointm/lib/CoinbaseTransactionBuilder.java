/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.bitcointm.lib;

public class CoinbaseTransactionBuilder extends TransactionBuilder {
	
	/*
	 * This override is changing the method visibility.
	 */
	@Override
	public TransactionBuilder addInput(ScriptBuilder2 inputScript) {
		return super.addInput(inputScript);
	}
}
