package it.unica.tcs.lib.model.prefix;

import com.google.inject.Inject;

import it.unica.tcs.lib.client.BitcoinClientI;
import it.unica.tcs.lib.model.process.Process;

public class Put extends AbstractPrefix {

	@Inject private BitcoinClientI client;
	private final String txhex;
	
	Put(String txhex, Process next) {
		super(next);
		this.txhex = txhex;
	}

	@Override
	public boolean ready() {
		return true;
	}

	@Override
	public void execute() {
		String txid = client.sendTransaction(txhex);
		Prefix ask = PrefixFactory.ask(txid);
		ask.execute();
		next.run();
	}

}
