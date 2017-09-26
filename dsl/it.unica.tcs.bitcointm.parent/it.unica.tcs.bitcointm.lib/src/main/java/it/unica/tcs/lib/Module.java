package it.unica.tcs.lib;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.sulacosoft.bitcoindconnector4j.BitcoindApi;
import com.sulacosoft.bitcoindconnector4j.BitcoindApiFactory;

import it.unica.tcs.lib.client.BitcoinClientI;
import it.unica.tcs.lib.client.Reliability;
import it.unica.tcs.lib.client.TransactionNotFoundException;
import it.unica.tcs.lib.client.impl.RPCBitcoinClient;

public class Module extends AbstractModule {

	@Override
	protected void configure() {
		bind(BitcoinClientI.class).to(RPCBitcoinClient.class);
	}
	
	@Provides
	BitcoindApi provideBitcoindApi() {
		String address = "co2.unica.it";
		int port = 18332;
		String protocol = "http";
		String user = "bitcoin";
		String password = "L4mbWnzC35BNrmTJ";
		return BitcoindApiFactory.createConnection(address, port, protocol, user, password);
	}
	
	public static void main(String[] args) throws TransactionNotFoundException {
		Injector injector = Guice.createInjector(new Module());
		
		BitcoinClientI client = injector.getInstance(BitcoinClientI.class);

		System.out.println("Best block count: " + client.getBlockCount());
		System.out.println("Get transaction: " + client.getRawTransaction("17a2d3aeea1d742c9e42629bbf9ca04c0a19061497142f1f8b390ea43b1d5845"));
		System.out.println("Is mined: " + client.isMined("82a560381ac769d778ad42d72c0355123c0df55282fe12630638740a18cc7b66"));
		System.out.println("Is mined (reliability low): " + client.isMined("82a560381ac769d778ad42d72c0355123c0df55282fe12630638740a18cc7b66", Reliability.LOW));
	}
	
}
