package it.unica.tcs.lib.model;

import it.unica.tcs.lib.BitcoinUtilsFactory;
import it.unica.tcs.lib.client.BitcoinClientI;

public class Put extends AbstractPrefix {

	private BitcoinClientI client;
	private final String txhex;
	
	Put(String txhex) {
		this.txhex = txhex;
		this.client = BitcoinUtilsFactory.create().getBitcoinClient();
	}

	@Override
	public boolean ready() {
		return true;
	}

	@Override
	public void run() {
//		String txid = 
				client.sendRawTransaction(txhex);
//		Prefix ask = PrefixFactory.ask(txid);
//		ask.run();
	}

}
