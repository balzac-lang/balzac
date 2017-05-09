package it.unica.tcs.bitcoin;

import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.Utils;

public class Test {

	
	
	public static void main(String[] args) {

		ECKey clientKey = new ECKey();
		
		System.out.println(clientKey);
		
		String pvtKey = clientKey.getPrivateKeyAsHex();
		String pubKey = clientKey.getPublicKeyAsHex();
		
		System.out.println(pvtKey);
		System.out.println(pubKey);

		ECKey clientKeyCopy = ECKey.fromPrivate(Utils.parseAsHexOrBase58(pvtKey));
		
		System.out.println(clientKeyCopy);
	}

}
