package it.unica.tcs.lib;

import java.util.Arrays;

import static com.google.common.base.Preconditions.*;

import it.unica.tcs.lib.utils.BitcoinUtils;

public abstract class Hash {

	private final byte[] bytes;
	
	public Hash(byte[] bytes) {
		this.bytes = bytes;
	}
	
	public byte[] getBytes() {
		return bytes;
	}
	
	public String getBytesAsString() {
		return BitcoinUtils.encode(bytes);
	}
	
	abstract public String getType();
	
	public static final class Hash160 extends Hash {
		public Hash160(byte[] bytes) {
			super(bytes);
			checkArgument(bytes.length==20, "Hash160: expected byte array of length 20");
		}
		public String getType() { 
			return "hash160";
		}
	}
	
	public static final class Hash256 extends Hash {
		public Hash256(byte[] bytes) {
			super(bytes);
			checkArgument(bytes.length==32, "Hash256: expected byte array of length 32");
		}
		public String getType() { 
			return "hash256";
		}
	}
	
	public static final class Ripemd160 extends Hash {
		public Ripemd160(byte[] bytes) {
			super(bytes);
			checkArgument(bytes.length==20, "Ripemd160: expected byte array of length 20");
		}
		public String getType() { 
			return "ripemd";
		}
	}
	
	public static final class Sha256 extends Hash {
		public Sha256(byte[] bytes) {
			super(bytes);
			checkArgument(bytes.length==32, "Sha256: expected byte array of length 32");
		}
		public String getType() { 
			return "sha256";
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(bytes);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Hash))
			return false;
		Hash other = (Hash) obj;
		if (!Arrays.equals(bytes, other.bytes))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getType()+":"+BitcoinUtils.encode(bytes);
	}
	
	public static void main(String[] args) {
		System.out.println(new Hash160(new byte[20]).equals(new Ripemd160(new byte[20])));
	}
}
