/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.lib.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;

public class ServerSocketDaemon implements Runnable {

	private BlockingQueue<String> buffer = new SynchronousQueue<>(true);
	private int port;
	private boolean online;
	
	public static final String STOP = "[stop]";
	public static final String ACK = "[ack]";
	
	public ServerSocketDaemon(int port) {
		this.port = port;
		this.online = false;
	}

	public void waitForOnline() {
		try {
			while(!online) {
				Thread.sleep(500);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		
		try (
			ServerSocket server = new ServerSocket(port);
		) {
			this.port = server.getLocalPort();	// if the port is 0, a free port is assigned by the system 
			this.online = true;

			System.out.println("["+this.toString()+"] daemon started at port "+port);
			
			while (true) {
				
				System.out.println("["+this.toString()+"] opening connection");
				try (
						Socket clientSocket = server.accept();
						BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
						PrintWriter out = new PrintWriter(clientSocket.getOutputStream())
				) {
					System.out.println("["+this.toString()+"] connection "+clientSocket.getPort());
					
					System.out.println("["+this.toString()+"] waiting input");

					String inputLine = in.readLine();
					
					System.out.println("["+this.toString()+"] read: "+inputLine);
					System.out.println("["+this.toString()+"] writing buffer");
					buffer.put(inputLine.toString());
					
					System.out.println("["+this.toString()+"] closing connection "+clientSocket.getPort());
					clientSocket.close();
				} catch (IOException e) {
					// TODO: change to any kind of logger
					System.err.println("Exception caught when trying to listen on port " + server.getLocalPort() + " or listening for a connection");
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
		} catch (IOException e1) {
			throw new RuntimeException(e1);
		}

	}
	
	public String read() {
		try {
			System.out.println("["+this.toString()+"] reading");
			return buffer.take();
		} catch (InterruptedException e) {
			e.printStackTrace();
			return null;
		}
		finally {
			System.out.println("["+this.toString()+"] return");
		}
	}
	
	public int getPort() {
		return port;
	}
}
