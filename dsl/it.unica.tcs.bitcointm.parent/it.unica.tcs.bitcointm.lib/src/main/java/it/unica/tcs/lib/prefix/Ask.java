/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.lib.prefix;

import java.util.List;

import it.unica.tcs.lib.TransactionBuilder;
import it.unica.tcs.lib.process.Process;

public class Ask extends Assert {

	Ask(List<TransactionBuilder> txs, Process next) {
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
