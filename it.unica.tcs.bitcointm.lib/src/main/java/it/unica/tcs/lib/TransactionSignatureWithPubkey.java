/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.lib;

import org.bitcoinj.crypto.TransactionSignature;

public class TransactionSignatureWithPubkey extends TransactionSignature {

    private final byte[] pubkey;

    public TransactionSignatureWithPubkey(TransactionSignature sig, byte[] pubkey) {
        super(sig.r, sig.s);
        this.pubkey = pubkey;
    }

    public byte[] getPubkey() {
        return pubkey;
    }
}
