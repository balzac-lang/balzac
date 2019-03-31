/*
 * Copyright 2019 Nicola Atzei
 */

package it.unica.tcs.lib.model;

import org.bitcoinj.core.NetworkParameters;

import it.unica.tcs.lib.ITransactionBuilder;

public class PlaceholderUtils {
    public static final long INT = 0L;
    public static final String STRING = "";
    public static final boolean BOOLEAN = false;
    public static final Hash HASH = new Hash(new byte[0]);
    public static final Signature SIGNATURE = new Signature(new byte[0], new byte[0]);

    private static final NetworkParameters TESTNET = NetworkParameters.fromID(NetworkParameters.ID_TESTNET);
    private static final NetworkParameters MAINNET = NetworkParameters.fromID(NetworkParameters.ID_MAINNET);
    private static final PrivateKey privateKeyTest = PrivateKey.fresh(TESTNET);
    private static final PrivateKey privateKeyMain = PrivateKey.fresh(MAINNET);
    private static final PublicKey publicKeyTest = PublicKey.from(privateKeyTest);
    private static final PublicKey publicKeyMain = PublicKey.from(privateKeyMain);
    private static final Address addressTest = Address.from(privateKeyTest);
    private static final Address addressMain = Address.from(privateKeyMain);
    private static final ITransactionBuilder txTest = ITransactionBuilder.fromSerializedTransaction(TESTNET, "02000000010000000000000000000000000000000000000000000000000000000000000000ffffffff02012affffffff0a00e1f505000000001976a91402d997548d397534c4d8175c047cb38909beb9b888ac00e1f505000000001976a91402d997548d397534c4d8175c047cb38909beb9b888ac00e1f505000000001976a91402d997548d397534c4d8175c047cb38909beb9b888ac00e1f505000000001976a91402d997548d397534c4d8175c047cb38909beb9b888ac00e1f505000000001976a91402d997548d397534c4d8175c047cb38909beb9b888ac00e1f505000000001976a91402d997548d397534c4d8175c047cb38909beb9b888ac00e1f505000000001976a91402d997548d397534c4d8175c047cb38909beb9b888ac00e1f505000000001976a91402d997548d397534c4d8175c047cb38909beb9b888ac00e1f505000000001976a91402d997548d397534c4d8175c047cb38909beb9b888ac00e1f505000000001976a91402d997548d397534c4d8175c047cb38909beb9b888ac00000000");
    private static final ITransactionBuilder txMain = ITransactionBuilder.fromSerializedTransaction(MAINNET, "02000000010000000000000000000000000000000000000000000000000000000000000000ffffffff02012affffffff0a00e1f505000000001976a91402d997548d397534c4d8175c047cb38909beb9b888ac00e1f505000000001976a91402d997548d397534c4d8175c047cb38909beb9b888ac00e1f505000000001976a91402d997548d397534c4d8175c047cb38909beb9b888ac00e1f505000000001976a91402d997548d397534c4d8175c047cb38909beb9b888ac00e1f505000000001976a91402d997548d397534c4d8175c047cb38909beb9b888ac00e1f505000000001976a91402d997548d397534c4d8175c047cb38909beb9b888ac00e1f505000000001976a91402d997548d397534c4d8175c047cb38909beb9b888ac00e1f505000000001976a91402d997548d397534c4d8175c047cb38909beb9b888ac00e1f505000000001976a91402d997548d397534c4d8175c047cb38909beb9b888ac00e1f505000000001976a91402d997548d397534c4d8175c047cb38909beb9b888ac00000000");
    
    public static PrivateKey KEY(NetworkParameters params) {
    	if (TESTNET.equals(params))
    		return privateKeyTest;
        return privateKeyMain;
    }

    public static PublicKey PUBKEY(NetworkParameters params) {
    	if (TESTNET.equals(params))
    		return publicKeyTest;
        return publicKeyMain;
    }

    public static Address ADDRESS(NetworkParameters params) {
    	if (TESTNET.equals(params))
    		return addressTest;
        return addressMain;
    }

    public static ITransactionBuilder TX(NetworkParameters params) {
    	if (TESTNET.equals(params))
    		return txTest;
        return txMain;
	}
}
