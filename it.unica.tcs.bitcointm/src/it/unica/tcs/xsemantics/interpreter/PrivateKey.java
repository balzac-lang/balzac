package it.unica.tcs.xsemantics.interpreter;

import org.bitcoinj.core.DumpedPrivateKey;

public interface PrivateKey extends PublicKey, Address {

    public byte[] getPrivateKeyByte();

    public String getPrivateKeyWif();

    public String getPrivateKeyString();

    public static PrivateKey fromBase58(String wif) {
        DumpedPrivateKey key = DumpedPrivateKey.fromBase58(null, wif); 
        return new PrivateKeyImpl(key.getKey().getPrivKeyBytes(), key.getParameters());
    }
}