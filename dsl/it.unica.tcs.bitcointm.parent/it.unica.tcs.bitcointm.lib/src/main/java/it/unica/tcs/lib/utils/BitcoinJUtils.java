/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.lib.utils;

import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Utils;
import org.bitcoinj.script.ScriptBuilder;
import org.spongycastle.crypto.digests.RIPEMD160Digest;

public class BitcoinJUtils {

	public static ECKey wifToECKey(String wif, NetworkParameters params) {
		return DumpedPrivateKey.fromBase58(params, wif).getKey();
	}
	
	public static String encode(byte[] bytes) {
		return Utils.HEX.encode(bytes);
	}
	
	public static byte[] decode(String chars) {
		return Utils.HEX.decode(chars);
	}
	
	public static byte[] hash160(byte[] bytes) {
		return Utils.sha256hash160(bytes);
	}
	
	public static byte[] hash256(byte[] bytes) {
		return Sha256Hash.hashTwice(bytes);
	}
	
	public static byte[] sha256(byte[] bytes) {
		return Sha256Hash.hash(bytes);
	}
	
	public static byte[] ripemd160(byte[] bytes) {
		RIPEMD160Digest digest = new RIPEMD160Digest();
        digest.update(bytes, 0, bytes.length);
        byte[] ripmemdHash = new byte[20];
        digest.doFinal(ripmemdHash, 0);
        return ripmemdHash;        
	}
	
	/**
	 * @param obj
	 * @return the bytes representation of the given object
	 */
	public static byte[] toBytes(Object obj) {
		if (obj instanceof Integer)
			return _toBytes((Integer) obj);
		else if (obj instanceof String)
			return _toBytes((String) obj);
		else if (obj instanceof byte[])
			return _toBytes((byte[]) obj);
		else if (obj instanceof Boolean)
			return _toBytes((Boolean) obj);
		throw new IllegalArgumentException();
	}
	
    private static byte[] _toBytes(Integer n) {
    	return new ScriptBuilder().number(n).build().getProgram();
    }
    
    private static byte[] _toBytes(Boolean b) {
    	return new ScriptBuilder().number(b? 1 : 0).build().getProgram();
    }
    
    private static byte[] _toBytes(byte[] b) {
    	return b;
    }
    
    private static byte[] _toBytes(String s) {
    	return new ScriptBuilder().data(s.getBytes()).build().getProgram();
    }
}
