package it.unica.tcs.utils

import it.unica.tcs.bitcoinTM.BooleanType
import it.unica.tcs.bitcoinTM.HashType
import it.unica.tcs.bitcoinTM.IntType
import it.unica.tcs.bitcoinTM.SignatureType
import it.unica.tcs.bitcoinTM.StringType
import it.unica.tcs.bitcoinTM.Type

class CompilerUtils {
	
	def static String compileType(Type type) {
    	if(type instanceof IntType) return "Integer"
    	if(type instanceof HashType) return "byte[]"
    	if(type instanceof StringType) return "String"
    	if(type instanceof BooleanType) return "Boolean"
    	if(type instanceof SignatureType) return "byte[]"
    	
    	throw new it.unica.tcs.compiler.CompileException("Unexpected type "+type.class.simpleName)
    }
    
	def static Class<?> convertType(Type type) {
    	if(type instanceof IntType) return Integer
    	if(type instanceof HashType) return typeof(byte[])
    	if(type instanceof StringType) return String
    	if(type instanceof BooleanType) return Boolean
    	if(type instanceof SignatureType) return typeof(byte[])
    	
    	throw new it.unica.tcs.compiler.CompileException("Unexpected type "+type.class.simpleName)
    }
}
