/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.ui.hover

import com.google.inject.Inject
import it.unica.tcs.bitcoinTM.Constant
import it.unica.tcs.bitcoinTM.KeyLiteral
import it.unica.tcs.bitcoinTM.Parameter
import it.unica.tcs.bitcoinTM.PubKeyLiteral
import it.unica.tcs.bitcoinTM.Transaction
import it.unica.tcs.compiler.TransactionCompiler
import it.unica.tcs.lib.utils.BitcoinUtils
import it.unica.tcs.utils.ASTUtils
import it.unica.tcs.xsemantics.BitcoinTMStringRepresentation
import it.unica.tcs.xsemantics.Rho
import org.bitcoinj.core.Address
import org.bitcoinj.core.ECKey
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.ui.editor.hover.html.DefaultEObjectHoverProvider

class BitcoinTMEObjectHoverProvider extends DefaultEObjectHoverProvider {

    @Inject extension ASTUtils
    @Inject extension TransactionCompiler
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
        <pre>
        «IF key.value.isPrivateKey»
            «val pvtEC = BitcoinUtils.wifToECKey(wif, key.networkParams)»
            Private key
                base58 (wif) = «wif»
                hex          = «pvtEC.privateKeyAsHex»
            
            Public key
                base58 (wif) = «pvtEC.toAddress(key.networkParams).toBase58»
                hex          = «BitcoinUtils.encode(pvtEC.pubKey)»
                hash160      = «BitcoinUtils.encode(pvtEC.pubKeyHash)»
        «ENDIF»
        «IF key.value.isAddress»
            «val pubEC = Address.fromBase58(key.networkParams, wif)»
            Public key
                base58 (wif) = «wif»
                hash160      = «BitcoinUtils.encode(pubEC.hash160)»
        «ENDIF»
        </pre>
        '''

    def dispatch String getDocumentationInternal(PubKeyLiteral pubkey) '''
        <pre>
        Public key
            hex     = «pubkey.value»
            address = «ECKey.fromPublicOnly(BitcoinUtils.decode(pubkey.value)).toAddress(pubkey.networkParams)»
        </pre>
        '''

    def dispatch String getDocumentationInternal(Transaction tx) '''
        <pre>
        «tx.compileTransaction(new Rho).toString»
        </pre>
        '''
}
