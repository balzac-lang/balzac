/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.utils;

import java.util.Arrays;
import java.util.List;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.Base58;
import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.Transaction.SigHash;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.EcoreUtil2;

import com.google.inject.Singleton;

import it.unica.tcs.bitcoinTM.Modifier;
import it.unica.tcs.bitcoinTM.Network;
import it.unica.tcs.lib.BitcoinTMUtils;
import it.unica.tcs.lib.utils.BitcoinJUtils;
import it.unica.tcs.validation.ValidationResult;

@Singleton
class JavaASTUtils {

	public byte[] wifToAddressHash(String wif, NetworkParameters params) {
		return wifToAddress(wif, params).getHash160();
	}
	
	public Address wifToAddress(String wif, NetworkParameters params) {
		Address pubkeyAddr = Address.fromBase58(params, wif);
		return pubkeyAddr;
	}
	
	public byte[] privateKeyToPubkeyBytes(String wif, NetworkParameters params) {
		return DumpedPrivateKey.fromBase58(params, wif).getKey().getPubKey();
	}

	public ValidationResult isBase58WithChecksum(String key) {
		try {
			Base58.decodeChecked(key);
			return ValidationResult.VALIDATION_OK;
		} catch (AddressFormatException e1) {
			return new ValidationResult(false, e1.getMessage());
		}
	}
	
	public ValidationResult isValidPrivateKey(String key, NetworkParameters params) {
		try {
			DumpedPrivateKey.fromBase58(params, key);
			return ValidationResult.VALIDATION_OK;
		} catch (AddressFormatException e2) {
			return new ValidationResult(false, e2.getMessage());
		}
	}
		
	public ValidationResult isValidPublicKey(String key, NetworkParameters params) {
		try {
			Address.fromBase58(params, key);
			return ValidationResult.VALIDATION_OK;
		} catch (AddressFormatException e2) {
			return new ValidationResult(false, e2.getMessage());
		}
	}
	
	public NetworkParameters networkParams(EObject obj) {
		List<Network> list = EcoreUtil2.getAllContentsOfType(EcoreUtil2.getRootContainer(obj), Network.class);
			
		if (list.size()==0)	// network undeclared, assume testnet
			return TestNet3Params.get();	
			
		if (list.size()==1)
			return list.get(0).isTestnet()? TestNet3Params.get(): MainNetParams.get();
			
		throw new IllegalStateException();
	}
	

	
		
	public ValidationResult isValidTransaction(String txString, NetworkParameters params) {
		
		try {
			Transaction tx = BitcoinTMUtils.create().bitcoinLib().getTransactionByIdOrHex(txString, params);
			tx.verify();
			return ValidationResult.VALIDATION_OK;
		}
		catch (Exception e) {
			return new ValidationResult(false, e.getMessage());				
		}
	}
	
	public long getOutputAmount(String txString, NetworkParameters params, int index) {
		try {
			Transaction tx = new Transaction(params, BitcoinJUtils.decode(txString));
			return tx.getOutput(index).getValue().value;
		}
		catch (Exception e) {
			return -1;
		}
	}
	
	public ValidationResult isValidKeyPair(String pvtKey, String pubKey, NetworkParameters params) {
		ECKey keyPair = DumpedPrivateKey.fromBase58(params, pvtKey).getKey();
		Address pubkeyAddr = Address.fromBase58(params, pubKey);

		boolean isValid = Arrays.equals(keyPair.getPubKeyHash(), pubkeyAddr.getHash160());
		
		return isValid? ValidationResult.VALIDATION_OK: ValidationResult.VALIDATION_ERROR;
	}
	
	public SigHash toHashType(Modifier mod) {
		switch (mod) {
		case AIAO:
        case SIAO: return SigHash.ALL;
        case AISO:
        case SISO: return SigHash.SINGLE;
        case AINO:
        case SINO: return SigHash.NONE;
        default: throw new IllegalStateException();
		}
    }
	
	public boolean toAnyoneCanPay(Modifier mod) {
		switch (mod) {
        case SIAO:
        case SISO:
        case SINO: return true;
        case AIAO:
        case AISO:
        case AINO: return false;
        default: throw new IllegalStateException();
		}
	}
}
