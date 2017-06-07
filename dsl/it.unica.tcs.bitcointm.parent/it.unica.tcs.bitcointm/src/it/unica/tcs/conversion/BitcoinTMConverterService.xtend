package it.unica.tcs.conversion

import com.google.inject.Inject
import org.eclipse.xtext.common.services.DefaultTerminalConverters
import org.eclipse.xtext.conversion.IValueConverter
import org.eclipse.xtext.conversion.ValueConverter
import org.eclipse.xtext.conversion.ValueConverterException
import org.eclipse.xtext.conversion.impl.INTValueConverter
import org.eclipse.xtext.nodemodel.INode
import org.eclipse.xtext.util.Strings

class BitcoinTMConverterService extends DefaultTerminalConverters {
	
	@Inject private BTCValueConverter btcValueConverter;
	
	@ValueConverter(rule = "Number")
    def IValueConverter<Integer> getNumberConverter() {
        return btcValueConverter
	}
	
	public static class IntUnderscoreValueConverter extends INTValueConverter {
		override Integer toValue(String string, INode node) {
			if (Strings.isEmpty(string))
				throw new ValueConverterException("Couldn't convert empty string to an int value.", node, null);
			var withoutUnderscore = string.replace("_", "");
			if (Strings.isEmpty(withoutUnderscore))
				throw new ValueConverterException("Couldn't convert input '" + string + "' to an int value.", node, null);
			return super.toValue(withoutUnderscore, node);
		}
	}
	
	public static class BTCValueConverter extends IntUnderscoreValueConverter {

		override Integer toValue(String _string, INode node) {
			val ONE_BTC_SATOSHI = 100_000_000
			val isBTC = _string.contains("BTC");
			var string = _string.replaceAll("\\s*BTC","")
			var stringArray = string.split("\\.")
			
			if (stringArray.length==1) {
				// simple integer
				if (isBTC) {
					return super.toValue(string, node) * ONE_BTC_SATOSHI;
				}
				else {
					return super.toValue(string, node)
				}
			}
			else if (stringArray.length==2) {
				// decimal number
				
				if (!isBTC)
					throw new ValueConverterException("Decimal values are not permitted, except when using the keyword 'BTC'.", node, null);
					
				var integerPart = stringArray.get(0)
				var decimalPart = stringArray.get(1)
				
				// remove trailing zeros
				decimalPart = decimalPart.replaceAll("0*$", "");
								
				if (decimalPart.length>8)
					throw new ValueConverterException("Couldn't convert input '" + string + "' to an int value. The decimal part is less than 10^-8", node, null);
				
				// 0 right padding
				decimalPart = String.format("%-8s", decimalPart ).replace(' ', '0');
				
				return super.toValue(integerPart, node) * ONE_BTC_SATOSHI + super.toValue(decimalPart, node)
			}
			else 			
				throw new ValueConverterException("Couldn't convert input '" + string + "' to an int value.", node, null);
		}
	}
}