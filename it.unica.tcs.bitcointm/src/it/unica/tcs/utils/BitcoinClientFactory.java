/*
 * Copyright 2018 Nicola Atzei
 */

package it.unica.tcs.utils;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import it.unica.tcs.lib.client.BitcoinClientI;

public class BitcoinClientFactory {

    @Inject
    private BitcoinClientI mainnetClient;

    @Inject
    @Named("testnet")
    private BitcoinClientI testnetClient;

    public BitcoinClientI getBitcoinClient(NetworkParameters params) {
        if (TestNet3Params.get().equals(params)) {
            Preconditions.checkNotNull(testnetClient);
            return testnetClient;
        }
        else if (MainNetParams.get().equals(params)) {
            Preconditions.checkNotNull(mainnetClient);
            return mainnetClient;
        }
        throw new IllegalArgumentException("Invalid parameter "+params);
    }
}
