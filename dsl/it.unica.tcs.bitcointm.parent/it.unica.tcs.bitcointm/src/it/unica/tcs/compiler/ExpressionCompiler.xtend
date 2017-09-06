package it.unica.tcs.compiler

import com.google.inject.Inject
import it.unica.tcs.bitcoinTM.AfterTimeLock
import it.unica.tcs.bitcoinTM.AndExpression
import it.unica.tcs.bitcoinTM.ArithmeticSigned
import it.unica.tcs.bitcoinTM.Between
import it.unica.tcs.bitcoinTM.BooleanLiteral
import it.unica.tcs.bitcoinTM.BooleanNegation
import it.unica.tcs.bitcoinTM.Comparison
import it.unica.tcs.bitcoinTM.Equals
import it.unica.tcs.bitcoinTM.Expression
import it.unica.tcs.bitcoinTM.Hash
import it.unica.tcs.bitcoinTM.HashLiteral
import it.unica.tcs.bitcoinTM.IfThenElse
import it.unica.tcs.bitcoinTM.KeyDeclaration
import it.unica.tcs.bitcoinTM.Max
import it.unica.tcs.bitcoinTM.Min
import it.unica.tcs.bitcoinTM.Minus
import it.unica.tcs.bitcoinTM.NumberLiteral
import it.unica.tcs.bitcoinTM.OrExpression
import it.unica.tcs.bitcoinTM.Plus
import it.unica.tcs.bitcoinTM.Signature
import it.unica.tcs.bitcoinTM.Size
import it.unica.tcs.bitcoinTM.StringLiteral
import it.unica.tcs.bitcoinTM.UserDefinedTxBody
import it.unica.tcs.bitcoinTM.VariableReference
import it.unica.tcs.bitcoinTM.Versig
import it.unica.tcs.bitcointm.lib.ScriptBuilder2
import it.unica.tcs.xsemantics.BitcoinTMTypeSystem
import org.bitcoinj.core.DumpedPrivateKey
import org.bitcoinj.core.Transaction.SigHash
import org.bitcoinj.script.ScriptBuilder

import static org.bitcoinj.script.ScriptOpCodes.*

import static extension it.unica.tcs.utils.BitcoinJUtils.*
import static extension it.unica.tcs.utils.CompilerUtils.*

/*
 * EXPRESSIONS
 * 
 * N.B. the compiler tries to simplify simple expressions like
 * <ul> 
 *  <li> 1+2 ≡ 3
 *  <li> if (12==10+2) then "foo" else "bar" ≡ "foo"
 * </ul>
 */
class ExpressionCompiler {
	
	@Inject private extension BitcoinTMTypeSystem typeSystem
	
    def dispatch ScriptBuilder2 compileExpression(Expression exp, Context ctx) {
        throw new CompileException
    }
    
    def dispatch ScriptBuilder2 compileExpression(KeyDeclaration stmt, Context ctx) {
        /* push the public key */
        val pvtkey = stmt.body.pvt.value
        val key = DumpedPrivateKey.fromBase58(stmt.networkParams, pvtkey).key

        new ScriptBuilder2().data(key.pubKey)
    }

    def dispatch ScriptBuilder2 compileExpression(Hash hash, Context ctx) {
        var res = typeSystem.interpret(hash)
        
        if (res.failed) {
		    var sb = hash.value.compileExpression(ctx)
	        
	        switch(hash.type) {
	        	case "sha256":	 	sb.op(OP_SHA256)
	        	case "ripemd160":	sb.op(OP_RIPEMD160)
	        	case "hash256":	 	sb.op(OP_HASH256)
	        	case "hash160":		sb.op(OP_HASH160)
	        }
        }
        else {
        	new ScriptBuilder2().append(res)
        }
    }

    def dispatch ScriptBuilder2 compileExpression(AfterTimeLock stmt, Context ctx) {
        var sb = new ScriptBuilder()
        sb.number(stmt.timelock.value)
        sb.op(OP_CHECKLOCKTIMEVERIFY)
        sb.op(OP_DROP)
        stmt.continuation.compileExpression(ctx)
    }

