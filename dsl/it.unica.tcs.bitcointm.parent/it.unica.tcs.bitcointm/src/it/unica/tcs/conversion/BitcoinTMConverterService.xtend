package it.unica.tcs.conversion

import com.google.common.io.BaseEncoding.DecodingException
import com.google.inject.Inject
import org.bitcoinj.core.Utils
import org.eclipse.xtext.common.services.DefaultTerminalConverters
import org.eclipse.xtext.conversion.IValueConverter
import org.eclipse.xtext.conversion.ValueConverter
import org.eclipse.xtext.conversion.ValueConverterException
import org.eclipse.xtext.conversion.impl.AbstractLexerBasedConverter
import org.eclipse.xtext.conversion.impl.INTValueConverter
import org.eclipse.xtext.nodemodel.INode
import org.eclipse.xtext.util.Strings

class BitcoinTMConverterService extends DefaultTerminalConverters {
	
	@Inject private NumberValueConverter numberValueConverter;
	@Inject private HashValueConverter customTerminalConverter;
	
	@ValueConverter(rule = "Number")
    def IValueConverter<Integer> getNumberConverter() {
        return numberValueConverter
	}
	
//	@ValueConverter(rule = "BASE58")
//    def IValueConverter<byte[]> getbase58Converter() {
//        return customTerminalConverter
//	}
	
	@ValueConverter(rule = "BTC_HASH")
    def IValueConverter<byte[]> getSha256Converter() {
        return customTerminalConverter
	}
	
	
	
	/*
	 * 
	 *	Move classes below in separated files 
	 * 
	 */
	 
	 
	public static class HashValueConverter extends AbstractLexerBasedConverter<byte[]> {
		
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
				return Utils.HEX.decode(value.toLowerCase)
			}
			catch (IllegalArgumentException e) {
				throw new ValueConverterException("Couldn't convert input '" + value + "' to an int value. \n\nDetails: "+e.cause.message, node, e);
			}
			catch (DecodingException e) {
				throw new ValueConverterException("Couldn't convert input '" + value + "' to an int value. \n\nDetails: "+e.message, node, e);
			}
		}
	}
	
	public static class HEXValueConverter extends AbstractLexerBasedConverter<Integer> {

		override String toEscapedString(Integer value) {
			return Integer.toString(value, 16);
		}
		
		override void assertValidValue(Integer value) {
			super.assertValidValue(value);
			if (value < 0)
				throw new ValueConverterException(getRuleName() + "-value may not be negative (value: " + value + ").", null, null);
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
	
	public static class HexUnderscoreValueConverter extends HEXValueConverter {
		
		override Integer toValue(String string, INode node) {
			if (Strings.isEmpty(string))
				throw new ValueConverterException("Couldn't convert empty string to an int value.", node, null);
			var withoutUnderscore = string.replace("_", "");
			if (Strings.isEmpty(withoutUnderscore))
				throw new ValueConverterException("Couldn't convert input '" + string + "' to an int value.", node, null);
			return super.toValue(withoutUnderscore, node);
		}
	}
	
	public static class NumberValueConverter extends AbstractLexerBasedConverter<Integer> {

		@Inject IntUnderscoreValueConverter intConverter
		@Inject HexUnderscoreValueConverter hexConverter

		override Integer toValue(String _string, INode node) {
			val ONE_BTC_SATOSHI = 100_000_000
			val isBTC = _string.contains("BTC");
			var string = _string.replaceAll("\\s*BTC","")
			var stringArray = string.split("\\.")
			
			if (stringArray.length==1) {
				
				// hex integer
				if (string.startsWith("0x")) {
					return hexConverter.toValue(string, node)
				}
				else // simple integer
					if (isBTC) {
						return intConverter.toValue(string, node) * ONE_BTC_SATOSHI;
					}
					else {
						return intConverter.toValue(string, node)
					}
			}
			else if (stringArray.length==2) {
				// decimal number
				
				if (!isBTC)
					throw new ValueConverterException("Decimal values are not permitted, except when followed by the keyword 'BTC'.", node, null);
					
				var integerPart = stringArray.get(0)
				var decimalPart = stringArray.get(1)
				
				// remove trailing zeros
				decimalPart = decimalPart.replaceAll("0*$", "");
								
				if (decimalPart.length>8)
					throw new ValueConverterException("Couldn't convert input '" + string + "' to an int value. The decimal part is less than 10^-8", node, null);
				
				// 0 right padding
				decimalPart = String.format("%-8s", decimalPart ).replace(' ', '0');
				
				return intConverter.toValue(integerPart, node) * ONE_BTC_SATOSHI + intConverter.toValue(decimalPart, node)
			}
			else 			
				throw new ValueConverterException("Couldn't convert input '" + string + "' to an int value.", node, null);
		}
	}
}