package it.unica.tcs.validation;

import org.bitcoinj.core.Utils;

public class BitcoinJUtils extends AbstractBitcoinTMValidator{

	
	public static boolean isValidKey(String key) {
		return Utils.parseAsHexOrBase58(key)!=null;
	}
}
