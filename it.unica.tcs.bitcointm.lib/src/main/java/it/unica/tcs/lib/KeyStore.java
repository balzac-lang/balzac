/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.lib;

import static com.google.common.base.Preconditions.checkState;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.Utils;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;

import it.unica.tcs.lib.utils.BitcoinUtils;

public class KeyStore {

    private final Map<String, ECKey> store = new ConcurrentHashMap<>();
    
    KeyStore() {}
    
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
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Entry<String,ECKey> e : store.entrySet()) {
            ECKey key = e.getValue();
            sb.append(e.getKey()).append(" -> (private) ").append(key.getPrivateKeyAsWiF(TestNet3Params.get()))
            .append(" or ").append(key.getPrivateKeyAsWiF(MainNetParams.get())).append("\n");
            sb.append(e.getKey()).append(" -> (public) ").append(key.getPublicKeyAsHex()).append(", (hash) ").append(BitcoinUtils.encode(key.getPubKeyHash())).append("\n");
        }
        
        return sb.toString(); 
    }
}
