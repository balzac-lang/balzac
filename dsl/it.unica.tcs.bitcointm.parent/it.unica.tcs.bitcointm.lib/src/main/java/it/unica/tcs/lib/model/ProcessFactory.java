/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.lib.model;

import static com.google.common.base.Preconditions.checkState;

public class ProcessFactory {

	public static Process choice(Prefix... prefixes) {
		checkState(prefixes.length>0);
		return new Choice(prefixes);
	}
}
