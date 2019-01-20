/*
 * Copyright 2019 Nicola Atzei
 */

package it.unica.tcs.lib.model;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Arrays;
import java.util.Optional;

import it.unica.tcs.lib.utils.BitcoinUtils;


public class Signature {

    private final byte[] signature;
    private Optional<byte[]> pubkey = Optional.empty();

    public Signature(byte[] signature) {
    	this(signature, null);
    }

    public Signature(byte[] signature, byte[] pubkey) {
    	checkArgument(signature != null, "Signature cannot be null");
    	this.signature = Arrays.copyOf(signature, signature.length);
    	setPubkey(pubkey);	
	}

	public byte[] getSignature() {
		return signature;
	}
	
	public Optional<byte[]> getPubkey() {
		return pubkey;
	}
	
	public void setPubkey(byte[] pubkey) {
		this.pubkey = Optional.ofNullable(pubkey != null? Arrays.copyOf(pubkey, pubkey.length): null);
	}

	@Override
    public String toString() {
        return "sig:"+BitcoinUtils.encode(signature) + (pubkey.isPresent()? "[pubkey:" + BitcoinUtils.encode(pubkey.get()) +  "]" : "");
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
}
