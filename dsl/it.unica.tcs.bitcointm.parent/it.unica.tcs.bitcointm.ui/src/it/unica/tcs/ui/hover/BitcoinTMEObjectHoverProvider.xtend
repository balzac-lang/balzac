package it.unica.tcs.ui.hover

import it.unica.tcs.bitcoinTM.BooleanType
import it.unica.tcs.bitcoinTM.IntType
import it.unica.tcs.bitcoinTM.Parameter
import it.unica.tcs.bitcoinTM.RelativeTime
import it.unica.tcs.bitcoinTM.SignatureType
import it.unica.tcs.bitcoinTM.StringType
import it.unica.tcs.bitcoinTM.TypeVariable
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.EReference
import org.eclipse.xtext.ui.editor.hover.html.DefaultEObjectHoverProvider

import static extension it.unica.tcs.utils.ASTUtils.*

class BitcoinTMEObjectHoverProvider extends DefaultEObjectHoverProvider {
	
	
	override String getLabel(EObject eobj) {
		return eobj.labelInternal
	}
	
	override String getFirstLine(EObject eobj) {
		return eobj.firstLineInternal
	}
	
	override boolean hasHover(EObject eobj) {
		if (eobj instanceof RelativeTime)
			return eobj.isDate;
		
		return super.hasHover(eobj)
	}
	
	
	
	
	
	dispatch def String getLabelInternal(Parameter p) {
		return p.name+" : "+p.paramType?.toStringType
	}
	
	// base case
	dispatch def String getLabelInternal(EObject obj) {
		return super.getLabel(obj)
	}
	
	
	dispatch def String getFirstLineInternal(RelativeTime p) {
		
		var time = p.value.castUnsignedShort	// consider only last 16 bits
		
		if (p.isDate) {
			time *= 512;
			var int timeRemainder 
			
			val days = time / (24*60*60)
			timeRemainder = time % (24*60*60)
			
			val hours = timeRemainder / (60*60)
			timeRemainder = time % (60*60)
			
			val minutes = timeRemainder / (60)
			timeRemainder = time % (60)
						
			val seconds = timeRemainder
			
			return '''
			«days» «IF days==1»day«ELSE»days«ENDIF», 
			«hours» «IF hours==1»hour«ELSE»hours«ENDIF»
			«minutes» «IF minutes==1»minute«ELSE»minutes«ENDIF»
			«seconds» «IF seconds==1»second«ELSE»seconds«ENDIF»
			'''
		}
		else {
			return '''«time» blocks'''
		}
	}
	
	dispatch def String getFirstLineInternal(EReference obj) {
		println("firstline: "+obj)
		return super.getFirstLine(obj)
	}
	
	// base case
	dispatch def String getFirstLineInternal(EObject obj) {
		return super.getFirstLine(obj)
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
