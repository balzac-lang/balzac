/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.lib.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

public class ServerSocketDaemon implements Runnable {

	private List<String> buffer = new LinkedList<>();
	private final static int DELAY = 500;
	private int port;
	
	public ServerSocketDaemon(int port) throws IOException {
		this.port = port; 
	}

	@Override
	public void run() {
		
		try (
			ServerSocket server = new ServerSocket(port);
		) {
			
			while (true) {
				
				try (
						Socket clientSocket = server.accept();
						BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))
				) {
					
					StringBuilder sb = new StringBuilder();
					String inputLine;
					
					while ((inputLine = in.readLine()) != null) {
						sb.append(inputLine).append("\n");
						System.out.println("[daemon] read: "+inputLine);
					}
					
					buffer.add(sb.toString().trim());
					
				} catch (IOException e) {
					// TODO: change to any kind of logger
					System.err.println("Exception caught when trying to listen on port " + server.getLocalPort() + " or listening for a connection");
					e.printStackTrace();
				}
			}
			
		} catch (IOException e1) {
			throw new RuntimeException(e1);
		}

	}
	
	public String read() {
		while(buffer.size()==0) 
			silentSleep();
		return buffer.remove(0);
	}
	
	private void silentSleep() {
		try {
			Thread.sleep(DELAY);
		} catch (InterruptedException e) {}
	}

	public int getPort() {
		return port;
	}
}
