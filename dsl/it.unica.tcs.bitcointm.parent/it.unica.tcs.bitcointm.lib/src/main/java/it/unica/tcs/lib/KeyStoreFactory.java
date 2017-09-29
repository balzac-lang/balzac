package it.unica.tcs.lib;

public class KeyStoreFactory {

	private final static KeyStore keystore = new KeyStore();
	
	public static KeyStore getInstance() {
		return keystore;
	}

}
