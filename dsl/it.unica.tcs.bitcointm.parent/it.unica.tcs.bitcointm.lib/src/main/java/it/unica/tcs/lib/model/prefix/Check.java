/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.lib.model.prefix;

import static com.google.common.base.Preconditions.checkState;

import java.util.function.Supplier;

import it.unica.tcs.lib.model.process.Process;

public class Check extends AbstractPrefix {

	private final Supplier<Boolean> condition;
	
	Check(Supplier<Boolean> condition, Process next) {
		super(next);
		this.condition = condition;
	}

	@Override
	public boolean ready() {
		return condition.get();
	}

	@Override
	public void execute() {
		checkState(ready());
		next.run();
	}

	@Override
	public String toString(){
		return "assert <e>"+(next!=null?" . "+next.toString():"");
	}
}
