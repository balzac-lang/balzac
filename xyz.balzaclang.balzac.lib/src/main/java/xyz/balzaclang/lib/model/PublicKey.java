/*
 * Copyright 2019 Nicola Atzei
 */

package xyz.balzaclang.lib.model;

import org.bitcoinj.core.ECKey;

import xyz.balzaclang.lib.utils.BitcoinUtils;

public interface PublicKey {

    public byte[] getBytes();

    public String getBytesAsString();

    public Address toAddress(NetworkType params);

    public Address toTestnetAddress();

    public Address toMainnetAddress();

    public static PublicKey fromBytes(byte[] pubkey) {
    	return new PublicKeyImpl(pubkey);
    }

    public static PublicKey fromString(String str) {
        return fromBytes(BitcoinUtils.decode(str));
    }

    public static PublicKey fresh() {
        return fromBytes(new ECKey().getPubKey());
    }

    public static PublicKey from(PublicKey key) {
    	return fromBytes(key.getBytes());
    }

    public static PublicKey from(PrivateKey key) {
    	return from(key.toPublicKey());
    }
}