/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.lib;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.core.TransactionOutPoint;
import org.bitcoinj.script.Script;

import it.unica.tcs.lib.Wrapper.NetworkParametersWrapper;

public class TransactionBuilder implements ITransactionBuilder {

	private static final long serialVersionUID = 1L;

	private static final long UNSET_LOCKTIME = -1;

	private final NetworkParametersWrapper params;
	
	private final List<Input> inputs = new ArrayList<>();
	private final List<Output> outputs = new ArrayList<>();
	private long locktime = UNSET_LOCKTIME;
	private final Env env = new Env();
	private final Map<String,Consumer<Object>> variableHooks = new HashMap<>();
	
	public TransactionBuilder(NetworkParametersWrapper params) {
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
	public Object getValueOrDefault(String name, Object defaultValue) {
		return env.getValueOrDefault(name, defaultValue);
	}
	
	@Override
	public <E> E getValue(String name, Class<E> clazz) {
		return env.getValue(name, clazz);
	}

	@Override
	public TransactionBuilder addVariable(String name, Class<?> type) {
		env.addVariable(name, type);
		return this;
	}

	@Override
	public TransactionBuilder removeVariable(String name) {
		for (Input in : inputs) {
			checkState(!in.getScript().hasVariable(name), "input script "+in.getScript()+" use variable '"+name+"'");			
		}
		for (Output out : outputs) {
			checkState(!out.getScript().hasVariable(name), "output script "+out.getScript()+" use variable '"+name+"'");			
		}
		env.removeVariable(name);
		variableHooks.remove(name);
		return this;
	}
	
	@Override
	public TransactionBuilder bindVariable(String name, Object value) {
		env.bindVariable(name, value);
		if (variableHooks.containsKey(name))
			variableHooks.get(name).accept(value);
		return this;
	}
	
	/**
	 * Add an hook that will be executed when the variable {@code name} will have been bound.
	 * The hook is a {@link Consumer} that will take the value of the variable.
	 * 
	 * @param name the name of the variable
	 * @param hook the consumer
	 * @return this builder
	 */
	public TransactionBuilder addHookToVariableBinding(String name, Consumer<Object> hook) {
		checkNotNull(name, "'name' cannot be null");
		checkNotNull(hook, "'hook' cannot be null");
		checkArgument(hasVariable(name), "'name' is not a variable");
		variableHooks.put(name, hook);
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
	public Collection<String> getBoundVariables() {
		return env.getBoundVariables();
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
	 * Remove the unused variables of this builder.
	 * A transaction variable is unused if it is unused by all the
	 * input/output scripts.
	 * 
	 * @return this builder
	 */
	public TransactionBuilder removeUnusedVariables() {
		for (String name : getVariables()) {
			
			boolean used = false;
			
			for (Input in : inputs) {
				used = used || in.getScript().hasVariable(name);
			}

			for (Output out : outputs) {
				used = used || out.getScript().hasVariable(name);
			}
			
			if (!used)
				removeVariable(name);
		}
		
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
		checkNotNull(input, "'input' cannot be null");
		checkArgument(getFreeVariables().containsAll(input.getScript().getFreeVariables()), "the input script contains free-variables "+input.getScript().getFreeVariables()+", but the transactions only contains "+getFreeVariables());
		for (String fv : input.getScript().getFreeVariables()) {
			checkArgument(input.getScript().getType(fv).equals(getType(fv)), "input script variable '"+fv+"' is of type "+input.getScript().getType(fv)+" while the tx variable is of type "+getType(fv));
		}
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
		for (String fv : outputScript.getFreeVariables()) {
			checkArgument(outputScript.getType(fv).equals(getType(fv)), "input script variable '"+fv+"' is of type "+outputScript.getType(fv)+" while the tx variable is of type "+getType(fv));
		}
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
		
		Transaction tx = new Transaction(params.get());
		
		// inputs
		for (Input input : inputs) {
			ITransactionBuilder parentTransaction2 = input.getParentTx();
			
			if (!input.hasParentTx()) {
				// coinbase transaction
				byte[] script = new byte[]{};	// script will be set later
				TransactionInput txInput = new TransactionInput(params.get(), tx, script);
				tx.addInput(txInput);
				checkState(txInput.isCoinBase(), "'txInput' is expected to be a coinbase");
			}
			else {
				Transaction parentTransaction = parentTransaction2.toTransaction();
				TransactionOutPoint outPoint = new TransactionOutPoint(params.get(), input.getOutIndex(), parentTransaction);
				byte[] script = new byte[]{};	// script will be set later
				TransactionInput txInput = new TransactionInput(params.get(), tx, script, outPoint);
				
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
			
			for(String freeVarName : getVariables()) {
				if (sb.hasVariable(freeVarName) && sb.isFree(freeVarName)) {					
					sb.bindVariable(freeVarName, this.getValue(freeVarName));
				}
			}
			checkState(sb.isReady(), "script cannot have free variables: "+sb.toString());
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
			for(String freeVarName : getVariables()) {
				if (sb.hasVariable(freeVarName) && sb.isFree(freeVarName)) {					
					sb.bindVariable(freeVarName, this.getValue(freeVarName));
				}
			}
			
			checkState(sb.getFreeVariables().size()==0, "script cannot have free variables: "+sb.toString());
			
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
		
		sb.append(" hashCode : ").append(hashCode()).append("\n");
		sb.append(" coinbase : ").append(isCoinbase()).append("\n");
		sb.append(" ready    : ").append(isReady()).append("\n");
		sb.append(" locktime : ").append(locktime!=UNSET_LOCKTIME? locktime: "none").append("\n");
		
		addVariables(sb, this);
		addInputs(sb, this.inputs);
		addOutputs(sb, this.outputs);
		
		return sb.toString();
	}
	
	
	private static void addVariables(StringBuilder sb, EnvI<?> env) {
		TablePrinter tp = new TablePrinter();
		tp.title = "Variables";
		tp.noValueRow = "No variables";
		tp.setHeader(new String[]{"Name", "Type", "Binding"});
		for (String name : new TreeSet<>(env.getVariables())) {
			tp.addRow(new String[]{
					name, 
					env.getType(name).getSimpleName(), 
					env.getValueOrDefault(name, "").toString()});
		}
		sb.append("\n\n");
		sb.append(tp.build());
	}
	
	private static void addInputs(StringBuilder sb, List<Input> inputs) {
		TablePrinter tp = new TablePrinter();
		tp.title = "Inputs";
		tp.noValueRow = "No inputs";
		tp.setHeader(new String[]{"Index", "Outpoint", "Locktime", "Ready", "Variables", "Script"});
		int i=0;
		for (Input input : inputs) {
			String index = String.valueOf(i++);
			String outpoint = input.hasParentTx()?input.getOutIndex()+":"+input.getParentTx().hashCode():"none";
			String locktime = input.hasLocktime()?String.valueOf(input.getLocktime()):"none";
			String ready = String.valueOf(input.getScript().isReady());
			List<String> vars = getCompactVariables(input.getScript());
			String script = input.getScript().serialize();
						
			tp.addRow(new String[]{
					index, 
					outpoint, 
					locktime,
					ready,
					vars.isEmpty()?"":vars.get(0),
					script});
			
			for (int j=1; j<vars.size();j++) {
				tp.addRow(new String[]{"","","","",vars.isEmpty()?"":vars.get(j),""});
			}
		}
		sb.append("\n\n");
		sb.append(tp.build());
	}
	
	private static void addOutputs(StringBuilder sb, List<Output> inputs) {
		TablePrinter tp = new TablePrinter();
		tp.title = "Outputs";
		tp.noValueRow = "No outputs";
		tp.setHeader(new String[]{"Index", "Value", "Type", "Ready", "Variables", "Script"});
		int i=0;
		for (Output output : inputs) {
			String index = String.valueOf(i++);
			String value = String.valueOf(output.getValue());
			String type = String.valueOf(output.getScript().getType());
			String ready = output.getScript().isReady()+"";
			List<String> vars = getCompactVariables(output.getScript());
			String script = output.getScript().serialize();
						
			tp.addRow(new String[]{
					index, 
					value, 
					type,
					ready,
					vars.isEmpty()?"":vars.get(0),
					script});
			
			for (int j=1; j<vars.size();j++) {
				tp.addRow(new String[]{"","","","",vars.isEmpty()?"":vars.get(j),""});
			}
		}
		sb.append("\n\n");
		sb.append(tp.build());
	}
	private static List<String> getCompactVariables(EnvI<?> env) {
		List<String> res = new ArrayList<>();
		Collection<String> variables = new TreeSet<>(env.getVariables());
		
		int size = variables.stream().map(v->("("+env.getType(v).getSimpleName()+") "+v).length()).reduce(0, Integer::max);
				
		for (String v : variables) {
			res.add(StringUtils.rightPad("("+env.getType(v).getSimpleName()+") "+v, size)+(env.isBound(v)? " -> "+env.getValue(v):""));
		}
		return res;
	}

}





class TablePrinter {
	String title;
	
	String[] header;
	
	List<String[]> valuesPerLine = new ArrayList<>();
	Map<Integer,Integer> maxColLength = new TreeMap<>();
	
	String rowPrefix = " ";
	int rowPrefixSize = 1;
	
	String rowSuffix = " ";
	int rowSuffixSize = 1;
	
	String valueSeparator = " ";
	int valueSeparatorSize = 6; 
	
	String noValueRow = "no values";
	
	void setHeader(String... header) {
		this.header = header;
		for (int i=0; i<header.length; i++) {
			this.maxColLength.putIfAbsent(i, header[i].length());
		}
	}
		
	
	void addRow(String... values) {
		for (int i=0; i<values.length; i++) {
			String v = values[i];
			this.maxColLength.putIfAbsent(i, header[i].length());
			
			if (maxColLength.get(i) < v.length())
				this.maxColLength.put(i, v.length());
		}
		this.valuesPerLine.add(values);
	}
	
	void printTitle(StringBuilder sb) {
		sb.append(StringUtils.repeat(rowPrefix, rowPrefixSize));
		sb.append(title);
		sb.append("\n");
	}
	
	void printNoValues(StringBuilder sb) {
		sb.append(StringUtils.repeat(rowPrefix, rowPrefixSize));
		sb.append(noValueRow);
		sb.append("\n");
	}
		
	void printLine(StringBuilder sb, char ch) {
		int size = maxColLength.values().stream().reduce(0, Integer::sum)+rowPrefixSize+rowSuffixSize+valueSeparatorSize*(header.length-1);
		sb.append(StringUtils.repeat(ch, size));
		sb.append("\n");
	}
	
	void printRow(StringBuilder sb, String[] values) {
		sb.append(StringUtils.repeat(rowPrefix, rowPrefixSize));
		for (int col=0; col<values.length; col++) {
			sb.append(StringUtils.rightPad(values[col], maxColLength.get(col)));
			if (col!=values.length-1)
				sb.append(StringUtils.repeat(valueSeparator, valueSeparatorSize));
		}
		sb.append(StringUtils.repeat(rowSuffix, rowSuffixSize));
		sb.append("\n");
	}
	
	String build() {
		StringBuilder sb = new StringBuilder();
		
		printTitle(sb);
		
		printLine(sb, '=');
		printRow(sb, header);
		printLine(sb, '-');
		
		if (valuesPerLine.isEmpty()) {
			printNoValues(sb);
		}
		else {
			for (int row=0; row<valuesPerLine.size(); row++) {
				String[] values = this.valuesPerLine.get(row);
				printRow(sb, values);
			}
		}
		
		printLine(sb, '=');
		return sb.toString();
	}
}









