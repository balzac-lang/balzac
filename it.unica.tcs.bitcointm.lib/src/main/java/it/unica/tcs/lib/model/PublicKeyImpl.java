/*
 * Copyright 2019 Nicola Atzei
 */

package it.unica.tcs.lib.model;

import java.util.Arrays;

import org.bitcoinj.core.NetworkParameters;

import it.unica.tcs.lib.utils.BitcoinUtils;

class PublicKeyImpl implements PublicKey {

    private final byte[] pubkey;

    PublicKeyImpl(byte[] pubkey) {
        this.pubkey = pubkey;
    }

    @Override
    public byte[] getBytes() {
        return Arrays.copyOf(pubkey, pubkey.length);
    }

    @Override
    public String getBytesAsString() {
        return BitcoinUtils.encode(pubkey);
    }

    @Override
    public Address toAddress(NetworkParameters params) {
    	return Address.fromPubkey(pubkey, params);
    }
    
    @Override
    public Address toTestnetAddress() {
    	return toAddress(NetworkParameters.fromID(NetworkParameters.ID_TESTNET));
    }
    
    @Override
    public Address toMainnetAddress() {
    	return toAddress(NetworkParameters.fromID(NetworkParameters.ID_MAINNET));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(pubkey);
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
        PublicKeyImpl other = (PublicKeyImpl) obj;
        if (!Arrays.equals(pubkey, other.pubkey))
            return false;
        return true;
    }
    
    @Override
    public String toString() {
        return getBytesAsString();
    }
}
