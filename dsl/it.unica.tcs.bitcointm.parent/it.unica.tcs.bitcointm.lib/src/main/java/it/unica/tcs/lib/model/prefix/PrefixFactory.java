/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.lib.model.prefix;

import static it.unica.tcs.lib.model.process.ProcessFactory.nil;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import it.unica.tcs.lib.model.process.Process;

public class PrefixFactory {

	public static Prefix tau() {
		return check();
	}
	
	public static Prefix tau(Process next) {
		return check(next);
	}

	public static Prefix ask(String... txsid) {
		return ask(txsid, nil());
	}
	
	public static Prefix ask(List<String> txsid) {
		return ask(txsid, nil());
	}
	
	public static Prefix ask(String[] txsid, Process next) {
		return ask(Arrays.asList(txsid), next);
	}
	
	public static Prefix ask(List<String> txsid, Process next) {
		return new Ask(txsid, next);
	}

	public static Prefix check(Supplier<Boolean> condition) {
		return check(condition, nil());
	}
	
	public static Prefix check(Supplier<Boolean> condition, Process next) {
		return new Check(condition, next);
	}
	
	public static Prefix check() {
		return check(() ->true, nil());
	}
	
	public static Prefix check(Process next) {
		return check(() ->true, next);
	}
	
	public static Prefix send() {
		return send(nil());
	}
	
	public static Prefix send(Process next) {
		return new Send(next);
	}
	
	public static Prefix receive() {
		return receive(nil());
	}
	
	public static Prefix receive(Process next) {
		return new Receive(next);
	}
}
