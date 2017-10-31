/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.lib;

import it.unica.tcs.lib.Wrapper.NetworkParametersWrapper;
import it.unica.tcs.lib.script.InputScript;

public class CoinbaseTransactionBuilder extends TransactionBuilder {
	
	private static final long serialVersionUID = 1L;

	public CoinbaseTransactionBuilder(NetworkParametersWrapper params) {
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
