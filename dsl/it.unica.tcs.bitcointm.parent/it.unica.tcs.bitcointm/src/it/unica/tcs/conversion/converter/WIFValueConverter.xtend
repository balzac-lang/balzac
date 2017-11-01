/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.conversion.converter

import org.bitcoinj.core.DumpedPrivateKey
import org.eclipse.xtext.conversion.ValueConverterException
import org.eclipse.xtext.conversion.impl.AbstractLexerBasedConverter
import org.eclipse.xtext.nodemodel.INode

class WIFValueConverter extends AbstractLexerBasedConverter<String> {
	
	override toValue(String string, INode node) throws ValueConverterException {
		
		var value = string.substring(string.indexOf(':')+1)
		
		try {
			DumpedPrivateKey.fromBase58(null, value);	// it ignores the network byte
			return value;
		}
		catch (Exception e) {
			throw new ValueConverterException("Couldn't convert input '" + value + "' to a valid private key. \n\nDetails: "+e.message, node, e);
		}
	}
}