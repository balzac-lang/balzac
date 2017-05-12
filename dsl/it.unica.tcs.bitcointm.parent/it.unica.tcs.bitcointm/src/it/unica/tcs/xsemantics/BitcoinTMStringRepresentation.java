/**
 * 
 */
package it.unica.tcs.xsemantics;

import java.util.Map.Entry;

import it.unica.tcs.bitcoinTM.BooleanLiteral;
import it.unica.tcs.bitcoinTM.BooleanType;
import it.unica.tcs.bitcoinTM.IntType;
import it.unica.tcs.bitcoinTM.Parameter;
import it.unica.tcs.bitcoinTM.SignatureType;
import it.unica.tcs.bitcoinTM.StringLiteral;
import it.unica.tcs.bitcoinTM.StringType;
import it.unica.tcs.bitcoinTM.Type;
import it.unica.tcs.bitcoinTM.TypeVariable;
import it.unica.tcs.bitcoinTM.VariableReference;
import it.xsemantics.runtime.StringRepresentation;

/**
 * @author Lorenzo Bettini
 * 
 */
public class BitcoinTMStringRepresentation extends StringRepresentation {


	protected String stringRep(BooleanLiteral intConstant) {
		return intConstant.getValue() + "";
	}

	protected String stringRep(StringLiteral stringConstant) {
		return "'" + stringConstant.getValue() + "'";
	}

	protected String stringRep(Parameter parameter) {
		return parameter.getName()
				+ ((parameter.getParamType()) != null ? " : "
						+ string(parameter.getParamType()) : "");
	}
	
	protected String stringRep(VariableReference variable) {
		return variable.getRef().getName();
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
