/*
 * Copyright 2018 Nicola Atzei
 */
package it.unica.tcs.lib.client;

public class BitcoinClientException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public BitcoinClientException() { }

    public BitcoinClientException(String message) {
        super(message);
    }

    public BitcoinClientException(Throwable cause) {
        super(cause);
    }

    public BitcoinClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public BitcoinClientException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
