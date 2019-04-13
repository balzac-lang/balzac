/*
 * Copyright 2019 Nicola Atzei
 */

package it.unica.tcs.lib.model;

public class PlaceholderUtils {
    public static final long INT = 0L;
    public static final String STRING = "";
    public static final boolean BOOLEAN = false;
    public static final Hash HASH = new Hash(new byte[0]);
    public static final Signature SIGNATURE = new Signature(new byte[0], PublicKey.fresh());

    private static final PrivateKey privateKeyTest = PrivateKey.fresh(NetworkType.TESTNET);
    private static final PrivateKey privateKeyMain = PrivateKey.fresh(NetworkType.MAINNET);
    private static final PublicKey publicKeyTest = PublicKey.from(privateKeyTest);
    private static final PublicKey publicKeyMain = PublicKey.from(privateKeyMain);
    private static final Address addressTest = Address.from(privateKeyTest);
    private static final Address addressMain = Address.from(privateKeyMain);
    private static final ITransactionBuilder txTest = ITransactionBuilder.fromSerializedTransaction(NetworkType.TESTNET, "02000000010000000000000000000000000000000000000000000000000000000000000000ffffffff02012affffffff0a00e1f505000000001976a91402d997548d397534c4d8175c047cb38909beb9b888ac00e1f505000000001976a91402d997548d397534c4d8175c047cb38909beb9b888ac00e1f505000000001976a91402d997548d397534c4d8175c047cb38909beb9b888ac00e1f505000000001976a91402d997548d397534c4d8175c047cb38909beb9b888ac00e1f505000000001976a91402d997548d397534c4d8175c047cb38909beb9b888ac00e1f505000000001976a91402d997548d397534c4d8175c047cb38909beb9b888ac00e1f505000000001976a91402d997548d397534c4d8175c047cb38909beb9b888ac00e1f505000000001976a91402d997548d397534c4d8175c047cb38909beb9b888ac00e1f505000000001976a91402d997548d397534c4d8175c047cb38909beb9b888ac00e1f505000000001976a91402d997548d397534c4d8175c047cb38909beb9b888ac00000000");
    private static final ITransactionBuilder txMain = ITransactionBuilder.fromSerializedTransaction(NetworkType.MAINNET, "02000000010000000000000000000000000000000000000000000000000000000000000000ffffffff02012affffffff0a00e1f505000000001976a91402d997548d397534c4d8175c047cb38909beb9b888ac00e1f505000000001976a91402d997548d397534c4d8175c047cb38909beb9b888ac00e1f505000000001976a91402d997548d397534c4d8175c047cb38909beb9b888ac00e1f505000000001976a91402d997548d397534c4d8175c047cb38909beb9b888ac00e1f505000000001976a91402d997548d397534c4d8175c047cb38909beb9b888ac00e1f505000000001976a91402d997548d397534c4d8175c047cb38909beb9b888ac00e1f505000000001976a91402d997548d397534c4d8175c047cb38909beb9b888ac00e1f505000000001976a91402d997548d397534c4d8175c047cb38909beb9b888ac00e1f505000000001976a91402d997548d397534c4d8175c047cb38909beb9b888ac00e1f505000000001976a91402d997548d397534c4d8175c047cb38909beb9b888ac00000000");

    public static PrivateKey KEY(NetworkType params) {
        return params.isTestnet()? privateKeyTest : privateKeyMain;
    }

    public static PublicKey PUBKEY(NetworkType params) {
        return params.isTestnet()? publicKeyTest : publicKeyMain;
    }

    public static Address ADDRESS(NetworkType params) {
        return params.isTestnet()? addressTest : addressMain;
    }

    public static ITransactionBuilder TX(NetworkType params) {
        return params.isTestnet()? txTest : txMain;
	}
}
