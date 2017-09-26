/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.lib.model.prefix;

import it.unica.tcs.lib.model.process.Process;

public abstract class AbstractPrefix implements Prefix {

	protected final Process next;
	
	AbstractPrefix(Process next) {
		this.next = next;
	}

	abstract public void execute();
}
