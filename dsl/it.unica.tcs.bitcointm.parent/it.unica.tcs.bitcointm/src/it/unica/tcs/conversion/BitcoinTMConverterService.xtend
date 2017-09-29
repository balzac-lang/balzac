/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.conversion

import com.google.inject.Inject
import it.unica.tcs.conversion.converter.DelayValueConverter
import it.unica.tcs.conversion.converter.HashValueConverter
import it.unica.tcs.conversion.converter.IntUnderscoreValueConverter
import it.unica.tcs.conversion.converter.NumberValueConverter
import it.unica.tcs.conversion.converter.TimestampValueConverter
import org.eclipse.xtext.common.services.DefaultTerminalConverters
import org.eclipse.xtext.conversion.IValueConverter
import org.eclipse.xtext.conversion.ValueConverter
import org.eclipse.xtext.conversion.impl.AbstractLexerBasedConverter
import org.eclipse.xtext.nodemodel.INode
import org.eclipse.xtext.conversion.ValueConverterException

class BitcoinTMConverterService extends DefaultTerminalConverters {
	
	@Inject private NumberValueConverter numberValueConverter;
	@Inject private HashValueConverter customTerminalConverter;
	@Inject private TimestampValueConverter timestampTerminalConverter;
	@Inject private IntUnderscoreValueConverter intTerminalConverter;
	@Inject private DelayValueConverter delayConverter;
		
	@ValueConverter(rule = "Number")
    def IValueConverter<Integer> getNumberConverter() {
        return numberValueConverter
	}
	
	@ValueConverter(rule = "BTC_HASH")
    def IValueConverter<byte[]> getSha256Converter() {
        return customTerminalConverter
	}

	@ValueConverter(rule = "TIMESTAMP")
    def IValueConverter<Integer> getTimestampConverter() {
        return timestampTerminalConverter
	}	

	@ValueConverter(rule = "INT")
    def IValueConverter<Integer> getIntConverter() {
        return intTerminalConverter
	}
		
	@ValueConverter(rule = "Delay")
    def IValueConverter<Integer> getDelay() {
        return delayConverter
	}
	
	@ValueConverter(rule = "TXID")
    def IValueConverter<String> getTXID() {
        return new AbstractLexerBasedConverter<String>() {
			override toValue(String string, INode node) throws ValueConverterException {
				string.substring(5)
			}
        }
	}
	
	@ValueConverter(rule = "TXSERIAL")
    def IValueConverter<String> getHEX() {
        return new AbstractLexerBasedConverter<String>() {
			override toValue(String string, INode node) throws ValueConverterException {
				string.substring(4)
			}
        }
	}	
	
}