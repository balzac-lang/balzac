package it.unica.tcs.utils

import it.unica.tcs.bitcoinTM.BooleanType
import it.unica.tcs.bitcoinTM.HashType
import it.unica.tcs.bitcoinTM.IntType
import it.unica.tcs.bitcoinTM.SignatureType
import it.unica.tcs.bitcoinTM.StringType
import it.unica.tcs.bitcoinTM.Type
import it.unica.tcs.bitcointm.lib.ScriptBuilder2
import it.unica.tcs.compiler.CompileException
import it.xsemantics.runtime.Result
import org.bitcoinj.script.Script
import org.bitcoinj.script.ScriptOpCodes

class CompilerUtils {
	
	def static String compileType(Type type) {
    	if(type instanceof IntType) return "Integer"
    	if(type instanceof HashType) return "byte[]"
    	if(type instanceof StringType) return "String"
    	if(type instanceof BooleanType) return "Boolean"
    	if(type instanceof SignatureType) return "byte[]"
    	
    	throw new CompileException("Unexpected type "+type.class.simpleName)
    }
    
	def static Class<?> convertType(Type type) {
    	if(type instanceof IntType) return Integer
    	if(type instanceof HashType) return typeof(byte[])
    	if(type instanceof StringType) return String
    	if(type instanceof BooleanType) return Boolean
    	if(type instanceof SignatureType) return typeof(byte[])
    	
    	throw new CompileException("Unexpected type "+type.class.simpleName)
    }
    
    /* ScriptBuilder extensions */
	def static ScriptBuilder2 append(ScriptBuilder2 sb, Script other) {
		return sb.append(other)
	}

	def static ScriptBuilder2 append(ScriptBuilder2 sb, ScriptBuilder2 other) {
		return sb.append(other);
	}

	def static ScriptBuilder2 append(ScriptBuilder2 sb, Result<Object> res) {
		
		if (res.getFirst() instanceof String){
            sb.data((res.getFirst() as String).getBytes());
        }
        else if (res.getFirst() instanceof Integer) {
            sb.number(res.getFirst() as Integer);
        }
        else if (res.getFirst() instanceof Boolean) {
            if (res.getFirst() as Boolean) {
                sb.op(ScriptOpCodes.OP_TRUE);
            }
            else sb.op(ScriptOpCodes.OP_FALSE);
        }
        else if (res.getFirst() instanceof byte[]) {
            sb.data(res.getFirst() as byte[]);
        }
        else throw new IllegalArgumentException("Unxpected type "+res.getFirst().getClass());
		return sb;
	}
}
