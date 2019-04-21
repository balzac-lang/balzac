/*
 * Copyright 2019 Nicola Atzei
 */

package xyz.balzaclang.xsemantics;

import java.util.Map.Entry;

import org.eclipse.xsemantics.runtime.StringRepresentation;

import xyz.balzaclang.balzac.AddressType;
import xyz.balzaclang.balzac.BooleanLiteral;
import xyz.balzaclang.balzac.BooleanType;
import xyz.balzaclang.balzac.HashType;
import xyz.balzaclang.balzac.IntType;
import xyz.balzaclang.balzac.KeyType;
import xyz.balzaclang.balzac.Parameter;
import xyz.balzaclang.balzac.PubkeyType;
import xyz.balzaclang.balzac.Reference;
import xyz.balzaclang.balzac.SignatureType;
import xyz.balzaclang.balzac.StringLiteral;
import xyz.balzaclang.balzac.StringType;
import xyz.balzaclang.balzac.This;
import xyz.balzaclang.balzac.TransactionType;
import xyz.balzaclang.balzac.Type;
import xyz.balzaclang.balzac.TypeVariable;
import xyz.balzaclang.utils.ASTExtensions;

public class BalzacStringRepresentation extends StringRepresentation {

    public String stringRep(This intConstant) {
        return "this";
    }

    public String stringRep(BooleanLiteral intConstant) {
        return intConstant.isTrue()? "true" : "false";
    }

    public String stringRep(StringLiteral stringConstant) {
        return "'" + stringConstant.getValue() + "'";
    }

    public String stringRep(Parameter parameter) {
        return parameter.getName()
                + ((parameter.getType()) != null ? " : "
                        + string(parameter.getType()) : "");
    }

    public String stringRep(Reference ref) {
        return stringRep(ASTExtensions.nodeToString(ref));
    }

    public String stringRep(TypeSubstitutions substitutions) {
        return "subst{" + stringIterable(substitutions.getSubstitutions())
                + "}";
    }

    public String stringRep(Entry<String, Type> entry) {
        return string(entry.getKey()) + "=" + string(entry.getValue());
    }

    public String stringRep(Type type) {
        if (type instanceof IntType) {
            return ((IntType) type).getValue().getLiteral();
        }
        else if (type instanceof StringType) {
            return ((StringType) type).getValue().getLiteral();
        }
        else if (type instanceof BooleanType) {
            return ((BooleanType) type).getValue().getLiteral();
        }
        else if (type instanceof SignatureType) {
            return ((SignatureType) type).getValue().getLiteral();
        }
        else if (type instanceof TransactionType) {
            return ((TransactionType) type).getValue().getLiteral();
        }
        else if (type instanceof KeyType) {
            return ((KeyType) type).getValue().getLiteral();
        }
        else if (type instanceof HashType) {
            return ((HashType) type).getValue().getLiteral();
        }
        else if (type instanceof AddressType) {
            return ((AddressType) type).getValue().getLiteral();
        }
        else if (type instanceof PubkeyType) {
            return ((PubkeyType) type).getValue().getLiteral();
        }
        else if (type instanceof TypeVariable) {
            return ((TypeVariable) type).getValue();
        }
        else if (type == null) {
            return "";
        }
        else return "["+type.getClass().getSimpleName()+"]";
    }
}
