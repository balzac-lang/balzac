/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.lib.utils;

import java.nio.charset.Charset;

import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.Utils;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.script.ScriptOpCodes;
import org.spongycastle.crypto.digests.RIPEMD160Digest;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import it.unica.tcs.lib.Hash;
import it.unica.tcs.lib.Hash.Hash160;
import it.unica.tcs.lib.Hash.Hash256;
import it.unica.tcs.lib.Hash.Ripemd160;
import it.unica.tcs.lib.Hash.Sha256;
import it.unica.tcs.lib.client.BitcoinClientException;
import it.unica.tcs.lib.client.BitcoinClientI;
import it.unica.tcs.lib.client.TransactionNotFoundException;

@Singleton
public class BitcoinUtils {

	@Inject private BitcoinClientI bitcoin;

	public Transaction getTransactionByIdOrHex(String txString, NetworkParameters params) throws BitcoinClientException {
		Transaction tx;
		try {
			tx = new Transaction(params, decode(txString));
		}
		catch (Exception e) {
			try {
				tx = getTransaction(txString, params);
			}
			catch (Exception e1) {
				throw new BitcoinClientException(e1) ;
			}
		}
		return tx;
	}
	
	public Transaction getTransaction(String txid, NetworkParameters params) throws TransactionNotFoundException {
		byte[] payloadBytes = decode(bitcoin.getRawTransaction(txid));
		return new Transaction(params, payloadBytes);
	}
	
	public static ECKey wifToECKey(String wif, NetworkParameters params) {
		return DumpedPrivateKey.fromBase58(params, wif).getKey();
	}
	
	public static String encode(byte[] bytes) {
		return Utils.HEX.encode(bytes);
	}
	
	public static byte[] decode(String chars) {
		return Utils.HEX.decode(chars);
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
	
	public static Hash160 hash160(Object obj) {
		return hash160(toScript(obj).getProgram());
	}

	public static Hash256 hash256(Object obj) {
		return hash256(toScript(obj).getProgram());
	}
	
	public static Ripemd160 ripemd160(Object obj) {
		return ripemd160(toScript(obj).getProgram());
	}
	
	public static Sha256 sha256(Object obj) {
		return sha256(toScript(obj).getProgram());
	}
	
	public static Script toScript(Object obj) {
		if (obj instanceof Integer)
			return new ScriptBuilder().number((Integer) obj).build();
		
		else if (obj instanceof String)
			return new ScriptBuilder().data(((String) obj).getBytes(Charset.forName("UTF-8"))).build();

		else if (obj instanceof Hash)
			return new ScriptBuilder().data(((Hash) obj).getBytes()).build();

		else if (obj instanceof Boolean) {
			if ((Boolean) obj) new ScriptBuilder().number(ScriptOpCodes.OP_TRUE).build();
			else new ScriptBuilder().op(ScriptOpCodes.OP_FALSE).build();
		}
		
		throw new IllegalArgumentException();
	}

}
