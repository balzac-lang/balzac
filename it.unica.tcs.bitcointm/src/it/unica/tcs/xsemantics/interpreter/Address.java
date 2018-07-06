/*
 * Copyright 2018 Nicola Atzei
 */

package it.unica.tcs.xsemantics.interpreter;

import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.LegacyAddress;
import org.bitcoinj.core.NetworkParameters;

public interface Address {

    public byte[] getAddressByte();

    public String getAddressWif();

    public static Address fromBase58(String wif) {
        LegacyAddress addr = LegacyAddress.fromBase58(null, wif);
        return new AddressImpl(addr.getHash(), addr.getParameters());
    }

    public static Address fromPubkey(byte[] pubkey, NetworkParameters params) {
        LegacyAddress addr = LegacyAddress.fromKey(params, ECKey.fromPublicOnly(pubkey));
        return new AddressImpl(addr.getHash(), addr.getParameters());
    }
}