/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.conversion

import com.google.inject.Inject
import it.unica.tcs.conversion.converter.NumberValueConverter
import it.unica.tcs.conversion.converter.TimestampValueConverter
import it.unica.tcs.conversion.converter.ints.IntUnderscoreValueConverter
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
import org.eclipse.xtext.conversion.impl.AbstractValueConverter

class BalzacConverterService extends DefaultTerminalConverters {

    @Inject NumberValueConverter numberValueConverter;
    @Inject TimestampValueConverter timestampTerminalConverter;
    @Inject IntUnderscoreValueConverter intTerminalConverter;

    @ValueConverter(rule = "Number")
    def IValueConverter<Long> getNumberConverter() {
        return numberValueConverter
    }

    @ValueConverter(rule = "HASH_TERM")
    def IValueConverter<byte[]> getHash160Converter() {
        return new AbstractLexerBasedConverter<byte[]>() {
            override toValue(String string, INode node) throws ValueConverterException {
                val value = string.split(":").get(1).toLowerCase
                if (value.length % 2 == 1) {
                    throw new ValueConverterException("Couldn't convert input '" + value + "' to a valid hash: digits must be even.", node, null);
                }
                try {
                    return BitcoinUtils.decode(value)
                }
                catch (Exception e) {
                    throw new ValueConverterException("Couldn't convert input '" + value + "' to a valid hash. Details: "+e.message, node, null);
                }
            }
        }
    }

    @ValueConverter(rule = "TIMESTAMP")
    def IValueConverter<Long> getTimestampConverter() {
        return timestampTerminalConverter
    }

    @ValueConverter(rule = "INT")
    def IValueConverter<Integer> getIntConverter() {
        return intTerminalConverter
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
                    throw new ValueConverterException("Couldn't convert input '" + value + "' to a valid signature. Details: "+e.message, node, null);
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
                    throw new ValueConverterException("Couldn't convert input '" + value + "' to a valid private key. Details: "+e.message, node, null);
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
                    throw new ValueConverterException("Couldn't convert input '" + value + "' to a valid address. Details: "+e.message, node, null);
            }
        }
    }

    @ValueConverter(rule = "MINUTE_DELAY")
    def IValueConverter<Integer> getMinuteDelay() {
        return new AbstractLexerBasedConverter<Integer>() {
            override toValue(String string, INode node) throws ValueConverterException {
                Integer.parseInt(string.substring(0, string.indexOf("m"))) * 60
            }
        }
    }

    @ValueConverter(rule = "HOUR_DELAY")
    def IValueConverter<Integer> getHourDelay() {
        return new AbstractLexerBasedConverter<Integer>() {
            override toValue(String string, INode node) throws ValueConverterException {
                Integer.parseInt(string.substring(0, string.indexOf("h"))) * 60 * 60
            }
        }
    }

    @ValueConverter(rule = "DAY_DELAY")
    def IValueConverter<Integer> getDayDelay() {
        return new AbstractLexerBasedConverter<Integer>() {
            override toValue(String string, INode node) throws ValueConverterException {
                Integer.parseInt(string.substring(0, string.indexOf("d"))) * 60 * 60 * 24
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

    @ValueConverter(rule = "BTC_DECIMAL")
    def IValueConverter<Integer> getBTC_DECIMAL() {
        return new AbstractValueConverter<Integer> {
            override toValue(String string, INode node) throws ValueConverterException {
                var value = string.split("\\.").get(1).trim

                // remove trailing zeros
                value = value.replaceAll("0*$", "");

                if (value.length > 8)
                    throw new ValueConverterException("Couldn't convert input '" + value + "' to an int value. The decimal must be less than 10^-8", node, null);

                // 0 right padding
                value = String.format("%-8s", value).replace(' ', '0');
                return intTerminalConverter.toValue(value, node)
            }
            override toString(Integer value) throws ValueConverterException {
                value.toString
            }
        }
    }
}