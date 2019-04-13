/*
 * Copyright 2019 Nicola Atzei
 */

package it.unica.tcs.lib.model;

import java.util.Arrays;

import org.bitcoinj.core.LegacyAddress;

import it.unica.tcs.lib.utils.BitcoinUtils;

class AddressImpl implements Address {

    private final byte[] address;
    protected final NetworkType params;

    AddressImpl(byte[] address, NetworkType params) {
        this.address = address;
        this.params = params;
    }

    @Override
    public byte[] getBytes() {
        return Arrays.copyOf(address, address.length);
    }

    @Override
    public String getWif() {
        return LegacyAddress.fromPubKeyHash(params.toNetworkParameters(), address).toBase58();
    }

    @Override
    public String getBytesAsString() {
        return BitcoinUtils.encode(address);
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
        AddressImpl other = (AddressImpl) obj;
        if (!Arrays.equals(address, other.address))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return getWif();
    }
}
