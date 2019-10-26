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
package xyz.balzaclang.lib.model;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.bitcoinj.script.ScriptPattern;
import org.junit.Test;

public class PlaceholderUtilsTest {

    @Test
    public void placeholderIntIsZero() {
        assertEquals(0, PlaceholderUtils.INT);
    }

    @Test
    public void placeholderStringIsEmpty() {
        assertEquals("", PlaceholderUtils.STRING);
    }

    @Test
    public void placeholderBooleanIsFalse() {
        assertEquals(false, PlaceholderUtils.BOOLEAN);
    }

    @Test
    public void placeholderHashIsZeroBites() {
        assertArrayEquals(new byte[0], PlaceholderUtils.HASH.getBytes());
    }

    @Test
    public void placeholderSignatureIsZeroBites() {
        assertArrayEquals(new byte[0], PlaceholderUtils.SIGNATURE.getSignature());
    }

    @Test
    public void publicKeyIsDerivedFromPrivateOneMainnet() {
        _publicKeyIsDerivedFromPrivateOne(NetworkType.MAINNET);
    }

    @Test
    public void publicKeyIsDerivedFromPrivateOneTestnet() {
        _publicKeyIsDerivedFromPrivateOne(NetworkType.TESTNET);
    }

    private void _publicKeyIsDerivedFromPrivateOne(NetworkType params) {
        assertEquals(
                PlaceholderUtils.KEY(params).toPublicKey(),
                PlaceholderUtils.PUBKEY(params)
                );
    }

    @Test
    public void addressIsDerivedFromPrivateKeyMainnet() {
        _addressIsDerivedFromPrivateKey(NetworkType.MAINNET);
    }

    @Test
    public void addressIsDerivedFromPrivateKeyTestnet() {
        _addressIsDerivedFromPrivateKey(NetworkType.TESTNET);
    }

    private void _addressIsDerivedFromPrivateKey(NetworkType params) {
        assertEquals(
                PlaceholderUtils.KEY(params).toAddress(),
                PlaceholderUtils.ADDRESS(params)
                );
    }

    @Test
    public void placeholderTransactionMainnet() {
        _placeholderTransaction(NetworkType.MAINNET);
    }

    @Test
    public void placeholderTransactionTestnet() {
        _placeholderTransaction(NetworkType.TESTNET);
    }

    private void _placeholderTransaction(NetworkType params) {
        ITransactionBuilder tx = PlaceholderUtils.TX(params);
        // is coinbase
        assertTrue(tx.isCoinbase());

        // have 10 outputs
        assertEquals(10, tx.getOutputs().size());

        // each one
        for (Output output : tx.getOutputs()) {
            // is P2PKH
            assertTrue(output.getScript().isP2PKH());
            // worth 50 BTC
            assertEquals(50_0000_0000L, output.getValue());
            // address is from placeholder
            assertArrayEquals(
                    PlaceholderUtils.ADDRESS(params).getBytes(),
                    ScriptPattern.extractHashFromP2PKH(output.getScript().getOutputScript()));
        }
    }
}
