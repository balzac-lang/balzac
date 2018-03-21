/*
 * Copyright 2018 Nicola Atzei
 */

package it.unica.tcs.utils;

import java.util.concurrent.TimeUnit;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import it.unica.tcs.lib.client.BitcoinClientI;
import it.unica.tcs.lib.client.impl.RPCBitcoinClient;

@Singleton
public class BitcoinClientFactory {

    @Inject(optional=true) @Named("tnTestnetHost") private String testnetHost;
    @Inject(optional=true) @Named("tnTestnetPort") private Integer testnetPort;
    @Inject(optional=true) @Named("tnTestnetPath") private String testnetPath;
    @Inject(optional=true) @Named("tnTestnetUsername") private String testnetUsername;
    @Inject(optional=true) @Named("tnTestnetPassword") private String testnetPassword;
    @Inject(optional=true) @Named("tnTestnetTimeout") private Integer testnetTimeout;

    @Inject(optional=true) @Named("tnMainnetHost") private String mainnetHost;
    @Inject(optional=true) @Named("tnMainnetPort") private Integer mainnetPort;
    @Inject(optional=true) @Named("tnMainnetPath") private String mainnetPath;
    @Inject(optional=true) @Named("tnMainnetUsername") private String mainnetUsername;
    @Inject(optional=true) @Named("tnMainnetPassword") private String mainnetPassword;
    @Inject(optional=true) @Named("tnMainnetTimeout") private Integer mainnetTimeout;

    private BitcoinClientI mainnetClient;
    private BitcoinClientI testnetClient;

    public BitcoinClientI getBitcoinClient(NetworkParameters params) {
        if (TestNet3Params.get().equals(params)) {
            if (testnetHost != null && testnetPort != null && testnetPath != null && testnetUsername != null && testnetPassword != null && testnetTimeout != null) {
                System.out.println("Returning trusted node defined by properties [testnet]");
                return new RPCBitcoinClient(testnetHost, testnetPort, "http", testnetPath, testnetUsername, testnetPassword, testnetTimeout, TimeUnit.MILLISECONDS);
            }
            else {
                Preconditions.checkNotNull(testnetClient);
                return testnetClient;                
            }
        }
        else if (MainNetParams.get().equals(params)) {
            if (mainnetHost != null && mainnetPort != null && mainnetPath != null && mainnetUsername != null && mainnetPassword != null && mainnetTimeout != null) {
                System.out.println("Returning trusted node defined by properties [mainnet]");
                return new RPCBitcoinClient(mainnetHost, mainnetPort, "http", mainnetPath, mainnetUsername, mainnetPassword, mainnetTimeout, TimeUnit.MILLISECONDS);
            }
            else {
                Preconditions.checkNotNull(mainnetClient);
                return mainnetClient;
            }
        }
        throw new IllegalArgumentException("Invalid parameter "+params);
    }

    public BitcoinClientI getMainnetClient() {
        return mainnetClient;
    }

    public void setMainnetClient(BitcoinClientI mainnetClient) {
        this.mainnetClient = mainnetClient;
    }

    public BitcoinClientI getTestnetClient() {
        return testnetClient;
    }

    public void setTestnetClient(BitcoinClientI testnetClient) {
        this.testnetClient = testnetClient;
    }
}
