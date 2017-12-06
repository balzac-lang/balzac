package it.unica.tcs.lib.script;

import static org.junit.Assert.*;

import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import static org.bitcoinj.script.ScriptOpCodes.*;
import org.junit.Test;

import it.unica.tcs.lib.script.AbstractScriptBuilder;
import it.unica.tcs.lib.script.ScriptBuilder2;

public class AbstractScriptBuilderTest {

	@Test
	public void test_optimize() {
		
		Script s = new ScriptBuilder()
				.op(OP_TOALTSTACK)
				.op(OP_FROMALTSTACK)
				.build();
		
		Script opt = AbstractScriptBuilder.optimize(s);
		
		assertTrue(s!=opt);
		assertEquals(2, s.getChunks().size());
		assertEquals(0, opt.getChunks().size());
		assertArrayEquals(s.getProgram(), new byte[]{OP_TOALTSTACK, OP_FROMALTSTACK});
		assertArrayEquals(opt.getProgram(), new byte[]{});
	}
	
	@Test
	public void test_optimize2() {
		
		Script s = new ScriptBuilder()
				.op(OP_TOALTSTACK)
				.number(4)
				.op(OP_FROMALTSTACK)
				.build();
		
		Script opt = AbstractScriptBuilder.optimize(s);
		
		assertTrue(s!=opt);
		assertEquals(3, s.getChunks().size());
		assertEquals(3, opt.getChunks().size());
		assertArrayEquals(s.getProgram(), new byte[]{OP_TOALTSTACK, OP_4, OP_FROMALTSTACK});
		assertArrayEquals(s.getProgram(), opt.getProgram());
	}
	
	@Test
	public void test_optimize3() {
		
		Script s = new ScriptBuilder()
				.op(OP_TOALTSTACK)
				.op(OP_FROMALTSTACK)
				.number(4)
				.op(OP_TOALTSTACK)
				.op(OP_FROMALTSTACK)
				.build();
		
		Script opt = AbstractScriptBuilder.optimize(s);
		
		assertTrue(s!=opt);
		assertEquals(5, s.getChunks().size());
		assertEquals(1, opt.getChunks().size());
		assertArrayEquals(s.getProgram(), new byte[]{OP_TOALTSTACK, OP_FROMALTSTACK, OP_4, OP_TOALTSTACK, OP_FROMALTSTACK});
		assertArrayEquals(opt.getProgram(), new byte[]{OP_4});
	}
	
	@Test
	public void test_optimize4() {
		
		Script s = new ScriptBuilder()
				.op(OP_TOALTSTACK)
				.op(OP_TOALTSTACK)
				.op(OP_FROMALTSTACK)
				.op(OP_FROMALTSTACK)
				.number(4)
				.build();
		
		Script opt = AbstractScriptBuilder.optimize(s);
		
		assertTrue(s!=opt);
		assertEquals(5, s.getChunks().size());
		assertEquals(1, opt.getChunks().size());
		assertArrayEquals(s.getProgram(), new byte[]{OP_TOALTSTACK, OP_TOALTSTACK, OP_FROMALTSTACK, OP_FROMALTSTACK, OP_4});
		assertArrayEquals(opt.getProgram(), new byte[]{OP_4});
	}
	
	@Test
	public void test_optimize5() {
		
		Script s = new ScriptBuilder()
				.op(OP_TOALTSTACK)
				.op(OP_TOALTSTACK)
				.number(4)
				.op(OP_FROMALTSTACK)
				.op(OP_FROMALTSTACK)
				.build();
		
		Script opt = AbstractScriptBuilder.optimize(s);
		
		assertTrue(s!=opt);
		assertEquals(5, s.getChunks().size());
		assertEquals(5, opt.getChunks().size());
		assertArrayEquals(s.getProgram(), new byte[]{OP_TOALTSTACK, OP_TOALTSTACK, OP_4, OP_FROMALTSTACK, OP_FROMALTSTACK});
		assertArrayEquals(s.getProgram(), opt.getProgram());
	}
	
	
	
	
	
	
	@Test
	public void test_optimize_sb() {

		Script s = new ScriptBuilder2()
				.op(OP_TOALTSTACK)
				.op(OP_FROMALTSTACK)
				.optimize()
				.build();
		
		assertEquals(0, s.getChunks().size());
		assertArrayEquals(s.getProgram(), new byte[]{});
	}
	
	@Test
	public void test_optimize2_sb() {
		
		Script s = new ScriptBuilder2()
				.op(OP_TOALTSTACK)
				.number(4)
				.op(OP_FROMALTSTACK)
				.optimize()
				.build();
		
		assertEquals(3, s.getChunks().size());
		assertArrayEquals(s.getProgram(), new byte[]{OP_TOALTSTACK, OP_4, OP_FROMALTSTACK});
	}
	
	@Test
	public void test_optimize3_sb() {
		
		Script s = new ScriptBuilder2()
				.op(OP_TOALTSTACK)
				.op(OP_FROMALTSTACK)
				.number(4)
				.op(OP_TOALTSTACK)
				.op(OP_FROMALTSTACK)
				.optimize()
				.build();
		
		assertEquals(1, s.getChunks().size());
		assertArrayEquals(s.getProgram(), new byte[]{OP_4});
	}
	
	@Test
	public void test_optimize4_sb() {
		
		Script s = new ScriptBuilder2()
				.op(OP_TOALTSTACK)
				.op(OP_TOALTSTACK)
				.op(OP_FROMALTSTACK)
				.op(OP_FROMALTSTACK)
				.number(4)
				.optimize()
				.build();
		
		assertEquals(1, s.getChunks().size());
		assertArrayEquals(s.getProgram(), new byte[]{OP_4});
	}
	
	@Test
	public void test_optimize5_sb() {
		
		Script s = new ScriptBuilder2()
				.op(OP_TOALTSTACK)
				.op(OP_TOALTSTACK)
				.number(4)
				.op(OP_FROMALTSTACK)
				.op(OP_FROMALTSTACK)
				.optimize()
				.build();
		
		assertEquals(5, s.getChunks().size());
		assertArrayEquals(s.getProgram(), new byte[]{OP_TOALTSTACK, OP_TOALTSTACK, OP_4, OP_FROMALTSTACK, OP_FROMALTSTACK});
	}
}
