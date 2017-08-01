package it.unica.tcs.generator;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.tuple.ImmutablePair;

import it.unica.tcs.bitcoinTM.Parameter;

/**
 * Everything that is relevant to achieve the compilation.
 */
public class Context {

	public Map<Parameter, ImmutablePair<Integer,Integer>> altstack = new HashMap<>();
	
	public void clear() {
		altstack.clear();
	}
	
}