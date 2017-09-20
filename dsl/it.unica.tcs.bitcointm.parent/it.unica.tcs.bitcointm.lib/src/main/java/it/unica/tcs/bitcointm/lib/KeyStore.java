package it.unica.tcs.bitcointm.lib;

import static com.google.common.base.Preconditions.checkState;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.Utils;

public class KeyStore {

	private static final KeyStore instance = new KeyStore();
	private KeyStore() {}

	private final Map<String, ECKey> store = new ConcurrentHashMap<>();
	
	public static KeyStore getInstance() {
		return instance;
	}
	
	public String addKey(ECKey key) {
		checkState(!key.isPubKeyOnly(), "Only private key are allowed.");
		String uniqueID = getUniqueID(key);
		store.put(uniqueID, key);
		return uniqueID;
	}
	
	public ECKey getKey(String keyID) {
		checkState(store.containsKey(keyID));
		return store.get(keyID);
	}
	
	public String getUniqueID(ECKey key) {
		// TODO: caching?
		return Utils.HEX.encode(Utils.sha256hash160(key.getPrivKeyBytes()));
	}
	
	public void clear() {
		store.clear();
	}
}
