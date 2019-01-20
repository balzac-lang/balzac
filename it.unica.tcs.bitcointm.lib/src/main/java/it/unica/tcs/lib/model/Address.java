/*
 * Copyright 2019 Nicola Atzei
 */

package it.unica.tcs.lib.model;

import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.LegacyAddress;
import org.bitcoinj.core.NetworkParameters;

public interface Address {

    public byte[] getAddressByte();

    public String getAddressWif();

    public String getAddressByteString();

    public static Address fromBase58(String wif) {
        LegacyAddress addr = LegacyAddress.fromBase58(null, wif);
        return new AddressImpl(addr.getHash(), addr.getParameters());
    }

    public static Address fromPubkey(byte[] pubkey, NetworkParameters params) {
        LegacyAddress addr = LegacyAddress.fromKey(params, ECKey.fromPublicOnly(pubkey));
        return new AddressImpl(addr.getHash(), addr.getParameters());
    }

    public static Address fresh(NetworkParameters params) {
        LegacyAddress addr = LegacyAddress.fromKey(params, new ECKey());
        return new AddressImpl(addr.getHash(), addr.getParameters());
    }
}