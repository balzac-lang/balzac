/*
 * Copyright 2019 Nicola Atzei
 */

package it.unica.tcs.lib.validation;

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
        return new ValidationResult(false, "");
    }
}
