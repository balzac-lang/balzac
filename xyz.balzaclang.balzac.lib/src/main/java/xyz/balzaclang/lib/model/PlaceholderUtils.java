/*
 * Copyright 2019 Nicola Atzei
 */

package xyz.balzaclang.lib.model;

import xyz.balzaclang.lib.model.script.InputScript;
import xyz.balzaclang.lib.model.script.OutputScript;

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
        return params.isTestnet()?
                placeholderTransaction(NetworkType.TESTNET):
                placeholderTransaction(NetworkType.MAINNET);
    }

    private static ITransactionBuilder placeholderTransaction(NetworkType params) {
        CoinbaseTransactionBuilder tx = new CoinbaseTransactionBuilder(params);
        tx.addInput(InputScript.create().number(42));
        for (int i=0; i<10; i++) {
            tx.addOutput(OutputScript.createP2PKH(ADDRESS(params)), 50_000_000_00L);
        }
        return tx;
    }
}
