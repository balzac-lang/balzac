/*
 * Copyright 2017 Nicola Atzei
 */
package it.unica.tcs.lib;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.script.Script;

import it.unica.tcs.lib.script.InputScriptImpl;
import it.unica.tcs.lib.script.OpReturnOutputScript;
import it.unica.tcs.lib.script.OutputScriptImpl;
import it.unica.tcs.lib.script.P2PKHOutputScript;

public class SerialTransactionBuilder implements ITransactionBuilder {

    private static final long serialVersionUID = 1L;

    transient private Transaction tx;

    private transient NetworkParameters params;
    private final byte[] bytes;

    public SerialTransactionBuilder(NetworkParameters params, byte[] bytes) {
        this.params = params;
        this.bytes = bytes;
    }

    private Transaction getTx() {
        if (tx == null) {
            tx = new Transaction(params, bytes);
        }
        return tx;
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public Transaction toTransaction() {
        return getTx();
    }

    @Override
    public Transaction toTransaction(ECKeyStore kstore) {
        return toTransaction();
    }

    @Override
    public boolean isCoinbase() {
        return getTx().isCoinBase();
    }

    @Override
    public List<Input> getInputs() {
        return new AbstractList<Input>() {

            @Override
            public Input get(int index) {
                return Input.of(new InputScriptImpl(getTx().getInput(index).getScriptSig()));
            }

            @Override
            public int size() {
                return getTx().getInputs().size();
            }
        };
    }

    @Override
    public List<Output> getOutputs() {
        return new AbstractList<Output>() {

            @Override
            public Output get(int index) {
                Script s = getTx().getOutput(index).getScriptPubKey();
                if (s.isPayToScriptHash()) {
                    return Output.of(new OutputScriptImpl(s), getTx().getOutput(index).getValue().value);
                }
                if (s.isSentToAddress()) {
                    return Output.of(new P2PKHOutputScript(s), getTx().getOutput(index).getValue().value);
                }
                if (s.isOpReturn()) {
                    return Output.of(new OpReturnOutputScript(s), getTx().getOutput(index).getValue().value);
                }
                throw new UnsupportedOperationException();
            }

            @Override
            public int size() {
                return getTx().getOutputs().size();
            }
        };
    }

    @Override
    public boolean hasVariable(String name) {
        return false;
    }

    @Override
    public boolean isFree(String name) {
        return false;
    }

    @Override
    public boolean isBound(String name) {
        return false;
    }

    @Override
    public Class<?> getType(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getValue(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <E> E getValue(String name, Class<E> clazz) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getValueOrDefault(String name, Object defaultValue) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ITransactionBuilder addVariable(String name, Class<?> type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ITransactionBuilder removeVariable(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ITransactionBuilder bindVariable(String name, Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<String> getVariables() {
        return new ArrayList<>();
    }

    @Override
    public Collection<String> getFreeVariables() {
        return new ArrayList<>();
    }

    @Override
    public Collection<String> getBoundVariables() {
        return new ArrayList<>();
    }

    @Override
    public void clear() {
    }

    @Override
    public String toString() {
        return "SerializedTransaction\n\n" + getTx().toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(bytes);
        result = prime * result + ((params == null) ? 0 : params.hashCode());
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
        SerialTransactionBuilder other = (SerialTransactionBuilder) obj;
        if (!Arrays.equals(bytes, other.bytes))
            return false;
        if (params == null) {
            if (other.params != null)
                return false;
        } else if (!params.equals(other.params))
            return false;
        return true;
    }

    // Java serialization

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeUTF(params.getId());
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        params = NetworkParameters.fromID(in.readUTF());
    }
}
