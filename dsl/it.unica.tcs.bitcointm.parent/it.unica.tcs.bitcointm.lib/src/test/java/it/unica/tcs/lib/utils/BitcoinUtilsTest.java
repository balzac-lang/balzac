package it.unica.tcs.lib.utils;

import static org.junit.Assert.assertArrayEquals;

import java.util.LinkedList;

import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.script.ScriptOpCodes;
import org.junit.Test;

import it.unica.tcs.lib.Hash;
import it.unica.tcs.lib.Hash.Hash160;
import it.unica.tcs.lib.Hash.Hash256;
import it.unica.tcs.lib.Hash.Ripemd160;
import it.unica.tcs.lib.Hash.Sha256;

public class BitcoinUtilsTest {

	@Test
	public void test_hash_refl() {
		@SuppressWarnings("unchecked")
		Class<? extends Hash>[] hashClasses = new Class[]{Ripemd160.class, Sha256.class, Hash160.class, Hash256.class};
		Object[] values = new Object[]{1,"",true,new byte[42]};
		
		for (Class<? extends Hash> hashCls : hashClasses) {
			for (Object v : values) {
					assertArrayEquals(
						executeScript(v, hashCls), 
						BitcoinUtils.hash(v, hashCls).getBytes()
					);
			}
		}
	}
	
	@Test
	public void test_hashes_empty() {
		assertArrayEquals(
				executeScript(new byte[]{}, Ripemd160.class), 
				BitcoinUtils.ripemd160(new byte[]{}).getBytes()
		);
		assertArrayEquals(
				executeScript(new byte[]{}, Sha256.class), 
				BitcoinUtils.sha256(new byte[]{}).getBytes()
		);
		assertArrayEquals(
				executeScript(new byte[]{}, Hash160.class), 
				BitcoinUtils.hash160(new byte[]{}).getBytes()
		);
		assertArrayEquals(
				executeScript(new byte[]{}, Hash256.class), 
				BitcoinUtils.hash256(new byte[]{}).getBytes()
		);
		assertArrayEquals(
				executeScript(new byte[]{}, Hash256.class), 
				BitcoinUtils.sha256(BitcoinUtils.sha256(new byte[]{})).getBytes()
		);
		assertArrayEquals(
				executeScript(new byte[]{}, Hash160.class), 
				BitcoinUtils.ripemd160(BitcoinUtils.sha256(new byte[]{})).getBytes()
		);
		assertArrayEquals(
				executeScript(executeScript(new byte[]{}, Sha256.class), Sha256.class), 
				BitcoinUtils.hash256(new byte[]{}).getBytes()
		);
		assertArrayEquals(
				executeScript(executeScript(new byte[]{}, Sha256.class), Ripemd160.class), 
				BitcoinUtils.hash160(new byte[]{}).getBytes()
		);
	}
	
