/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.lib.model;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

public abstract class Participant implements Runnable {
	
	private final String name;
	private final ExecutorService executor;
//	private final ServerSocket serverSocket;
	
	public Participant(String name) {
		this.name = name;
		this.executor = Executors.newCachedThreadPool();
//		this.serverSocket = new ServerSocket(port);
	}
	
	public String getName() {
		return this.name;
	}

	abstract public void run();
	
	public void parallel(Process... processes) {
		for (Process process : processes)
			executor.execute(process);
	}
	
	public Process ask(String... txsid) {
		return ProcessFactory.choice(PrefixFactory.ask(txsid));
	}
	
	public Process ask(List<String> txsid) {
		return ProcessFactory.choice(PrefixFactory.ask(txsid));
	}
	
	public Process check(Supplier<Boolean> condition) {
		return ProcessFactory.choice(PrefixFactory.check(condition));
	}
	
	public Process check() {
		return ProcessFactory.choice(PrefixFactory.check());
	}
	
	public Process put(String txhex) {
		return ProcessFactory.choice(PrefixFactory.put(txhex));
	}
	
	public void send(Object msg, Participant p) {
		
	}
	
	public Object receive(Participant p) {
		return null;
	}
	
}
