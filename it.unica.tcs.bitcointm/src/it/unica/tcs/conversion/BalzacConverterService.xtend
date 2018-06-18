/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.conversion

import com.google.inject.Inject
import it.unica.tcs.conversion.converter.LongUnderscoreValueConverter
import it.unica.tcs.conversion.converter.NumberValueConverter
import it.unica.tcs.conversion.converter.TimestampValueConverter
import it.unica.tcs.lib.utils.BitcoinUtils
import org.bitcoinj.core.Address
import org.bitcoinj.core.DumpedPrivateKey
import org.bitcoinj.crypto.TransactionSignature
import org.eclipse.xtext.common.services.DefaultTerminalConverters
import org.eclipse.xtext.conversion.IValueConverter
import org.eclipse.xtext.conversion.ValueConverter
import org.eclipse.xtext.conversion.ValueConverterException
import org.eclipse.xtext.conversion.impl.AbstractLexerBasedConverter
import org.eclipse.xtext.nodemodel.INode

class BalzacConverterService extends DefaultTerminalConverters {

    @Inject private NumberValueConverter numberValueConverter;
    @Inject private TimestampValueConverter timestampTerminalConverter;
    @Inject private LongUnderscoreValueConverter longTerminalConverter;

    @ValueConverter(rule = "Number")
    def IValueConverter<Long> getNumberConverter() {
        return numberValueConverter
    }

    @ValueConverter(rule = "HASH_TERM")
    def IValueConverter<byte[]> getHash160Converter() {
        return new AbstractLexerBasedConverter<byte[]>() {
            override toValue(String string, INode node) throws ValueConverterException {
                return BitcoinUtils.decode(string.split(":").get(1).toLowerCase)
            }
        }
    }

    @ValueConverter(rule = "TIMESTAMP")
    def IValueConverter<Long> getTimestampConverter() {
        return timestampTerminalConverter
    }

    @ValueConverter(rule = "LONG")
    def IValueConverter<Long> getLongConverter() {
        return longTerminalConverter
    }

    @ValueConverter(rule = "TXID")
    def IValueConverter<String> getTXID() {
        return new AbstractLexerBasedConverter<String>() {
            override toValue(String string, INode node) throws ValueConverterException {
                string.split(":").get(1)
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

    @ValueConverter(rule = "SIGHEX")
    def IValueConverter<String> getSig() {
        return new AbstractLexerBasedConverter<String>() {
            override toValue(String string, INode node) throws ValueConverterException {
                val value = string.split(":").get(1)

                try {
                    TransactionSignature.decodeFromBitcoin(BitcoinUtils.decode(value), true, true)
                }
                catch (Exception e) {
                    throw new ValueConverterException("Couldn't convert input '" + value + "' to a valid signature.\n\nDetails: "+e.message, node, e);
                }
                value
            }
        }
    }

    @ValueConverter(rule = "KEY_WIF")
    def IValueConverter<String> getKeyWIF() {
        return new AbstractLexerBasedConverter<String>() {

            override toValue(String string, INode node) throws ValueConverterException {
                var value = string.split(":").get(1)

                try {
                    DumpedPrivateKey.fromBase58(null, value);   // it ignores the network byte
                    return value;
                }
                catch (Exception e) {
                    throw new ValueConverterException("Couldn't convert input '" + value + "' to a valid private key. \n\nDetails: "+e.message, node, e);
            	}
            }
        }
    }
    
    
    @ValueConverter(rule = "ADDRESS_WIF")
    def IValueConverter<String> getAddressWIF() {
        return new AbstractLexerBasedConverter<String>() {

            override toValue(String string, INode node) throws ValueConverterException {
                var value = string.split(":").get(1)

                try {
                    Address.fromString(null, value);    // it ignores the network byte
                    return value;
                }
                catch(Exception e)
                    throw new ValueConverterException("Couldn't convert input '" + value + "' to a valid address. \n\nDetails: "+e.message, node, e);
            }
        }
    }

    @ValueConverter(rule = "MINUTE_DELAY")
    def IValueConverter<Long> getMinuteDelay() {
        return new AbstractLexerBasedConverter<Long>() {
            override toValue(String string, INode node) throws ValueConverterException {
                Long.parseLong(string.substring(0, string.indexOf("m")))
            }
        }
    }

    @ValueConverter(rule = "HOUR_DELAY")
    def IValueConverter<Long> getHourDelay() {
        return new AbstractLexerBasedConverter<Long>() {
            override toValue(String string, INode node) throws ValueConverterException {
                Long.parseLong(string.substring(0, string.indexOf("h")))
            }
        }
    }

    @ValueConverter(rule = "DAY_DELAY")
    def IValueConverter<Long> getDayDelay() {
        return new AbstractLexerBasedConverter<Long>() {
            override toValue(String string, INode node) throws ValueConverterException {
                Long.parseLong(string.substring(0, string.indexOf("d")))
            }
        }
    }

    @ValueConverter(rule = "PUBKEY")
    def IValueConverter<String> getPUBKEY() {
        return new AbstractLexerBasedConverter<String>() {
            override toValue(String string, INode node) throws ValueConverterException {
                string.split(":").get(1)
            }
        }
    }
}