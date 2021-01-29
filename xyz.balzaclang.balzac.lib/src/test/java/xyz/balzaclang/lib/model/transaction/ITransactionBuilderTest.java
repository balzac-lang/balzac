/*
 * Copyright 2020 Nicola Atzei
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
package xyz.balzaclang.lib.model.transaction;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.security.KeyStoreException;

import org.junit.Test;

import xyz.balzaclang.lib.PrivateKeysStore;
import xyz.balzaclang.lib.model.NetworkType;
import xyz.balzaclang.lib.utils.BitcoinUtils;

public class ITransactionBuilderTest {

    @Test
    public void test_twoSerialedTransactionsAreEquals() throws KeyStoreException {
        SerialTransactionBuilder serial1 = new SerialTransactionBuilder(NetworkType.MAINNET, BitcoinUtils.decode(
            "020000000167cb3934c8e2d6e5a976eb927cbb792b060ae9ed5bb53c7347e8ac05314455f0000000006b483045022100b8baf18e4711c90b5484634670aca585535a8777bac3d4e8c4b7ea3f460ff0130220242fec7028332c65c347f64f2b508275e7453fcabc3f770c4fcdb8facb11805f0121033806fe039c8d149c8e68f4665b4a479acab773b20bddf7df0df59ba4f0567341ffffffff0100e1f505000000001976a914c0cc4b28d2402bb28429f8d9b8e22a815678b03388ac00000000"));
        SerialTransactionBuilder serial2 = new SerialTransactionBuilder(NetworkType.MAINNET, BitcoinUtils.decode(
            "020000000167cb3934c8e2d6e5a976eb927cbb792b060ae9ed5bb53c7347e8ac05314455f0000000006b483045022100b8baf18e4711c90b5484634670aca585535a8777bac3d4e8c4b7ea3f460ff0130220242fec7028332c65c347f64f2b508275e7453fcabc3f770c4fcdb8facb11805f0121033806fe039c8d149c8e68f4665b4a479acab773b20bddf7df0df59ba4f0567341ffffffff0100e1f505000000001976a914c0cc4b28d2402bb28429f8d9b8e22a815678b03388ac00000000"));
        assertTrue(serial1.equals(serial2));
        assertTrue(ITransactionBuilder.equals(serial1, serial2, new PrivateKeysStore()));
    }

    @Test
    public void test_twoSerialedTransactionsWithDifferentNetworkAreNotEqual() throws KeyStoreException {
        SerialTransactionBuilder serial1 = new SerialTransactionBuilder(NetworkType.MAINNET, BitcoinUtils.decode(
            "020000000167cb3934c8e2d6e5a976eb927cbb792b060ae9ed5bb53c7347e8ac05314455f0000000006b483045022100b8baf18e4711c90b5484634670aca585535a8777bac3d4e8c4b7ea3f460ff0130220242fec7028332c65c347f64f2b508275e7453fcabc3f770c4fcdb8facb11805f0121033806fe039c8d149c8e68f4665b4a479acab773b20bddf7df0df59ba4f0567341ffffffff0100e1f505000000001976a914c0cc4b28d2402bb28429f8d9b8e22a815678b03388ac00000000"));
        SerialTransactionBuilder serial2 = new SerialTransactionBuilder(NetworkType.TESTNET, BitcoinUtils.decode(
            "020000000167cb3934c8e2d6e5a976eb927cbb792b060ae9ed5bb53c7347e8ac05314455f0000000006b483045022100b8baf18e4711c90b5484634670aca585535a8777bac3d4e8c4b7ea3f460ff0130220242fec7028332c65c347f64f2b508275e7453fcabc3f770c4fcdb8facb11805f0121033806fe039c8d149c8e68f4665b4a479acab773b20bddf7df0df59ba4f0567341ffffffff0100e1f505000000001976a914c0cc4b28d2402bb28429f8d9b8e22a815678b03388ac00000000"));
        assertFalse(serial1.equals(serial2));
        assertFalse(ITransactionBuilder.equals(serial1, serial2, new PrivateKeysStore()));
    }

}
