/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.lib;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bitcoinj.core.Coin;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.core.TransactionOutPoint;
import org.bitcoinj.script.Script;

public class TransactionBuilder implements ITransactionBuilder, EnvI<TransactionBuilder> {

	private static final long UNSET_LOCKTIME = -1;

	private final NetworkParameters params;
	
	private final List<Input> inputs = new ArrayList<>();
	private final List<Output> outputs = new ArrayList<>();
	private long locktime = UNSET_LOCKTIME;
	private final Env env = new Env();
	
	public TransactionBuilder(NetworkParameters params) {
		this.params = params;
	}
	
	@Override
	public boolean hasVariable(String name) {
		return env.hasVariable(name);
	}

	@Override
	public boolean isFree(String name) {
		return env.isFree(name);
	}

	@Override
	public boolean isBound(String name) {
		return env.isBound(name);
	}

	@Override
	public Class<?> getType(String name) {
		return env.getType(name);
	}
	
	@Override
	public Object getValue(String name) {
		return env.getValue(name);
	}

	@Override
	public TransactionBuilder addVariable(String name, Class<?> type) {
		env.addVariable(name, type);
		return this;
	}

	@Override
	public TransactionBuilder bindVariable(String name, Object value) {
		env.bindVariable(name, value);
		return this;
	}

	@Override
	public Collection<String> getVariables() {
		return env.getVariables();
	}

	@Override
	public Collection<String> getFreeVariables() {
		return env.getFreeVariables();
	}
	
	@Override
	public void clear() {
		env.clear();
	}

	@Override
	public Collection<String> getBoundFreeVariables() {
		return env.getBoundFreeVariables();
	}
		
	@Override
	public List<Input> getInputs() {
		return inputs;
	}

	@Override
	public List<Output> getOutputs() {
		return outputs;
	}

	/**
	 * Add a new transaction input.
	 * <p>This method is only used by {@link CoinbaseTransactionBuilder} to provide a valid input.
	 * In this way, we avoid to expose other implementation details, even to subclasses</p>
	 * @param inputScript the input script that redeem {@code tx} at {@code outIndex}.
	 * @return this builder.
	 * @throws IllegalArgumentException
	 *             if the parent transaction binding does not match its free
	 *             variables, or the input script free variables are not
	 *             contained within this tx free variables.
	 * @see CoinbaseTransactionBuilder
	 */
	protected TransactionBuilder addInput(InputScript inputScript) {
		checkState(this.inputs.size()==0, "addInput(ScriptBuilder2) can be invoked only once");
		return addInput(Input.of(inputScript));
	}
	
	/**
	 * Add a new transaction input.
	 * @param tx the parent transaction to redeem.
	 * @param outIndex the index of the output script to redeem.
	 * @param freeVarBindingsOfTx the parent transaction bindings.
	 * @param inputScript the input script that redeem {@code tx} at {@code outIndex}.
	 * @return this builder.
	 * @throws IllegalArgumentException
	 *             if the parent transaction binding does not match its free
	 *             variables, or the input script free variables are not
	 *             contained within this tx free variables.
	 */
	public TransactionBuilder addInput(ITransactionBuilder tx, int outIndex, InputScript inputScript) {
		return addInput(Input.of(tx, outIndex, inputScript));
	}
	
	/**
	 * Add a new transaction input.
	 * @param tx the parent transaction to redeem.
	 * @param outIndex the index of the output script to redeem.
	 * @param freeVarBindingsOfTx the parent transaction bindings.
	 * @param inputScript the input script that redeem {@code tx} at {@code outIndex}.
	 * @param locktime relative locktime.
	 * @return this builder.
	 * @throws IllegalArgumentException
	 *             if the parent transaction binding does not match its free
	 *             variables, or the input script free variables are not
	 *             contained within this tx free variables.
	 */
	public TransactionBuilder addInput(ITransactionBuilder tx, int outIndex, InputScript inputScript, long locktime) {
		return addInput(Input.of(tx, outIndex, inputScript, locktime));
	}
	
	public TransactionBuilder addInput(Input input) {
		checkArgument(this.getFreeVariables().containsAll(input.getScript().getFreeVariables()), "the input script contains free-variables "+input.getScript().getFreeVariables()+", but the transactions only contains "+this.getFreeVariables());
		inputs.add(input);
		return this;
	}
	
	/**
	 * Add a new transaction output.
	 * @param outputScript the output script.
	 * @param satoshis the amount of satoshis of the output.
	 * @return this builder.
	 * @throws IllegalArgumentException
	 *             if the output script free variables are not contained within
	 *             this tx free variables.
	 */
	public TransactionBuilder addOutput(OutputScript outputScript, int satoshis) {
		checkArgument(getFreeVariables().containsAll(outputScript.getFreeVariables()), "the output script contains free-variables "+outputScript.getFreeVariables()+", but the transactions only contains "+getFreeVariables());
		outputs.add(Output.of(outputScript, satoshis));
		return this;
	}
	
	/**
	 * Set the transaction locktime (absolute locktime which could represent a block number or a timestamp).
	 * @param locktime the value to set.
	 * @return this builder.
	 */
	public TransactionBuilder setLocktime(long locktime) {
		this.locktime = locktime;
		return this;
	}

