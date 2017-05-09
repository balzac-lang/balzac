package it.unica.tcs.validation;

import org.eclipse.xtext.validation.Check;

import it.unica.tcs.bitcoinTM.BitcoinTMPackage;
import it.unica.tcs.bitcoinTM.KeyDeclaration;

public class BitcoinJValidator extends AbstractBitcoinTMValidator{

	
	@Check
	public void checkKey(KeyDeclaration key) {
		info("Just a test.", 
			BitcoinTMPackage.Literals.KEY_DECLARATION__BODY
		);
	}
}
