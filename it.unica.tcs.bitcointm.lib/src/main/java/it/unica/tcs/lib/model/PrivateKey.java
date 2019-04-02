/*
 * Copyright 2019 Nicola Atzei
 */

package it.unica.tcs.lib.model;

import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.ECKey;

public interface PrivateKey {

    public byte[] getBytes();

    public String getWif();

    public String getBytesAsString();

    public PublicKey toPublicKey();

    public Address toAddress();

    public static PrivateKey fromBase58(String wif) {
        DumpedPrivateKey key = DumpedPrivateKey.fromBase58(null, wif); 
        return from(key.getKey().getPrivKeyBytes(), NetworkType.from(key.getParameters()));
    }

    public static PrivateKey from(byte[] keyBytes, NetworkType params) {
        return new PrivateKeyImpl(keyBytes, params);
    }

    public static PrivateKey fresh(NetworkType params) {
        return from(new ECKey().getPrivKeyBytes(), params);
    }

    public static PrivateKey copy(PrivateKey key, NetworkType params) {
        return from(key.getBytes(), params);
    }

}