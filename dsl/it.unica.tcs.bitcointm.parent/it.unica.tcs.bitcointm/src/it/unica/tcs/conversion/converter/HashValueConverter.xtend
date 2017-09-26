/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.conversion.converter

import it.unica.tcs.lib.utils.BitcoinJUtils
import org.eclipse.xtext.conversion.ValueConverterException
import org.eclipse.xtext.conversion.impl.AbstractLexerBasedConverter
import org.eclipse.xtext.nodemodel.INode

class HashValueConverter extends AbstractLexerBasedConverter<byte[]> {
		
	override toValue(String string, INode node) throws ValueConverterException {
		
		var prefix = string.substring(0,string.indexOf(':'))
		var value = string.substring(string.indexOf(':')+1)
		
		if (
			prefix=="sha256" && value.length!=64
			|| prefix=="ripemd160" && value.length!=40
			|| prefix=="hash256" && value.length!=64
			|| prefix=="hash160" && value.length!=40
		) {
			throw new ValueConverterException("Invalid length", node, null);
		}
		
		try {
			return BitcoinJUtils.decode(value.toLowerCase)
		}
		catch (Exception e) {
			throw new ValueConverterException("Couldn't convert input '" + value + "' to an int value. \n\nDetails: "+e.message, node, e);
		}
	}
}