/*
 * Copyright 2019 Nicola Atzei
 */

package it.unica.tcs.utils;

import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import it.unica.tcs.lib.client.BitcoinClient;
import it.unica.tcs.lib.client.impl.RPCBitcoinClient;
import it.unica.tcs.lib.model.NetworkType;

@Singleton
public class BitcoinClientFactory {

    private static final Logger logger = Logger.getLogger(BitcoinClientFactory.class);

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

    private BitcoinClient mainnetClient;
    private BitcoinClient testnetClient;

    public BitcoinClient getBitcoinClient(NetworkType params) {
        if (params.isTestnet()) {
            if (testnetHost != null && testnetPort != null && testnetPath != null && testnetUsername != null && testnetPassword != null && testnetTimeout != null) {
                logger.info("Returning trusted node defined by properties [testnet]");
                return new RPCBitcoinClient(testnetHost, testnetPort, "http", testnetPath, testnetUsername, testnetPassword, testnetTimeout, TimeUnit.MILLISECONDS);
            }
            else {
                Preconditions.checkNotNull(testnetClient);
                return testnetClient;
            }
        }
        else if (params.isMainnet()) {
            if (mainnetHost != null && mainnetPort != null && mainnetPath != null && mainnetUsername != null && mainnetPassword != null && mainnetTimeout != null) {
                logger.info("Returning trusted node defined by properties [mainnet]");
                return new RPCBitcoinClient(mainnetHost, mainnetPort, "http", mainnetPath, mainnetUsername, mainnetPassword, mainnetTimeout, TimeUnit.MILLISECONDS);
            }
            else {
                Preconditions.checkNotNull(mainnetClient);
                return mainnetClient;
            }
        }
        throw new IllegalArgumentException("Invalid parameter "+params);
    }

    public BitcoinClient getMainnetClient() {
        return mainnetClient;
    }

    public void setMainnetClient(BitcoinClient mainnetClient) {
        this.mainnetClient = mainnetClient;
    }

    public BitcoinClient getTestnetClient() {
        return testnetClient;
    }

    public void setTestnetClient(BitcoinClient testnetClient) {
        this.testnetClient = testnetClient;
    }
}
