/*
 * Copyright 2018 Nicola Atzei
 */

package it.unica.tcs.xsemantics.interpreter;

import java.util.Arrays;

import org.bitcoinj.core.LegacyAddress;
import org.bitcoinj.core.NetworkParameters;

public class Address {
    
    private final byte[] address;
    protected final NetworkParameters params;

    public Address(byte[] address, NetworkParameters params) {
        this.address = address;
        this.params = params;
    }

    public byte[] getAddressByte() {
        return address;
    }

    public String getAddressWif() {
        return LegacyAddress.fromPubKeyHash(params, address).toBase58();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(address);
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
        Address other = (Address) obj;
        if (!Arrays.equals(address, other.address))
            return false;
        return true;
    }

    public static Address fromBase58(String wif) {
        LegacyAddress addr = LegacyAddress.fromBase58(null, wif);
        return new Address(addr.getHash(), addr.getParameters());
    }
}
