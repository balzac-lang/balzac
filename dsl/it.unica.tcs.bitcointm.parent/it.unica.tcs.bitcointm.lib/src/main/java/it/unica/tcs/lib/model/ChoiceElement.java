/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.lib.model;

public class ChoiceElement implements Process, Prefix {

	private final Prefix prefix;
	private final Process next;
	
	ChoiceElement(Prefix prefix) {
		this(prefix, () -> {});		
	}

	ChoiceElement(Prefix prefix, Process next) {
		this.prefix = prefix;
		this.next = next;
	}

	@Override
	public boolean ready() {
		return prefix.ready();
	}

	@Override
	public void run() {
		prefix.run();
		next.run();
	}

}
