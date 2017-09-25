/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.bitcointm.lib;

public class Alice extends Participant {

	public Alice() {
		super(Alice.class.getName());
	}

	@Override
	public void start() {
		
		parallel(() -> {
			
		});
	}
}
