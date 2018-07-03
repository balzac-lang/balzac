/*
 * Copyright 2018 Nicola Atzei
 */

package it.unica.tcs.xsemantics.interpreter;

import java.util.Arrays;

import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;

public class PrivateKey extends PublicKey {

    private final byte[] privkey;
    
    public PrivateKey(byte[] privkey, NetworkParameters params) {
        super(ECKey.fromPrivate(privkey).getPubKey(), params);
        this.privkey = privkey;
    }

    public byte[] getPrivateKeyByte() {
        return privkey;
    }

    public String getPrivateKeyWif() {
        return ECKey.fromPrivate(privkey).getPrivateKeyAsWiF(params);
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Arrays.hashCode(privkey);
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
        PrivateKey other = (PrivateKey) obj;
        if (!Arrays.equals(privkey, other.privkey))
            return false;
        return true;
    }

    public static PrivateKey fromBase58(String wif) {
        DumpedPrivateKey key = DumpedPrivateKey.fromBase58(null, wif); 
        return new PrivateKey(key.getKey().getPrivKeyBytes(), key.getParameters());
    }

    @Override
    public String toString() {
        return getPrivateKeyWif();
    }
}
