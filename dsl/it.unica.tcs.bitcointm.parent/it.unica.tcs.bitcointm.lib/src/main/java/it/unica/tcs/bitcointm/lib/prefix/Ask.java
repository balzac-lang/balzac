package it.unica.tcs.bitcointm.lib.prefix;

import java.util.List;

import it.unica.tcs.bitcointm.lib.TransactionBuilder;
import it.unica.tcs.bitcointm.lib.process.Process;

public class Ask extends Assert {

	public Ask(List<TransactionBuilder> txs, Process next) {
		super(()->{
			
			return false;
		}, 
		next);
	}

	@Override
	public String toString(){
		return "ask {}";
	}
}
