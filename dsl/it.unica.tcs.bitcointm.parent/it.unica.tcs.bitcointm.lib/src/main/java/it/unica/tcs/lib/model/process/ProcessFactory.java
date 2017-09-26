/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.lib.model.process;

import static com.google.common.base.Preconditions.checkState;

import java.util.function.Supplier;

import it.unica.tcs.lib.model.prefix.Prefix;

public class ProcessFactory {

	public static Process nil() {
		return new NullProcess();
	}
	
	public static Process choice(Prefix... prefixes) {
		checkState(prefixes.length>0);
		return new Choice(prefixes);
	}
	
	public static Process ifThenElse(Supplier<Boolean> condition, Process _then) {
		return new Process() {
			@Override
			public void run() {
				if (condition.get()) {
					_then.run();
				}				
			}
		};
	}
	
	public static Process ifThenElse(Supplier<Boolean> condition, Process _then, Process _else) {
		return new Process() {
			@Override
			public void run() {
				if (condition.get()) {
					_then.run();
				}
				else {
					_else.run();
				}
			}
		};
	}
	
	public static Process parallel(Process... processes) {
		return new Process() {
						
			@Override
			public void run() {
				
			}
		};
	}
}
