/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.conversion.converter

import com.google.inject.Inject
import java.util.regex.Matcher
import java.util.regex.Pattern
import org.eclipse.xtext.conversion.ValueConverterException
import org.eclipse.xtext.conversion.impl.AbstractLexerBasedConverter
import org.eclipse.xtext.nodemodel.INode

class NumberValueConverter extends AbstractLexerBasedConverter<Integer> {

	@Inject IntUnderscoreValueConverter intConverter
	@Inject IntHexUnderscoreValueConverter hexConverter

	val patternS = 
			"((?<intpart>\\d([\\d_]*[\\d]+)?)(\\.(?<decpart>\\d([\\d_]*[\\d]+)?))?(\\s*(?<btcpart>BTC)?))"
			+ "|"
			+ "(?<hexpart>(0x|0X)[\\dA-Fa-f][\\dA-Fa-f_]*[\\dA-Fa-f]+)";
	
	val Pattern pattern = Pattern.compile(patternS);
	val ONE_BTC_SATOSHI = 100_000_000

	override Integer toValue(String string, INode node) {
		
		val Matcher matcher = pattern.matcher(string);
		
		if (matcher.matches()) {
			
			val isBTC = matcher.group("btcpart")!==null
			val intpart = matcher.group("intpart")
			var decpart = matcher.group("decpart")
			val hexpart = matcher.group("hexpart")
			
			if (hexpart!==null)
				return hexConverter.toValue(hexpart, node)
			
			if (decpart===null) {
				if (isBTC) { 
					return intConverter.toValue(intpart, node) * ONE_BTC_SATOSHI;
				}
				else 
					return intConverter.toValue(intpart, node)
			}
			else {
				if (!isBTC)
					throw new ValueConverterException("Decimal values are not permitted, except when followed by the keyword 'BTC'.", node, null);
				
				// remove trailing zeros
				decpart = decpart.replaceAll("0*$", "");
	
				if (decpart.length > 8)
					throw new ValueConverterException("Couldn't convert input '" + string + "' to an int value. The decimal part is less than 10^-8", node, null);
	
				// 0 right padding
				decpart = String.format("%-8s", decpart).replace(' ', '0');
	
				return intConverter.toValue(intpart, node) * ONE_BTC_SATOSHI + intConverter.toValue(decpart, node)
			}	
		}
		else {
			throw new ValueConverterException("Couldn't convert input '" + string + "' to an int value.", node, null);
		}
	}
}
