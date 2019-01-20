/*
 * Copyright 2018 Nicola Atzei
 */

package it.unica.tcs.lib.model;

import java.util.Arrays;

import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;

import it.unica.tcs.lib.utils.BitcoinUtils;

class PrivateKeyImpl implements PrivateKey {

    private final NetworkParameters params;
    private final byte[] privkey;
    private final PublicKey pubkey;
    private final Address address;
    
    public PrivateKeyImpl(byte[] privkey, NetworkParameters params) {
        this.params = params;
        this.privkey = privkey;
        this.pubkey = PublicKey.fromString(BitcoinUtils.encode(ECKey.fromPrivate(privkey).getPubKey()), params);
        this.address = Address.fromPubkey(pubkey.getPublicKeyByte(), params);
    }

    @Override
    public byte[] getPrivateKeyByte() {
        return privkey;
    }

    @Override
    public String getPrivateKeyWif() {
        return ECKey.fromPrivate(privkey).getPrivateKeyAsWiF(params);
    }

    @Override
    public String getPrivateKeyString() {
        return BitcoinUtils.encode(privkey);
    }

    @Override
    public byte[] getPublicKeyByte() {
        return pubkey.getPublicKeyByte();
    }

    @Override
    public String getPublicKeyString() {
        return pubkey.getPublicKeyString();
    }

    @Override
    public byte[] getAddressByte() {
        return address.getAddressByte();
    }

    @Override
    public String getAddressWif() {
        return address.getAddressWif();
    }

    @Override
    public String getAddressString() {
        return address.getAddressString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(privkey);
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
        PrivateKeyImpl other = (PrivateKeyImpl) obj;
        if (!Arrays.equals(privkey, other.privkey))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return getPrivateKeyWif();
    }
}
