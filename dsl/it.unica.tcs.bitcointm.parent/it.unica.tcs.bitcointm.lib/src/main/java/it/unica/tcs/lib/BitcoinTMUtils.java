package it.unica.tcs.lib;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.sulacosoft.bitcoindconnector4j.BitcoindApi;
import com.sulacosoft.bitcoindconnector4j.BitcoindApiFactory;

import it.unica.tcs.lib.client.BitcoinClientI;
import it.unica.tcs.lib.client.Reliability;
import it.unica.tcs.lib.client.TransactionNotFoundException;
import it.unica.tcs.lib.client.impl.RPCBitcoinClient;
import it.unica.tcs.lib.utils.BitcoinJUtils;

public class BitcoinTMUtils {

	private final Injector injector;

    private BitcoinTMUtils(Module... modules) {
    	
    	Module defaultModule = new Module(){
			@Override
			public void configure(Binder builder) {
				builder.bind(BitcoinClientI.class).to(RPCBitcoinClient.class);
			}
			
			@Provides
			private BitcoindApi provideBitcoindApi() {
				String address = "co2.unica.it";
				int port = 18332;
				String protocol = "http";
				String user = "bitcoin";
				String password = "L4mbWnzC35BNrmTJ";
				return BitcoindApiFactory.createConnection(address, port, protocol, user, password);
			}
		};
		this.injector = Guice.createInjector(defaultModule).createChildInjector(modules);
    }

    public static BitcoinTMUtils create(Module... modules) {
        return new BitcoinTMUtils(modules);
    }

    public BitcoinClientI bitcoinClient() {
        return injector.getInstance(BitcoinClientI.class);
    }
    
    public BitcoinJUtils bitcoinLib() {
        return injector.getInstance(BitcoinJUtils.class);
    }
	
	
	public static void main(String[] args) throws TransactionNotFoundException {
		
		BitcoinTMUtils lib = BitcoinTMUtils.create();
		
		BitcoinClientI client = lib.bitcoinClient();

		System.out.println("Best block count: " + client.getBlockCount());
		System.out.println("Get transaction: " + client.getRawTransaction("17a2d3aeea1d742c9e42629bbf9ca04c0a19061497142f1f8b390ea43b1d5845"));
		System.out.println("Is mined: " + client.isMined("82a560381ac769d778ad42d72c0355123c0df55282fe12630638740a18cc7b66"));
		System.out.println("Is mined (reliability low): " + client.isMined("82a560381ac769d778ad42d72c0355123c0df55282fe12630638740a18cc7b66", Reliability.LOW));
	}
	
}
