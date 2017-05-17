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
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.EcoreUtil2;

import it.unica.tcs.bitcoinTM.NetworkDeclaration;

public class BitcoinJUtils extends AbstractBitcoinTMValidator{

	public static byte[] wifToHash(String wif, NetworkParameters params) {
		Address pubkeyAddr = Address.fromBase58(params, wif);
		return pubkeyAddr.getHash160();
	}

	public static boolean isBase58WithChecksum(String key) {
		try {
			Base58.decodeChecked(key);
			return true;
		} catch (AddressFormatException e1) {
			return false;
		}
	}
	
	public static boolean isValidPrivateKey(String key, NetworkParameters params) {
		try {
			DumpedPrivateKey.fromBase58(params, key);
			return true;
		} catch (AddressFormatException e2) {
			System.out.println(e2);
			return false;
		}
	}
		
	public static boolean isValidPublicKey(String key, NetworkParameters params) {
		try {
			Address.fromBase58(params, key);
			return true;
		} catch (AddressFormatException e2) {
			System.out.println(e2);
			return false;
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
	

	public static boolean isValidTransaction(String txString) {
		
		/*
		 * TODO: perché non consentire anche il recupero della transazione
		 * tramite il suo hash? Non so ancora quale sia il modo migliore per farlo.
		 */
		try {
			NetworkParameters params = NetworkParameters.fromID(NetworkParameters.ID_MAINNET);
			Transaction tx = new Transaction(params, Utils.parseAsHexOrBase58(txString));
			
			tx.verify();
		}
		catch (Exception e) {
			return false;
		}
		
		return true;
	}
	
	public static boolean isValidKeyPair(String pvtKey, String pubKey, NetworkParameters params) {
		
		System.out.println(params.getId());
		
		ECKey keyPair = DumpedPrivateKey.fromBase58(params, pvtKey).getKey();
		Address pubkeyAddr = Address.fromBase58(params, pubKey);
		
		return Arrays.equals(keyPair.getPubKeyHash(), pubkeyAddr.getHash160());
	}
}
