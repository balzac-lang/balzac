package it.unica.tcs.lib.client;

public enum Confidentiality {

    LOW(1),
    MEDIUM(3),
    HIGH(6);
    
    private final int confirmations;
    
    private Confidentiality(int confirmations) {
        this.confirmations = confirmations;
    }

    public int getConfirmations() {
        return confirmations;
    }
}
