/*
 * Copyright 2018 Nicola Atzei
 */

package it.unica.tcs.xsemantics.interpreter;

import java.util.Arrays;

import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;

import it.unica.tcs.lib.utils.BitcoinUtils;

public class PublicKey extends Address {

    private final byte[] pubkey;

    public PublicKey(byte[] pubkey, NetworkParameters params) {
        super(ECKey.fromPublicOnly(pubkey).getPubKeyHash(), params);
        this.pubkey = pubkey;
    }
    
    public byte[] getPublicKeyByte() {
        return pubkey;
    }

    public String getPublivKeyString() {
        return BitcoinUtils.encode(pubkey);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Arrays.hashCode(pubkey);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        PublicKey other = (PublicKey) obj;
        if (!Arrays.equals(pubkey, other.pubkey))
            return false;
        return true;
    }

    public static PublicKey fromString(String str, NetworkParameters params) {
        return new PublicKey(BitcoinUtils.decode(str), params);
    }

    @Override
    public String toString() {
        return getPublivKeyString();
    }
}
