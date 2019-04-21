/*
 * Copyright 2019 Nicola Atzei
 */
package xyz.balzaclang.xsemantics;

import com.google.inject.Inject;

import xyz.balzaclang.balzac.Transaction;
import xyz.balzaclang.compiler.TransactionCompiler;
import xyz.balzaclang.lib.model.ITransactionBuilder;

/**
 * This class solves maven-build problems of interpreter.xsemantics
 */
class TransactionCompilerDelegate {

    @Inject private TransactionCompiler compiler;

    public ITransactionBuilder compileTransaction(Transaction tx, Rho rho) {
        return compiler.compileTransaction(tx, rho);
    }
}
