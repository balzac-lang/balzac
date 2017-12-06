/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.lib.model;

import static com.google.common.base.Preconditions.checkState;

public class Send extends AbstractPrefix {

	Send() {
	}

	@Override
	public boolean ready() {
		return false;
	}

	@Override
	public void run() {
		checkState(ready());
	}

}
