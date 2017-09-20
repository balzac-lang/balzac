package it.unica.tcs.bitcointm.lib.prefix;

import it.unica.tcs.bitcointm.lib.process.Process;

public class Tau extends AbstractPrefix {

	public Tau(Process next) {
		super(next);
	}

	@Override
	public void execute() {}

	@Override
	public String toString(){
		return "t";
	}
}
