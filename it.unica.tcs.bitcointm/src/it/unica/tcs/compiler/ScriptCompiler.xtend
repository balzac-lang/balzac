/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.compiler

import com.google.inject.Inject
import it.unica.tcs.bitcoinTM.AfterTimeLock
import it.unica.tcs.bitcoinTM.AndScriptExpression
import it.unica.tcs.bitcoinTM.Between
import it.unica.tcs.bitcoinTM.BitcoinTMFactory
import it.unica.tcs.bitcoinTM.BooleanLiteral
import it.unica.tcs.bitcoinTM.Constant
import it.unica.tcs.bitcoinTM.Declaration
import it.unica.tcs.bitcoinTM.DeclarationLeft
import it.unica.tcs.bitcoinTM.Hash160
import it.unica.tcs.bitcoinTM.Hash256
import it.unica.tcs.bitcoinTM.HashLiteral
import it.unica.tcs.bitcoinTM.IfThenElse
import it.unica.tcs.bitcoinTM.Input
import it.unica.tcs.bitcoinTM.KeyLiteral
import it.unica.tcs.bitcoinTM.Literal
import it.unica.tcs.bitcoinTM.Max
import it.unica.tcs.bitcoinTM.Min
import it.unica.tcs.bitcoinTM.NumberLiteral
import it.unica.tcs.bitcoinTM.OrScriptExpression
import it.unica.tcs.bitcoinTM.Output
import it.unica.tcs.bitcoinTM.Parameter
import it.unica.tcs.bitcoinTM.Reference
import it.unica.tcs.bitcoinTM.Referrable
import it.unica.tcs.bitcoinTM.Ripemd160
import it.unica.tcs.bitcoinTM.Script
import it.unica.tcs.bitcoinTM.ScriptArithmeticSigned
import it.unica.tcs.bitcoinTM.ScriptBooleanNegation
import it.unica.tcs.bitcoinTM.ScriptComparison
import it.unica.tcs.bitcoinTM.ScriptEquals
import it.unica.tcs.bitcoinTM.ScriptExpression
import it.unica.tcs.bitcoinTM.ScriptMinus
import it.unica.tcs.bitcoinTM.ScriptPlus
import it.unica.tcs.bitcoinTM.Sha256
import it.unica.tcs.bitcoinTM.Signature
import it.unica.tcs.bitcoinTM.SignatureLiteral
import it.unica.tcs.bitcoinTM.Size
import it.unica.tcs.bitcoinTM.StringLiteral
import it.unica.tcs.bitcoinTM.TransactionLiteral
import it.unica.tcs.bitcoinTM.Versig
import it.unica.tcs.lib.script.InputScript
import it.unica.tcs.lib.script.InputScriptImpl
import it.unica.tcs.lib.script.OpReturnOutputScript
import it.unica.tcs.lib.script.OutputScript
import it.unica.tcs.lib.script.P2PKHOutputScript
import it.unica.tcs.lib.script.P2SHInputScript
import it.unica.tcs.lib.script.P2SHOutputScript
import it.unica.tcs.lib.script.ScriptBuilder2
import it.unica.tcs.lib.utils.BitcoinUtils
import it.unica.tcs.utils.ASTUtils
import it.unica.tcs.utils.CompilerUtils
import javax.inject.Singleton
import org.bitcoinj.core.DumpedPrivateKey
import org.eclipse.xtext.EcoreUtil2

import static org.bitcoinj.script.ScriptOpCodes.*

/*
 * EXPRESSIONS
 * 
 * N.B. the compiler tries to simplify simple expressions like
 * <ul> 
 *  <li> 1+2 ≡ 3
 *  <li> if (12==10+2) then "foo" else "bar" ≡ "foo"
 * </ul>
 */
@Singleton
class ScriptCompiler {
	
	@Inject private extension CompilerUtils
    @Inject private extension ASTUtils
    
