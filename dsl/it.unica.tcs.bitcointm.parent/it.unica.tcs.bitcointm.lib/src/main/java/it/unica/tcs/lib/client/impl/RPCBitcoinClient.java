package it.unica.tcs.lib.client.impl;

import com.sulacosoft.bitcoindconnector4j.BitcoindApi;
import com.sulacosoft.bitcoindconnector4j.BitcoindApiFactory;
import com.sulacosoft.bitcoindconnector4j.core.BitcoindException;
import com.sulacosoft.bitcoindconnector4j.response.RawTransaction;

import it.unica.tcs.lib.client.BitcoinClientI;
import it.unica.tcs.lib.client.Reliability;
import it.unica.tcs.lib.client.TransactionNotFoundException;

public class RPCBitcoinClient implements BitcoinClientI {

	private static final RPCBitcoinClient instance = new RPCBitcoinClient();	
	
	private final BitcoindApi api;
	
	private RPCBitcoinClient() {
		// bitcoind server connection parameters
		String address = "co2.unica.it";
		int port = 18332;
		String protocol = "http";
		String user = "bitcoin";
		String password = "L4mbWnzC35BNrmTJ";

		// create api
		this.api = BitcoindApiFactory.createConnection(address, port, protocol, user, password);
	}
	
	public static RPCBitcoinClient instance() {
		return instance;
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

	public static void main(String[] args) throws TransactionNotFoundException {
		RPCBitcoinClient client = RPCBitcoinClient.instance();

		System.out.println("Best block count: " + client.getBlockCount());
		System.out.println("Get transaction: " + client.getRawTransaction("17a2d3aeea1d742c9e42629bbf9ca04c0a19061497142f1f8b390ea43b1d5845"));
		System.out.println("Is mined: " + client.isMined("82a560381ac769d778ad42d72c0355123c0df55282fe12630638740a18cc7b66"));
		System.out.println("Is mined (reliability low): " + client.isMined("82a560381ac769d778ad42d72c0355123c0df55282fe12630638740a18cc7b66", Reliability.LOW));
	}
	
}
