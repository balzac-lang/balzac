package it.unica.tcs.lib.model;

import org.bitcoinj.core.NetworkParameters;

public enum NetworkType {
    MAINNET,
    TESTNET;

    public boolean isTestnet() {
        return this == TESTNET;
    }

    public boolean isMainnet() {
        return this == MAINNET;
    }

    public NetworkParameters toNetworkParameters() {
        return this == TESTNET? NetworkParameters.fromID(NetworkParameters.ID_TESTNET): NetworkParameters.fromID(NetworkParameters.ID_MAINNET);
    }

    static NetworkType from(NetworkParameters parameters) {
        return parameters.getId().equals(NetworkParameters.ID_TESTNET)? TESTNET: MAINNET;
    }
}
