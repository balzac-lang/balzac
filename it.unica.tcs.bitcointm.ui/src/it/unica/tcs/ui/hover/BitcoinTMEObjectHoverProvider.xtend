/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.ui.hover

import com.google.inject.Inject
import it.unica.tcs.bitcoinTM.AddressLiteral
import it.unica.tcs.bitcoinTM.Constant
import it.unica.tcs.bitcoinTM.KeyLiteral
import it.unica.tcs.bitcoinTM.Parameter
import it.unica.tcs.bitcoinTM.PubKeyLiteral
import it.unica.tcs.bitcoinTM.Transaction
import it.unica.tcs.lib.utils.BitcoinUtils
import it.unica.tcs.utils.ASTUtils
import it.unica.tcs.xsemantics.BitcoinTMStringRepresentation
import org.bitcoinj.core.Address
import org.bitcoinj.core.ECKey
import org.bitcoinj.core.LegacyAddress
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.ui.editor.hover.html.DefaultEObjectHoverProvider

class BitcoinTMEObjectHoverProvider extends DefaultEObjectHoverProvider {

    @Inject extension ASTUtils
    @Inject extension BitcoinTMStringRepresentation

    override String getLabel(EObject eobj) {
        return eobj.labelInternal
    }

    override String getFirstLine(EObject eobj) {
        return eobj.firstLineInternal
    }

  override boolean hasHover(EObject eobj) {
      if (eobj instanceof KeyLiteral)
          return true;

      if (eobj instanceof PubKeyLiteral)
          return true;

      return super.hasHover(eobj)
  }

    override getDocumentation(EObject eobj) {
        var doc = super.getDocumentation(eobj)
        doc = if (doc === null) "" else doc+"</br></br>"
        return doc+eobj.documentationInternal
    }



    // base case getLabelInternal
    dispatch def String getLabelInternal(EObject obj) {
        return super.getLabel(obj)
    }

    dispatch def String getLabelInternal(Parameter p) {
        return p.name+" : "+p.type.stringRep
    }

    dispatch def String getLabelInternal(Constant p) {
        return p.name+" : "+p.type.stringRep
    }

    // base case getFirstLineInternal
    def String getFirstLineInternal(EObject obj) {
        return super.getFirstLine(obj)
    }

    def String getFirstLineInternal(Transaction tx) {
        '''transaction «tx.name»'''
    }


    // base case getDocumentationInternal
    def dispatch String getDocumentationInternal(EObject obj) ''''''

    def dispatch String getDocumentationInternal(Constant c) {
        c.exp.documentationInternal
    }

    def dispatch String getDocumentationInternal(KeyLiteral key) '''
        «val wif = key.value»
        «val pvtEC = BitcoinUtils.wifToECKey(wif, key.networkParams)»
        <pre>
            Private key
                base58 (wif) = «wif»
                hex          = «pvtEC.privateKeyAsHex»

            Public key
                hex          = «BitcoinUtils.encode(pvtEC.pubKey)»
        
            Address
                base58 (wif) = «LegacyAddress.fromKey(key.networkParams, pvtEC).toBase58»
                hash160      = «BitcoinUtils.encode(pvtEC.pubKeyHash)»
        </pre>
        '''
    def dispatch String getDocumentationInternal(PubKeyLiteral pubkey) '''
        «val pubEC = ECKey.fromPublicOnly(BitcoinUtils.decode(pubkey.value))»
        <pre>
            Public key
                hex          = «pubkey.value»
            
            Address
                base58 (wif) = «LegacyAddress.fromKey(pubkey.networkParams, pubEC).toBase58»
                hash160      = «BitcoinUtils.encode(pubEC.pubKeyHash)»
        </pre>
        '''
        
    def dispatch String getDocumentationInternal(AddressLiteral addrLit) '''
        «val wif = addrLit.value»
        <pre>
            «val addr = Address.fromString(addrLit.networkParams, wif)»
            Address
                base58 (wif) = «wif»
                hash160      = «BitcoinUtils.encode(addr.hash)»
        </pre>
        '''

}
