/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.lib.model.prefix;

import static com.google.common.base.Preconditions.checkState;

import java.util.List;

import com.google.inject.Inject;

import it.unica.tcs.lib.client.BitcoinClientI;
import it.unica.tcs.lib.model.process.Process;

public class Ask extends AbstractPrefix {

	@Inject private BitcoinClientI client;
	private final List<String> txsid;
	
	Ask(List<String> txsid, Process next) {
		super(next);
		this.txsid = txsid;
	}
	
	@Override
	public boolean ready() {
		return txsid.stream().allMatch((tx) -> client.isMined(tx));
	}

	@Override
	public void execute() {
		checkState(ready());
		next.run();
	}

	@Override
	public String toString(){
		return "ask {}";
	}
}