	/**
	 * Recursively check that this transaction and all the ancestors don't have free variables.
	 * @return true if this transaction and all the ancestors don't have free variables, false otherwise.
	 */
	@Override
	public boolean isReady() {
		return env.isReady() && inputs.size()>0 && outputs.size()>0 && 
				inputs.stream()
					.filter(Input::hasParentTx)
					.map(Input::getParentTx)
					.allMatch(ITransactionBuilder::isReady);
	}
	
	/**
	 * Create a bitcoinj transaction. This method assumes that this builder {@link #isReady()} (i.e. has not
	 * unbound free variables.
	 * @param params network parameters.
	 * @return a bitcoinj transaction.
	 */
	@Override
	public Transaction toTransaction() {
		checkState(this.isReady(), "the transaction and all its ancestors are not ready");
		
		Transaction tx = new Transaction(params);
		
		// inputs
		for (Input input : inputs) {
			ITransactionBuilder parentTransaction2 = input.getParentTx();
			
			if (!input.hasParentTx()) {
				// coinbase transaction
				byte[] script = new byte[]{};	// script will be set later
				TransactionInput txInput = new TransactionInput(params, tx, script);
				tx.addInput(txInput);
				checkState(txInput.isCoinBase(), "'txInput' is expected to be a coinbase");
			}
			else {
				Transaction parentTransaction = parentTransaction2.toTransaction();
				TransactionOutPoint outPoint = new TransactionOutPoint(params, input.getOutIndex(), parentTransaction);
				byte[] script = new byte[]{};	// script will be set later
				TransactionInput txInput = new TransactionInput(params, tx, script, outPoint);
				
				//set checksequenseverify (relative locktime)
				if (input.getLocktime()==UNSET_LOCKTIME) {
					// see BIP-0065
					if (this.locktime!=UNSET_LOCKTIME)
						txInput.setSequenceNumber(TransactionInput.NO_SEQUENCE-1);
				}
				else {
					txInput.setSequenceNumber(input.getLocktime());
				}
//				txInput.setScriptSig(input.script.build());
				tx.addInput(txInput);
			}
		}
				
		// outputs
		for (Output output : outputs) {
			// bind free variables
			OutputScript sb = output.getScript();
			
			for(String freeVarName : getFreeVariables()) {
				if (sb.hasVariable(freeVarName) && sb.isFree(freeVarName)) {					
					sb.bindVariable(freeVarName, this.getType(freeVarName));
				}
			}
			checkState(sb.getFreeVariables().size()==0);
			checkState(sb.signatureSize()==0);
			
			Script outScript;
			
			if (sb instanceof P2SHOutputScript) {
				// P2SH
				outScript = ((P2SHOutputScript) sb).getOutputScript();
			}
			else {
				outScript = sb.build();
			}
			
			Coin value = Coin.valueOf(output.getValue());
			tx.addOutput(value, outScript);
		}
		
		//set checklocktime (absolute locktime)
		if (locktime!=UNSET_LOCKTIME) {
			tx.setLockTime(locktime);
		}
		
		// set all the signatures within the input scripts (which are never part of the signature)
		for (int i=0; i<tx.getInputs().size(); i++) {
			TransactionInput txInput = tx.getInputs().get(i);
			InputScript sb = inputs.get(i).getScript();
			
			// bind free variables
			for(String freeVarName : getFreeVariables()) {
				if (sb.hasVariable(freeVarName) && sb.isFree(freeVarName)) {					
					sb.bindVariable(freeVarName, this.getType(freeVarName));
				}
			}
			
			checkState(sb.getFreeVariables().size()==0, "input script cannot have free variables");
			
			byte[] outScript;
			if (txInput.isCoinBase()) {
				outScript = new byte[]{};
			}
			else {
				if (txInput.getOutpoint().getConnectedOutput().getScriptPubKey().isPayToScriptHash()) {
					checkState(sb instanceof P2SHInputScript, "why not?");
					P2SHInputScript p2shInput = (P2SHInputScript) sb;
					outScript = p2shInput.getRedeemScript().build().getProgram();
				}
				else
					outScript = txInput.getOutpoint().getConnectedPubKeyScript();
			}
			sb.setAllSignatures(tx, i, outScript);
            checkState(sb.signatureSize()==0,  "all the signatures should have been set");
            
            // update scriptSig
            txInput.setScriptSig(sb.build());
		}
		
		return tx;
	}

	@Override
	public boolean isCoinbase() {
		return inputs.size()==1 && inputs.get(0).getParentTx() == null;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("TransactionBuilder\n\n");
		if (isCoinbase()) 
			sb.append("        coinbase\n");
		if (getFreeVariables().size()>0) {
			sb.append("\n        free-variables : \n");
			for (String name : getFreeVariables())
				sb.append("            ")
				.append(name).append(" -> ").append(getType(name))
				.append(" [").append(isBound(name)? getValue(name): "none").append("]")
				.append("\n");
		}
		sb.append("        ready : ").append(isReady()).append("\n");
		
		if (inputs.size()>0) {
			sb.append("\n        inputs : \n");
			for(Input in : inputs) {
				sb.append("            [").append(in.getOutIndex()).append("] ").append(in.getScript().toString()).append("\n");
			}
		}
		if (outputs.size()>0) {
			sb.append("\n        outputs : \n");
			for(Output out : outputs) {
				sb.append("            [").append(out.getValue()).append("] ").append(out.getScript().toString()).append("\n");
			}
		}
		return sb.toString();
	}

}
