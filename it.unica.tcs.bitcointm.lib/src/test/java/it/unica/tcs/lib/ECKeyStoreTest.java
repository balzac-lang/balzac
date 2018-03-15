/*
 * Copyright 2018 Nicola Atzei
 */
package it.unica.tcs.lib;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.security.KeyStoreException;

import org.bitcoinj.core.ECKey;
import org.bitcoinj.params.TestNet3Params;
import org.junit.Test;

public class ECKeyStoreTest {

    @Test
    public void changePassword() throws KeyStoreException {
        ECKeyStore ks = new ECKeyStore();
        ECKey k1 = new ECKey();
        String wif = k1.getPrivateKeyEncoded(TestNet3Params.get()).toBase58();
        // add a key
        String alias1 = ks.addKey(k1);
        // change password
        ks.changePassword("test".toCharArray());
        // retrieve the key
        ECKey k2 = ks.getKey(ECKeyStore.getUniqueID(wif));

        assertTrue(ks.containsKey(alias1));
        assertEquals(k1.getPrivKey(), k2.getPrivKey());
    }
}
