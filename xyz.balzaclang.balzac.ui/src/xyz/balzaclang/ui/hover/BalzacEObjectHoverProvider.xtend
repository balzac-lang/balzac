/*
 * Copyright 2017 Nicola Atzei
 */

package xyz.balzaclang.ui.hover

import com.google.inject.Inject
import xyz.balzaclang.balzac.AddressLiteral
import xyz.balzaclang.balzac.Constant
import xyz.balzaclang.balzac.Interpretable
import xyz.balzaclang.balzac.KeyLiteral
import xyz.balzaclang.balzac.Parameter
import xyz.balzaclang.balzac.PubKeyLiteral
import xyz.balzaclang.balzac.ScriptParameter
import xyz.balzaclang.balzac.Transaction
import xyz.balzaclang.balzac.TransactionParameter
import xyz.balzaclang.lib.model.Address
import xyz.balzaclang.lib.model.PrivateKey
import xyz.balzaclang.lib.model.PublicKey
import xyz.balzaclang.utils.ASTUtils
import xyz.balzaclang.xsemantics.BalzacStringRepresentation
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.ui.editor.hover.html.DefaultEObjectHoverProvider

class BalzacEObjectHoverProvider extends DefaultEObjectHoverProvider {

    @Inject extension ASTUtils
    @Inject extension BalzacStringRepresentation

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
    dispatch def String getFirstLineInternal(EObject obj) {
        return super.getFirstLine(obj)
    }

    dispatch def String getFirstLineInternal(Transaction tx) {
        '''transaction «tx.name»'''
    }

    dispatch def String getFirstLineInternal(ScriptParameter p) {
        '''Parameter «p.name»«IF p.type!==null» : «p.type.stringRep»«ENDIF»'''
    }

    dispatch def String getFirstLineInternal(TransactionParameter p) {
        '''Parameter «p.name»«IF p.type!==null» : «p.type.stringRep»«ENDIF»'''
    }

    // base case getDocumentationInternal
    def dispatch String getDocumentationInternal(EObject obj) ''''''

    def dispatch String getDocumentationInternal(Constant c) {
        (c.exp as Interpretable).interpretSafe.documentationInternal
    }

    def dispatch String getDocumentationInternal(KeyLiteral key) '''
        «val privKey = PrivateKey.fromBase58(key.value)»
        <pre>
            Private key
                base58 (wif) = «privKey.wif»
                hex          = «privKey.bytesAsString»

            Public key
                hex          = «privKey.toPublicKey.bytesAsString»

            Address
                base58 (wif) = «privKey.toAddress.wif»
                hash160      = «privKey.toAddress.bytesAsString»
        </pre>
        '''

    def dispatch String getDocumentationInternal(PubKeyLiteral pkey) '''
        «val pubkey = PublicKey.fromString(pkey.value)»
        <pre>
            Public key
                hex          = «pubkey.bytesAsString»

            Address
                base58 (wif) [MAINNET] = «pubkey.toMainnetAddress.wif»
                base58 (wif) [TESTNET] = «pubkey.toTestnetAddress.wif»
                hash160                = «pubkey.toMainnetAddress.bytesAsString»
        </pre>
        '''

    def dispatch String getDocumentationInternal(AddressLiteral addrLit) '''
        «val addr = Address.fromBase58(addrLit.value)»
        <pre>
            Address
                base58 (wif) = «addr.wif»
                hash160      = «addr.bytesAsString»
        </pre>
        '''

}
