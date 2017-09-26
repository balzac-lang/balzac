/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.lib.prefix;

import it.unica.tcs.lib.process.Process;

public abstract class AbstractPrefix implements Prefix {

	protected final Process next;
	
	AbstractPrefix(Process next) {
		this.next = next;
	}

	abstract public void execute();
}
