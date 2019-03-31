/*
 * Copyright 2019 Nicola Atzei
 */

package it.unica.tcs.lib.model;

import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.LegacyAddress;
import org.bitcoinj.core.NetworkParameters;

public interface Address {

	public NetworkParameters getNetworkParameters();

    public byte[] getBytes();

    public String getWif();

    public String getBytesAsString();

    public static Address fromBase58(String wif) {
        LegacyAddress addr = LegacyAddress.fromBase58(null, wif);
        return new AddressImpl(addr.getHash(), addr.getParameters());
    }

    public static Address fromPubkey(byte[] pubkey, NetworkParameters params) {
        LegacyAddress addr = LegacyAddress.fromKey(params, ECKey.fromPublicOnly(pubkey));
        return new AddressImpl(addr.getHash(), addr.getParameters());
    }

    public static Address from(Address address) {
        return fromBase58(address.getWif());
    }

    public static Address from(PublicKey pubkey,  NetworkParameters params) {
        return from(pubkey.toAddress(params));
    }

    public static Address from(PrivateKey key) {
        return from(key.toAddress());
    }

    public static Address fresh(NetworkParameters params) {
        LegacyAddress addr = LegacyAddress.fromKey(params, new ECKey());
        return new AddressImpl(addr.getHash(), addr.getParameters());
    }
}