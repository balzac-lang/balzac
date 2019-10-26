/*
 * Copyright 2019 Nicola Atzei
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package xyz.balzaclang.lib;

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
