/*
 * Copyright 2019 Nicola Atzei
 */

package it.unica.tcs.lib.model;

import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;

public interface PrivateKey {

    public byte[] getBytes();

    public String getWif();

    public String getBytesAsString();

    public PublicKey toPublicKey();

    public Address toAddress();

    public static PrivateKey fromBase58(String wif) {
        DumpedPrivateKey key = DumpedPrivateKey.fromBase58(null, wif); 
        return new PrivateKeyImpl(key.getKey().getPrivKeyBytes(), key.getParameters());
    }

    public static PrivateKey fresh(NetworkParameters params) {
        DumpedPrivateKey key = new ECKey().getPrivateKeyEncoded(params); 
        return new PrivateKeyImpl(key.getKey().getPrivKeyBytes(), key.getParameters());
    }
}