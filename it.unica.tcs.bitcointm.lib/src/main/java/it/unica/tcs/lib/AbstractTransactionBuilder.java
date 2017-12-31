/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.lib;

import java.util.Collection;
import java.util.List;

import org.bitcoinj.core.Transaction;

public abstract class AbstractTransactionBuilder implements ITransactionBuilder {
    
    private static final long serialVersionUID = 1L;

    @Override
    public boolean hasVariable(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isFree(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isBound(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Class<? extends Object> getType(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getValue(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <A> A getValue(String name, Class<A> clazz) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getValueOrDefault(String name, Object defaultValue) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ITransactionBuilder addVariable(String name, Class<? extends Object> type) {
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
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<String> getFreeVariables() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<String> getBoundVariables() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isReady() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Transaction toTransaction() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Input> getInputs() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Output> getOutputs() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isCoinbase() {
        throw new UnsupportedOperationException();
    }
}
