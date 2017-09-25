package it.unica.tcs.bitcointm.lib;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class Participant {

	private final String name;
	private final ExecutorService executor;
	
	public Participant(String name) {
		this.name = name;
		this.executor = Executors.newCachedThreadPool();
	}
	
	public String getName() {
		return this.name;
	}

	abstract public void start();
	
	public void parallel(Runnable process) {
		executor.execute(process);
	}
}
