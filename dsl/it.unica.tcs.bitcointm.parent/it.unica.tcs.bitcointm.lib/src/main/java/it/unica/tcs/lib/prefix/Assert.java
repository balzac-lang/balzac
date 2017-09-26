/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.lib.prefix;

import static com.google.common.base.Preconditions.checkState;

import java.util.function.Supplier;

import it.unica.tcs.lib.process.Process;

public class Assert extends AbstractPrefix {

	private final Supplier<Boolean> condition;
	
	Assert(Supplier<Boolean> condition, Process next) {
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
