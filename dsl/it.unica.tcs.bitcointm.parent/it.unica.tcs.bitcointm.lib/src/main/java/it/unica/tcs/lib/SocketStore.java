/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.lib;

import static com.google.common.base.Preconditions.checkState;

import java.io.IOException;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SocketStore {

	private static final SocketStore instance = new SocketStore();
	private SocketStore() {}

	private final Map<String, Socket> store = new ConcurrentHashMap<>();
	
	public static SocketStore getInstance() {
		return instance;
	}
	
	public void addParticipant(String participant, String host, int port) {
		
		try {
			store.put(participant, new Socket(host, port));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Socket getKey(String participant) {
		checkState(store.containsKey(participant));
		return store.get(participant);
	}
	
	public void clear() {
		store.clear();
	}
}