    def InputScript compileInput(Input input, OutputScript outScript) {

        if (outScript.isP2PKH) {
        	/*
        	 * P2PKH
        	 */
        	var sig = input.exps.get(0) as Signature
            var pubkey = sig.key.interpretSafe(KeyLiteral).value.privateWifToPubkeyBytes(input.networkParams)
            
            var sb = sig.compileInputExpression
            sb.data(pubkey)

            /* <sig> <pubkey> */
            return new InputScriptImpl().append(sb) as InputScript
        }
        else if (outScript.isP2SH) {
        	
        	var inputTx = input.txRef
        	
        	/*
        	 * P2SH
        	 */
        	val redeemScript = 
	        	if (inputTx instanceof TransactionLiteral) {
	        		// get the redeem script from the AST (specified by the user)
	                val s = input.redeemScript.compileRedeemScript
	                
	                if (!s.ready)
		                throw new CompileException("This redeem script cannot have free variables")
		            
		            s
	        	}
	        	else if (inputTx instanceof Reference) {
	        		
				if (inputTx.ref.isTx || inputTx.ref.isTxParameter) {
					outScript as P2SHOutputScript
				}
					else if (inputTx.ref.isTxLiteral){
						// get the redeem script from the AST (specified by the user)
		                val s = input.redeemScript.compileRedeemScript
		                
		                if (!s.ready)
			                throw new CompileException("This redeem script cannot have free variables")
			            
			            s
			        }
	        	}
	        	else 
	        		throw new CompileException('''Unexpected class «inputTx»''')
	        	
	        val p2sh = new P2SHInputScript(redeemScript)
                
            // build the list of expression pushes (actual parameters) 
            input.exps.forEach[e|
            	p2sh.append(e.compileInputExpression)
            ]
            
            /* <e1> ... <en> <serialized script> */
            return p2sh
        }
        else
            throw new CompileException("cannot redeem OP_RETURN outputs")
    }
    
    /**
     * 
     */
    def InputScript compileInputExpression(ScriptExpression exp) {
    	var ctx = new Context
        new InputScriptImpl().append(exp.compileExpression(ctx)) as InputScript
    }
    
    /**
     * 
     */
    def OutputScript compileOutputScript(Output output) {
		
        var outScript = output.script

        if (outScript.isP2PKH) {
            var versig = outScript.exp as Versig
            
            var res = versig.pubkeys.get(0).interpretSafe
            
            if (res instanceof KeyLiteral) {
	            var wif = res.value
	            var pkHash = BitcoinUtils.wifToECKey(wif, output.networkParams).pubKeyHash
	
	            /* OP_DUP OP_HASH160 <pkHash> OP_EQUALVERIFY OP_CHECKSIG */
	            val sb = new P2PKHOutputScript()
	            sb.op(OP_DUP)
	              .op(OP_HASH160)
	              .data(pkHash)
	              .op(OP_EQUALVERIFY)
	              .op(OP_CHECKSIG)
	            return sb            	
            }
            else if (res instanceof Reference) {
            	val sb = new P2PKHOutputScript()
	            sb.op(OP_DUP)
	              .op(OP_HASH160)
	              .addVariable(res.ref.name, DumpedPrivateKey)
	              .op(OP_EQUALVERIFY)
	              .op(OP_CHECKSIG)
	            return sb
            }
            else
            	throw new CompileException("unexpected result "+res)
            	
        } else if (outScript.isP2SH) {
            
            // get the redeem script to serialize
            var redeemScript = output.script.compileRedeemScript

            /* OP_HASH160 <script hash-160> OP_EQUAL */
            redeemScript
        } else if (outScript.isOpReturn) {
            var c = outScript.exp as StringLiteral
            var data = c.value.bytes

            /* OP_RETURN <bytes> */
            new OpReturnOutputScript(data)
        } else
            throw new UnsupportedOperationException
    }


	/**
	 * Return the redeem script (in the P2SH case) from the given output.
	 * 
	 * <p>
	 * This function is invoked to generate both the output script (hashing the result) and
	 * input script (pushing the bytes).
	 * <p>
	 * It also prepends a magic number and altstack instruction.
	 */
	def P2SHOutputScript compileRedeemScript(Script script) {
        
        var ctx = new Context
        
        // build the redeem script to serialize
        var redeemScript = new P2SHOutputScript()
        for (var i=script.params.size-1; i>=0; i--) {
            val p = script.params.get(i)
            var numberOfRefs = EcoreUtil2.getAllContentsOfType(script.exp, Reference).filter[v|v.ref==p].size 
            
            ctx.altstack.put(p, AltStackEntry.of(ctx.altstack.size, numberOfRefs))    // update the context
            
            redeemScript.op(OP_TOALTSTACK)
        }
        
        redeemScript.append(script.exp.compileExpression(ctx)).optimize() as P2SHOutputScript
	}
	
    
    
    
    
    
    