	@Test
	public void test_hashes_boolean() {
		assertArrayEquals(
				executeScript(true, Ripemd160.class), 
				BitcoinUtils.ripemd160(true).getBytes()
		);
		assertArrayEquals(
				executeScript(true, Sha256.class), 
				BitcoinUtils.sha256(true).getBytes()
		);
		assertArrayEquals(
				executeScript(true, Hash160.class), 
				BitcoinUtils.hash160(true).getBytes()
		);
		assertArrayEquals(
				executeScript(true, Hash256.class), 
				BitcoinUtils.hash256(true).getBytes()
		);
		assertArrayEquals(
				executeScript(true, Hash256.class), 
				BitcoinUtils.sha256(BitcoinUtils.sha256(true)).getBytes()
		);
		assertArrayEquals(
				executeScript(true, Hash160.class), 
				BitcoinUtils.ripemd160(BitcoinUtils.sha256(true)).getBytes()
		);
		assertArrayEquals(
				executeScript(executeScript(true, Sha256.class), Sha256.class), 
				BitcoinUtils.hash256(true).getBytes()
		);
		assertArrayEquals(
				executeScript(executeScript(true, Sha256.class), Ripemd160.class), 
				BitcoinUtils.hash160(true).getBytes()
		);
		
		
		assertArrayEquals(
				executeScript(false, Ripemd160.class), 
				BitcoinUtils.ripemd160(false).getBytes()
		);
		assertArrayEquals(
				executeScript(false, Sha256.class), 
				BitcoinUtils.sha256(false).getBytes()
		);
		assertArrayEquals(
				executeScript(false, Hash160.class), 
				BitcoinUtils.hash160(false).getBytes()
		);
		assertArrayEquals(
				executeScript(false, Hash256.class), 
				BitcoinUtils.hash256(false).getBytes()
		);
		assertArrayEquals(
				executeScript(false, Hash256.class), 
				BitcoinUtils.sha256(BitcoinUtils.sha256(false)).getBytes()
		);
		assertArrayEquals(
				executeScript(false, Hash160.class), 
				BitcoinUtils.ripemd160(BitcoinUtils.sha256(false)).getBytes()
		);
		assertArrayEquals(
				executeScript(executeScript(false, Sha256.class), Sha256.class), 
				BitcoinUtils.hash256(false).getBytes()
		);
		assertArrayEquals(
				executeScript(executeScript(false, Sha256.class), Ripemd160.class), 
				BitcoinUtils.hash160(false).getBytes()
		);
	}
	
	@Test
	public void test_hashes_integers() {
		for (int i=-100; i<100; i++) {
			assertArrayEquals(
					executeScript(i, Ripemd160.class), 
					BitcoinUtils.ripemd160(i).getBytes()
			);
			assertArrayEquals(
					executeScript(i, Sha256.class), 
					BitcoinUtils.sha256(i).getBytes()
			);
			assertArrayEquals(
					executeScript(i, Hash160.class), 
					BitcoinUtils.hash160(i).getBytes()
			);
			assertArrayEquals(
					executeScript(i, Hash256.class), 
					BitcoinUtils.hash256(i).getBytes()
			);
			assertArrayEquals(
					executeScript(i, Hash256.class), 
					BitcoinUtils.sha256(BitcoinUtils.sha256(i)).getBytes()
			);
			assertArrayEquals(
					executeScript(i, Hash160.class), 
					BitcoinUtils.ripemd160(BitcoinUtils.sha256(i)).getBytes()
			);
			assertArrayEquals(
					executeScript(executeScript(i, Sha256.class), Sha256.class), 
					BitcoinUtils.hash256(i).getBytes()
			);
			assertArrayEquals(
					executeScript(executeScript(i, Sha256.class), Ripemd160.class), 
					BitcoinUtils.hash160(i).getBytes()
			);
		}
	}
	
	
	
	
	
	
	
	private byte[] executeScript(Object input, Class<? extends Hash> clazz) {
		if (input instanceof byte[])
			return executeScript((byte[]) input, clazz);
		return executeScript(BitcoinUtils.toScript(input), clazz);
	}
	
	private byte[] executeScript(byte[] script, Class<? extends Hash> clazz) {
		return executeScript(new ScriptBuilder().data(script).build(), clazz);
	}
	
	private byte[] executeScript(Script s, Class<? extends Hash> clazz) {
		int operation;
		
		if (clazz.equals(Sha256.class))
			operation = ScriptOpCodes.OP_SHA256;
		else if (clazz.equals(Ripemd160.class))
			operation = ScriptOpCodes.OP_RIPEMD160;
		else if (clazz.equals(Hash256.class))
			operation = ScriptOpCodes.OP_HASH256;
		else if (clazz.equals(Hash160.class))
			operation = ScriptOpCodes.OP_HASH160;
		else
			throw new IllegalArgumentException("unexpected class "+clazz.getName());
		
		LinkedList<byte[]> stack = new LinkedList<>();
		Script.executeScript(null, 0, new ScriptBuilder(s).op(operation).build(), stack, Script.ALL_VERIFY_FLAGS);
		byte[] res = stack.getLast();
		return res;
	}
	
}
