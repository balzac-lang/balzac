package it.unica.tcs.bitcointm.lib.prefix;

import it.unica.tcs.bitcointm.lib.process.Process;

public abstract class AbstractPrefix implements Prefix {

	private final Process next;
	
	public AbstractPrefix(Process next) {
		this.next = next;
	}

	abstract public void execute();
	
	@Override
	public Process continuation() {
		return next;
	}

}
