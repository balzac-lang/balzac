/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.lib.model.prefix;

import static it.unica.tcs.lib.model.process.ProcessFactory.nil;

import java.util.function.Supplier;

import it.unica.tcs.lib.model.process.Process;

public class PrefixFactory {

	public static Prefix createTau() {
		return createAssert();
	}
	
	public static Prefix createTau(Process next) {
		return createAssert(next);
	}

	public static Prefix createAsk() {
		return createAssert();
	}
	
	public static Prefix createAsk(Process next) {
		return createAssert(next);
	}

	
	public static Check createAssert(Supplier<Boolean> condition) {
		return new Check(condition, nil());
	}
	
	public static Check createAssert(Supplier<Boolean> condition, Process next) {
		return new Check(condition, next);
	}
	
	public static Check createAssert() {
		return new Check(() ->true, nil());
	}
	
	public static Check createAssert(Process next) {
		return new Check(() ->true, next);
	}
	
	public static Send createSend() {
		return new Send(nil());
	}
	
	public static Send createSend(Process next) {
		return new Send(next);
	}
	
	public static Receive createReceive() {
		return new Receive(nil());
	}
	
	public static Receive createReceive(Process next) {
		return new Receive(next);
	}
}
