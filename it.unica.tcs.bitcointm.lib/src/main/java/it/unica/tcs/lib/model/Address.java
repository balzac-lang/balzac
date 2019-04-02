/*
 * Copyright 2019 Nicola Atzei
 */

package it.unica.tcs.lib.model;

import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.LegacyAddress;

public interface Address {

    public byte[] getBytes();

    public String getWif();

    public String getBytesAsString();

    public static Address fromBase58(String wif) {
        LegacyAddress addr = LegacyAddress.fromBase58(null, wif);
        return new AddressImpl(addr.getHash(), NetworkType.from(addr.getParameters()));
    }

    public static Address fromPubkey(byte[] pubkey, NetworkType params) {
        LegacyAddress addr = LegacyAddress.fromKey(params.toNetworkParameters(), ECKey.fromPublicOnly(pubkey));
        return new AddressImpl(addr.getHash(), params);
    }

    public static Address from(Address address) {
        return fromBase58(address.getWif());
    }

    public static Address from(PublicKey pubkey,  NetworkType params) {
        return from(pubkey.toAddress(params));
    }

    public static Address from(PrivateKey key) {
        return from(key.toAddress());
    }

    public static Address fresh(NetworkType params) {
        LegacyAddress addr = LegacyAddress.fromKey(params.toNetworkParameters(), new ECKey());
        return new AddressImpl(addr.getHash(), params);
    }
}