/*
 * Copyright 2017 Nicola Atzei
 */
package it.unica.tcs.lib;

import java.util.concurrent.TimeUnit;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.name.Names;
import com.google.inject.util.Modules;

import it.unica.tcs.lib.client.BitcoinClientI;
import it.unica.tcs.lib.client.impl.RPCBitcoinClient;

public class BitcoinUtilsFactory {

    private final Module module;

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
        this.module = Modules.combine(defaultModule, Modules.combine(modules));
    }

    public static BitcoinUtilsFactory create(Module... modules) {
        return new BitcoinUtilsFactory(modules);
    }

    public Module getModule() {
        return module;
    }
}
