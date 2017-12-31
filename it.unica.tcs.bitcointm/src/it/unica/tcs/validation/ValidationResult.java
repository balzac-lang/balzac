package it.unica.tcs.validation;

public class ValidationResult {
    
    public static ValidationResult VALIDATION_OK = new ValidationResult(true);
    public static ValidationResult VALIDATION_ERROR = new ValidationResult(false);
    
    public final boolean ok;
    public final String message;
    
    public ValidationResult(boolean ok) {
        this(ok, null);
    }
    
    public ValidationResult(boolean ok, String message) {
        this.ok = ok;
        this.message = message;
    }
}