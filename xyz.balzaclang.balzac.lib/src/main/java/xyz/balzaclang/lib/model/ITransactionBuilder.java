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

package xyz.balzaclang.lib.model;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.Serializable;
import java.util.List;

import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.Utils;

import xyz.balzaclang.lib.ECKeyStore;
import xyz.balzaclang.lib.utils.EnvI;

public interface ITransactionBuilder extends EnvI<Object,ITransactionBuilder>, Serializable {

    /**
     * Check that this transaction builder is ready to be converted using {@link #toTransaction()}.
     * @return true if this transaction builder is ready to be converted, false otherwise.
     */
    public abstract boolean isReady();

    /**
     * Create a bitcoinj transaction. This method assumes that this builder {@link #isReady()} (i.e. has not
     * unbound free variables.
     * @return a bitcoinj transaction.
     */
    public abstract Transaction toTransaction();

    /**
     * Create a bitcoinj transaction. This method assumes that this builder {@link #isReady()} (i.e. has not
     * unbound free variables.
     * @param kstore a keystore containing the private keys needed for transaction building.
     * @return a bitcoinj transaction.
     */
    public abstract Transaction toTransaction(ECKeyStore kstore);

    /**
     * Return the inputs.
     * @return the inputs
     */
    public abstract List<Input> getInputs();

    /**
     * Return the number of outputs.
     * @return the number of outputs
     */
    public abstract List<Output> getOutputs();

    /**
     * Return true if this builder will generate a coinbase transaction, false otherwise.
     * @return true if this builder will generate a coinbase transaction, false otherwise.
     */
    public abstract boolean isCoinbase();

    /**
     * Return a transaction builder from a bitcoin serialized transaction
     * @param params the network parameters
     * @param bytes the payload of the transaction
     * @return the builder
     */
    public static ITransactionBuilder fromSerializedTransaction(NetworkType params, String bytes) {
        return fromSerializedTransaction(params, Utils.HEX.decode(bytes));
    }

    /**
     * Return a transaction builder from a {@link Transaction}.
     * Same of <code>fromSerializedTransaction(tx.getParams(), tx.bitcoinSerialize()}</code>
     * @param tx a bitcoin transaction
     * @return the builder
     */
    public static ITransactionBuilder fromSerializedTransaction(Transaction tx) {
        return fromSerializedTransaction(NetworkType.from(tx.getParams()), tx.bitcoinSerialize());
    }

    /**
     * Return a transaction builder from a bitcoin serialized transaction
     * @param params the network parameters
     * @param bytes the payload of the transaction
     * @return the builder
     */
    public static ITransactionBuilder fromSerializedTransaction(NetworkType params, byte[] bytes) {
        return new SerialTransactionBuilder(params, bytes);
    }

    public static boolean equals(TransactionBuilder a, SerialTransactionBuilder b) {
        return a.isReady() && a.toTransaction().equals(b.toTransaction());
    }

    public static boolean equals(SerialTransactionBuilder a, TransactionBuilder b) {
        return equals(b, a);
    }

    public static boolean equals(ITransactionBuilder a, Transaction tx) {
        return equals(a, fromSerializedTransaction(tx));
    }

    public static boolean equals(Transaction tx, ITransactionBuilder b) {
        return equals(b, fromSerializedTransaction(tx));
    }

    public static boolean equals(ITransactionBuilder a, ITransactionBuilder b) {
        checkNotNull(a);
        checkNotNull(b);
        checkArgument(a instanceof TransactionBuilder || a instanceof SerialTransactionBuilder);
        checkArgument(b instanceof TransactionBuilder || b instanceof SerialTransactionBuilder);
        if (a.getClass().equals(b.getClass())) {
            return a.equals(b);
        }
        else {
            if (a instanceof TransactionBuilder && b instanceof SerialTransactionBuilder) {
                return equals((TransactionBuilder) a, (SerialTransactionBuilder) b);
            }
            if (a instanceof TransactionBuilder && b instanceof SerialTransactionBuilder) {
                return equals((SerialTransactionBuilder) a, (TransactionBuilder) b);
            }
            throw new IllegalStateException("Not reachable");
        }
    }
}