    def dispatch ScriptBuilder2 compileExpression(AndExpression stmt, Context ctx) {
        var res = typeSystem.interpret(stmt)
        
        if (res.failed) {
	        var sb = stmt.left.compileExpression(ctx)
	        sb.append(stmt.right.compileExpression(ctx))
	        sb.op(OP_BOOLAND)            
        }
        else {
        	new ScriptBuilder2().append(res)
        }
    }

    def dispatch ScriptBuilder2 compileExpression(OrExpression stmt, Context ctx) {
        var res = typeSystem.interpret(stmt)
        
        if (res.failed) {
	        var sb = stmt.left.compileExpression(ctx)
	        sb.append(stmt.right.compileExpression(ctx))
	        sb.op(OP_BOOLOR)            
        }
        else {
        	new ScriptBuilder2().append(res)
        }
    }

    def dispatch ScriptBuilder2 compileExpression(Plus stmt, Context ctx) {
        var res = typeSystem.interpret(stmt)
        
        if (res.failed) {
            var sb = stmt.left.compileExpression(ctx)
            sb.append(stmt.right.compileExpression(ctx))
            sb.op(OP_ADD)
        }
        else {
        	new ScriptBuilder2().append(res)
        }
    }

    def dispatch ScriptBuilder2 compileExpression(Minus stmt, Context ctx) {
        var res = typeSystem.interpret(stmt)
        
        if (res.failed) {
            var sb = stmt.left.compileExpression(ctx)
            sb.append(stmt.right.compileExpression(ctx))
            sb.op(OP_SUB)
        }
        else {
        	new ScriptBuilder2().append(res)
        }
    }

    def dispatch ScriptBuilder2 compileExpression(Max stmt, Context ctx) {
        var res = typeSystem.interpret(stmt)
        
        if (res.failed) {
            var sb = stmt.left.compileExpression(ctx)
            sb.append(stmt.right.compileExpression(ctx))
            sb.op(OP_MAX)
        }
        else {
        	new ScriptBuilder2().append(res)
        }
    }

    def dispatch ScriptBuilder2 compileExpression(Min stmt, Context ctx) {
        var res = typeSystem.interpret(stmt)
        
        if (res.failed) {
            var sb = stmt.left.compileExpression(ctx)
            sb.append(stmt.right.compileExpression(ctx))
            sb.op(OP_MIN)
        }
        else {
        	new ScriptBuilder2().append(res)
        }
    }

    def dispatch ScriptBuilder2 compileExpression(Size stmt, Context ctx) {
        var res = typeSystem.interpret(stmt)
        
        if (res.failed) {
	        var sb = stmt.value.compileExpression(ctx)
	        sb.op(OP_SIZE)
        }
        else {
        	new ScriptBuilder2().append(res)
        }
        
    }

    def dispatch ScriptBuilder2 compileExpression(BooleanNegation stmt, Context ctx) {
        var res = typeSystem.interpret(stmt)
        
        if (res.failed) {
            var sb = stmt.exp.compileExpression(ctx)
            sb.op(OP_NOT)            
        }
        else {
        	new ScriptBuilder2().append(res)
        }
    }

    def dispatch ScriptBuilder2 compileExpression(ArithmeticSigned stmt, Context ctx) {
        var res = typeSystem.interpret(stmt)
        
        if (res.failed) {
            var sb = stmt.exp.compileExpression(ctx)
            sb.op(OP_NOT)
        }
        else {
        	new ScriptBuilder2().append(res)
        }
    }

    def dispatch ScriptBuilder2 compileExpression(Between stmt, Context ctx) {
        var res = typeSystem.interpret(stmt)
        
        if (res.failed) {
            var sb = stmt.value.compileExpression(ctx)
            sb.append(stmt.left.compileExpression(ctx))
            sb.append(stmt.right.compileExpression(ctx))
            sb.op(OP_WITHIN)
        }
        else {
        	new ScriptBuilder2().append(res)
        }
    }

