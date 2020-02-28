/*
 * Copyright 2019 Nicola Atzei
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package xyz.balzaclang.lib.validation;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.AddressFormatException.InvalidCharacter;
import org.bitcoinj.core.AddressFormatException.InvalidChecksum;
import org.bitcoinj.core.AddressFormatException.InvalidDataLength;
import org.bitcoinj.core.AddressFormatException.WrongNetwork;
import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.VerificationException;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptException;

import xyz.balzaclang.lib.ECKeyStore;
import xyz.balzaclang.lib.model.NetworkType;
import xyz.balzaclang.lib.model.script.OutputScript;
import xyz.balzaclang.lib.model.transaction.ITransactionBuilder;
import xyz.balzaclang.lib.utils.BitcoinUtils;
import xyz.balzaclang.lib.validation.ValidationResult.InputValidationError;

public class Validator {

    public static ValidationResult checkOutputScriptSize(OutputScript script) {
        if (!script.isReady()) {
            return ValidationResult.ok("The output script is not ready");
        }
        int scriptSize = script.build().getProgram().length;
        if (scriptSize > 520) {
            return ValidationResult.error("The output scripts is greater than 520 bytes");
        }
        else {
            return ValidationResult.ok("The output scripts less than 520 bytes");
        }
    }

    public static ValidationResult checkWitnessesCorrecltySpendsOutputs(ITransactionBuilder txBuilder, ECKeyStore keyStore) {
        // preconditions
        if (txBuilder.isCoinbase()) {
            return ValidationResult.ok("Transaction is a coinbase");
        }
        if (!txBuilder.isReady()) {
            return ValidationResult.ok("Transaction is not ready");
        }

        try {
            Transaction tx = txBuilder.toTransaction(keyStore);
            for (int i=0; i<tx.getInputs().size(); i++) {
                Script inputScript = tx.getInput(i).getScriptSig();
                Script outputScript = tx.getInput(i).getOutpoint().getConnectedOutput().getScriptPubKey();

                try {
                    inputScript.correctlySpends(tx, i, outputScript, Script.ALL_VERIFY_FLAGS);
                }
                catch (ScriptException e) {
                    return new InputValidationError(i, e.getMessage(), inputScript, outputScript);
                }
            }
            return ValidationResult.ok("All inputs correctly spend their outputs");
        }
        catch(Exception e) {
            String message = "Generic error.";
            message += e.getMessage() != null? " Details: " + e.getMessage() : "";
            return ValidationResult.error(message);
        }
    }

    public static ValidationResult validateRawTransaction(String bytes, NetworkType params) {
        return transactionExceptionHandler(() -> {
            Transaction tx = new Transaction(params.toNetworkParameters(), BitcoinUtils.decode(bytes));
            tx.verify();
        });
    }

    public static ValidationResult validatePrivateKey(String wif, NetworkType params) {
        return base58ExceptionHandler(() -> {
            DumpedPrivateKey.fromBase58(params.toNetworkParameters(), wif);
        });
    }

    public static ValidationResult validateAddress(String wif, NetworkType params) {
        return base58ExceptionHandler(() -> {
            Address.fromString(params.toNetworkParameters(), wif);
        });
    }

    private static ValidationResult base58ExceptionHandler(Runnable body) {
        String message = "Unknown error";
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
        catch (Exception e) {
            message = "Generic error.";
            message += e.getMessage() != null? " Details: " + e.getMessage() : "";
        }
        return ValidationResult.error(message);
    }

    private static ValidationResult transactionExceptionHandler(Runnable body) {
        String message = "Unknown error";
        try {
            body.run();
            return ValidationResult.ok();
        }
        catch (VerificationException e) {
            message = "Transaction is invalid. Details: " + e.getMessage();
        }
        catch (Exception e) {
            message = "Generic error.";
            message += e.getMessage() != null? " Details: " + e.getMessage() : "";
        }
        return ValidationResult.error(message);
    }

}
