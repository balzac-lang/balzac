/*
 * Copyright 2020 Nicola Atzei
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package xyz.balzaclang.lib.model;

import static com.google.common.base.Preconditions.checkArgument;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import org.apache.commons.lang3.reflect.MethodUtils;
import org.bitcoinj.core.Utils;
import org.bitcoinj.script.ScriptBuilder;
import org.bouncycastle.crypto.digests.RIPEMD160Digest;

import xyz.balzaclang.lib.utils.BitcoinUtils;

public class Hash implements Comparable<Hash> {

    private final byte[] bytes;

    public enum HashAlgorithm {
        SHA256, RIPEMD160, HASH256, HASH160, SHA1
    }

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

    public static Hash hash(Object input, HashAlgorithm alg) {
        checkArgument(input instanceof Number || input instanceof Hash || input instanceof Boolean
            || input instanceof String || input instanceof byte[]);

        String methodName = switch (alg) {
            case HASH160 ->     { yield "hash160"; }
            case HASH256 ->     { yield "hash256"; }
            case RIPEMD160 ->   { yield "ripemd160"; }
            case SHA256 ->      { yield "sha256"; }
            case SHA1 ->        { yield "sha1"; }
        };

        try {
            Method method = MethodUtils.getMatchingMethod(Hash.class, methodName, input.getClass());
            return Hash.class.cast(method.invoke(null, input));
        } catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new IllegalStateException(e);
        }
    }

    public static Hash sha1(byte[] bytes) {
        return hash(bytes, "SHA-1");
    }

    public static Hash sha256(byte[] bytes) {
        return hash(bytes, "SHA-256");
    }

    public static Hash ripemd160(byte[] bytes) {
        RIPEMD160Digest digest = new RIPEMD160Digest();
        digest.update(bytes, 0, bytes.length);
        byte[] ripmemdHash = new byte[20];
        digest.doFinal(ripmemdHash, 0);
        return new Hash(ripmemdHash);
    }

    public static Hash hash160(byte[] bytes) {
        return ripemd160(sha256(bytes));
    }

    public static Hash hash256(byte[] bytes) {
        return sha256(sha256(bytes));
    }

    private static Hash hash(byte[] bytes, String algo) {
        try {
            MessageDigest digest = MessageDigest.getInstance(algo);
            digest.update(bytes);
            byte[] digested = digest.digest();
            return new Hash(digested);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Unable to instantiate a message digest for " + algo, e);
        }
    }

    public static Hash hash160(Hash obj) {
        return hash160(obj.getBytes());
    }

    public static Hash hash256(Hash obj) {
        return hash256(obj.getBytes());
    }

    public static Hash ripemd160(Hash obj) {
        return ripemd160(obj.getBytes());
    }

    public static Hash sha256(Hash obj) {
        return sha256(obj.getBytes());
    }

    public static Hash sha1(Hash obj) {
        return sha1(obj.getBytes());
    }

    public static Hash hash160(String obj) {
        return hash160(obj.getBytes(Charset.forName("UTF-8")));
    }

    public static Hash hash256(String obj) {
        return hash256(obj.getBytes(Charset.forName("UTF-8")));
    }

    public static Hash ripemd160(String obj) {
        return ripemd160(obj.getBytes(Charset.forName("UTF-8")));
    }

    public static Hash sha256(String obj) {
        return sha256(obj.getBytes(Charset.forName("UTF-8")));
    }

    public static Hash sha1(String obj) {
        return sha1(obj.getBytes(Charset.forName("UTF-8")));
    }

    public static Hash hash160(Boolean obj) {
        return hash160(obj ? TRUE : FALSE);
    }

    public static Hash hash256(Boolean obj) {
        return hash256(obj ? TRUE : FALSE);
    }

    public static Hash ripemd160(Boolean obj) {
        return ripemd160(obj ? TRUE : FALSE);
    }

    public static Hash sha256(Boolean obj) {
        return sha256(obj ? TRUE : FALSE);
    }

    public static Hash sha1(Boolean obj) {
        return sha1(obj ? TRUE : FALSE);
    }

    public static Hash hash160(Number obj) {
        byte[] bytes = getIntegerBytes(obj);
        return hash160(bytes);
    }

    public static Hash hash256(Number obj) {
        byte[] bytes = getIntegerBytes(obj);
        return hash256(bytes);
    }

    public static Hash ripemd160(Number obj) {
        byte[] bytes = getIntegerBytes(obj);
        return ripemd160(bytes);
    }

    public static Hash sha256(Number obj) {
        byte[] bytes = getIntegerBytes(obj);
        return sha256(bytes);
    }

    public static Hash sha1(Number obj) {
        byte[] bytes = getIntegerBytes(obj);
        return sha1(bytes);
    }

    private static byte[] getIntegerBytes(Number n1) {
        long n = n1.longValue();
        if (n == 0)
            return ZERO;
        if (n == -1)
            return NEGATIVE_ONE;
        if (1 <= n && n <= 16)
            return Utils.reverseBytes(Utils.encodeMPI(BigInteger.valueOf(n), false));
        return new ScriptBuilder().number(n).build().getChunks().get(0).data; // get the data part of the push operation
    }

    private static final byte[] FALSE = new byte[] {};
    private static final byte[] NEGATIVE_ONE = Utils.reverseBytes(Utils.encodeMPI(BigInteger.ONE.negate(), false));
    private static final byte[] TRUE = Utils.reverseBytes(Utils.encodeMPI(BigInteger.ONE, false));;
    private static final byte[] ZERO = FALSE;
}
