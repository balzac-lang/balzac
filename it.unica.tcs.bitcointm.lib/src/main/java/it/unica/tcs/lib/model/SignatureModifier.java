package it.unica.tcs.lib.model;

import org.bitcoinj.core.Transaction.SigHash;

public enum SignatureModifier {

    ALL_INPUT_ALL_OUTPUT,
    ALL_INPUT_SINGLE_OUTPUT,
    ALL_INPUT_NO_OUTPUT,
    SINGLE_INPUT_ALL_OUTPUT,
    SINGLE_INPUT_SINGLE_OUTPUT,
    SINGLE_INPUT_NO_OUTPUT;

    public boolean isAllInput() {
        return this == ALL_INPUT_ALL_OUTPUT || this == ALL_INPUT_SINGLE_OUTPUT || this == ALL_INPUT_NO_OUTPUT;
    }

    public boolean isSingleInput() {
        return this == SINGLE_INPUT_ALL_OUTPUT || this == SINGLE_INPUT_SINGLE_OUTPUT || this == SINGLE_INPUT_NO_OUTPUT;
    }

    public boolean isAllOutput() {
        return this == ALL_INPUT_ALL_OUTPUT || this == SINGLE_INPUT_ALL_OUTPUT;
    }

    public boolean isSingleOutput() {
        return this == ALL_INPUT_SINGLE_OUTPUT || this == SINGLE_INPUT_SINGLE_OUTPUT;
    }

    public boolean isNoOutput() {
        return this == ALL_INPUT_NO_OUTPUT || this == SINGLE_INPUT_NO_OUTPUT;
    }

    public SigHash toHashType() {
        switch (this) {
        case ALL_INPUT_ALL_OUTPUT:
        case SINGLE_INPUT_ALL_OUTPUT:
            return SigHash.ALL;
        case ALL_INPUT_SINGLE_OUTPUT:
        case SINGLE_INPUT_SINGLE_OUTPUT:
            return SigHash.SINGLE;
        case ALL_INPUT_NO_OUTPUT:
        case SINGLE_INPUT_NO_OUTPUT:
            return SigHash.NONE;
        default:
            throw new IllegalStateException();
        }
    }

    public boolean toAnyoneCanPay() {
        switch (this) {
        case SINGLE_INPUT_ALL_OUTPUT:
        case SINGLE_INPUT_SINGLE_OUTPUT:
        case SINGLE_INPUT_NO_OUTPUT:
            return true;
        case ALL_INPUT_ALL_OUTPUT:
        case ALL_INPUT_SINGLE_OUTPUT:
        case ALL_INPUT_NO_OUTPUT:
            return false;
        default:
            throw new IllegalStateException();
        }
    }    
}
