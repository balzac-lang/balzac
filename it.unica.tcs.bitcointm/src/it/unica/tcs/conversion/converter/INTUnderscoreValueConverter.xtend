/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.conversion.converter

import org.eclipse.xtext.conversion.ValueConverterException
import org.eclipse.xtext.nodemodel.INode
import org.eclipse.xtext.util.Strings

class LongUnderscoreValueConverter extends LONGValueConverter {

    override Long toValue(String string, INode node) {
        if (Strings.isEmpty(string))
            throw new ValueConverterException("Couldn't convert empty string to an int value.", node, null);
        var withoutUnderscore = string.replace("_", "");
        if (Strings.isEmpty(withoutUnderscore))
            throw new ValueConverterException("Couldn't convert input '" + string + "' to an int value.", node, null);
        return super.toValue(withoutUnderscore, node);
    }
}


