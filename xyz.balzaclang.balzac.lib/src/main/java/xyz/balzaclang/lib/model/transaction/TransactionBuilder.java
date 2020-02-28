/*
 * Copyright 2019 Nicola Atzei
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package xyz.balzaclang.lib.model.transaction;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.security.KeyStoreException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.core.TransactionOutPoint;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptPattern;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import xyz.balzaclang.lib.ECKeyStore;
import xyz.balzaclang.lib.model.NetworkType;
import xyz.balzaclang.lib.model.script.InputScript;
import xyz.balzaclang.lib.model.script.OutputScript;
import xyz.balzaclang.lib.utils.Env;
import xyz.balzaclang.lib.utils.EnvI;
import xyz.balzaclang.lib.utils.TablePrinter;

public class TransactionBuilder implements ITransactionBuilder {

    private static final long serialVersionUID = 1L;

    private static final long UNSET_LOCKTIME = -1;

    private transient NetworkType params;

    private final List<Input> inputs = new ArrayList<>();
    private final List<Output> outputs = new ArrayList<>();
    private long locktime = UNSET_LOCKTIME;
    private final Env<Object> env = new Env<>();

    private final Map<Set<String>, Consumer<Map<String, Object>>> variablesHook = new HashMap<>();

    public TransactionBuilder(NetworkType params) {
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
            checkState(!in.getScript().hasVariable(name),
                "input script " + in.getScript() + " use variable '" + name + "'");
        }
        for (Output out : outputs) {
            checkState(!out.getScript().hasVariable(name),
                "output script " + out.getScript() + " use variable '" + name + "'");
        }
        env.removeVariable(name);
        variablesHook.remove(ImmutableSet.of(name));
        return this;
    }

    @Override
    public TransactionBuilder bindVariable(String name, Object value) {
        env.bindVariable(name, value);
        Iterator<Set<String>> it = variablesHook.keySet().iterator();
        while (it.hasNext()) {
            Set<String> variables = it.next();
            boolean allBound = variables.stream().allMatch(this::isBound);
            if (allBound) {
                Map<String, Object> values = variables.stream().collect(Collectors.toMap(v -> v, v -> getValue(v)));
                Consumer<Map<String, Object>> hook = variablesHook.get(variables);
                hook.accept(values); // execute the hook
                it.remove(); // remove the hook
            }
        }
        return this;
    }

    /**
     * Add an hook that will be executed when all the variable {@code names} will
     * have been bound. The hook is a {@link Consumer} that will take the value of
     * the variable.
     *
     * @param names a set of variables names
     * @param hook  the consumer
     * @return this builder
     */
    public TransactionBuilder addHookToVariableBinding(Set<String> names, Consumer<Map<String, Object>> hook) {
        checkNotNull(names, "'names' cannot be null");
        checkNotNull(hook, "'hook' cannot be null");
        checkArgument(!names.isEmpty(), "cannot add an hook for an empty set of variables");
        for (String name : names) {
            checkArgument(hasVariable(name), "'" + name + "' is not a variable");
            checkArgument(isFree(name), "'" + name + "' is not a free");
        }
        checkArgument(!variablesHook.containsKey(ImmutableSet.copyOf(names)),
            "an hook for variables " + names + " is already defined");
        variablesHook.put(ImmutableSet.copyOf(names), hook);
        return this;
    }

    public boolean hasHook(String name, String... names) {
        checkNotNull(name, "'name' cannot be null");
        checkNotNull(names, "'names' cannot be null");
        return variablesHook.containsKey(Sets.union(Sets.newHashSet(name), Sets.newHashSet(names)));
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
     * Remove the unused variables of this builder. A transaction variable is unused
     * if it is unused by all the input/output scripts.
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
     * <p>
     * This method is only used by {@link CoinbaseTransactionBuilder} to provide a
     * valid input. In this way, we avoid to expose other implementation details,
     * even to subclasses
     * </p>
     * 
     * @param inputScript the input script that redeem {@code tx} at
     *                    {@code outIndex}.
     * @return this builder.
     * @throws IllegalArgumentException if the parent transaction binding does not
     *                                  match its free variables, or the input
     *                                  script free variables are not contained
     *                                  within this tx free variables.
     * @see CoinbaseTransactionBuilder
     */
    protected TransactionBuilder addInput(InputScript inputScript) {
        checkState(this.inputs.size() == 0, "addInput(ScriptBuilder2) can be invoked only once");
        return addInput(Input.of(inputScript));
    }

    /**
     * Add a new transaction input.
     * 
     * @param tx          the parent transaction to redeem.
     * @param outIndex    the index of the output script to redeem.
     * @param inputScript the input script that redeem {@code tx} at
     *                    {@code outIndex}.
     * @return this builder.
     * @throws IllegalArgumentException if the parent transaction binding does not
     *                                  match its free variables, or the input
     *                                  script free variables are not contained
     *                                  within this tx free variables.
     */
    public TransactionBuilder addInput(ITransactionBuilder tx, int outIndex, InputScript inputScript) {
        return addInput(Input.of(tx, outIndex, inputScript));
    }

    /**
     * Add a new transaction input.
     * 
     * @param tx          the parent transaction to redeem.
     * @param outIndex    the index of the output script to redeem.
     * @param inputScript the input script that redeem {@code tx} at
     *                    {@code outIndex}.
     * @param locktime    relative locktime.
     * @return this builder.
     * @throws IllegalArgumentException if the parent transaction binding does not
     *                                  match its free variables, or the input
     *                                  script free variables are not contained
     *                                  within this tx free variables.
     */
    public TransactionBuilder addInput(ITransactionBuilder tx, int outIndex, InputScript inputScript, long locktime) {
        return addInput(Input.of(tx, outIndex, inputScript, locktime));
    }

    public TransactionBuilder addInput(Input input) {
        checkNotNull(input, "'input' cannot be null");
        checkArgument(getFreeVariables().containsAll(input.getScript().getFreeVariables()),
            "the input script contains free-variables "
                + input.getScript().getFreeVariables()
                + ", but the transactions only contains "
                + getFreeVariables());
        for (String fv : input.getScript().getFreeVariables()) {
            checkArgument(input.getScript().getType(fv).equals(getType(fv)),
                "input script variable '"
                    + fv
                    + "' is of type "
                    + input.getScript().getType(fv)
                    + " while the tx variable is of type "
                    + getType(fv));
        }
        inputs.add(input);
        return this;
    }

    /**
     * Add a new transaction output.
     * 
     * @param outputScript the output script.
     * @param satoshis     the amount of satoshis of the output.
     * @return this builder.
     * @throws IllegalArgumentException if the output script free variables are not
     *                                  contained within this tx free variables.
     */
    public TransactionBuilder addOutput(OutputScript outputScript, long satoshis) {
        checkArgument(getFreeVariables().containsAll(outputScript.getFreeVariables()),
            "the output script contains free-variables "
                + outputScript.getFreeVariables()
                + ", but the transactions only contains "
                + getFreeVariables());
        for (String fv : outputScript.getFreeVariables()) {
            checkArgument(outputScript.getType(fv).equals(getType(fv)),
                "input script variable '"
                    + fv
                    + "' is of type "
                    + outputScript.getType(fv)
                    + " while the tx variable is of type "
                    + getType(fv));
        }
        outputs.add(Output.of(outputScript, satoshis));
        return this;
    }

    /**
     * Set the transaction locktime (absolute locktime which could represent a block
     * number or a timestamp).
     * 
     * @param locktime the value to set.
     * @return this builder.
     */
    public TransactionBuilder setLocktime(long locktime) {
        this.locktime = locktime;
        return this;
    }

    /**
     * Recursively check that this transaction and all the ancestors don't have free
     * variables.
     * 
     * @return true if this transaction and all the ancestors don't have free
     *         variables, false otherwise.
     */
    @Override
    public boolean isReady() {
        return env.isReady() && inputs.size() > 0 && outputs.size() > 0 && inputs.stream().filter(Input::hasParentTx)
            .map(Input::getParentTx).allMatch(ITransactionBuilder::isReady);
    }

    @Override
    public Transaction toTransaction(ECKeyStore keystore) {
        checkState(this.isReady(), "the transaction and all its ancestors are not ready");

        Transaction tx = new Transaction(params.toNetworkParameters());

        // set version
        tx.setVersion(2);

        // inputs
        for (Input input : inputs) {

            if (!input.hasParentTx()) {
                // coinbase transaction
                byte[] script = new byte[] {}; // script will be set later
                TransactionInput txInput = new TransactionInput(params.toNetworkParameters(), tx, script);
                tx.addInput(txInput);
                checkState(txInput.isCoinBase(), "'txInput' is expected to be a coinbase");
            }
            else {
                ITransactionBuilder parentTransaction2 = input.getParentTx();
                Transaction parentTransaction = parentTransaction2.toTransaction(keystore);
                TransactionOutPoint outPoint = new TransactionOutPoint(params.toNetworkParameters(),
                    input.getOutIndex(), parentTransaction);
                byte[] script = new byte[] {}; // script will be set later
                TransactionInput txInput = new TransactionInput(params.toNetworkParameters(), tx, script, outPoint);

                // set checksequenseverify (relative locktime)
                if (input.getLocktime() == UNSET_LOCKTIME) {
                    // see BIP-0065
                    if (this.locktime != UNSET_LOCKTIME)
                        txInput.setSequenceNumber(TransactionInput.NO_SEQUENCE - 1);
                }
                else {
                    txInput.setSequenceNumber(input.getLocktime());
                }
//              txInput.setScriptSig(input.script.build());
                tx.addInput(txInput);
            }
        }

        // outputs
        for (Output output : outputs) {
            // bind free variables
            OutputScript sb = output.getScript();

            for (String freeVarName : getVariables()) {
                if (sb.hasVariable(freeVarName) && sb.isFree(freeVarName)) {
                    sb.bindVariable(freeVarName, this.getValue(freeVarName));
                }
            }
            checkState(sb.isReady(), "script cannot have free variables: " + sb.toString());
            checkState(sb.signatureSize() == 0);

            Script outScript = sb.getOutputScript();
            Coin value = Coin.valueOf(output.getValue());
            tx.addOutput(value, outScript);
        }

        // set checklocktime (absolute locktime)
        if (locktime != UNSET_LOCKTIME) {
            tx.setLockTime(locktime);
        }

        // set all the signatures within the input scripts (which are never part of the
        // signature)
        for (int i = 0; i < tx.getInputs().size(); i++) {
            TransactionInput txInput = tx.getInputs().get(i);
            InputScript inputScript = inputs.get(i).getScript();

            // bind free variables
            for (String freeVarName : getVariables()) {
                if (inputScript.hasVariable(freeVarName) && inputScript.isFree(freeVarName)) {
                    inputScript.bindVariable(freeVarName, this.getValue(freeVarName));
                }
            }

            checkState(inputScript.isReady(), "script cannot have free variables: " + inputScript.toString());

            byte[] outScript;
            boolean isP2PKH = false;

            if (txInput.isCoinBase()) {
                outScript = new byte[] {};
            }
            else {
                // set outScript
                if (ScriptPattern.isP2SH(txInput.getOutpoint().getConnectedOutput().getScriptPubKey())) {
                    checkState(inputScript.isP2SH(), "why not?");
                    outScript = inputScript.getRedeemScript().build().getProgram();
                }
                else
                    outScript = txInput.getOutpoint().getConnectedPubKeyScript();

                // set isP2PKH
                isP2PKH = ScriptPattern.isP2PKH(txInput.getOutpoint().getConnectedOutput().getScriptPubKey());
            }

            try {
                inputScript.setAllSignatures(keystore, tx, i, outScript, isP2PKH);
            } catch (KeyStoreException e) {
                throw new RuntimeException(e);
            }
            checkState(inputScript.signatureSize() == 0, "all the signatures should have been set");

            // update scriptSig
            txInput.setScriptSig(inputScript.build());
        }

        return tx;
    }

    @Override
    public boolean isCoinbase() {
        return inputs.size() == 1 && !inputs.get(0).hasParentTx();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("\n");

        addInfo(sb, this);
        addVariables(sb, this);
        addInputs(sb, this.inputs);
        addOutputs(sb, this.outputs);

        return sb.toString();
    }

    private static void addInfo(StringBuilder sb, TransactionBuilder tb) {
        TablePrinter tp = new TablePrinter("General info", 2);
        tp.addRow("hashcode", tb.hashCode());
        tp.addRow("coinbase", tb.isCoinbase());
        tp.addRow("ready", tb.isReady());
        tp.addRow("locktime", tb.locktime != UNSET_LOCKTIME ? String.valueOf(tb.locktime) : "none");
        sb.append(tp.toString());
        sb.append("\n");
    }

    private static void addVariables(StringBuilder sb, EnvI<Object, ?> env) {
        TablePrinter tp = new TablePrinter("Variables", new String[] { "Name", "Type", "Binding" }, "No variables");
        for (String name : new TreeSet<>(env.getVariables())) {
            tp.addRow(name, env.getType(name).getSimpleName(), env.getValueOrDefault(name, "").toString());
        }
        sb.append(tp.toString());
        sb.append("\n");
    }

    private static void addInputs(StringBuilder sb, List<Input> inputs) {
        TablePrinter tp = new TablePrinter("Inputs",
            new String[] { "Index", "Outpoint", "Locktime", "Type", "Ready", "Variables", "Script" }, "No inputs");
        int i = 0;
        for (Input input : inputs) {
            String index = String.valueOf(i++);
            String outpoint = input.hasParentTx() ? input.getOutIndex() + ":" + input.getParentTx().hashCode() : "none";
            String locktime = input.hasLocktime() ? String.valueOf(input.getLocktime()) : "none";
            String type = String.valueOf(input.getScript().getType());
            String ready = String.valueOf(input.getScript().isReady());
            List<String> vars = getCompactVariables(input.getScript());
            String script = input.getScript().toString();

            tp.addRow(index, outpoint, locktime, type, ready, vars.isEmpty() ? "" : vars.get(0), script);

            for (int j = 1; j < vars.size(); j++) {
                tp.addRow(new String[] { "", "", "", "", vars.isEmpty() ? "" : vars.get(j), "" });
            }
        }
        sb.append(tp.toString());
        sb.append("\n");
    }

    private static void addOutputs(StringBuilder sb, List<Output> inputs) {
        TablePrinter tp = new TablePrinter("Outputs",
            new String[] { "Index", "Value", "Type", "Ready", "Variables", "Script" }, "No outputs");
        int i = 0;
        for (Output output : inputs) {
            String index = String.valueOf(i++);
            String value = String.valueOf(output.getValue());
            String type = String.valueOf(output.getScript().getType());
            String ready = output.getScript().isReady() + "";
            List<String> vars = getCompactVariables(output.getScript());
            String script = output.getScript().toString();

            tp.addRow(index, value, type, ready, vars.isEmpty() ? "" : vars.get(0), script);

            for (int j = 1; j < vars.size(); j++) {
                tp.addRow("", "", "", "", vars.isEmpty() ? "" : vars.get(j), "");
            }
        }
        sb.append(tp.toString());
        sb.append("\n");
    }

    private static List<String> getCompactVariables(EnvI<Object, ?> env) {
        List<String> res = new ArrayList<>();
        Collection<String> variables = new TreeSet<>(env.getVariables());

        int size = variables.stream().map(v -> ("(" + env.getType(v).getSimpleName() + ") " + v).length()).reduce(0,
            Integer::max);

        for (String v : variables) {
            res.add(StringUtils.rightPad("(" + env.getType(v).getSimpleName() + ") " + v, size)
                + (env.isBound(v) ? " -> " + env.getValue(v) : ""));
        }
        return res;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((env == null) ? 0 : env.hashCode());
        result = prime * result + ((inputs == null) ? 0 : inputs.hashCode());
        result = prime * result + (int) (locktime ^ (locktime >>> 32));
        result = prime * result + ((outputs == null) ? 0 : outputs.hashCode());
        result = prime * result + ((variablesHook == null) ? 0 : variablesHook.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TransactionBuilder other = (TransactionBuilder) obj;
        if (env == null) {
            if (other.env != null)
                return false;
        }
        else if (!env.equals(other.env))
            return false;
        if (inputs == null) {
            if (other.inputs != null)
                return false;
        }
        else if (!inputs.equals(other.inputs))
            return false;
        if (locktime != other.locktime)
            return false;
        if (outputs == null) {
            if (other.outputs != null)
                return false;
        }
        else if (!outputs.equals(other.outputs))
            return false;
        if (variablesHook == null) {
            if (other.variablesHook != null)
                return false;
        }
        else if (!variablesHook.equals(other.variablesHook))
            return false;
        return true;
    }
}
