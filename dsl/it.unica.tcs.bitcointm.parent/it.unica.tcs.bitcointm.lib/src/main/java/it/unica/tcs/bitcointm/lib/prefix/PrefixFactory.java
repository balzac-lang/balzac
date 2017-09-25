package it.unica.tcs.bitcointm.lib.prefix;

import static it.unica.tcs.bitcointm.lib.process.ProcessFactory.nil;

import java.util.function.Supplier;

import it.unica.tcs.bitcointm.lib.process.Process;

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

	
	public static Assert createAssert(Supplier<Boolean> condition) {
		return new Assert(condition, nil());
	}
	
	public static Assert createAssert(Supplier<Boolean> condition, Process next) {
		return new Assert(condition, next);
	}
	
	public static Assert createAssert() {
		return new Assert(() ->true, nil());
	}
	
	public static Assert createAssert(Process next) {
		return new Assert(() ->true, next);
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
