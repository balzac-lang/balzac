/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.utils

import com.google.inject.Inject
import com.google.inject.Singleton
import it.unica.tcs.bitcoinTM.BooleanType
import it.unica.tcs.bitcoinTM.Expression
import it.unica.tcs.bitcoinTM.HashType
import it.unica.tcs.bitcoinTM.IntType
import it.unica.tcs.bitcoinTM.Network
import it.unica.tcs.bitcoinTM.Parameter
import it.unica.tcs.bitcoinTM.SignatureType
import it.unica.tcs.bitcoinTM.StringType
import it.unica.tcs.bitcoinTM.Type
import it.unica.tcs.bitcointm.lib.ScriptBuilder2
import it.unica.tcs.compiler.CompileException
import it.unica.tcs.generator.ExpressionGenerator
import it.xsemantics.runtime.Result
import org.bitcoinj.script.Script
import org.bitcoinj.script.ScriptOpCodes
import org.eclipse.emf.common.util.EList
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.EcoreUtil2

@Singleton
class CompilerUtils {
	
	@Inject private extension ExpressionGenerator
	
	def String compileActualParams(EList<Expression> actualParams) {
		actualParams.map[e|e.compileExpression].join(",")
	}
		
	def String compileFormalParams(EList<Parameter> formalParams) {
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
    
    /* ScriptBuilder extensions */
	def ScriptBuilder2 append(ScriptBuilder2 sb, Script other) {
		return sb.append(other)
	}

	def ScriptBuilder2 append(ScriptBuilder2 sb, ScriptBuilder2 other) {
		return sb.append(other);
	}

	def ScriptBuilder2 append(ScriptBuilder2 sb, Result<Object> res) {
		
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
            else sb.number(ScriptOpCodes.OP_FALSE);
        }
        else if (res.getFirst() instanceof byte[]) {
            sb.data(res.getFirst() as byte[]);
        }
        else throw new IllegalArgumentException("Unxpected type "+res.getFirst().getClass());
		return sb;
	}
}
