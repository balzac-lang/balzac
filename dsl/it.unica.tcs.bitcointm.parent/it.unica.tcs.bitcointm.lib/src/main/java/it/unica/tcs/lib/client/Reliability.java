package it.unica.tcs.lib.client;

public enum Reliability {

	LOW(1),
	MEDIUM(3),
	HIGH(6);
	
	private final int confirmations;
	
	private Reliability(int confirmations) {
		this.confirmations = confirmations;
	}

	public int getConfirmations() {
		return confirmations;
	}
}
