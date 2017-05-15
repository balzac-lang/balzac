package it.unica.tcs.validation;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.Utils;

public class BitcoinJUtils extends AbstractBitcoinTMValidator{

	
	public static boolean isValidKey(String key) {
		return Utils.parseAsHexOrBase58(key)!=null;
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
}
