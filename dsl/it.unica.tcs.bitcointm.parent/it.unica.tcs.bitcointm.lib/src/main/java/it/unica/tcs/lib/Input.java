/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.lib;

import java.io.Serializable;

/*
 * Input internal representation (not visible outside)
 */
public class Input implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private static int UNSET_OUTINDEX = -1;
	private static int UNSET_LOCKTIME = -1;
	
	private final ITransactionBuilder parentTx;
	private final int outIndex;
	private final InputScript script;
	private final long locktime;
	
	private Input(ITransactionBuilder parentTx, int outIndex, InputScript script, long locktime) {
		this.parentTx = parentTx;
		this.script = script;
		this.outIndex = outIndex;
		this.locktime = locktime;
	}
	
	static Input of(InputScript script){
		return of(null, UNSET_OUTINDEX, script, UNSET_LOCKTIME);
	}
	
	static Input of(InputScript script, long locktime){
		return of(null, UNSET_OUTINDEX, script, locktime);
	}
	
	static Input of(ITransactionBuilder tx, int index, InputScript script){
		return of(tx, index, script, UNSET_LOCKTIME);
	}
	
	static Input of(ITransactionBuilder tx, int index, InputScript script, long locktime){
		return new Input(tx, index, script, locktime);
	}

	public boolean hasParentTx() {
		return getParentTx()!=null;
	};
	
	public boolean hasLocktime() {
		return locktime!=UNSET_LOCKTIME;
	};
	
	public ITransactionBuilder getParentTx() {
		return parentTx;
	}

	public int getOutIndex() {
		return outIndex;
	}

	public InputScript getScript() {
		return script;
	}

	public long getLocktime() {
		return locktime;
	}
}