package it.unica.tcs.bitcointm.lib.prefix;

import it.unica.tcs.bitcointm.lib.process.Process;

public interface Prefix {

	public void execute();
	
	public Process continuation(); 
}
