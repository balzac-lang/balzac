/*
 * Copyright 2019 Nicola Atzei
 */

package it.unica.tcs.lib.model;

import java.math.BigInteger;
import java.util.Arrays;

import it.unica.tcs.lib.utils.BitcoinUtils;

public class Hash implements Comparable<Hash> {

    private final byte[] bytes;
    public enum HashAlgorithm { SHA256, RIPEMD160, HASH256, HASH160, SHA1 }

    public Hash(byte[] bytes) {
        this.bytes = bytes;
    }

    public byte[] getBytes() {
        return Arrays.copyOf(bytes, bytes.length);
    }

    public String getBytesAsString() {
        return BitcoinUtils.encode(bytes);
    }

    public static Hash fromString(String str) {
        return new Hash(BitcoinUtils.decode(str));
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
        return getBytesAsString();
    }

    @Override
    public int compareTo(Hash o) {
        if (bytes.length != o.bytes.length) {
            return bytes.length - o.bytes.length;
        }
        BigInteger a = new BigInteger(1, bytes);
        BigInteger b = new BigInteger(1, o.bytes);
        return a.compareTo(b);
    }
}
