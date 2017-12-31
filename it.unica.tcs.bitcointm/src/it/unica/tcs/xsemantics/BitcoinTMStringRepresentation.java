/*
 * Copyright 2017 Nicola Atzei
 */

/**
 * 
 */
package it.unica.tcs.xsemantics;

import java.util.Map.Entry;

import it.unica.tcs.bitcoinTM.BooleanLiteral;
import it.unica.tcs.bitcoinTM.BooleanType;
import it.unica.tcs.bitcoinTM.IntType;
import it.unica.tcs.bitcoinTM.Parameter;
import it.unica.tcs.bitcoinTM.Reference;
import it.unica.tcs.bitcoinTM.SignatureType;
import it.unica.tcs.bitcoinTM.StringLiteral;
import it.unica.tcs.bitcoinTM.StringType;
import it.unica.tcs.bitcoinTM.Type;
import it.unica.tcs.bitcoinTM.TypeVariable;
import it.xsemantics.runtime.StringRepresentation;

/**
 * @author Lorenzo Bettini
 * 
 */
public class BitcoinTMStringRepresentation extends StringRepresentation {


    protected String stringRep(BooleanLiteral intConstant) {
        return intConstant.isTrue()? "true" : "false";
    }

    protected String stringRep(StringLiteral stringConstant) {
        return "'" + stringConstant.getValue() + "'";
    }

    protected String stringRep(Parameter parameter) {
        return parameter.getName()
                + ((parameter.getType()) != null ? " : "
                        + string(parameter.getType()) : "");
    }

    protected String stringRep(Reference ref) {
        return stringRep(ref.getRef());
    }

    protected String stringRep(TypeSubstitutions substitutions) {
        return "subst{" + stringIterable(substitutions.getSubstitutions())
                + "}";
    }

    protected String stringRep(Entry<String, Type> entry) {
        return string(entry.getKey()) + "=" + string(entry.getValue());
    }

    protected String stringRep(TypeVariable typeVariable) {
        return typeVariable.getValue();
    }
    
    protected String stringRep(StringType type) {
        return type.getValue().getLiteral();
    }

    protected String stringRep(IntType type) {
        return type.getValue().getLiteral();
    }

    protected String stringRep(BooleanType type) {
        return type.getValue().getLiteral();
    }

    protected String stringRep(SignatureType type) {
        return type.getValue().getLiteral();
    }
    
}
