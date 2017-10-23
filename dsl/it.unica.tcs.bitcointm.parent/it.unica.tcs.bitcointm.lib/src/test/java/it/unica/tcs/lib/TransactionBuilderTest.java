package it.unica.tcs.lib;

import static org.junit.Assert.*;

import org.bitcoinj.params.MainNetParams;
import org.junit.Test;

import it.unica.tcs.lib.Wrapper.NetworkParametersWrapper;

public class TransactionBuilderTest {

	@Test
	public void test() {
		TransactionBuilder tb = new TransactionBuilder(NetworkParametersWrapper.wrap(MainNetParams.get()));
		tb.addVariable("foo", Integer.class);
		tb.addVariable("veryLongName", String.class);
		tb.addVariable("anotherVeryLongName", String.class).bindVariable("anotherVeryLongName", "hihihhihihihihihiihihihi");
		
		tb.addInput(Input.of((InputScript) new InputScriptImpl().number(5)));
		tb.addInput(Input.of((InputScript) new InputScriptImpl().number(42)));
		tb.addOutput((OutputScript) new P2SHOutputScript().number(3).addVariable("veryLongName", String.class).addVariable("foo", String.class).bindVariable("foo", "pippo"), 34);
		
		System.out.println(tb);
	}

	
	@Test
	public void test_addInput() {
		TransactionBuilder tb = new TransactionBuilder(NetworkParametersWrapper.wrap(MainNetParams.get()));
		tb.addVariable("foo", Integer.class);
		
		Input in = Input.of((InputScript) new InputScriptImpl().number(5).addVariable("foo", String.class), 34);
		
		try {
			tb.addInput(in);
			fail();
		}
		catch (IllegalArgumentException e){}
	}
}
