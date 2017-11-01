/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.ui.hover

import com.google.inject.Inject
import it.unica.tcs.bitcoinTM.BooleanType
import it.unica.tcs.bitcoinTM.IntType
import it.unica.tcs.bitcoinTM.KeyDeclaration
import it.unica.tcs.bitcoinTM.Parameter
import it.unica.tcs.bitcoinTM.RelativeTime
import it.unica.tcs.bitcoinTM.SignatureType
import it.unica.tcs.bitcoinTM.StringType
import it.unica.tcs.bitcoinTM.TypeVariable
import it.unica.tcs.lib.utils.BitcoinUtils
import it.unica.tcs.utils.ASTUtils
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.ui.editor.hover.html.DefaultEObjectHoverProvider
import it.unica.tcs.bitcoinTM.TransactionDeclaration
import it.unica.tcs.compiler.TransactionCompiler

class BitcoinTMEObjectHoverProvider extends DefaultEObjectHoverProvider {
	
	@Inject extension ASTUtils
	@Inject extension TransactionCompiler
	
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
	
	override getDocumentation(EObject eobj) {
        return eobj.documentationInternal
    }
	
	
	
	// base case getLabelInternal
	dispatch def String getLabelInternal(EObject obj) {
		return super.getLabel(obj)
	}

	dispatch def String getLabelInternal(Parameter p) {
		return p.name+" : "+p.paramType?.toStringType
	}
	

	// base case getFirstLineInternal
	def String getFirstLineInternal(EObject obj) {
		return super.getFirstLine(obj)
	}
	
	def String getFirstLineInternal(TransactionDeclaration tx) {
		'''transaction «tx.name»'''
	}
	
	// base case getDocumentationInternal
	def dispatch String getDocumentationInternal(EObject obj) {
		return super.getDocumentation(obj)
	}
	
	def dispatch String getDocumentationInternal(KeyDeclaration pvt) '''
		«IF !pvt.isPlaceholder»
			«val pvtEC = BitcoinUtils.wifToECKey(pvt.value, pvt.networkParams)»
			<pre>
			Private key
			    base58 (wif) = «pvt.value»
			    hex          = «pvtEC.privateKeyAsHex»
			    
			Public key
			    base58 (wif) = «pvtEC.toAddress(pvt.networkParams).toBase58»
			    hex          = «BitcoinUtils.encode(pvtEC.pubKey)»
			    hash160      = «BitcoinUtils.encode(pvtEC.pubKeyHash)»
			</pre>
		«ENDIF»
		'''
	
	def dispatch String getDocumentationInternal(TransactionDeclaration tx) {
		'''
		<pre>
		«tx.compileTransaction.toString»
		</pre>
		'''		
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
