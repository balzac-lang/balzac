/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.lib.model;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

public abstract class Participant implements Runnable {
	
	private final String name;
	private final ExecutorService executor;
	private final ServerSocketDaemon receiverDeamon;
	
	public Participant(String name, int port) {
		this.name = name;
		this.executor = Executors.newCachedThreadPool();
		
		try {
			this.receiverDeamon = new ServerSocketDaemon(port);
			this.executor.execute(receiverDeamon);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public String getName() {
		return this.name;
	}
	
	public InetAddress getHost() throws UnknownHostException {
		return InetAddress.getLocalHost();
	}
	
	public int getPort() {
		return receiverDeamon.getPort();
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
	
	public void send(Integer msg, Participant p) {
		send(msg.toString(), p);
	}
	
	public void send(String msg, Participant p) {
		try (
			Socket socket = new Socket(p.getHost(), p.getPort());
			BufferedWriter writer = new BufferedWriter( new OutputStreamWriter(socket.getOutputStream()));
		) {
			writer.write(msg.trim());
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String receive(Participant p) {
		return receiverDeamon.read();
	}
	
}
