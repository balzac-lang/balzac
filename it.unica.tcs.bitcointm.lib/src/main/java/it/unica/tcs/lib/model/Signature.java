/*
 * Copyright 2018 Nicola Atzei
 */

package it.unica.tcs.lib.model;

import java.util.Arrays;

import it.unica.tcs.lib.utils.BitcoinUtils;
import static com.google.common.base.Preconditions.checkNotNull;


public class Signature {

    private final byte[] signature;
    private byte[] pubkey;

    public Signature(byte[] signature, byte[] pubkey) {
		this.signature = signature;
		this.pubkey = pubkey;
	}

	public byte[] getSignature() {
		return signature;
	}

	@Override
    public String toString() {
        return "sig:"+BitcoinUtils.encode(signature) + (hasPubkey()? "[pubkey:" + BitcoinUtils.encode(pubkey) +  "]" : "");
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(signature);
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
		Signature other = (Signature) obj;
		if (!Arrays.equals(signature, other.signature))
			return false;
		return true;
	}

	public boolean hasPubkey() {
		return pubkey != null;
	}

	public byte[] getPubkey() {
		checkNotNull(pubkey);
		return pubkey;
	}

	public void setPubkey(byte[] pubkey) {
		this.pubkey = pubkey;
	}
}
