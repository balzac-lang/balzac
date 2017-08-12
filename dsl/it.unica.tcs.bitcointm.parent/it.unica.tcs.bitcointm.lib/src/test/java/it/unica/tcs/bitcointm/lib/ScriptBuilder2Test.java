package it.unica.tcs.bitcointm.lib;

import static org.junit.Assert.assertEquals;

import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.Transaction.SigHash;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.params.MainNetParams;
import org.junit.Test;

public class ScriptBuilder2Test {

	@Test
    public void test_size() {
        ScriptBuilder2 sb = new ScriptBuilder2();
        assertEquals(0, sb.size());
        assertEquals(0, sb.freeVariableSize());
        assertEquals(0, sb.signatureSize());
        sb.number(5);
        assertEquals(1, sb.size());
        assertEquals(0, sb.freeVariableSize());
        assertEquals(0, sb.signatureSize());
    }
    
    @Test
    public void test_freeVariable() {
        ScriptBuilder2 sb = new ScriptBuilder2();
        assertEquals(0, sb.size());
        assertEquals(0, sb.freeVariableSize());
        assertEquals(0, sb.signatureSize());
        sb.freeVariable("pippo", Integer.class);
        assertEquals(1, sb.size());
        assertEquals(1, sb.freeVariableSize());
        assertEquals(0, sb.signatureSize());
        sb = sb.setFreeVariable("pippo", 5);
        assertEquals(1, sb.size());
        assertEquals(0, sb.freeVariableSize());
        assertEquals(0, sb.signatureSize());
    }
    
    @Test
    public void test_signature() {
        ScriptBuilder2 sb = new ScriptBuilder2();
        assertEquals(0, sb.size());
        assertEquals(0, sb.freeVariableSize());
        assertEquals(0, sb.signatureSize());
        sb.signaturePlaceholder(new ECKey(), SigHash.ALL, false);
        assertEquals(1, sb.size());
        assertEquals(0, sb.freeVariableSize());
        assertEquals(1, sb.signatureSize());
        Transaction tx = new Transaction(new MainNetParams());
        tx.addInput(new TransactionInput(new MainNetParams(), null, new byte[]{42,42}));
        sb = sb.setSignatures(tx, 0, new byte[]{});
        assertEquals(1, sb.size());
        assertEquals(0, sb.freeVariableSize());
        assertEquals(0, sb.signatureSize());
    }
    
    @Test(expected=IllegalStateException.class)
    public void test_appendFail() {
        ScriptBuilder2 sb = new ScriptBuilder2();
        sb.freeVariable("pippo", Integer.class);
        new ScriptBuilder2().append(sb);
    }

}
