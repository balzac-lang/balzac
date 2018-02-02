/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.lib;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.Transaction.SigHash;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.script.Script;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import it.unica.tcs.lib.script.ScriptBuilder2;

public class ScriptBuilder2Test {

    ECKeyStore ecks;

    @Before
    public void before() throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        ecks = ECKeyStore.create();
        ecks.changePassword(new char[]{'t','e','s','t'});
    }

    @After
    public void after() {
        ecks.getKeyStoreFile().delete();
    }

    @Test
    public void test_size() {
        ScriptBuilder2 sb = new ScriptBuilder2();
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
        ScriptBuilder2 sb = new ScriptBuilder2();
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
        ScriptBuilder2 sb = new ScriptBuilder2();

        assertEquals(0, sb.size());
        assertEquals(0, sb.getFreeVariables().size());
        assertEquals(0, sb.signatureSize());

        ECKey k1 = new ECKey();
        ECKey k2 = new ECKey();

        sb.setKeyStore(ecks);
        ecks.addKey(k1);
        ecks.addKey(k2);

        sb.signaturePlaceholder(k1, SigHash.ALL, false);
        sb.signaturePlaceholder(k2, SigHash.ALL, false);
        System.out.println(sb);

        assertEquals(2, sb.size());
        assertEquals(0, sb.getFreeVariables().size());
        assertEquals(2, sb.signatureSize());

        Transaction tx = new Transaction(new MainNetParams());
        tx.addInput(new TransactionInput(new MainNetParams(), null, new byte[]{42,42}));
        sb.setAllSignatures(tx, 0, new byte[]{});
        System.out.println(sb);

        assertEquals(2, sb.size());
        assertEquals(0, sb.getFreeVariables().size());
        assertEquals(0, sb.signatureSize());
    }

    @Test
    public void test_serialize_freeVariable() {
        ScriptBuilder2 sb = new ScriptBuilder2();
        sb.number(15);
        sb.addVariable("Donald", String.class);
        String expected = "15 [var,Donald,java.lang.String]";
        String actual = sb.serialize();
        assertEquals(expected, actual);
    }

    @Test
    public void test_derialize_freeVariable() {
        String serialScript = "15 [var,Donald,java.lang.String]";
        ScriptBuilder2 res = new ScriptBuilder2(serialScript);

        assertEquals(1, res.getFreeVariables().size());
        assertEquals(2, res.size());
        assertEquals(String.class, res.getType("Donald"));
    }

    @Test
    public void test_serialize_signature1() {
        ECKey key = new ECKey();
        SigHash hashType = SigHash.ALL;
        ScriptBuilder2 sb = new ScriptBuilder2();
        sb.number(15);
        sb.signaturePlaceholder(key, hashType, false);

        String expected = "15 [sig,"+ECKeyStore.getUniqueID(key)+",**]";
        String actual = sb.serialize();
        assertEquals(expected, actual);
    }

    @Test
    public void test_serialize_signature2() {
        ECKey key = new ECKey();
        SigHash hashType = SigHash.ALL;
        ScriptBuilder2 sb = new ScriptBuilder2();
        sb.number(15);
        sb.signaturePlaceholder(key, hashType, true);

        String expected = "15 [sig,"+ECKeyStore.getUniqueID(key)+",1*]";
        String actual = sb.serialize();
        assertEquals(expected, actual);
    }

    @Test
    public void test_serialize_signature3() {
        ECKey key = new ECKey();
        SigHash hashType = SigHash.SINGLE;
        ScriptBuilder2 sb = new ScriptBuilder2();
        sb.number(15);
        sb.signaturePlaceholder(key, hashType, false);

        String expected = "15 [sig,"+ECKeyStore.getUniqueID(key)+",*1]";
        String actual = sb.serialize();
        assertEquals(expected, actual);
    }

    @Test
    public void test_serialize_signature4() {
        ECKey key = new ECKey();
        SigHash hashType = SigHash.SINGLE;
        ScriptBuilder2 sb = new ScriptBuilder2();
        sb.number(15);
        sb.signaturePlaceholder(key, hashType, true);

        String expected = "15 [sig,"+ECKeyStore.getUniqueID(key)+",11]";
        String actual = sb.serialize();
        assertEquals(expected, actual);
    }

    @Test
    public void test_serialize_signature5() {
        ECKey key = new ECKey();
        SigHash hashType = SigHash.NONE;
        ScriptBuilder2 sb = new ScriptBuilder2();
        sb.number(15);
        sb.signaturePlaceholder(key, hashType, false);

        String expected = "15 [sig,"+ECKeyStore.getUniqueID(key)+",*0]";
        String actual = sb.serialize();
        assertEquals(expected, actual);
    }

    @Test
    public void test_serialize_signature6() {
        ECKey key = new ECKey();
        SigHash hashType = SigHash.NONE;
        ScriptBuilder2 sb = new ScriptBuilder2();
        sb.number(15);
        sb.signaturePlaceholder(key, hashType, true);

        String expected = "15 [sig,"+ECKeyStore.getUniqueID(key)+",10]";
        String actual = sb.serialize();
        assertEquals(expected, actual);
    }
    @Test
    public void test_derialize_signature() throws KeyStoreException {
        ECKey key = new ECKey();
        String keyID = ECKeyStore.getUniqueID(key);
        String serialScript = "15 [sig,"+keyID+",**]";

        ScriptBuilder2 res = new ScriptBuilder2(serialScript);

        assertEquals(1, res.signatureSize());
        assertEquals(2, res.size());
        assertEquals(serialScript, res.serialize());

        serialScript = "15 [sig,"+keyID+",1*]";
        res = new ScriptBuilder2(serialScript);

        assertEquals(1, res.signatureSize());
        assertEquals(2, res.size());
        assertEquals(serialScript, res.serialize());

        serialScript = "15 [sig,"+keyID+",*0]";
        res = new ScriptBuilder2(serialScript);

        assertEquals(1, res.signatureSize());
        assertEquals(2, res.size());
        assertEquals(serialScript, res.serialize());

        serialScript = "15 [sig,"+keyID+",10]";
        res = new ScriptBuilder2(serialScript);

        assertEquals(1, res.signatureSize());
        assertEquals(2, res.size());
        assertEquals(serialScript, res.serialize());

        serialScript = "15 [sig,"+keyID+",*1]";
        res = new ScriptBuilder2(serialScript);

        assertEquals(1, res.signatureSize());
        assertEquals(2, res.size());
        assertEquals(serialScript, res.serialize());

        serialScript = "15 [sig,"+keyID+",11]";
        res = new ScriptBuilder2(serialScript);

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
            assertEquals(s, new ScriptBuilder2(s).serialize());
        }
    }
}
