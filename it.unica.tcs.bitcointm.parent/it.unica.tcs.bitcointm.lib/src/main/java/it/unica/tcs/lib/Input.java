/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.lib;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.Serializable;
import java.util.function.Supplier;

import it.unica.tcs.lib.script.InputScript;

/*
 * Input internal representation (not visible outside)
 */
public class Input implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private static final int UNSET_OUTINDEX = -1;
	private static final int UNSET_LOCKTIME = -1;
		
	private final Supplier<ITransactionBuilder> parentTx;
	private final int outIndex;
	private final InputScript script;
	private final long locktime;
	
	private Input(Supplier<ITransactionBuilder> parentTx, int outIndex, InputScript script, long locktime) {
		this.parentTx = parentTx;
		this.script = script;
		this.outIndex = outIndex;
		this.locktime = locktime;
	}
	
	static Input of(InputScript script){
		return of((Supplier<ITransactionBuilder>) null, UNSET_OUTINDEX, script, UNSET_LOCKTIME);
	}
	
	static Input of(InputScript script, long locktime){
		return of((Supplier<ITransactionBuilder>) null, UNSET_OUTINDEX, script, locktime);
	}
	
	static Input of(int index, InputScript script){
		return of((Supplier<ITransactionBuilder>) null, index, script, UNSET_LOCKTIME);
	}
	
	static Input of(Supplier<ITransactionBuilder> tx, int index, InputScript script){
		return of(tx, index, script, UNSET_LOCKTIME);
	}
	
	static Input of(ITransactionBuilder tx, int index, InputScript script){
		return of(()->tx, index, script, UNSET_LOCKTIME);
	}
	
	static Input of(ITransactionBuilder tx, int index, InputScript script, long locktime){
		return of(()->tx, index, script, locktime);
	}
	
	static Input of(Supplier<ITransactionBuilder> tx, int index, InputScript script, long locktime){
		checkNotNull(script);
		return new Input(tx, index, script, locktime);
	}

	public boolean hasParentTx() {
		return getParentTx()!=null;
	};
	
	public boolean hasLocktime() {
		return locktime!=UNSET_LOCKTIME;
	};
	
	public Supplier<ITransactionBuilder> getParentTx() {
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (locktime ^ (locktime >>> 32));
		result = prime * result + outIndex;
		result = prime * result + ((parentTx == null) ? 0 : parentTx.hashCode());
		result = prime * result + ((script == null) ? 0 : script.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Input other = (Input) obj;
		if (locktime != other.locktime)
			return false;
		if (outIndex != other.outIndex)
			return false;
		if (parentTx == null) {
			if (other.parentTx != null)
				return false;
		} else if (!parentTx.equals(other.parentTx))
			return false;
		if (script == null) {
			if (other.script != null)
				return false;
		} else if (!script.equals(other.script))
			return false;
		return true;
	}
	
}