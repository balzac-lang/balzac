/*
 * Copyright 2017 Nicola Atzei
 */
package it.unica.tcs.lib;

import java.util.Arrays;

import it.unica.tcs.lib.utils.BitcoinUtils;

public class Hash {

    private final byte[] bytes;
    public enum HashAlgorithm { SHA256, RIPEMD160, HASH256, HASH160, SHA1 }

    public Hash(byte[] bytes) {
        this.bytes = bytes;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public String getBytesAsString() {
        return BitcoinUtils.encode(bytes);
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
}
