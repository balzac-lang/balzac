package it.unica.tcs.lib;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
			
			for (int i : new int[]{0,1,2,3,4,5,6,7,8,9,10,11,12}) {
//				System.out.println("[Alice] sending "+i);
				send(i, bob);
//				System.out.println("[Alice] sent "+i);
				try {
					Thread.sleep(0);
				} catch (InterruptedException e) {}
			}
		}
	}
	
	public static class Bob extends Participant {

		public Bob() {
			super("Bob");
		}

		@Override
		public void run() {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {}
			while (true) {
//				System.out.println("[Bob] receiving");
				Integer i = Integer.valueOf(receive(alice));
//				System.out.println("[Bob] received "+i);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {}
			}
		}
	}

	
	public static void main(String[] args) {
		ExecutorService executor = Executors.newCachedThreadPool();
		executor.execute(alice);
		executor.execute(bob);
	}
}
