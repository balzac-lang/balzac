/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.utils

import com.google.inject.Inject
import com.google.inject.Singleton
import it.unica.tcs.bitcoinTM.BooleanType
import it.unica.tcs.bitcoinTM.HashType
import it.unica.tcs.bitcoinTM.IntType
import it.unica.tcs.bitcoinTM.Network
import it.unica.tcs.bitcoinTM.Parameter
import it.unica.tcs.bitcoinTM.ProtocolExpression
import it.unica.tcs.bitcoinTM.SignatureType
import it.unica.tcs.bitcoinTM.StringType
import it.unica.tcs.bitcoinTM.Type
import it.unica.tcs.compiler.CompileException
import it.unica.tcs.generator.ProtocolExpressionGenerator
import java.util.List
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.EcoreUtil2

@Singleton
class CompilerUtils {
	
	@Inject private extension ProtocolExpressionGenerator
	
	def String compileActualParams(List<ProtocolExpression> actualParams) {
		actualParams.map[e|e.compileProtocolExpression].join(",")
	}
	
	def String compileFormalParams(List<Parameter> formalParams) {
		formalParams.map[p|p.paramType.compileType+" "+p.name].join(", ")
    }
	
	def String compileType(Type type) {
    	if(type instanceof IntType) return "Integer"
    	if(type instanceof HashType) return "byte[]"
    	if(type instanceof StringType) return "String"
    	if(type instanceof BooleanType) return "Boolean"
    	if(type instanceof SignatureType) return "byte[]"
    	
    	throw new CompileException("Unexpected type "+type.class.simpleName)
    }
    
    def String compileNetworkParams(EObject obj) {
		val list = EcoreUtil2.getAllContentsOfType(EcoreUtil2.getRootContainer(obj), Network);
			
		if (list.size()==0)	// network undeclared, assume testnet
			return "NetworkParameters.fromID(NetworkParameters.ID_TESTNET)"
			
		if (list.size()==1)
			return if (list.get(0).isTestnet()) 
				   "NetworkParameters.fromID(NetworkParameters.ID_TESTNET)" 
				   else "NetworkParameters.fromID(NetworkParameters.ID_MAINNET)"
			
		throw new IllegalStateException();
	}
    
	def Class<?> convertType(Type type) {
    	if(type instanceof IntType) return Integer
    	if(type instanceof HashType) return typeof(byte[])
    	if(type instanceof StringType) return String
    	if(type instanceof BooleanType) return Boolean
    	if(type instanceof SignatureType) return typeof(byte[])
    	
    	throw new CompileException("Unexpected type "+type.class.simpleName)
    }
}
