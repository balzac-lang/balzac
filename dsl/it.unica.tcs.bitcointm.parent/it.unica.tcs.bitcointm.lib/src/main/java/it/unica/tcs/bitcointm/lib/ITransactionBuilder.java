package it.unica.tcs.bitcointm.lib;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;

import it.unica.tcs.bitcointm.lib.utils.BitcoinJUtils;

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

	/**
	 * Return true if this builder will generate a coinbase transaction, false otherwise.
	 * @return true if this builder will generate a coinbase transaction, false otherwise.
	 */
	public abstract boolean isCoinbase();
	
	
	/**
	 * Return a transaction builder from a bitcoin serialized transaction 
	 * @param params the network parameters
	 * @param bytes the payload of the transaction
	 * @return the builder
	 */
	public static ITransactionBuilder fromSerializedTransaction(NetworkParameters params, String bytes) {
		return fromSerializedTransaction(params, BitcoinJUtils.decode(bytes));
	}
	
	/**
	 * Return a transaction builder from a bitcoin serialized transaction 
	 * @param params the network parameters
	 * @param bytes the payload of the transaction
	 * @return the builder
	 */
	public static ITransactionBuilder fromSerializedTransaction(NetworkParameters params, byte[] bytes) {
		return new ITransactionBuilder() {
			private final Transaction tx = new Transaction(params, bytes);
			@Override public boolean isReady() { return true; }
			@Override public Transaction toTransaction(NetworkParameters params) { return tx; }
			@Override public int getInputsSize() { return tx.getInputs().size(); }
			@Override public int getOutputsSize() { return tx.getOutputs().size(); }
			@Override public boolean isCoinbase() { return tx.isCoinBase(); }
			@Override public String toString() { return "SerializedTransaction\n\n"+tx.toString(); }
		};
	}
}