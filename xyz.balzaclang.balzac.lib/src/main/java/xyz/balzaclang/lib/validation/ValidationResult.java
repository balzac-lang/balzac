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
            this.reedemScript = isP2SH(output) ? decode(getLastChunk(input)).toString() : null;
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
