package it.unica.tcs.lib.client.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sulacosoft.bitcoindconnector4j.BitcoindApi;
import com.sulacosoft.bitcoindconnector4j.core.BitcoindException;
import com.sulacosoft.bitcoindconnector4j.response.RawTransaction;

import it.unica.tcs.lib.client.BitcoinClientI;
import it.unica.tcs.lib.client.Reliability;
import it.unica.tcs.lib.client.TransactionNotFoundException;

@Singleton
public class RPCBitcoinClient implements BitcoinClientI {

	@Inject private BitcoindApi api;
	
	@Override
	public int getBlockCount() {
		return api.getblockcount();
	}

	@Override
	public String getRawTransaction(String txid) throws TransactionNotFoundException {
		return api.getrawtransaction(txid);
	}

	@Override
	public boolean isMined(String txid) {
		return isMined(txid, Reliability.HIGH);
	}

	@Override
	public boolean isMined(String txid, Reliability reliability) {
		try {
			RawTransaction tx = api.getrawtransaction(txid, true);
			return tx.getConfirmations() >= reliability.getConfirmations();
		}
		catch (BitcoindException e) {
			return false;
		}
	}
}
