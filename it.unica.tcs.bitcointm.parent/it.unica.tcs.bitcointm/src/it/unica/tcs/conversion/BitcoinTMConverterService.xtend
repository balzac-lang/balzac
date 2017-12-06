/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.conversion

import com.google.inject.Inject
import it.unica.tcs.conversion.converter.DelayValueConverter
import it.unica.tcs.conversion.converter.HashValueConverter
import it.unica.tcs.conversion.converter.LongUnderscoreValueConverter
import it.unica.tcs.conversion.converter.NumberValueConverter
import it.unica.tcs.conversion.converter.TimestampValueConverter
import it.unica.tcs.conversion.converter.WIFValueConverter
import it.unica.tcs.lib.client.BitcoinClientI
import org.eclipse.xtext.common.services.DefaultTerminalConverters
import org.eclipse.xtext.conversion.IValueConverter
import org.eclipse.xtext.conversion.ValueConverter
import org.eclipse.xtext.conversion.ValueConverterException
import org.eclipse.xtext.conversion.impl.AbstractLexerBasedConverter
import org.eclipse.xtext.nodemodel.INode

class BitcoinTMConverterService extends DefaultTerminalConverters {
	
	@Inject private BitcoinClientI bitcoin;
	@Inject private NumberValueConverter numberValueConverter;
	@Inject private HashValueConverter hashConverter;
	@Inject private TimestampValueConverter timestampTerminalConverter;
	@Inject private LongUnderscoreValueConverter longTerminalConverter;
	@Inject private DelayValueConverter delayConverter;
	@Inject private WIFValueConverter wifConverter;
		
	@ValueConverter(rule = "Number")
    def IValueConverter<Long> getNumberConverter() {
        return numberValueConverter
	}
	
	@ValueConverter(rule = "HASH_160")
    def IValueConverter<byte[]> getHash160Converter() {
        return hashConverter
	}

	@ValueConverter(rule = "HASH_256")
    def IValueConverter<byte[]> getHash256Converter() {
        return hashConverter
	}

	@ValueConverter(rule = "RIPMED_160")
    def IValueConverter<byte[]> getRipmed160Converter() {
        return hashConverter
	}

	@ValueConverter(rule = "SHA_256")
    def IValueConverter<byte[]> getSha256Converter() {
        return hashConverter
	}

	@ValueConverter(rule = "TIMESTAMP")
    def IValueConverter<Long> getTimestampConverter() {
        return timestampTerminalConverter
	}	

	@ValueConverter(rule = "LONG")
    def IValueConverter<Long> getLongConverter() {
        return longTerminalConverter
	}
		
	@ValueConverter(rule = "Delay")
    def IValueConverter<Long> getDelay() {
        return delayConverter
	}
	
	@ValueConverter(rule = "TXID")
    def IValueConverter<String> getTXID() {
        return new AbstractLexerBasedConverter<String>() {
			override toValue(String string, INode node) throws ValueConverterException {
				val id = string.split(":").get(1)
				
				try {
					return bitcoin.getRawTransaction(id);
				}
				catch (Exception e) {
					throw new ValueConverterException("Couldn't convert tx id '" + id + "' to transaction hex. Unable to fetch the transaction. "+e.message, node, e);
				}
				
			}
        }
	}
	
	@ValueConverter(rule = "TXSERIAL")
    def IValueConverter<String> getTX() {
        return new AbstractLexerBasedConverter<String>() {
			override toValue(String string, INode node) throws ValueConverterException {
				string.split(":").get(1)
			}
        }
	}
		
	@ValueConverter(rule = "WIF")
    def IValueConverter<String> getWIF() {
        return wifConverter
	}
	
}