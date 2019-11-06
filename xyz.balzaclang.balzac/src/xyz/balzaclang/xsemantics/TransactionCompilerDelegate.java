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
package xyz.balzaclang.xsemantics;

import com.google.inject.Inject;

import xyz.balzaclang.balzac.Transaction;
import xyz.balzaclang.compiler.TransactionCompiler;
import xyz.balzaclang.lib.model.transaction.ITransactionBuilder;

/**
 * This class solves maven-build problems of interpreter.xsemantics
 */
class TransactionCompilerDelegate {

    @Inject private TransactionCompiler compiler;

    public ITransactionBuilder compileTransaction(Transaction tx, Rho rho) {
        return compiler.compileTransaction(tx, rho);
    }
}
