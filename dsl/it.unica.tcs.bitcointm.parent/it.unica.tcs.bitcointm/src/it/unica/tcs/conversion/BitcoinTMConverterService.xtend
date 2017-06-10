package it.unica.tcs.conversion

import com.google.inject.Inject
import it.unica.tcs.conversion.converter.HashValueConverter
import it.unica.tcs.conversion.converter.NumberValueConverter
import org.eclipse.xtext.common.services.DefaultTerminalConverters
import org.eclipse.xtext.conversion.IValueConverter
import org.eclipse.xtext.conversion.ValueConverter

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
	
}