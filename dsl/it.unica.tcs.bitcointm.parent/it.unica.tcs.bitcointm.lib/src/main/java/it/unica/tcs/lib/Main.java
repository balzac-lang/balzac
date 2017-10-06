package it.unica.tcs.lib;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import it.unica.tcs.lib.model.Participant;

public class Main {

	private static Alice alice = new Alice();
	private static Bob bob = new Bob();
	
	public static class Alice extends Participant {

		public Alice() {
			super("Alice");
		}

		@Override
		public void run() {
			int i=0;
			while (i++<5) {
				send(i, bob);
			}
			try {
				stop();
			} catch (InterruptedException e) {}
			System.out.println("Alice END");
		}
	}
	
	public static class Bob extends Participant {

		public Bob() {
			super("Bob");
		}

		@Override
		public void run() {
			int j=0;
			while (j++<5) {
				receive(alice);
			}
			try {
				stop();
			} catch (InterruptedException e) {}
			System.out.println("Bob END");
		}
	}

	
	public static void main(String[] args) throws InterruptedException {
		
        ClassLoader cl = ClassLoader.getSystemClassLoader();

        URL[] urls = ((URLClassLoader)cl).getURLs();

        for(URL url: urls){
        	System.out.println(url.getFile());
        }
		ExecutorService executor = Executors.newCachedThreadPool();
		executor.execute(alice);
		executor.execute(bob);
		System.out.println("awaiting termination");
		executor.shutdown();
		executor.awaitTermination(1, TimeUnit.DAYS);
		System.out.println("END");
	}
}
