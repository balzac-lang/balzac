package it.unica.tcs.lib.client;

public interface BitcoinClientI {

	public int getBlockCount();
	
	public String getRawTransaction(String txid) throws TransactionNotFoundException;
	
	public boolean isMined(String txid);

	public boolean isMined(String txid, Reliability reliability);
	
	public String sendTransaction(String transaction);
}
