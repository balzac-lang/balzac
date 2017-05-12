package it.unica.tcs.ui.hover

import it.unica.tcs.bitcoinTM.BooleanType
import it.unica.tcs.bitcoinTM.IntType
import it.unica.tcs.bitcoinTM.Parameter
import it.unica.tcs.bitcoinTM.SignatureType
import it.unica.tcs.bitcoinTM.StringType
import it.unica.tcs.bitcoinTM.TypeVariable
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.ui.editor.hover.html.DefaultEObjectHoverProvider

class BitcoinTMEObjectHoverProvider extends DefaultEObjectHoverProvider {
	
	
	override String getLabel(EObject eobj) {
		return eobj.labelInternal
	}
	
	dispatch def String getLabelInternal(Parameter p) {
		return p.name+" : "+p.paramType.toStringType
	}
	
	dispatch def String getLabelInternal(EObject obj) {
		return super.getLabel(obj)
	}
	
	
	/*
	 * get the string literal for the types
	 */
	 
	dispatch def String toStringType(IntType type) {
		return type.value.literal
	}
	
	dispatch def String toStringType(StringType type) {
		return type.value.literal
	}
	
	dispatch def String toStringType(BooleanType type) {
		return type.value.literal
	}
	
	dispatch def String toStringType(SignatureType type) {
		return type.value.literal
	}
	
	dispatch def String toStringType(TypeVariable type) {
		return type.value
	}
}
