/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.lib.process;

import it.unica.tcs.lib.prefix.Prefix;

public class Choice implements Process {

	private final Prefix[] prefixes;
	private final static int POLLING_DELAY = 1000;
	
	public Choice(Prefix... prefixes) {
		this.prefixes = prefixes;
	}

	@Override
	public void run() {
		
		for (int i=0; ; i++) {
			Prefix p = prefixes[i];
			if (p.ready()) {
				p.execute();
				break;
			}
			
			if (i==prefixes.length-1) {
				i=-1;
				silentSleep();
			}
		}
	}

	
	private void silentSleep() {
		try {
			Thread.sleep(POLLING_DELAY);
		} catch (InterruptedException e) {}
	}
}
