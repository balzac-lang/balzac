/*
 * Copyright 2020 Nicola Atzei
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

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.bitcoinj.core.Transaction;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptPattern;

import xyz.balzaclang.lib.ECKeyStore;
import xyz.balzaclang.lib.model.NetworkType;
import xyz.balzaclang.lib.model.script.InputScript;
import xyz.balzaclang.lib.model.script.OutputScript;

public class SerialTransactionBuilder implements ITransactionBuilder {

    private static final long serialVersionUID = 1L;

    transient private Transaction tx;

    private transient NetworkType params;
    private final byte[] bytes;

    public SerialTransactionBuilder(NetworkType params, byte[] bytes) {
        this.params = params;
        this.bytes = bytes;
    }

    private Transaction getTx() {
        if (tx == null) {
            tx = new Transaction(params.toNetworkParameters(), bytes);
        }
        return tx;
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public Transaction toTransaction(ECKeyStore kstore) {
        return getTx();
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
                return Input.of(InputScript.create().append(getTx().getInput(index).getScriptSig()));
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
                if (ScriptPattern.isP2SH(s)) {
                    return Output.of(OutputScript.createP2SH().append(s), getTx().getOutput(index).getValue().value);
                }
                if (ScriptPattern.isP2PKH(s)) {
                    return Output.of(OutputScript.createP2PKH(s), getTx().getOutput(index).getValue().value);
                }
                if (ScriptPattern.isOpReturn(s)) {
                    return Output.of(OutputScript.createOP_RETURN(s), getTx().getOutput(index).getValue().value);
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
        }
        else if (!params.equals(other.params))
            return false;
        return true;
    }
}
