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

package xyz.balzaclang.lib.model.script;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.Transaction.SigHash;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.script.Script;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import xyz.balzaclang.lib.ECKeyStore;
import xyz.balzaclang.lib.model.NetworkType;
import xyz.balzaclang.lib.model.PrivateKey;
import xyz.balzaclang.lib.model.script.AbstractScriptBuilderWithVar.ScriptBuilderWithVar;

public class ScriptBuilderWithVarTest {

    ECKeyStore ecks;

    @Before
    public void before() throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        ecks = new ECKeyStore();
        ecks.changePassword(new char[]{'t','e','s','t'});
    }

    @After
    public void after() {
    }

    @Test
    public void test_size() {
        ScriptBuilderWithVar sb = new ScriptBuilderWithVar();
        assertEquals(0, sb.size());
        assertEquals(0, sb.getFreeVariables().size());
        assertEquals(0, sb.signatureSize());
        sb.number(5);
        assertEquals(1, sb.size());
        assertEquals(0, sb.getFreeVariables().size());
        assertEquals(0, sb.signatureSize());
    }

    @Test
    public void test_freeVariable() {
        ScriptBuilderWithVar sb = new ScriptBuilderWithVar();
        assertEquals(0, sb.size());
        assertEquals(0, sb.getVariables().size());
        assertEquals(0, sb.getFreeVariables().size());
        assertEquals(0, sb.signatureSize());

        sb.addVariable("foo", Long.class);
        assertTrue(sb.hasVariable("foo"));
        assertEquals(1, sb.size());
        assertEquals(1, sb.getVariables().size());
        assertEquals(1, sb.getFreeVariables().size());
        assertEquals(0, sb.signatureSize());

        sb = sb.bindVariable("foo", 5L);
        assertEquals(1, sb.size());
        assertEquals(1, sb.getVariables().size());
        assertEquals(1, sb.getBoundVariables().size());
        assertEquals(0, sb.getFreeVariables().size());
        assertEquals(0, sb.signatureSize());

        Script s = sb.build();
        assertEquals("5", s.toString());

        // sb state is unchanged
        assertEquals(1, sb.size());
        assertEquals(1, sb.getVariables().size());
        assertEquals(1, sb.getBoundVariables().size());
        assertEquals(0, sb.getFreeVariables().size());
        assertEquals(0, sb.signatureSize());


        sb = sb.removeVariable("foo");
        assertEquals(0, sb.size());
        assertFalse(sb.hasVariable("foo"));
        assertEquals(0, sb.getVariables().size());
        assertEquals(0, sb.getFreeVariables().size());
        assertEquals(0, sb.signatureSize());
    }


    @Test
    public void test_signature() throws KeyStoreException {
        ScriptBuilderWithVar sb = new ScriptBuilderWithVar();

        assertEquals(0, sb.size());
        assertEquals(0, sb.getFreeVariables().size());
        assertEquals(0, sb.signatureSize());

        PrivateKey k1 = PrivateKey.fresh(NetworkType.TESTNET);
        PrivateKey k2 = PrivateKey.fresh(NetworkType.TESTNET);

        String idK1 = ecks.addKey(k1);
        String idK2 = ecks.addKey(k2);

        sb.signaturePlaceholder(idK1, SigHash.ALL, false);
        sb.signaturePlaceholder(idK2, SigHash.ALL, false);
        System.out.println(sb);

        assertEquals(2, sb.size());
        assertEquals(0, sb.getFreeVariables().size());
        assertEquals(2, sb.signatureSize());

        Transaction tx = new Transaction(new MainNetParams());
        tx.addInput(new TransactionInput(new MainNetParams(), null, new byte[]{42,42}));
        sb.setAllSignatures(ecks, tx, 0, new byte[]{}, false);
        System.out.println(sb);

        assertEquals(2, sb.size());
        assertEquals(0, sb.getFreeVariables().size());
        assertEquals(0, sb.signatureSize());
    }

    @Test
    public void test_serialize_freeVariable() {
        ScriptBuilderWithVar sb = new ScriptBuilderWithVar();
        sb.number(15);
        sb.addVariable("Donald", String.class);
        String expected = "15 [var,Donald,java.lang.String]";
        String actual = sb.serialize();
        assertEquals(expected, actual);
    }

    @Test
    public void test_derialize_freeVariable() {
        String serialScript = "15 [var,Donald,java.lang.String]";
        ScriptBuilderWithVar res = new ScriptBuilderWithVar(serialScript);

        assertEquals(1, res.getFreeVariables().size());
        assertEquals(2, res.size());
        assertEquals(String.class, res.getType("Donald"));
    }

    @Test
    public void test_serialize_signature1() {
        PrivateKey key = PrivateKey.fresh(NetworkType.TESTNET);
        SigHash hashType = SigHash.ALL;
        ScriptBuilderWithVar sb = new ScriptBuilderWithVar();
        sb.number(15);
        sb.signaturePlaceholder(ECKeyStore.getUniqueID(key), hashType, false);

        String expected = "15 [sig,"+ECKeyStore.getUniqueID(key)+",AIAO]";
        String actual = sb.serialize();
        assertEquals(expected, actual);
    }

    @Test
    public void test_serialize_signature2() {
        PrivateKey key = PrivateKey.fresh(NetworkType.TESTNET);
        SigHash hashType = SigHash.ALL;
        ScriptBuilderWithVar sb = new ScriptBuilderWithVar();
        sb.number(15);
        sb.signaturePlaceholder(ECKeyStore.getUniqueID(key), hashType, true);

        String expected = "15 [sig,"+ECKeyStore.getUniqueID(key)+",SIAO]";
        String actual = sb.serialize();
        assertEquals(expected, actual);
    }

    @Test
    public void test_serialize_signature3() {
        PrivateKey key = PrivateKey.fresh(NetworkType.TESTNET);
        SigHash hashType = SigHash.SINGLE;
        ScriptBuilderWithVar sb = new ScriptBuilderWithVar();
        sb.number(15);
        sb.signaturePlaceholder(ECKeyStore.getUniqueID(key), hashType, false);

        String expected = "15 [sig,"+ECKeyStore.getUniqueID(key)+",AISO]";
        String actual = sb.serialize();
        assertEquals(expected, actual);
    }

    @Test
    public void test_serialize_signature4() {
        PrivateKey key = PrivateKey.fresh(NetworkType.TESTNET);
        SigHash hashType = SigHash.SINGLE;
        ScriptBuilderWithVar sb = new ScriptBuilderWithVar();
        sb.number(15);
        sb.signaturePlaceholder(ECKeyStore.getUniqueID(key), hashType, true);

        String expected = "15 [sig,"+ECKeyStore.getUniqueID(key)+",SISO]";
        String actual = sb.serialize();
        assertEquals(expected, actual);
    }

    @Test
    public void test_serialize_signature5() {
        PrivateKey key = PrivateKey.fresh(NetworkType.TESTNET);
        SigHash hashType = SigHash.NONE;
        ScriptBuilderWithVar sb = new ScriptBuilderWithVar();
        sb.number(15);
        sb.signaturePlaceholder(ECKeyStore.getUniqueID(key), hashType, false);

        String expected = "15 [sig,"+ECKeyStore.getUniqueID(key)+",AINO]";
        String actual = sb.serialize();
        assertEquals(expected, actual);
    }

    @Test
    public void test_serialize_signature6() {
        PrivateKey key = PrivateKey.fresh(NetworkType.TESTNET);
        SigHash hashType = SigHash.NONE;
        ScriptBuilderWithVar sb = new ScriptBuilderWithVar();
        sb.number(15);
        sb.signaturePlaceholder(ECKeyStore.getUniqueID(key), hashType, true);

        String expected = "15 [sig,"+ECKeyStore.getUniqueID(key)+",SINO]";
        String actual = sb.serialize();
        assertEquals(expected, actual);
    }
    @Test
    public void test_derialize_signature() throws KeyStoreException {
        PrivateKey key = PrivateKey.fresh(NetworkType.TESTNET);
        String keyID = ECKeyStore.getUniqueID(key);
        String serialScript = "15 [sig,"+keyID+",AIAO]";

        ScriptBuilderWithVar res = new ScriptBuilderWithVar(serialScript);

        assertEquals(1, res.signatureSize());
        assertEquals(2, res.size());
        assertEquals(serialScript, res.serialize());

        serialScript = "15 [sig,"+keyID+",SIAO]";
        res = new ScriptBuilderWithVar(serialScript);

        assertEquals(1, res.signatureSize());
        assertEquals(2, res.size());
        assertEquals(serialScript, res.serialize());

        serialScript = "15 [sig,"+keyID+",AINO]";
        res = new ScriptBuilderWithVar(serialScript);

        assertEquals(1, res.signatureSize());
        assertEquals(2, res.size());
        assertEquals(serialScript, res.serialize());

        serialScript = "15 [sig,"+keyID+",SINO]";
        res = new ScriptBuilderWithVar(serialScript);

        assertEquals(1, res.signatureSize());
        assertEquals(2, res.size());
        assertEquals(serialScript, res.serialize());

        serialScript = "15 [sig,"+keyID+",AISO]";
        res = new ScriptBuilderWithVar(serialScript);

        assertEquals(1, res.signatureSize());
        assertEquals(2, res.size());
        assertEquals(serialScript, res.serialize());

        serialScript = "15 [sig,"+keyID+",SISO]";
        res = new ScriptBuilderWithVar(serialScript);

        assertEquals(1, res.signatureSize());
        assertEquals(2, res.size());
        assertEquals(serialScript, res.serialize());
    }

    @Test
    public void test_serialize_deserialize() {
        String[] scripts = {
                "HASH160 PUSHDATA[8174e27d08a37d26e81bbb99c39d20426b782645] EQUAL",
                "DUP HASH160 PUSHDATA[a9776115106b0599bf5f6f82c22e83429babad4d] EQUALVERIFY CHECKSIG",
                "RETURN PUSHDATA[44415441]"
        };

        for (String s : scripts) {
            assertEquals(s, new ScriptBuilderWithVar(s).serialize());
        }
    }
}
