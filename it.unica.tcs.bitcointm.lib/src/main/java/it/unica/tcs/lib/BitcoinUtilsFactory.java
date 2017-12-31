package it.unica.tcs.lib;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.name.Names;
import com.sulacosoft.bitcoindconnector4j.BitcoindApi;

import it.unica.tcs.lib.client.BitcoinClientI;
import it.unica.tcs.lib.client.Confidentiality;
import it.unica.tcs.lib.client.TransactionNotFoundException;
import it.unica.tcs.lib.client.impl.RPCBitcoinClient;

public class BitcoinUtilsFactory {

    private final Injector injector;
    private final List<Module> modules;

    private BitcoinUtilsFactory(Module... modules) {
        
        Module defaultModule = new Module(){
            @Override
            public void configure(Binder builder) {
                builder.bind(BitcoinClientI.class).to(RPCBitcoinClient.class);
                builder.bind(String.class).annotatedWith(Names.named("bitcoind.address")).toInstance("co2.unica.it");
                builder.bind(Integer.class).annotatedWith(Names.named("bitcoind.port")).toInstance(18332);
                builder.bind(String.class).annotatedWith(Names.named("bitcoind.protocol")).toInstance("http");
                builder.bind(String.class).annotatedWith(Names.named("bitcoind.user")).toInstance("bitcoin");
                builder.bind(String.class).annotatedWith(Names.named("bitcoind.password")).toInstance("L4mbWnzC35BNrmTJ");
                builder.bind(Integer.class).annotatedWith(Names.named("bitcoind.timeout")).toInstance(3);
                builder.bind(TimeUnit.class).annotatedWith(Names.named("bitcoind.timeunit")).toInstance(TimeUnit.SECONDS);
            }
        };
        this.injector = Guice.createInjector(defaultModule).createChildInjector(modules);
        this.modules = new ArrayList<>();
        this.modules.add(defaultModule);
        this.modules.addAll(Arrays.asList(modules));
    }

    public static BitcoinUtilsFactory create(Module... modules) {
        return new BitcoinUtilsFactory(modules);
    }
  
    public List<Module> getModules() {
        return modules;
    }
    
    public BitcoinClientI getBitcoinClient() {
        return injector.getInstance(BitcoinClientI.class);
    }
    
    public static void main(String[] args) throws TransactionNotFoundException {
        
        BitcoinUtilsFactory lib = BitcoinUtilsFactory.create();
        
        RPCBitcoinClient client = lib.injector.getInstance(RPCBitcoinClient.class);
        
        BitcoindApi api = ((RPCBitcoinClient)client).getApi();
        
        System.out.println("Best block count: " + client.getBlockCount());
        System.out.println("Get raw transaction: " + client.getRawTransaction("17a2d3aeea1d742c9e42629bbf9ca04c0a19061497142f1f8b390ea43b1d5845"));
        System.out.println("Get raw transaction: " + api.getrawtransaction("17a2d3aeea1d742c9e42629bbf9ca04c0a19061497142f1f8b390ea43b1d5845"));
        System.out.println("Get raw transaction: " + api.getrawtransaction("ee40379cdf5439983d7603a88cafdd6de1c20d3b164850ab1055ed276ed95468"));
        System.out.println("Get raw transaction: " + api.getrawtransaction("17a2d3aeea1d742c9e42629bbf9ca04c0a19061497142f1f8b390ea43b1d5845", true));
        System.out.println("Get raw transaction: " + api.getrawtransaction("17a2d3aeea1d742c9e42629bbf9ca04c0a19061497142f1f8b390ea43b1d5845", false));
        System.out.println("Get UTXO: " + api.gettxout("17a2d3aeea1d742c9e42629bbf9ca04c0a19061497142f1f8b390ea43b1d5845", 0));
        System.out.println("Get UTXO: " + api.gettxout("17a2d3aeea1d742c9e42629bbf9ca04c0a19061497142f1f8b390ea43b1d5845", 2));
        System.out.println("Get isUTXO: " + client.isUTXO("17a2d3aeea1d742c9e42629bbf9ca04c0a19061497142f1f8b390ea43b1d5845"));
        System.out.println("Get isUTXO: " + client.isUTXO("17a2d3aeea1d742c9e42629bbf9ca04c0a19061497142f1f8b390ea43b1d5845", 1));
        System.out.println("Is mined: " + client.isMined("82a560381ac769d778ad42d72c0355123c0df55282fe12630638740a18cc7b66"));
        System.out.println("Is mined (reliability low): " + client.isMined("82a560381ac769d778ad42d72c0355123c0df55282fe12630638740a18cc7b66", Confidentiality.LOW));
    }
    
}
