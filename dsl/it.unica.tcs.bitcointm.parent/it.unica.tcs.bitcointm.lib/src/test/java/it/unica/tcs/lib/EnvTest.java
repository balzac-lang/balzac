package it.unica.tcs.lib;

import static org.junit.Assert.*;

import org.junit.Test;

public class EnvTest {

	@Test
	public void test_empty() {
		Env env = new Env();
		String name = "any";
		
		assertFalse(env.hasVariable(name));
		assertTrue(env.isReady());
		assertTrue(env.getVariables().isEmpty());
		assertTrue(env.getFreeVariables().isEmpty());
		assertTrue(env.getBoundVariables().isEmpty());
		
		try {
			env.isFree(name);
			fail();
		}
		catch (IllegalArgumentException e) {}
		
		try {
			env.isBound(name);
			fail();
		}
		catch (IllegalArgumentException e) {}

		
		try {
			env.getValue(name);
			fail();
		}
		catch (IllegalArgumentException e) {}

		try {
			env.getType(name);
			fail();
		}
		catch (IllegalArgumentException e) {}
		
		try {
			env.bindVariable(name, 10);
			fail();
		}
		catch (IllegalArgumentException e) {}
	}

	@Test
	public void test_add() {
		Env env = new Env();
		String name = "any";
		
		env.addVariable(name, Integer.class);
		env.addVariable(name, Integer.class);	// multi add with the same time is fine
		
		assertTrue(env.hasVariable(name));
		assertTrue(env.isFree(name));
		assertFalse(env.isBound(name));
				
		assertFalse(env.isReady());
		
		assertEquals(1, env.getVariables().size());
		assertEquals(1, env.getFreeVariables().size());
		assertEquals(0, env.getBoundVariables().size());
		
		assertEquals(Integer.class, env.getType(name));

		try {
			// fails if try to set another type
			env.addVariable(name, String.class);
			fail();
		}
		catch (IllegalArgumentException e) {}
		
		env.bindVariable(name, 10);
		
		assertTrue(env.hasVariable(name));
		assertFalse(env.isFree(name));
		assertTrue(env.isBound(name));
				
		assertTrue(env.isReady());
		
		assertEquals(1, env.getVariables().size());
		assertEquals(0, env.getFreeVariables().size());
		assertEquals(1, env.getBoundVariables().size());
		
		try {
			// fails if try to set another type
			env.bindVariable(name, 42);
			fail();
		}
		catch (IllegalArgumentException e) {}
	}
	
	@Test
	public void test_clear() {
		Env env = new Env();
		String name = "any";
		String name2 = "any2";
				
		env.addVariable(name, Integer.class);
		env.addVariable(name2, Long.class);	// multi add with the same time is fine
		
		assertFalse(env.isReady());
		assertEquals(2, env.getVariables().size());
		assertEquals(2, env.getFreeVariables().size());
		assertEquals(0, env.getBoundVariables().size());
		
		env.bindVariable(name, 5);
//		env.bindVariable(name2, 8745287489874L);
		System.out.println(env);
		
		env.clear();
		
		assertTrue(env.isReady());
		assertEquals(0, env.getVariables().size());
		assertEquals(0, env.getFreeVariables().size());
		assertEquals(0, env.getBoundVariables().size());
	}
	
}
