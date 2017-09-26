/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.lib.model.prefix;

import static com.google.common.base.Preconditions.checkState;

import it.unica.tcs.lib.model.process.Process;

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
