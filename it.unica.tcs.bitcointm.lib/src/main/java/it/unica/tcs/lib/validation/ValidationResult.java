/*
 * Copyright 2019 Nicola Atzei
 */

package it.unica.tcs.lib.validation;

import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptPattern;

public class ValidationResult {

    public final boolean error;
    public final String message;

    private ValidationResult(boolean error, String message) {
        this.error = error;
        this.message = message;
    }

    public static ValidationResult error(String message) {
        return new ValidationResult(true, message);
    }

    public static ValidationResult ok() {
        return ok("");
    }

    public static ValidationResult ok(String message) {
        return new ValidationResult(false, message);
    }

    public static class InputValidationError extends ValidationResult {
        public final int index;
        public final String inputScript;
        public final String outputScript;
        public final String reedemScript;

        public InputValidationError(int index, String message, Script input, Script output) {
            super(true, message);
            this.index = index;
            this.inputScript = input.toString();
            this.outputScript = output.toString();
            this.reedemScript = isP2SH(output)? decode(getLastChunk(input)).toString() : null;
        }

        private static boolean isP2SH(Script script) {
            return ScriptPattern.isP2SH(script);
        }

        private static Script decode(byte[] scriptByte) {
            return new Script(scriptByte);
        }

        private static byte[] getLastChunk(Script script) {
            return script.getChunks().get(script.getChunks().size() - 1).data;
        }
    }
}
