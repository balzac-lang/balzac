package it.unica.tcs.lib;

import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;

public abstract class AbstractScriptBuilder<T extends AbstractScriptBuilder<T>> extends ScriptBuilder {

	public AbstractScriptBuilder() {
		super();
	}

	public AbstractScriptBuilder(Script template) {
		super(template);
	}

	@Override
	@SuppressWarnings("unchecked")
	public T data(byte[] data) {
		super.data(data);
		return (T) this;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public T number(long num) {
		super.number(num);
		return (T) this;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public T op(int op) {
		super.op(op);
		return (T) this;
	}
}
