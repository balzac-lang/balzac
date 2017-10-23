/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.lib;

import static org.junit.Assert.*;

import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.Transaction.SigHash;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.script.Script;
import org.junit.Test;

import it.unica.tcs.lib.Wrapper.NetworkParametersWrapper;
import it.unica.tcs.lib.Wrapper.SigHashWrapper;
import it.unica.tcs.lib.utils.ObjectUtils;

public class ScriptBuilder2Test {

	/*
	 * TODO: la classe KeyStore dovrebbe essere mockata
	 */
	@Test
    public void test() {
		SigHashWrapper wr = SigHashWrapper.wrap(SigHash.ALL);
		String s = ObjectUtils.serializeObjectToStringQuietly(wr);
		SigHashWrapper wr2 = ObjectUtils.deserializeObjectFromStringQuietly(s, SigHashWrapper.class);
		
		assertEquals(SigHash.ALL, wr.get());
		assertEquals(SigHash.ALL, wr2.get());
    }
	
	@Test
    public void test1() {
		NetworkParametersWrapper net = NetworkParametersWrapper.wrap(TestNet3Params.get());
		String s2 = ObjectUtils.serializeObjectToStringQuietly(net);
		NetworkParametersWrapper net2 = ObjectUtils.deserializeObjectFromStringQuietly(s2, NetworkParametersWrapper.class);
		
		assertEquals(TestNet3Params.get(), net.get());
		assertEquals(TestNet3Params.get(), net2.get());
    }
	
	@Test
    public void test2() {
		NetworkParametersWrapper net = NetworkParametersWrapper.wrap(MainNetParams.get());
		String s2 = ObjectUtils.serializeObjectToStringQuietly(net);
		NetworkParametersWrapper net2 = ObjectUtils.deserializeObjectFromStringQuietly(s2, NetworkParametersWrapper.class);
		
		assertEquals(MainNetParams.get(), net.get());
		assertEquals(MainNetParams.get(), net2.get());
    }
	
	@Test
    public void test3() {
		CoinbaseTransactionBuilder net = new CoinbaseTransactionBuilder(NetworkParametersWrapper.wrap(MainNetParams.get()));
		net.addInput(Input.of(new InputScriptImpl()));
		String s2 = ObjectUtils.serializeObjectToStringQuietly(net);
		ITransactionBuilder net2 = ObjectUtils.deserializeObjectFromStringQuietly(s2, ITransactionBuilder.class);
		
		assertTrue(net.isCoinbase());
		assertTrue(net2.isCoinbase());
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
        
        sb.addVariable("pippo", Integer.class);
        assertTrue(sb.hasVariable("pippo"));
        assertEquals(1, sb.size());
        assertEquals(1, sb.getVariables().size());
        assertEquals(1, sb.getFreeVariables().size());
        assertEquals(0, sb.signatureSize());
        
        sb = sb.bindVariable("pippo", 5);
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
        
        
        sb = sb.removeVariable("pippo");
        assertEquals(0, sb.size());
        assertFalse(sb.hasVariable("pippo"));
        assertEquals(0, sb.getVariables().size());
        assertEquals(0, sb.getFreeVariables().size());
        assertEquals(0, sb.signatureSize());
    }
    
    
    @Test
    public void test_signature() {
        ScriptBuilder2 sb = new ScriptBuilder2();
        assertEquals(0, sb.size());
        assertEquals(0, sb.getFreeVariables().size());
        assertEquals(0, sb.signatureSize());
        sb.signaturePlaceholder(new ECKey(), SigHash.ALL, false);
        assertEquals(1, sb.size());
        assertEquals(0, sb.getFreeVariables().size());
        assertEquals(1, sb.signatureSize());
        Transaction tx = new Transaction(new MainNetParams());
        tx.addInput(new TransactionInput(new MainNetParams(), null, new byte[]{42,42}));
        sb = sb.setAllSignatures(tx, 0, new byte[]{});
        assertEquals(1, sb.size());
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
    	KeyStore store = KeyStoreFactory.getInstance();
    	
    	ECKey key = new ECKey();
    	SigHash hashType = SigHash.ALL;
    	ScriptBuilder2 sb = new ScriptBuilder2();
    	sb.number(15);
    	sb.signaturePlaceholder(key, hashType, false);
    	
    	String expected = "15 [sig,"+store.getUniqueID(key)+",**]"; 
    	String actual = sb.serialize();
    	assertEquals(expected, actual);
    }

    @Test
    public void test_serialize_signature2() {
    	KeyStore store = KeyStoreFactory.getInstance();
    	
    	ECKey key = new ECKey();
    	SigHash hashType = SigHash.ALL;
    	ScriptBuilder2 sb = new ScriptBuilder2();
    	sb.number(15);
    	sb.signaturePlaceholder(key, hashType, true);
    	
    	String expected = "15 [sig,"+store.getUniqueID(key)+",1*]"; 
    	String actual = sb.serialize();
    	assertEquals(expected, actual);
    }
    
    @Test
    public void test_serialize_signature3() {
    	KeyStore store = KeyStoreFactory.getInstance();
    	
    	ECKey key = new ECKey();
    	SigHash hashType = SigHash.SINGLE;
    	ScriptBuilder2 sb = new ScriptBuilder2();
    	sb.number(15);
    	sb.signaturePlaceholder(key, hashType, false);
    	
    	String expected = "15 [sig,"+store.getUniqueID(key)+",*1]"; 
    	String actual = sb.serialize();
    	assertEquals(expected, actual);
    }

    @Test
    public void test_serialize_signature4() {
    	KeyStore store = KeyStoreFactory.getInstance();
    	
    	ECKey key = new ECKey();
    	SigHash hashType = SigHash.SINGLE;
    	ScriptBuilder2 sb = new ScriptBuilder2();
    	sb.number(15);
    	sb.signaturePlaceholder(key, hashType, true);
    	
    	String expected = "15 [sig,"+store.getUniqueID(key)+",11]"; 
    	String actual = sb.serialize();
    	assertEquals(expected, actual);
    }
    
    @Test
    public void test_serialize_signature5() {
    	KeyStore store = KeyStoreFactory.getInstance();
    	
    	ECKey key = new ECKey();
    	SigHash hashType = SigHash.NONE;
    	ScriptBuilder2 sb = new ScriptBuilder2();
    	sb.number(15);
    	sb.signaturePlaceholder(key, hashType, false);
    	
    	String expected = "15 [sig,"+store.getUniqueID(key)+",*0]"; 
    	String actual = sb.serialize();
    	assertEquals(expected, actual);
    }

    @Test
    public void test_serialize_signature6() {
    	KeyStore store = KeyStoreFactory.getInstance();
    	
    	ECKey key = new ECKey();
    	SigHash hashType = SigHash.NONE;
    	ScriptBuilder2 sb = new ScriptBuilder2();
    	sb.number(15);
    	sb.signaturePlaceholder(key, hashType, true);
    	
    	String expected = "15 [sig,"+store.getUniqueID(key)+",10]"; 
    	String actual = sb.serialize();
    	assertEquals(expected, actual);
    }
    @Test
    public void test_derialize_signature() {
    	KeyStore store = KeyStoreFactory.getInstance();
    	
    	ECKey key = new ECKey();
    	String keyID = store.addKey(key);
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
