package it.unica.tcs.bitcointm.lib.prefix;

import it.unica.tcs.bitcointm.lib.process.Process;

public abstract class AbstractPrefix implements Prefix {

	protected final Process next;
	
	AbstractPrefix(Process next) {
		this.next = next;
	}

	abstract public void execute();
}
