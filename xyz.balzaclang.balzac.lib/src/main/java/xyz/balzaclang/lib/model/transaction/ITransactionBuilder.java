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

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.Serializable;
import java.util.List;

import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.Utils;

import xyz.balzaclang.lib.ECKeyStore;
import xyz.balzaclang.lib.model.NetworkType;
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

    public static boolean equals(ITransactionBuilder a, ITransactionBuilder b, ECKeyStore kstore) {
        checkNotNull(a);
        checkNotNull(b);
        if (a instanceof TransactionBuilder && b instanceof TransactionBuilder) {
            if (a.isReady() && b.isReady())
                return a.toTransaction(kstore).equals(b.toTransaction(kstore));
            return a.equals(b);
        }
        else if (a instanceof SerialTransactionBuilder && b instanceof SerialTransactionBuilder) {
            return a.equals(b);
        }
        else if (a instanceof TransactionBuilder && b instanceof SerialTransactionBuilder) {
            return a.isReady() && a.toTransaction(kstore).equals(b.toTransaction(kstore));
        }
        else if (a instanceof SerialTransactionBuilder && b instanceof TransactionBuilder) {
            return b.isReady() && b.toTransaction(kstore).equals(a.toTransaction(kstore));
        }
        throw new IllegalArgumentException("Not reachable");
    }
}
