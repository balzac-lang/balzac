/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.conversion.converter

import org.eclipse.xtext.conversion.ValueConverterException
import org.eclipse.xtext.conversion.impl.AbstractLexerBasedConverter
import org.eclipse.xtext.nodemodel.INode
import org.eclipse.xtext.util.Strings

class LONGValueConverter extends AbstractLexerBasedConverter<Long> {

	override String toEscapedString(Long value) {
		return value.toString();
	}
	
	override void assertValidValue(Long value) {
		super.assertValidValue(value);
		if (value < 0)
			throw new ValueConverterException(getRuleName() + "-value may not be negative (value: " + value + ").", null, null);
	}
	
	override Long toValue(String string, INode node) {
		if (Strings.isEmpty(string))
			throw new ValueConverterException("Couldn't convert empty string to an int value.", node, null);
		try {
			return Long.parseLong(string, 10);
		} catch (NumberFormatException e) {
			throw new ValueConverterException("Couldn't convert '" + string + "' to an int value.", node, e);
		}
	}

}