package it.unica.tcs.xsemantics.interpreter;

import org.bitcoinj.core.NetworkParameters;

import it.unica.tcs.lib.utils.BitcoinUtils;

public interface PublicKey extends Address {

    public byte[] getPublicKeyByte();

    public String getPublicKeyString();
    
    public static PublicKey fromString(String str, NetworkParameters params) {
        return new PublicKeyImpl(BitcoinUtils.decode(str), params);
    }
}