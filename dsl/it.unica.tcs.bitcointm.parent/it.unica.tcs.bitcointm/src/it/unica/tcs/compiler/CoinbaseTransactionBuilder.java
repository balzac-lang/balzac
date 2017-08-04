package it.unica.tcs.compiler;

public class CoinbaseTransactionBuilder extends TransactionBuilder {

	@Override
	public TransactionBuilder addInput(ScriptBuilder2 inputScript) {
		return super.addInput(inputScript);
	}

	@Override
	public TransactionBuilder addInput(ITransactionBuilder tx, int outIndex, ScriptBuilder2 inputScript) {
		throw new UnsupportedOperationException();	
	}

	@Override
	public TransactionBuilder addInput(ITransactionBuilder tx, int outIndex, ScriptBuilder2 inputScript, long locktime) {
		throw new UnsupportedOperationException();	
	}
}
