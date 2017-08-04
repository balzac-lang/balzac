package it.unica.tcs.compiler;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;

public interface ITransactionBuilder {

	/**
	 * Check that this transaction builder is ready to be converted using {@link #toTransaction(NetworkParameters)}.
	 * @return true if this transaction builder is ready to be converted, false otherwise.
	 */
	public abstract boolean isReady();

	/**
	 * Create a bitcoinj transaction. This method assumes that this builder {@link #isReady()} (i.e. has not
	 * unbound free variables.
	 * @param params network parameters.
	 * @return a bitcoinj transaction.
	 */
	public abstract Transaction toTransaction(NetworkParameters params);

	/**
	 * Return the number of inputs.
	 * @return the number of inputs
	 */
	public abstract int getInputsSize();
	
	/**
	 * Return the number of outputs.
	 * @return the number of outputs
	 */
	public abstract int getOutputsSize();

}