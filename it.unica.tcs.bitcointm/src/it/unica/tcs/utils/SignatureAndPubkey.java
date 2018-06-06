package it.unica.tcs.utils;

import java.util.Arrays;

import it.unica.tcs.lib.utils.BitcoinUtils;

public class SignatureAndPubkey {

	public static final SignatureAndPubkey placeholder = new SignatureAndPubkey(new byte[0], new byte[0]);
    private final byte[] signature;
    private final byte[] pubkey;
    
    public SignatureAndPubkey(byte[] signature, byte[] pubkey) {
		this.signature = signature;
		this.pubkey = pubkey;
	}

	public byte[] getSignature() {
		return signature;
	}

	public byte[] getPubkey() {
		return pubkey;
	}

	@Override
    public String toString() {
        if (this == placeholder) return "_";
        return "sig:"+BitcoinUtils.encode(signature)+" [pubkey:"+BitcoinUtils.encode(pubkey)+"]";
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(pubkey);
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
		SignatureAndPubkey other = (SignatureAndPubkey) obj;
		if (!Arrays.equals(pubkey, other.pubkey))
			return false;
		if (!Arrays.equals(signature, other.signature))
			return false;
		return true;
	}
}
