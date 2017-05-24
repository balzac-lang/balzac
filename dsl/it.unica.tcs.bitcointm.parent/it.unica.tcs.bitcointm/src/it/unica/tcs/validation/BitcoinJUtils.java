package it.unica.tcs.validation;

import java.util.Arrays;
import java.util.List;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.Base58;
import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.Utils;
import org.bitcoinj.script.Script;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.EcoreUtil2;

import it.unica.tcs.bitcoinTM.NetworkDeclaration;

public class BitcoinJUtils {

	public static ValidationResult VALIDATION_OK = new ValidationResult(true);
	public static ValidationResult VALIDATION_ERROR = new ValidationResult(false);
	public static class ValidationResult {
		boolean ok;
		String message;
		
		public ValidationResult(boolean ok) {
			this(ok, null);
		}
		
		public ValidationResult(boolean ok, String message) {
			this.ok = ok;
			this.message = message;
		}
	}
	
	public static byte[] wifToHash(String wif, NetworkParameters params) {
		return wifToAddress(wif, params).getHash160();
	}
	
	public static Address wifToAddress(String wif, NetworkParameters params) {
		Address pubkeyAddr = Address.fromBase58(params, wif);
		return pubkeyAddr;
	}

	public static ValidationResult isBase58WithChecksum(String key) {
		try {
			Base58.decodeChecked(key);
			return VALIDATION_OK;
		} catch (AddressFormatException e1) {
			return new ValidationResult(false, e1.getMessage());
		}
	}
	
	public static ValidationResult isValidPrivateKey(String key, NetworkParameters params) {
		try {
			DumpedPrivateKey.fromBase58(params, key);
			return VALIDATION_OK;
		} catch (AddressFormatException e2) {
			return new ValidationResult(false, e2.getMessage());
		}
	}
		
	public static ValidationResult isValidPublicKey(String key, NetworkParameters params) {
		try {
			Address.fromBase58(params, key);
			return VALIDATION_OK;
		} catch (AddressFormatException e2) {
			return new ValidationResult(false, e2.getMessage());
		}
	}
	
	public static NetworkParameters networkParams(EObject obj) {
		List<NetworkDeclaration> list = EcoreUtil2.getAllContentsOfType(EcoreUtil2.getRootContainer(obj), NetworkDeclaration.class);
			
		if (list.size()==0)	// network undeclared, assume testnet
			return NetworkParameters.fromID(NetworkParameters.ID_TESTNET);	
			
		if (list.size()==1)
			return list.get(0).isTestnet()? 
					NetworkParameters.fromID(NetworkParameters.ID_TESTNET): 
					NetworkParameters.fromID(NetworkParameters.ID_MAINNET);
			
		throw new IllegalStateException();
	}
	

		
	public static ValidationResult isValidTransaction(String txString, NetworkParameters params) {
		
		/*
		 * TODO: perché non consentire anche il recupero della transazione
		 * tramite il suo hash? Non so ancora quale sia il modo migliore per farlo.
		 */
		try {
			Transaction tx = new Transaction(params, Utils.HEX.decode(txString));
			tx.verify();
		}
		catch (Exception e) {
			return new ValidationResult(false, e.getMessage());
		}
		
		return VALIDATION_OK;
	}
	
	public static long getOutputAmount(String txString, NetworkParameters params, int index) {
		try {
			Transaction tx = new Transaction(params, Utils.HEX.decode(txString));
			return tx.getOutput(index).getValue().value;
		}
		catch (Exception e) {
			return -1;
		}
	}
	
	public static int getNumberOfOutputs(String txString, NetworkParameters params) {
		try {
			Transaction tx = new Transaction(params, Utils.HEX.decode(txString));
			return tx.getOutputs().size();
		}
		catch (Exception e) {
			return -1;
		}
	}
	
	public static Script getOutput(String txString, NetworkParameters params, int index) {
		try {
			Transaction tx = new Transaction(params, Utils.HEX.decode(txString));
			return tx.getOutputs().get(index).getScriptPubKey();
		}
		catch (Exception e) {
			return null;
		}
	}
	
	public static ValidationResult isValidKeyPair(String pvtKey, String pubKey, NetworkParameters params) {
		ECKey keyPair = DumpedPrivateKey.fromBase58(params, pvtKey).getKey();
		Address pubkeyAddr = Address.fromBase58(params, pubKey);

		boolean isValid = Arrays.equals(keyPair.getPubKeyHash(), pubkeyAddr.getHash160());
		
		return isValid? VALIDATION_OK: VALIDATION_ERROR;
	}
}
