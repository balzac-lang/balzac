package it.unica.tcs.generator;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;

public interface ITransactionBuilder {

	/**
	 * Check that this transaction builder is ready to be converted using {@link #toTransaction(NetworkParameters)}.
	 * @return true if this transaction builder is ready to be converted, false otherwise.
	 */
	boolean isReady();

	/**
	 * Create a bitcoinj transaction. This method assumes that this builder {@link #isReady()} (i.e. has not
	 * unbound free variables.
	 * @param params network parameters.
	 * @return a bitcoinj transaction.
	 */
	Transaction toTransaction(NetworkParameters params);

}