    def dispatch ScriptBuilder2 compileExpression(Comparison stmt, Context ctx) {
        var res = typeSystem.interpret(stmt)
        
        if (res.failed) {
            var sb = stmt.left.compileExpression(ctx)
            sb.append(stmt.right.compileExpression(ctx))
    
            switch (stmt.op) {
                case "<": sb.op(OP_LESSTHAN)
                case ">": sb.op(OP_GREATERTHAN)
                case "<=": sb.op(OP_LESSTHANOREQUAL)
                case ">=": sb.op(OP_GREATERTHANOREQUAL)
            }
        }
        else {
        	new ScriptBuilder2().append(res)
        }
    }
    
    def dispatch ScriptBuilder2 compileExpression(Equals stmt, Context ctx) {
        var res = typeSystem.interpret(stmt)
        
        if (res.failed) {
            var sb = stmt.left.compileExpression(ctx)
            sb.append(stmt.right.compileExpression(ctx))
            
            switch (stmt.op) {
                case "==": sb.op(OP_EQUAL)
                case "!=": sb.op(OP_EQUAL).op(OP_NOT)
            }
        }
        else {
        	new ScriptBuilder2().append(res)
        }
    }

    def dispatch ScriptBuilder2 compileExpression(IfThenElse stmt, Context ctx) {
        var res = typeSystem.interpret(stmt)
        
        if (res.failed) {
            var sb = stmt.^if.compileExpression(ctx)
            sb.op(OP_IF)
            sb.append(stmt.then.compileExpression(ctx))
            sb.op(OP_ELSE)
            sb.append(stmt.^else.compileExpression(ctx))
            sb.op(OP_ENDIF)            
        }
        else {
        	new ScriptBuilder2().append(res)
        }
    }

    def dispatch ScriptBuilder2 compileExpression(Versig stmt, Context ctx) {
        if (stmt.pubkeys.size == 1) {
            var sb = stmt.signatures.get(0).compileExpression(ctx)
            sb.append(stmt.pubkeys.get(0).compileExpression(ctx))
            sb.op(OP_CHECKSIG)
        } else {
            val sb = new ScriptBuilder2().number(OP_0)
            stmt.signatures.forEach[s|sb.append(s.compileExpression(ctx))]
            sb.number(stmt.signatures.size)
            stmt.pubkeys.forEach[k|sb.append(k.compileExpression(ctx))]
            sb.number(stmt.pubkeys.size)
            sb.op(OP_CHECKMULTISIG)
        }
    }

    def dispatch ScriptBuilder2 compileExpression(NumberLiteral n, Context ctx) {
        new ScriptBuilder2().number(n.value)
    }

    def dispatch ScriptBuilder2 compileExpression(BooleanLiteral n, Context ctx) {
        if(n.isTrue)
        	new ScriptBuilder2().op(OP_TRUE)
    	else 
    		new ScriptBuilder2().number(OP_FALSE)
    }

    def dispatch ScriptBuilder2 compileExpression(StringLiteral s, Context ctx) {
        new ScriptBuilder2().data(s.value.bytes)
    }
    
    def dispatch ScriptBuilder2 compileExpression(HashLiteral s, Context ctx) {
        new ScriptBuilder2().data(s.value)
    }

    def dispatch ScriptBuilder2 compileExpression(Signature stmt, Context ctx) {
        
		var wif = stmt.key.body.pvt.value
		
		var key = DumpedPrivateKey.fromBase58(stmt.networkParams, wif).getKey();
        var hashType = switch(stmt.modifier) {
                case AIAO,
                case SIAO: SigHash.ALL
                case AISO,
                case SISO: SigHash.SINGLE
                case AINO,
                case SINO: SigHash.NONE
            }
        var anyoneCanPay = switch(stmt.modifier) {
                case SIAO,
                case SISO,
                case SINO: true
                case AIAO,
                case AISO,
                case AINO: false
            }
            
        // store an empty value
        var sb = new ScriptBuilder2().signaturePlaceholder(key, hashType, anyoneCanPay)
        sb
    }

    def dispatch ScriptBuilder2 compileExpression(VariableReference varRef, Context ctx) {
        
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
        
        var isTxParam = param.eContainer instanceof UserDefinedTxBody 
        
        if (isTxParam) {
        	return new ScriptBuilder2().freeVariable(param.name, param.paramType.convertType)
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
}
