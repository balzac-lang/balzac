/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.conversion.converter

import org.eclipse.xtext.conversion.ValueConverterException
import org.eclipse.xtext.conversion.impl.AbstractLexerBasedConverter
import org.eclipse.xtext.nodemodel.INode
import org.eclipse.xtext.util.Strings

class INTHEXValueConverter extends AbstractLexerBasedConverter<Integer> {

    override String toEscapedString(Integer value) {
        return Integer.toString(value, 16);
    }

    override void assertValidValue(Integer value) {
        super.assertValidValue(value);
        if (value < 0)
            throw new ValueConverterException(getRuleName() + "-value may not be negative (value: " + value + ").",
                null, null);
    }

    override Integer toValue(String string, INode node) {
        if (Strings.isEmpty(string))
            throw new ValueConverterException("Couldn't convert empty string to an int value.", node, null);
        try {
            var intValue = Integer.parseInt(string, 16);
            return Integer.valueOf(intValue);
        } catch (NumberFormatException e) {
            throw new ValueConverterException("Couldn't convert '" + string + "' to an int value.", node, e);
        }
    }

}
