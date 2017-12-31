package it.unica.tcs.lib.client;

public interface BitcoinClientI {

    public int getBlockCount();
    
    public String getRawTransaction(String txid) throws TransactionNotFoundException;
    
    public boolean isMined(String txid);

    public boolean isMined(String txid, Confidentiality reliability);
    
    public String sendRawTransaction(String transaction);
    
    public boolean isUTXO(String txid);
    
    public boolean isUTXO(String txid, int n);
}
