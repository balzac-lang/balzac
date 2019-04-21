/*
 * Copyright 2017 Nicola Atzei
 */

package xyz.balzaclang.conversion.converter

import com.google.inject.Inject
import xyz.balzaclang.conversion.converter.longs.LONGHEXValueConverter
import xyz.balzaclang.conversion.converter.longs.LongUnderscoreValueConverter
import org.eclipse.xtext.conversion.ValueConverterException
import org.eclipse.xtext.conversion.impl.AbstractValueConverter
import org.eclipse.xtext.nodemodel.INode

class NumberValueConverter extends AbstractValueConverter<Long> {

    @Inject LongUnderscoreValueConverter intConverter
    @Inject LONGHEXValueConverter hexConverter

    override Long toValue(String string, INode node) {
        if (string.startsWith("0x") || string.startsWith("0X")) {
            return hexConverter.toValue(string.substring(2), node)
        }
        else {
            return intConverter.toValue(string, node)
        }
    }

    override toString(Long value) throws ValueConverterException {
        return value.toString
    }

}
