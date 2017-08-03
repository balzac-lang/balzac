package it.unica.tcs.generator;

import java.util.HashMap;

import it.unica.tcs.bitcoinTM.Parameter;

/**
 * Everything that is relevant to achieve the compilation.
 */
public class Context {
	
	public static class AltStackEntry {
		public final Integer position;
		public final Integer occurrences;
		
		public AltStackEntry(Integer position, Integer occurrences) {
			this.position = position;
			this.occurrences = occurrences;
		}
		
		public static AltStackEntry of(Integer position, Integer occurrences) {
			return new AltStackEntry(position, occurrences);
		}
	}
	
	public static class AltStack extends HashMap<Parameter, AltStackEntry>{}
	
	public AltStack altstack = new AltStack();
	
	
	public void clear() {
		altstack.clear();
	}
	
}