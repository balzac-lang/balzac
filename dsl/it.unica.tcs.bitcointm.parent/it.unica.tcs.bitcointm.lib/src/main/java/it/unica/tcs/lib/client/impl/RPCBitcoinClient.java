package it.unica.tcs.lib.client.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.sulacosoft.bitcoindconnector4j.BitcoindApi;
import com.sulacosoft.bitcoindconnector4j.BitcoindApiFactory;
import com.sulacosoft.bitcoindconnector4j.core.BitcoindException;
import com.sulacosoft.bitcoindconnector4j.response.RawTransaction;

import it.unica.tcs.lib.client.BitcoinClientI;
import it.unica.tcs.lib.client.Confidentiality;
import it.unica.tcs.lib.client.TransactionNotFoundException;

@Singleton
public class RPCBitcoinClient implements BitcoinClientI {

	public BitcoindApi api;
	
	@Inject
	public RPCBitcoinClient(
			@Named("bitcoind.address") String address, 
			@Named("bitcoind.port") int port, 
			@Named("bitcoind.protocol") String protocol,
			@Named("bitcoind.user") String user,
			@Named("bitcoind.password") String password
			) {
		this.api = BitcoindApiFactory.createConnection(address, port, protocol, user, password);
	}
	
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
		return isMined(txid, Confidentiality.HIGH);
	}

	@Override
	public boolean isMined(String txid, Confidentiality reliability) {
		try {
			RawTransaction tx = api.getrawtransaction(txid, true);
			return tx.getConfirmations() >= reliability.getConfirmations();
		}
		catch (BitcoindException e) {
			return false;
		}
	}

	@Override
	public String sendRawTransaction(String transaction) {
		return api.sendrawtransaction(transaction);
	}

	@Override
	public boolean isUTXO(String txid) {
		return isUTXO(txid, 0);
	}

	@Override
	public boolean isUTXO(String txid, int n) {
		return this.api.gettxout(txid, n)!=null;
	}
}
