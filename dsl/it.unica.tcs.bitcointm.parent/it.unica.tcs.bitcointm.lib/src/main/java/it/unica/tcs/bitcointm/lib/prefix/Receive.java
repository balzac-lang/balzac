package it.unica.tcs.bitcointm.lib.prefix;

import static com.google.common.base.Preconditions.checkState;

import it.unica.tcs.bitcointm.lib.process.Process;

public class Receive extends AbstractPrefix {

	Receive(Process next) {
		super(next);
	}

	@Override
	public boolean ready() {
		return false;
	}

	@Override
	public void execute() {
		checkState(ready());
	}

}
