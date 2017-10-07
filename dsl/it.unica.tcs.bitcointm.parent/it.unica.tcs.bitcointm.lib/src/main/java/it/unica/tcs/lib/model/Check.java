/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.lib.model;

import static com.google.common.base.Preconditions.checkState;

import java.util.function.Supplier;

public class Check extends AbstractPrefix {

	private final Supplier<Boolean> condition;
	
	Check(Supplier<Boolean> condition) {
		this.condition = condition;
	}

	@Override
	public boolean ready() {
		return condition.get();
	}

	@Override
	public void run() {
		checkState(ready());
	}

	@Override
	public String toString(){
		return "assert <e>";
	}
}
