/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.compiler

import com.google.inject.Inject
import it.unica.tcs.bitcoinTM.AfterTimeLock
import it.unica.tcs.bitcoinTM.AndScriptExpression
import it.unica.tcs.bitcoinTM.Between
import it.unica.tcs.bitcoinTM.BooleanLiteral
import it.unica.tcs.bitcoinTM.Hash160
import it.unica.tcs.bitcoinTM.Hash256
import it.unica.tcs.bitcoinTM.HashLiteral
import it.unica.tcs.bitcoinTM.IfThenElse
import it.unica.tcs.bitcoinTM.Max
import it.unica.tcs.bitcoinTM.Min
import it.unica.tcs.bitcoinTM.NumberLiteral
import it.unica.tcs.bitcoinTM.OrScriptExpression
import it.unica.tcs.bitcoinTM.Ripemd160
import it.unica.tcs.bitcoinTM.ScriptArithmeticSigned
import it.unica.tcs.bitcoinTM.ScriptBooleanNegation
import it.unica.tcs.bitcoinTM.ScriptComparison
import it.unica.tcs.bitcoinTM.ScriptEquals
import it.unica.tcs.bitcoinTM.ScriptExpression
import it.unica.tcs.bitcoinTM.ScriptMinus
import it.unica.tcs.bitcoinTM.ScriptPlus
import it.unica.tcs.bitcoinTM.Sha256
import it.unica.tcs.bitcoinTM.Signature
import it.unica.tcs.bitcoinTM.Size
import it.unica.tcs.bitcoinTM.StringLiteral
import it.unica.tcs.bitcoinTM.TransactionDeclaration
import it.unica.tcs.bitcoinTM.VariableReference
import it.unica.tcs.bitcoinTM.Versig
import it.unica.tcs.lib.ScriptBuilder2
import it.unica.tcs.utils.ASTUtils
import it.unica.tcs.utils.CompilerUtils
import javax.inject.Singleton
import org.bitcoinj.core.DumpedPrivateKey

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
class ScriptExpressionCompiler {
	
	@Inject private extension CompilerUtils
    @Inject private extension ASTUtils astUtils
    
	def ScriptBuilder2 compileExpression(ScriptExpression exp, Context ctx) {
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
    
    def private dispatch ScriptBuilder2 compileExpressionInternal(Signature stmt, Context ctx) {
		var wif = stmt.key.value
		var key = DumpedPrivateKey.fromBase58(stmt.networkParams, wif).getKey();
        var hashType = stmt.modifier.toHashType
        var anyoneCanPay = stmt.modifier.toAnyoneCanPay
        var sb = new ScriptBuilder2().signaturePlaceholder(key, hashType, anyoneCanPay)
        sb
    }

    def private dispatch ScriptBuilder2 compileExpressionInternal(VariableReference varRef, Context ctx) {
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
        var param = varRef.ref
        var isTxParam = param.eContainer instanceof TransactionDeclaration 
        
        if (isTxParam) {
        	return new ScriptBuilder2().addVariable(param.name, param.paramType.convertType)
        }
        else {
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
        sb.append(stmt.right.compileExpression(ctx))
        sb.op(OP_BOOLAND)            
    }

    def private dispatch ScriptBuilder2 compileExpressionInternal(OrScriptExpression stmt, Context ctx) {
        var sb = stmt.left.compileExpression(ctx)
        sb.append(stmt.right.compileExpression(ctx))
        sb.op(OP_BOOLOR)            
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
            sb.data(stmt.pubkeys.get(0).value.privateKeyToPubkeyBytes(stmt.networkParams))
            sb.op(OP_CHECKSIG)
        } else {
            val sb = new ScriptBuilder2().number(OP_0)
            stmt.signatures.forEach[s|sb.append(s.compileExpression(ctx))]
            sb.number(stmt.signatures.size)
            stmt.pubkeys.forEach[k|sb.data(k.value.privateKeyToPubkeyBytes(stmt.networkParams))]
            sb.number(stmt.pubkeys.size)
            sb.op(OP_CHECKMULTISIG)
        }
    }
}
