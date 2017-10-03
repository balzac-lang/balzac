package it.unica.tcs.lib.model;

import com.google.inject.Inject;

import it.unica.tcs.lib.client.BitcoinClientI;

public class Put extends AbstractPrefix {

	@Inject private BitcoinClientI client;
	private final String txhex;
	
	Put(String txhex) {
		this.txhex = txhex;
	}

	@Override
	public boolean ready() {
		return true;
	}

	@Override
	public void execute() {
		String txid = client.sendRawTransaction(txhex);
		Prefix ask = PrefixFactory.ask(txid);
		ask.execute();
	}

}
