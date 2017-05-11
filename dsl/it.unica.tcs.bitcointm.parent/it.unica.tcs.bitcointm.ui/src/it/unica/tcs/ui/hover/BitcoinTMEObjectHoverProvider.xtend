package it.unica.tcs.ui.hover

import it.unica.tcs.bitcoinTM.Parameter
import org.eclipse.xtext.ui.editor.hover.html.DefaultEObjectHoverProvider
import org.eclipse.emf.ecore.EObject

class BitcoinTMEObjectHoverProvider extends DefaultEObjectHoverProvider {
	
	
	override String getLabel(EObject eobj) {
		return eobj.labelInternal
	}
	
	dispatch def String getLabelInternal(Parameter p) {
		return p.name+" : "+p.paramType?.value
	}
	
	dispatch def String getLabelInternal(EObject obj) {
		return super.getLabel(obj)
	}
	
}
