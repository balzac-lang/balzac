/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.lib;

import java.io.Serializable;
import java.util.List;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.Utils;

import it.unica.tcs.lib.Wrapper.NetworkParametersWrapper;

public interface ITransactionBuilder extends EnvI<ITransactionBuilder>, Serializable {

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
	public abstract Transaction toTransaction();

	/**
	 * Return the inputs.
	 * @return the inputs
	 */
	public abstract List<Input> getInputs();
	
	/**
	 * Return the number of outputs.
	 * @return the number of outputs
	 */
	public abstract List<Output> getOutputs();

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
		return fromSerializedTransaction(params, Utils.HEX.decode(bytes));
	}
	
	/**
	 * Return a transaction builder from a bitcoin serialized transaction 
	 * @param params the network parameters
	 * @param bytes the payload of the transaction
	 * @return the builder
	 */
	public static ITransactionBuilder fromSerializedTransaction(NetworkParameters params, byte[] bytes) {
		return new SerialTransactionBuilder(NetworkParametersWrapper.wrap(params), bytes);
	}
}
