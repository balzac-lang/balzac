/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.lib;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.List;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.Utils;
import org.bitcoinj.script.Script;

public interface ITransactionBuilder extends Serializable {

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
		return new ITransactionBuilder() {
			private static final long serialVersionUID = 1L;
			private final Transaction tx = new Transaction(params, bytes);
			@Override public boolean isReady() { return true; }
			@Override public Transaction toTransaction() { return tx; }
			@Override public boolean isCoinbase() { return tx.isCoinBase(); }
			@Override public List<Input> getInputs() { return new AbstractList<Input>() {

				@Override
				public Input get(int index) {
					return Input.of(new InputScriptImpl(tx.getInput(index).getScriptSig()));
				}

				@Override
				public int size() {
					return tx.getInputs().size();
				}
			}; }
			@Override public List<Output> getOutputs() { return new AbstractList<Output>() {

				@Override
				public Output get(int index) {
					Script s = tx.getOutput(index).getScriptPubKey();
					if (s.isPayToScriptHash()) {
						return Output.of(new P2SHOutputScript(s), tx.getOutput(index).getValue().value);
					}
					else if (s.isSentToAddress()) {
						return Output.of(new P2PKHOutputScript(s), tx.getOutput(index).getValue().value);
					}
					if (s.isOpReturn()) {
						return Output.of(new OpReturnOutputScript(s), tx.getOutput(index).getValue().value);
					}
					throw new UnsupportedOperationException();
				}

				@Override
				public int size() {
					return tx.getOutputs().size();
				}
			}; }
			@Override public String toString() { return "SerializedTransaction\n\n"+tx.toString(); }
		};
	}
}

















