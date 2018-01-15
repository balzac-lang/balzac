/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.lib.utils;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.nio.charset.Charset;

import org.apache.commons.lang3.reflect.MethodUtils;
import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Utils;
import org.bitcoinj.crypto.TransactionSignature;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.spongycastle.crypto.digests.RIPEMD160Digest;

import it.unica.tcs.lib.Hash;
import it.unica.tcs.lib.Hash.Hash160;
import it.unica.tcs.lib.Hash.Hash256;
import it.unica.tcs.lib.Hash.Ripemd160;
import it.unica.tcs.lib.Hash.Sha256;

public class BitcoinUtils {

    public static ECKey wifToECKey(String wif, NetworkParameters params) {
        return DumpedPrivateKey.fromBase58(params, wif).getKey();
    }

    public static String encode(byte[] bytes) {
        return Utils.HEX.encode(bytes);
    }

    public static byte[] decode(String chars) {
        return Utils.HEX.decode(chars.toLowerCase());
    }

    public static <T extends Hash> T hash(Object input, Class<T> clazz) {
        checkArgument(input instanceof Number || input instanceof Hash || input instanceof Boolean || input instanceof String || input instanceof byte[]);

        String methodName = null;
        if (clazz.equals(Hash160.class)) {
            methodName = "hash160";
        }
        else if (clazz.equals(Hash256.class)) {
            methodName = "hash256";
        }
        else if (clazz.equals(Ripemd160.class)) {
            methodName = "ripemd160";
        }
        else if (clazz.equals(Sha256.class)) {
            methodName = "sha256";
        }
        else throw new IllegalArgumentException("unexpected class "+clazz);

        try {
            Method method = MethodUtils.getMatchingMethod(BitcoinUtils.class, methodName, input.getClass());
            checkState(clazz.equals(method.getReturnType()));
            return clazz.cast(method.invoke(null, input));
        } catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new IllegalStateException(e);
        }
    }

    public static Hash160 hash160(byte[] bytes) {
        return new Hash160(Utils.sha256hash160(bytes));
    }

    public static Hash256 hash256(byte[] bytes) {
        return new Hash256(Sha256Hash.hashTwice(bytes));
    }

    public static Sha256 sha256(byte[] bytes) {
        return new Sha256(Sha256Hash.hash(bytes));
    }

    public static Ripemd160 ripemd160(byte[] bytes) {
        RIPEMD160Digest digest = new RIPEMD160Digest();
        digest.update(bytes, 0, bytes.length);
        byte[] ripmemdHash = new byte[20];
        digest.doFinal(ripmemdHash, 0);
        return new Ripemd160(ripmemdHash);
    }

    public static Hash160 hash160(Hash obj) {
        return hash160(obj.getBytes());
    }

    public static Hash256 hash256(Hash obj) {
        return hash256(obj.getBytes());
    }

    public static Ripemd160 ripemd160(Hash obj) {
        return ripemd160(obj.getBytes());
    }

    public static Sha256 sha256(Hash obj) {
        return sha256(obj.getBytes());
    }

    public static Hash160 hash160(String obj) {
        return hash160(obj.getBytes(Charset.forName("UTF-8")));
    }

    public static Hash256 hash256(String obj) {
        return hash256(obj.getBytes(Charset.forName("UTF-8")));
    }

    public static Ripemd160 ripemd160(String obj) {
        return ripemd160(obj.getBytes(Charset.forName("UTF-8")));
    }

    public static Sha256 sha256(String obj) {
        return sha256(obj.getBytes(Charset.forName("UTF-8")));
    }

    public static Hash160 hash160(Boolean obj) {
        return hash160(obj ? TRUE : FALSE);
    }

    public static Hash256 hash256(Boolean obj) {
        return hash256(obj ? TRUE : FALSE);
    }

    public static Ripemd160 ripemd160(Boolean obj) {
        return ripemd160(obj ? TRUE : FALSE);
    }

    public static Sha256 sha256(Boolean obj) {
        return sha256(obj ? TRUE : FALSE);
    }

    public static Hash160 hash160(Number obj) {
        byte[] bytes = getIntegerBytes(obj);
        return hash160(bytes);
    }

    public static Hash256 hash256(Number obj) {
        byte[] bytes = getIntegerBytes(obj);
        return hash256(bytes);
    }

    public static Ripemd160 ripemd160(Number obj) {
        byte[] bytes = getIntegerBytes(obj);
        return ripemd160(bytes);
    }

    public static Sha256 sha256(Number obj) {
        byte[] bytes = getIntegerBytes(obj);
        return sha256(bytes);
    }

    private static byte[] getIntegerBytes(Number n1) {
        long n = n1.longValue();
        if (n==0) return ZERO;
        if (n==-1) return NEGATIVE_ONE;
        if (1<=n && n<=16) return Utils.reverseBytes(Utils.encodeMPI(BigInteger.valueOf(n), false));
        return new ScriptBuilder().number(n).build().getChunks().get(0).data;   // get the data part of the push operation
    }

    private static final byte[] FALSE = new byte[]{};
    private static final byte[] NEGATIVE_ONE = Utils.reverseBytes(Utils.encodeMPI(BigInteger.ONE.negate(), false));
    private static final byte[] TRUE = Utils.reverseBytes(Utils.encodeMPI(BigInteger.ONE, false));;
    private static final byte[] ZERO = FALSE;



    public static Script toScript(Object obj) {

        if (obj instanceof Number)
            return new ScriptBuilder().number(((Number) obj).longValue()).build();

        else if (obj instanceof String)
            return new ScriptBuilder().data(((String) obj).getBytes(Charset.forName("UTF-8"))).build();

        else if (obj instanceof Hash)
            return new ScriptBuilder().data(((Hash) obj).getBytes()).build();

        else if (obj instanceof Boolean)
            return ((Boolean) obj)? new ScriptBuilder().opTrue().build(): new ScriptBuilder().opFalse().build();

        else if (obj instanceof DumpedPrivateKey)
            return new ScriptBuilder().data(((DumpedPrivateKey) obj).getKey().getPubKey()).build();

        else if (obj instanceof TransactionSignature)
            return new ScriptBuilder().data(((TransactionSignature) obj).encodeToBitcoin()).build();

        throw new IllegalArgumentException();
    }

}
