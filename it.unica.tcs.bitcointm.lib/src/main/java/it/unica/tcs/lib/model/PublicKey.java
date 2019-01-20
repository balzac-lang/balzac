package it.unica.tcs.lib.model;

import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;

import it.unica.tcs.lib.utils.BitcoinUtils;

public interface PublicKey extends Address {

    public byte[] getPublicKeyByte();

    public String getPublicKeyString();
    
    public static PublicKey fromString(String str, NetworkParameters params) {
        return new PublicKeyImpl(BitcoinUtils.decode(str), params);
    }

    public static PublicKey fresh(NetworkParameters params) {
        return new PublicKeyImpl(new ECKey().getPubKey(), params);
    }

    public static PublicKey fromBytes(byte[] pubkey, NetworkParameters params) {
    	return new PublicKeyImpl(pubkey, params);
    }
}