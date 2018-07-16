package it.unica.tcs.xsemantics.interpreter;

import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;

public interface PrivateKey extends PublicKey, Address {

    public byte[] getPrivateKeyByte();

    public String getPrivateKeyWif();

    public String getPrivateKeyString();

    public static PrivateKey fromBase58(String wif) {
        DumpedPrivateKey key = DumpedPrivateKey.fromBase58(null, wif); 
        return new PrivateKeyImpl(key.getKey().getPrivKeyBytes(), key.getParameters());
    }

    public static PrivateKey fresh(NetworkParameters params) {
        DumpedPrivateKey key = new ECKey().getPrivateKeyEncoded(params); 
        return new PrivateKeyImpl(key.getKey().getPrivKeyBytes(), key.getParameters());
    }
}