	def private ScriptBuilder2 compileExpression(ScriptExpression exp, Context ctx) {
        return exp.interpretSafe.compileExpressionInternal(ctx)
    }

	// default
    def private dispatch ScriptBuilder2 compileExpressionInternal(ScriptExpression exp, Context ctx) {
        throw new CompileException
    }

	/*
	 * Literals 
	 */
    def private dispatch ScriptBuilder2 compileExpressionInternal(NumberLiteral n, Context ctx) {
        new ScriptBuilder2().number(n.value)
    }

    def private dispatch ScriptBuilder2 compileExpressionInternal(BooleanLiteral n, Context ctx) {
        if(n.isTrue) new ScriptBuilder2().op(OP_TRUE)
    	else         new ScriptBuilder2().number(OP_FALSE)
    }

    def private dispatch ScriptBuilder2 compileExpressionInternal(StringLiteral s, Context ctx) {
        new ScriptBuilder2().data(s.value.bytes)
    }

    def private dispatch ScriptBuilder2 compileExpressionInternal(HashLiteral s, Context ctx) {
        new ScriptBuilder2().data(s.value)
    }
    
    def private dispatch ScriptBuilder2 compileExpressionInternal(KeyLiteral k, Context ctx) {
        new ScriptBuilder2().data(DumpedPrivateKey.fromBase58(null, k.value).key.pubKey)
    }
    
    def private dispatch ScriptBuilder2 compileExpressionInternal(SignatureLiteral s, Context ctx) {
        new ScriptBuilder2().data(BitcoinUtils.decode(s.value))
    }
    
    def private dispatch ScriptBuilder2 compileExpressionInternal(Signature stmt, Context ctx) {
		var wif = stmt.key.interpretSafe(KeyLiteral).value
		var key = DumpedPrivateKey.fromBase58(stmt.networkParams, wif).getKey();
        var hashType = stmt.modifier.toHashType
        var anyoneCanPay = stmt.modifier.toAnyoneCanPay
        var sb = new ScriptBuilder2().signaturePlaceholder(key, hashType, anyoneCanPay)
        sb
    }

    def private dispatch ScriptBuilder2 compileExpressionInternal(Hash160 hash, Context ctx) {
	    var sb = hash.value.compileExpression(ctx)
		sb.op(OP_HASH160)
    }

    def private dispatch ScriptBuilder2 compileExpressionInternal(Hash256 hash, Context ctx) {
	    var sb = hash.value.compileExpression(ctx)
        sb.op(OP_HASH256)
    }
    
        def private dispatch ScriptBuilder2 compileExpressionInternal(Ripemd160 hash, Context ctx) {
	    var sb = hash.value.compileExpression(ctx)
        sb.op(OP_RIPEMD160)
    }
    
    def private dispatch ScriptBuilder2 compileExpressionInternal(Sha256 hash, Context ctx) {
	    var sb = hash.value.compileExpression(ctx)
        sb.op(OP_SHA256)
    }

    def private dispatch ScriptBuilder2 compileExpressionInternal(AfterTimeLock stmt, Context ctx) {
        var sb = new ScriptBuilder2()
        sb.number(stmt.timelock.value)
        sb.op(OP_CHECKLOCKTIMEVERIFY)
        sb.op(OP_DROP)
        sb.append(stmt.continuation.compileExpression(ctx))
    }

    def private dispatch ScriptBuilder2 compileExpressionInternal(AndScriptExpression stmt, Context ctx) {
    	var sb = stmt.left.compileExpression(ctx)
        sb.op(OP_IF)
        sb.append(stmt.right.compileExpression(ctx))
        sb.op(OP_ELSE)	// short circuit
        val f = BitcoinTMFactory.eINSTANCE.createBooleanLiteral
        f.setTrue(false)
        sb.append(compileExpression(f,ctx))
        sb.op(OP_ENDIF)
    }

