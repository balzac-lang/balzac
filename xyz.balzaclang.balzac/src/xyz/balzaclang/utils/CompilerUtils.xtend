/*
 * Copyright 2017 Nicola Atzei
 */

package xyz.balzaclang.utils

import com.google.inject.Singleton
import xyz.balzaclang.balzac.AddressType
import xyz.balzaclang.balzac.BooleanType
import xyz.balzaclang.balzac.HashType
import xyz.balzaclang.balzac.IntType
import xyz.balzaclang.balzac.KeyType
import xyz.balzaclang.balzac.Network
import xyz.balzaclang.balzac.Parameter
import xyz.balzaclang.balzac.PubkeyType
import xyz.balzaclang.balzac.SignatureType
import xyz.balzaclang.balzac.StringType
import xyz.balzaclang.balzac.TransactionType
import xyz.balzaclang.balzac.Type
import xyz.balzaclang.compiler.CompileException
import xyz.balzaclang.lib.model.Address
import xyz.balzaclang.lib.model.Hash
import xyz.balzaclang.lib.model.ITransactionBuilder
import xyz.balzaclang.lib.model.PrivateKey
import xyz.balzaclang.lib.model.PublicKey
import xyz.balzaclang.lib.model.Signature
import java.util.List
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.EcoreUtil2

@Singleton
class CompilerUtils {

    def String compileFormalParams(List<Parameter> formalParams) {
        formalParams.map[p|p.type.compileType+" "+p.name].join(", ")
    }

    def String compileType(Type type) {
        if(type instanceof IntType) return "Long"
        if(type instanceof HashType) return "Hash"
        if(type instanceof StringType) return "String"
        if(type instanceof BooleanType) return "Boolean"
        if(type instanceof SignatureType) return "byte[]"

        throw new CompileException("Unexpected type "+type.class.simpleName)
    }

    def String compileNetworkParams(EObject obj) {
        val list = EcoreUtil2.getAllContentsOfType(EcoreUtil2.getRootContainer(obj), Network);

        if (list.size()==0) // network undeclared, assume testnet
            return "NetworkParameters.fromID(NetworkParameters.ID_TESTNET)"

        if (list.size()==1)
            return if (list.get(0).isTestnet())
                   "NetworkParameters.fromID(NetworkParameters.ID_TESTNET)"
                   else "NetworkParameters.fromID(NetworkParameters.ID_MAINNET)"

        throw new IllegalStateException();
    }

    def Class<?> convertType(Type type) {
        if(type instanceof IntType) return Long
        if(type instanceof StringType) return String
        if(type instanceof BooleanType) return Boolean
        if(type instanceof HashType) return Hash
        if(type instanceof KeyType) return PrivateKey
        if(type instanceof PubkeyType) return PublicKey
        if(type instanceof AddressType) return Address
        if(type instanceof TransactionType) return ITransactionBuilder
        if(type instanceof SignatureType) return Signature

        throw new CompileException("Unexpected type "+type?.class?.simpleName)
    }
}
