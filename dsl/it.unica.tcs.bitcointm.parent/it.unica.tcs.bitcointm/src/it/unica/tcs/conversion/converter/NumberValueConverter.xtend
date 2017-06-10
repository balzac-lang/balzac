package it.unica.tcs.conversion.converter

import com.google.inject.Inject
import org.eclipse.xtext.conversion.ValueConverterException
import org.eclipse.xtext.conversion.impl.AbstractLexerBasedConverter
import org.eclipse.xtext.nodemodel.INode

class NumberValueConverter extends AbstractLexerBasedConverter<Integer> {

	@Inject IntUnderscoreValueConverter intConverter
	@Inject HexUnderscoreValueConverter hexConverter

	override Integer toValue(String _string, INode node) {
		val ONE_BTC_SATOSHI = 100_000_000
		val isBTC = _string.contains("BTC");
		var string = _string.replaceAll("\\s*BTC", "")
		var stringArray = string.split("\\.")

		if (stringArray.length == 1) {

			// hex integer
			if (string.startsWith("0x")) {
				return hexConverter.toValue(string, node)
			} else // simple integer
			if (isBTC) {
				return intConverter.toValue(string, node) * ONE_BTC_SATOSHI;
			} else {
				return intConverter.toValue(string, node)
			}
		} else if (stringArray.length == 2) {
			// decimal number
			if (!isBTC)
				throw new ValueConverterException(
					"Decimal values are not permitted, except when followed by the keyword 'BTC'.", node, null);

			var integerPart = stringArray.get(0)
			var decimalPart = stringArray.get(1)

			// remove trailing zeros
			decimalPart = decimalPart.replaceAll("0*$", "");

			if (decimalPart.length > 8)
				throw new ValueConverterException("Couldn't convert input '" + string +
					"' to an int value. The decimal part is less than 10^-8", node, null);

			// 0 right padding
			decimalPart = String.format("%-8s", decimalPart).replace(' ', '0');

			return intConverter.toValue(integerPart, node) * ONE_BTC_SATOSHI + intConverter.toValue(decimalPart, node)
		} else
			throw new ValueConverterException("Couldn't convert input '" + string + "' to an int value.", node, null);
	}
}