    def private dispatch ScriptBuilder2 compileExpressionInternal(OrScriptExpression stmt, Context ctx) {
    	var sb = stmt.left.compileExpression(ctx)
        sb.op(OP_IF) // short circuit
        val f = BitcoinTMFactory.eINSTANCE.createBooleanLiteral
        f.setTrue(true)
        sb.append(compileExpression(f,ctx))
        sb.op(OP_ELSE)
        sb.append(stmt.right.compileExpression(ctx))
        sb.op(OP_ENDIF)
    }

    def private dispatch ScriptBuilder2 compileExpressionInternal(ScriptPlus stmt, Context ctx) {
        var sb = stmt.left.compileExpression(ctx)
        sb.append(stmt.right.compileExpression(ctx))
        sb.op(OP_ADD)
    }

    def private dispatch ScriptBuilder2 compileExpressionInternal(ScriptMinus stmt, Context ctx) {
        var sb = stmt.left.compileExpression(ctx)
        sb.append(stmt.right.compileExpression(ctx))
        sb.op(OP_SUB)
    }

    def private dispatch ScriptBuilder2 compileExpressionInternal(Max stmt, Context ctx) {
	    var sb = stmt.left.compileExpression(ctx)
	    sb.append(stmt.right.compileExpression(ctx))
	    sb.op(OP_MAX)
    }

    def private dispatch ScriptBuilder2 compileExpressionInternal(Min stmt, Context ctx) {
        var sb = stmt.left.compileExpression(ctx)
        sb.append(stmt.right.compileExpression(ctx))
        sb.op(OP_MIN)
    }

    def private dispatch ScriptBuilder2 compileExpressionInternal(Size stmt, Context ctx) {
        var sb = stmt.value.compileExpression(ctx)
        sb.op(OP_SIZE)
    }

    def private dispatch ScriptBuilder2 compileExpressionInternal(ScriptBooleanNegation stmt, Context ctx) {
        var sb = stmt.exp.compileExpression(ctx)
        sb.op(OP_NOT)            
    }

    def private dispatch ScriptBuilder2 compileExpressionInternal(ScriptArithmeticSigned stmt, Context ctx) {
        var sb = stmt.exp.compileExpression(ctx)
        sb.op(OP_NOT)
    }

    def private dispatch ScriptBuilder2 compileExpressionInternal(Between stmt, Context ctx) {
        var sb = stmt.value.compileExpression(ctx)
        sb.append(stmt.left.compileExpression(ctx))
        sb.append(stmt.right.compileExpression(ctx))
        sb.op(OP_WITHIN)
    }

    def private dispatch ScriptBuilder2 compileExpressionInternal(ScriptComparison stmt, Context ctx) {
        var sb = stmt.left.compileExpression(ctx)
        sb.append(stmt.right.compileExpression(ctx))

        switch (stmt.op) {
            case "<": sb.op(OP_LESSTHAN)
            case ">": sb.op(OP_GREATERTHAN)
            case "<=": sb.op(OP_LESSTHANOREQUAL)
            case ">=": sb.op(OP_GREATERTHANOREQUAL)
        }
    }
    
    def private dispatch ScriptBuilder2 compileExpressionInternal(ScriptEquals stmt, Context ctx) {
        var sb = stmt.left.compileExpression(ctx)
        sb.append(stmt.right.compileExpression(ctx))
        
        switch (stmt.op) {
            case "==": sb.op(OP_EQUAL)
            case "!=": sb.op(OP_EQUAL).op(OP_NOT)
        }
    }

    def private dispatch ScriptBuilder2 compileExpressionInternal(IfThenElse stmt, Context ctx) {
        var sb = stmt.^if.compileExpression(ctx)
        sb.op(OP_IF)
        sb.append(stmt.then.compileExpression(ctx))
        sb.op(OP_ELSE)
        sb.append(stmt.^else.compileExpression(ctx))
        sb.op(OP_ENDIF)            
    }

