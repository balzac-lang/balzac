package it.unica.tcs.bitcointm.lib;

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

	private static final long LOCKTIME_NOT_SET = -1;
	private static final int OUTINDEX_NOT_SET = -1;

	/*
	 * Input internal representation (not visible outside)
	 */
	protected static class Input {
		private final ITransactionBuilder tx;
		private final int outIndex;
		private final ScriptBuilder2 script;
		private final long locktime;
		
		private Input(ITransactionBuilder tx, int outIndex, ScriptBuilder2 script, long locktime) {
			this.tx = tx;
			this.script = script;
			this.outIndex = outIndex;
			this.locktime = locktime;
		}
		
		private static Input of(ITransactionBuilder tx, int index, ScriptBuilder2 script, long locktime){
			return new Input(tx, index, script, locktime);
		}
	}
	
	/*
	 * Output internal representation (not visible outside)
	 */
	protected static class Output {
		private final ScriptBuilder2 script;
		private final Integer value;
		
		private Output(ScriptBuilder2 script, Integer value) {
			this.script = script;
			this.value = value;
		}
		
		private static Output of(ScriptBuilder2 script, Integer value) {
			return new Output(script,value);
		}
	}
	
	/**
	 * <p>Free variables of the transaction. Input/Output script are {@link ScriptBuilder2}, and they can
	 * have free variables that must be a subset of this map.</p>
	 * 
	 * <p>The methods {@link TransactionBuilder#addInput(TransactionBuilder, Map, ScriptBuilder2)} and
	 * {@link TransactionBuilder#addOutput(ScriptBuilder2, int) will check this requirement.
	 */
	private final Map<String,Class<?>> freeVariables = new HashMap<>();
	private final Map<String,Object> freeVarBindings = new HashMap<>();
	private final List<Input> inputs = new ArrayList<>();
	private final List<Output> outputs = new ArrayList<>();
	private long locktime = LOCKTIME_NOT_SET;
	
	@Override
	public int getInputsSize() {
		return inputs.size();
	}

	@Override
	public int getOutputsSize() {
		return outputs.size();
	}

	/**
	 * Add a free variable.
	 * @param name the name of the variable
	 * @param clazz the expected type of the actual value for the variable
	 * @return this builder
	 */
	public TransactionBuilder freeVariable(String name, Class<?> clazz) {
		this.freeVariables.put(name, clazz);
		return this;
	}
	
	/**
	 * Return a copy the free variables.
	 * @return a map containing the free variables
	 */
	public Map<String,Class<?>> getFreeVariables() {
		return new HashMap<>(freeVariables);
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
	public TransactionBuilder addFreeVariableBinding(String name, Object value) {
		checkState(this.freeVariables.containsKey(name));
		checkState(this.freeVariables.get(name).isInstance(value));
		freeVarBindings.put(name, value);
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
	protected TransactionBuilder addInput(ScriptBuilder2 inputScript) {
		checkState(this.inputs.size()==0);
		return addInput(new TransactionBuilder(), OUTINDEX_NOT_SET, inputScript, LOCKTIME_NOT_SET);
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
	public TransactionBuilder addInput(ITransactionBuilder tx, int outIndex, ScriptBuilder2 inputScript) {
		return addInput(tx, outIndex, inputScript, LOCKTIME_NOT_SET);
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
	public TransactionBuilder addInput(ITransactionBuilder tx, int outIndex, ScriptBuilder2 inputScript, long locktime) {
		checkArgument(tx.isReady());
		checkArgument(freeVariables.entrySet().containsAll(inputScript.getFreeVariables().entrySet()));
		inputs.add(Input.of(tx, outIndex, inputScript, locktime));
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
	public TransactionBuilder addOutput(ScriptBuilder2 outputScript, int satoshis) {
		checkArgument(freeVariables.entrySet().containsAll(outputScript.getFreeVariables().entrySet()));
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
		boolean allBound = this.freeVariables.entrySet().stream().allMatch(e->{
			String name = e.getKey();
			Class<?> type = e.getValue();
			return this.freeVarBindings.containsKey(name) && type.isInstance(freeVarBindings.get(name));
		});		
		return allBound && inputs.size()>0 && outputs.size()>0 && 
				inputs.stream().map(x->x.tx).allMatch(ITransactionBuilder::isReady);
	}
	
	/**
	 * Create a bitcoinj transaction. This method assumes that this builder {@link #isReady()} (i.e. has not
	 * unbound free variables.
	 * @param params network parameters.
	 * @return a bitcoinj transaction.
	 */
	public Transaction toTransaction(NetworkParameters params) {
		checkArgument(this.getFreeVariables().equals(freeVarBindings.keySet()));
		
		Transaction tx = new Transaction(params);
		
		// inputs
		for (Input input : inputs) {
			// bind free variables
			ScriptBuilder2 sb = input.script;
			for(Entry<String, Object> freeVar : freeVarBindings.entrySet()) {
				sb.setFreeVariable(freeVar.getKey(), freeVar.getValue());
			}
			checkState(sb.freeVariableSize()==0);
			
			ITransactionBuilder parentTransaction2 = input.tx;
			
			if (input.outIndex == OUTINDEX_NOT_SET) {
				// coinbase transaction
				byte[] script = new byte[]{};	// script will be set later
				TransactionInput txInput = new TransactionInput(params, tx, script);
				tx.addInput(txInput);
				checkState(txInput.isCoinBase());
			}
			else {
				Transaction parentTransaction = parentTransaction2.toTransaction(params);
				TransactionOutPoint outPoint = new TransactionOutPoint(params, input.outIndex, parentTransaction);
				byte[] script = new byte[]{};	// script will be set later
				TransactionInput txInput = new TransactionInput(params, tx, script, outPoint);
				
				//set checksequenseverify (relative locktime)
				if (input.locktime==LOCKTIME_NOT_SET) {
					// see BIP-0065
					if (this.locktime!=LOCKTIME_NOT_SET)
						txInput.setSequenceNumber(TransactionInput.NO_SEQUENCE-1);
				}
				else {
					txInput.setSequenceNumber(input.locktime);
				}
				tx.addInput(txInput);
			}
		}
				
		// outputs
		for (Output output : outputs) {
			// bind free variables
			ScriptBuilder2 sb = output.script;
			for(Entry<String, Object> freeVar : freeVarBindings.entrySet()) {
				sb.setFreeVariable(freeVar.getKey(), freeVar.getValue());
			}
			checkState(sb.freeVariableSize()==0);
			checkState(sb.signatureSize()==0);
			
			Script outScript = sb.build();
			Coin value = Coin.valueOf(output.value);
			tx.addOutput(value, outScript);
		}
		
		//set checklocktime (absolute locktime)
		if (locktime!=LOCKTIME_NOT_SET) {
			tx.setLockTime(locktime);
		}
		
		// set all the signatures within the input scripts (which are never part of the signature)
		for (int i=0; i<tx.getInputs().size(); i++) {
			TransactionInput txInput = tx.getInputs().get(i);
			ScriptBuilder2 sb = inputs.get(i).script;
			byte[] outScript;
			if (txInput.getOutpoint().getConnectedOutput().getScriptPubKey().isPayToScriptHash())
				outScript = txInput.getScriptSig().getChunks().get(txInput.getScriptSig().getChunks().size()-1).data;
            else
            	outScript = txInput.getOutpoint().getConnectedPubKeyScript();
			sb.setSignatures(tx, i, outScript);
            checkState(sb.signatureSize()==0);
            checkState(sb.freeVariableSize()==0);
		}
		
		return tx;
	}
}
