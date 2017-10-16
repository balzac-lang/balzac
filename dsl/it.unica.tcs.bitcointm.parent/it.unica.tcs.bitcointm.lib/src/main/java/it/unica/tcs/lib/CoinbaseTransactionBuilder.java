/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.lib;

import org.bitcoinj.core.NetworkParameters;

public class CoinbaseTransactionBuilder extends TransactionBuilder {
	
	public CoinbaseTransactionBuilder(NetworkParameters params) {
		super(params);
	}

	/*
	 * This override is changing the method visibility.
	 */
	@Override
	public TransactionBuilder addInput(InputScript inputScript) {
		return super.addInput(inputScript);
	}
}