    def private dispatch ScriptBuilder2 compileExpressionInternal(Versig stmt, Context ctx) {
        if (stmt.pubkeys.size == 1) {
            var sb = stmt.signatures.get(0).compileExpression(ctx)
            sb.append((stmt.pubkeys.get(0) as ScriptExpression).compileExpression(ctx))
            sb.op(OP_CHECKSIG)
        } else {
            val sb = new ScriptBuilder2().number(OP_0)
            stmt.signatures.forEach[s|sb.append(s.compileExpression(ctx))]
            sb.number(stmt.signatures.size)
            stmt.pubkeys.forEach[k|
	            sb.append((k as ScriptExpression).compileExpression(ctx))
            ]
            sb.number(stmt.pubkeys.size)
            sb.op(OP_CHECKMULTISIG)
        }
    }
    
    
    def private dispatch ScriptBuilder2 compileExpressionInternal(Reference varRef, Context ctx) {
		return compileReferrable(varRef.ref, ctx)
    }
    
    
    def private dispatch ScriptBuilder2 compileReferrable(Referrable obj, Context ctx) {
    	throw new CompileException('''Cannot compile «obj.class»''')
    }
    
    def private dispatch ScriptBuilder2 compileReferrable(Constant const, Context ctx) {
    	val value = const.exp.interpretSafe
	        	
    	if (value instanceof Literal) {
        	return value.compileExpression(ctx)        		
    	}
    	else 
    		throw new CompileException('''Constant «const.name» does not evaluate to Literal value. This should be checked by validation.''')
    }
    
    def private dispatch ScriptBuilder2 compileReferrable(DeclarationLeft obj, Context ctx) {
		var refContainer = obj.eContainer
		if (refContainer instanceof Declaration) {        		
	        	val value = refContainer.right.value.interpretSafe
	        	
	        	if (value instanceof Literal) {
		        	return value.compileExpression(ctx)        		
	        	}
	        	else 
	        		throw new CompileException('''the right part of declaration «obj.name» does not evaluate to Literal value''')
        	}	        
        	else 
    			throw new CompileException('''unexpected class «refContainer»''')
    }
    
    def private dispatch ScriptBuilder2 compileReferrable(Parameter param, Context ctx) {
        /*
         * N: altezza dell'altstack
         * i: posizione della variabile interessata
         * 
         * OP_FROMALTSTACK( N - i )                svuota l'altstack fino a raggiungere x
         * 	                                       x ora è in cima al main stack
         * 
         * OP_DUP OP_TOALTSTACK        	           duplica x e lo rimanda sull'altstack
         * 
         * (OP_SWAP OP_TOALTSTACK)( N - i - 1 )    prende l'elemento sotto x e lo sposta sull'altstack
         * 
         */
    	var refContainer = param.eContainer
		if (refContainer instanceof Script) {
        	// script parameter
        	val sb = new ScriptBuilder2()
	        val pos = ctx.altstack.get(param).position
			var count = ctx.altstack.get(param).occurrences
	
	        if(pos === null) throw new CompileException;
	
	        (1 .. ctx.altstack.size - pos).forEach[x|sb.op(OP_FROMALTSTACK)]
	        
	        if (count==1) {
	        	// this is the last usage of the variable
	        	ctx.altstack.remove(param)							// remove the reference to its altstack position
	        	for (e : ctx.altstack.entrySet.filter[e|e.value.position>pos]) {	// update all the positions of the remaing elements
	        		ctx.altstack.put(e.key, AltStackEntry.of(e.value.position-1, e.value.occurrences))
	        	}
	        	
		        if (ctx.altstack.size - pos> 0)
		            (1 .. ctx.altstack.size - pos).forEach[x|sb.op(OP_SWAP).op(OP_TOALTSTACK)]
	        	
	        }
	        else {
	        	ctx.altstack.put(param, AltStackEntry.of(pos, count-1))
		        sb.op(OP_DUP).op(OP_TOALTSTACK)
	
		        if (ctx.altstack.size - pos - 1 > 0)
		            (1 .. ctx.altstack.size - pos - 1).forEach[x|sb.op(OP_SWAP).op(OP_TOALTSTACK)]	            
	        }
	        return sb
        }
        else if (param.isTxParameter) {
        	// transaction parameter
        	return new ScriptBuilder2().addVariable(param.name, param.type.convertType)
        }
    	else 
			throw new CompileException('''unexpected class «refContainer»''')
    }
    
    
}
