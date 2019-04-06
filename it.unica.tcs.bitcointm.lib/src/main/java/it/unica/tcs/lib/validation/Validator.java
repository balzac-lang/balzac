/*
 * Copyright 2019 Nicola Atzei
 */

package it.unica.tcs.lib.validation;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.AddressFormatException.InvalidCharacter;
import org.bitcoinj.core.AddressFormatException.InvalidChecksum;
import org.bitcoinj.core.AddressFormatException.InvalidDataLength;
import org.bitcoinj.core.AddressFormatException.WrongNetwork;
import org.bitcoinj.core.DumpedPrivateKey;

import it.unica.tcs.lib.model.NetworkType;

public class Validator {
    
    public static ValidationResult validatePrivateKey(String wif, NetworkType params) {
        return exceptionHandler(() -> {            
            DumpedPrivateKey.fromBase58(params.toNetworkParameters(), wif);
        });
    }
    
    public static ValidationResult validateAddress(String wif, NetworkType params) {
        return exceptionHandler(() -> {            
            Address.fromString(params.toNetworkParameters(), wif);
        });
    }

    private static ValidationResult exceptionHandler(Runnable body) {
        String message = "";
        try {
            body.run();
            return ValidationResult.ok();
        }
        catch (InvalidChecksum e) {
            message = "Checksum does not validate";
        }
        catch (InvalidCharacter e) {
            message = "Invalid character '" + Character.toString(e.character) + "' at position " + e.position;
        }
        catch (InvalidDataLength e) {
            message = "Invalid data length";
        }
        catch (WrongNetwork e) {
            message = "Wrong network type";
        }
        catch (AddressFormatException e) {
            message = "Unable to decode";
        }
        return ValidationResult.error(message);        
    }
    
}
