/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.lib.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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
	
	public Participant(String name) {
		this(name, 0);
	}
	
	public Participant(String name, int port) {
		this.name = name;
		this.executor = Executors.newCachedThreadPool();
		this.receiverDeamon = new ServerSocketDaemon(port);
		this.executor.execute(receiverDeamon);
		this.receiverDeamon.waitForOnline();
		System.out.println("["+this.toString()+"] Listening on port "+this.getPort());
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
			BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			PrintWriter writer = new PrintWriter(socket.getOutputStream());
		) {
			socket.setKeepAlive(true);
			System.out.println("["+this.toString()+"] connected to port "+socket.getPort());
			System.out.println("["+this.toString()+"] local port "+socket.getLocalPort());
			System.out.println("["+this.toString()+"] isConnected "+socket.isConnected());
			System.out.println("["+this.toString()+"] keepalive "+socket.getKeepAlive());
			System.out.println("["+this.toString()+"] isBound "+socket.isBound());
			System.out.println("["+this.toString()+"] isClosed "+socket.isClosed());
			System.out.println("["+this.toString()+"] toString "+socket.toString());
			System.out.println("["+this.toString()+"] sending "+msg);
			writer.println(msg.trim());
			writer.flush();
			System.out.println("["+this.toString()+"] waiting the server to close the connection");
			reader.readLine();
		} catch (UnknownHostException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} 
	}
	
	public String receive(Participant p) {
		return receiverDeamon.read();
	}
	
}
