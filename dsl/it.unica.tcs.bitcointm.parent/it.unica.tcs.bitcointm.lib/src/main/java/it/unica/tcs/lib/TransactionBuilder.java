/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.lib;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bitcoinj.core.Coin;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.core.TransactionOutPoint;
import org.bitcoinj.script.Script;

public class TransactionBuilder implements ITransactionBuilder {

	private static final long UNSET_LOCKTIME = -1;

	private final NetworkParameters params;
	
	public TransactionBuilder(NetworkParameters params) {
		this.params = params;
	}
	
	/**
	 * <p>Free variables of the transaction. Input/Output script are {@link ScriptBuilder2}, and they can
	 * have free variables that must be a subset of this map.</p>
	 * 
	 * <p>The methods {@link TransactionBuilder#addInput(TransactionBuilder, Map, ScriptBuilder2)} and
	 * {@link TransactionBuilder#addOutput(ScriptBuilder2, int) will check this requirement.
	 */
	private final Map<String,Class<?>> freeVariablesType = new HashMap<>();
	private final Map<String,Object> freeVariablesBinding = new HashMap<>();
	private final List<Input> inputs = new ArrayList<>();
	private final List<Output> outputs = new ArrayList<>();
	private long locktime = UNSET_LOCKTIME;
	
	@Override
	public List<Input> getInputs() {
		return inputs;
	}

	@Override
	public List<Output> getOutputs() {
		return outputs;
	}

	/**
	 * Add a free variable.
	 * @param name the name of the variable
	 * @param clazz the expected type of the actual value for the variable
	 * @return this builder
	 */
	public TransactionBuilder freeVariable(String name, Class<?> clazz) {
		this.freeVariablesType.put(name, clazz);
		return this;
	}
	
	/**
	 * Return a copy the free variables.
	 * @return a map containing the free variables
	 */
	public Map<String,Class<?>> getFreeVariables() {
		return new HashMap<>(freeVariablesType);
	}
	
	/**
	 * Add a free variable binding. 
	 * @param name the name of the free variable.
	 * @param value the value to be bound.
	 * @return this builder
	 * @throws IllegalArgumentException
	 *             if the provided name is not a free variable for this
	 *             transaction, or if the provided value is an not instance of
	 *             the expected class of the free variable.
	 */
	public TransactionBuilder setFreeVariable(String name, Object value) {
		checkState(this.freeVariablesType.containsKey(name), "'"+name+"' is not a free variable");
		checkState(this.freeVariablesType.get(name).isInstance(value), "'"+name+"' is associated with class '"+this.freeVariablesType.get(name)+"', but 'value' is object of class '"+value.getClass()+"'");
		freeVariablesBinding.put(name, value);
		return this;
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
		checkArgument(freeVariablesType.entrySet().containsAll(input.getScript().getFreeVariables().entrySet()), "the input script contains free-variables "+input.getScript().getFreeVariables().entrySet()+", but the transactions only contains "+freeVariablesType.entrySet());
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
		checkArgument(freeVariablesType.entrySet().containsAll(outputScript.getFreeVariables().entrySet()), "the output script contains free-variables "+outputScript.getFreeVariables().entrySet()+", but the transactions only contains "+freeVariablesType.entrySet());
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
	public boolean isReady() {
		boolean allBound = this.freeVariablesType.entrySet().stream().allMatch(e->{
			String name = e.getKey();
			Class<?> type = e.getValue();
			return this.freeVariablesBinding.containsKey(name) && type.isInstance(freeVariablesBinding.get(name));
		});		
		return allBound && inputs.size()>0 && outputs.size()>0 && 
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
			for(Entry<String, Object> freeVar : freeVariablesBinding.entrySet()) {
				sb.setFreeVariable(freeVar.getKey(), freeVar.getValue());
			}
			checkState(sb.freeVariableSize()==0);
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
			for(Entry<String, Object> freeVar : freeVariablesBinding.entrySet()) {
				sb.setFreeVariable(freeVar.getKey(), freeVar.getValue());
			}
			
			checkState(sb.freeVariableSize()==0, "input script cannot have free variables");
			
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
		if (freeVariablesBinding.size()>0) {
			sb.append("\n        bindings : \n");
			for (Entry<String,Object> binding : freeVariablesBinding.entrySet())
				sb.append("            ").append(binding.getKey()).append(" -> ").append(binding.getValue()).append("\n");
		}
		if (freeVariablesType.size()>0)
			sb.append("        freeVariables : "+this.freeVariablesType.keySet()+"\n");
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
