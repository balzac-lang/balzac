/**
 * 
 */
package it.unica.tcs.xsemantics;

import it.unica.tcs.bitcoinTM.BitcoinTMFactory;
import it.unica.tcs.bitcoinTM.TypeVariable;

/**
 * @author bettini
 * 
 */
public class LambdaUtils {

	protected int counter = 0;

	public void resetCounter() {
		counter = 0;
	}

	public TypeVariable createTypeVariable(String name) {
		TypeVariable typeVariable = BitcoinTMFactory.eINSTANCE
				.createTypeVariable();
		typeVariable.setTypevarName(name);
		return typeVariable;
	}

	public TypeVariable createFreshTypeVariable() {
		return createTypeVariable("X" + counter++);
	}

}
