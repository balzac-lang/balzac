package it.unica.tcs.utils;

public class SecureStorageUtils {

    /* Nodes */
    public static String SECURE_STORAGE__NODE__ROOT = "it.unica.tcs.bitcointm";
    public static String SECURE_STORAGE__NODE__KEYSTORE = SECURE_STORAGE__NODE__ROOT+".keystore";
    public static String SECURE_STORAGE__NODE__BITCOIN_NODES = SECURE_STORAGE__NODE__ROOT+".bitcoin";
    public static String SECURE_STORAGE__NODE__BITCOIN__TESTNET_NODE = SECURE_STORAGE__NODE__BITCOIN_NODES+".testnet";
    public static String SECURE_STORAGE__NODE__BITCOIN__MAINNET_NODE = SECURE_STORAGE__NODE__BITCOIN_NODES+".mainnet";

    /* Properties */
    public static String SECURE_STORAGE__PROPERTY__KEYSTORE_PASSWORD = "ksPassword";
    public static String SECURE_STORAGE__PROPERTY__TESTNET_PASSWORD = "testnetPassword";
    public static String SECURE_STORAGE__PROPERTY__MAINNET_PASSWORD = "mainnetPassword";
}
