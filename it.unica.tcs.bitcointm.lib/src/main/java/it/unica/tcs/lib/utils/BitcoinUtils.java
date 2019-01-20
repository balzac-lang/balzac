/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.lib.utils;

import static com.google.common.base.Preconditions.checkArgument;

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
import org.spongycastle.crypto.digests.SHA1Digest;

import it.unica.tcs.lib.model.Hash;
import it.unica.tcs.lib.model.Hash.HashAlgorithm;
import it.unica.tcs.lib.model.Signature;

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

    public static Hash hash(Object input, HashAlgorithm alg) {
        checkArgument(input instanceof Number || input instanceof Hash || input instanceof Boolean || input instanceof String || input instanceof byte[]);

        String methodName = null;
        
        switch (alg) {
        case HASH160: methodName = "hash160"; break;
        case HASH256: methodName = "hash256"; break;
        case RIPEMD160: methodName = "ripemd160"; break;
        case SHA256: methodName = "sha256"; break;
        case SHA1: methodName = "sha1"; break;
            default: throw new IllegalArgumentException("unexpected class "+alg);
        }
            
        try {
            Method method = MethodUtils.getMatchingMethod(BitcoinUtils.class, methodName, input.getClass());
            return Hash.class.cast(method.invoke(null, input));
        } catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new IllegalStateException(e);
        }
    }

    public static Hash hash160(byte[] bytes) {
        return new Hash(Utils.sha256hash160(bytes));
    }

    public static Hash hash256(byte[] bytes) {
        return new Hash(Sha256Hash.hashTwice(bytes));
    }

    public static Hash sha256(byte[] bytes) {
        return new Hash(Sha256Hash.hash(bytes));
    }

    public static Hash ripemd160(byte[] bytes) {
        RIPEMD160Digest digest = new RIPEMD160Digest();
        digest.update(bytes, 0, bytes.length);
        byte[] ripmemdHash = new byte[20];
        digest.doFinal(ripmemdHash, 0);
        return new Hash(ripmemdHash);
    }

    public static Hash sha1(byte[] bytes) {
        SHA1Digest digest = new SHA1Digest();
        digest.update(bytes, 0, bytes.length);
        byte[] sha1Hash = new byte[20];
        digest.doFinal(sha1Hash, 0);
        return new Hash(sha1Hash);
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
        if (n==0) return ZERO;
        if (n==-1) return NEGATIVE_ONE;
        if (1<=n && n<=16) return Utils.reverseBytes(Utils.encodeMPI(BigInteger.valueOf(n), false));
        return new ScriptBuilder().number(n).build().getChunks().get(0).data;   // get the data part of the push operation
    }

    private static final byte[] FALSE = new byte[]{};
    private static final byte[] NEGATIVE_ONE = Utils.reverseBytes(Utils.encodeMPI(BigInteger.ONE.negate(), false));
    private static final byte[] TRUE = Utils.reverseBytes(Utils.encodeMPI(BigInteger.ONE, false));;
    private static final byte[] ZERO = FALSE;


    // TODO: move this to a new interface
    public static Script toScript(Object obj) {

        if (obj instanceof Number)
            return new ScriptBuilder().number(((Number) obj).longValue()).build();

        else if (obj instanceof String)
            return new ScriptBuilder().data(((String) obj).getBytes(Charset.forName("UTF-8"))).build();

        else if (obj instanceof Hash)
            return new ScriptBuilder().data(((Hash) obj).getBytes()).build();

        else if (obj instanceof Boolean)
            return ((Boolean) obj)? new ScriptBuilder().opTrue().build(): new ScriptBuilder().opFalse().build();

        else if (obj instanceof TransactionSignature)
            return new ScriptBuilder().data(((TransactionSignature) obj).encodeToBitcoin()).build();
       
        else if (obj instanceof Signature) {
        	Signature sig = (Signature) obj;
        	ScriptBuilder sb = new ScriptBuilder();
        	sb.data(sig.getSignature());
        	if (sig.hasPubkey())
        		sb.data(sig.getPubkey());
        	return sb.build();
        }

        throw new IllegalArgumentException();
    }

}
