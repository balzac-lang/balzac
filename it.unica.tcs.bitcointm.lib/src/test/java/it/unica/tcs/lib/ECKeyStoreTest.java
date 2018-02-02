package it.unica.tcs.lib;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.security.KeyStoreException;

import org.bitcoinj.core.ECKey;
import org.bitcoinj.params.TestNet3Params;
import org.junit.Test;

public class ECKeyStoreTest {

    @Test
    public void changePassword() throws KeyStoreException {
        ECKeyStore ks = ECKeyStore.create();
        ECKey k1 = new ECKey();
        String wif = k1.getPrivateKeyEncoded(TestNet3Params.get()).toBase58();
        // add a key
        String alias1 = ks.addKey(k1);
        // change password
        ks.changePassword("test".toCharArray());
        // retrieve the key
        ECKey k2 = ks.getKey(ECKeyStore.getUniqueID(wif));

        assertTrue(ks.contains(alias1));
        assertEquals(k1.getPrivKey(), k2.getPrivKey());
        // cleanup
        ks.getKeyStoreFile().delete();
    }

    @Test
    public void changeKeyStoreFile() throws KeyStoreException, IOException {
        ECKeyStore ks = ECKeyStore.create();
        File old = ks.getKeyStoreFile();
        File tmp = File.createTempFile("keystore-test_new", ".p12");
        ks.changeKeyStoreFile(tmp, true);
        assertTrue(tmp.exists());
        assertFalse(old.exists());
        tmp.delete();
    }
}
