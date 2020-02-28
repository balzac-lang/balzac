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

import java.util.Arrays;
import java.util.Optional;

import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.SignatureDecodeException;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.VerificationException;
import org.bitcoinj.crypto.TransactionSignature;

import xyz.balzaclang.lib.ECKeyStore;
import xyz.balzaclang.lib.model.transaction.ITransactionBuilder;
import xyz.balzaclang.lib.model.transaction.Input;
import xyz.balzaclang.lib.model.transaction.Output;
import xyz.balzaclang.lib.utils.BitcoinUtils;

public class Signature {

    private final byte[] signature;
    private Optional<PublicKey> pubkey = Optional.empty();

    public static void isValidAndCanonical(byte[] signature) throws VerificationException, SignatureDecodeException {
        TransactionSignature.decodeFromBitcoin(signature, true, true);
    }

    public Signature(byte[] signature) {
        this(signature, null);
    }

    public Signature(byte[] signature, PublicKey pubkey) {
        checkArgument(signature != null, "Signature cannot be null");
        this.signature = Arrays.copyOf(signature, signature.length);
        setPubkey(pubkey);
    }

    public byte[] getSignature() {
        return signature;
    }

    public Optional<PublicKey> getPubkey() {
        return pubkey;
    }

    public void setPubkey(PublicKey pubkey) {
        this.pubkey = Optional.ofNullable(pubkey);
    }

    @Override
    public String toString() {
        return "sig:"
            + BitcoinUtils.encode(signature)
            + (pubkey.isPresent() ? "[pubkey:" + pubkey.get().getBytesAsString() + "]" : "");
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(signature);
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
        Signature other = (Signature) obj;
        if (!Arrays.equals(signature, other.signature))
            return false;
        return true;
    }

    public static Signature computeSignature(
        PrivateKey key,
        ITransactionBuilder txBuilder,
        ECKeyStore keyStore,
        int inputIndex,
        SignatureModifier modifier) {

        Transaction tx = txBuilder.toTransaction(keyStore);

        Input input = txBuilder.getInputs().get(inputIndex);
        int outputIndex = input.getOutIndex();
        Output output = input.getParentTx().getOutputs().get(outputIndex);
        byte[] outputScript = output.getScript().build().getProgram();

        TransactionSignature sig = tx.calculateSignature(inputIndex, ECKey.fromPrivate(key.getBytes()), outputScript,
            modifier.toHashType(), modifier.toAnyoneCanPay());

        return new Signature(sig.encodeToBitcoin(), key.toPublicKey());
    }
}
