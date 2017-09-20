package it.unica.tcs.bitcointm.lib.prefix;

import java.util.function.Supplier;

import it.unica.tcs.bitcointm.lib.process.Process;

public class Assert extends AbstractPrefix {

	private final static long POLLING_MSEC = 1_000;
	private final Supplier<Boolean> condition;
	
	public Assert(Supplier<Boolean> condition, Process next) {
		super(next);
		this.condition = condition;
	}

	@Override
	public void execute() {

		while(!condition.get()) {
			try {
				Thread.sleep(POLLING_MSEC);
			} catch (InterruptedException e) {}
		}
	}

	@Override
	public String toString(){
		return "assert <e>";
	}
}
