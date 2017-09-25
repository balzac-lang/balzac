package it.unica.tcs.compiler

import com.google.inject.Inject
import it.unica.tcs.bitcoinTM.Expression
import it.unica.tcs.bitcoinTM.Input
import it.unica.tcs.bitcoinTM.Output
import it.unica.tcs.bitcoinTM.Parameter
import it.unica.tcs.bitcoinTM.Script
import it.unica.tcs.bitcoinTM.SerialTransactionDeclaration
import it.unica.tcs.bitcoinTM.Signature
import it.unica.tcs.bitcoinTM.StringLiteral
import it.unica.tcs.bitcoinTM.TransactionDeclaration
import it.unica.tcs.bitcoinTM.UserTransactionDeclaration
import it.unica.tcs.bitcoinTM.VariableReference
import it.unica.tcs.bitcoinTM.Versig
import it.unica.tcs.bitcointm.lib.CoinbaseTransactionBuilder
import it.unica.tcs.bitcointm.lib.ITransactionBuilder
import it.unica.tcs.bitcointm.lib.ScriptBuilder2
import it.unica.tcs.bitcointm.lib.TransactionBuilder
import it.unica.tcs.utils.ASTUtils
import it.unica.tcs.utils.CompilerUtils
import it.unica.tcs.xsemantics.BitcoinTMTypeSystem
import org.bitcoinj.script.Script.ScriptType
import org.bitcoinj.script.ScriptBuilder
import org.eclipse.xtext.EcoreUtil2

import static org.bitcoinj.script.ScriptOpCodes.*

import static extension it.unica.tcs.utils.Utils2.*
import static extension it.unica.tcs.bitcointm.lib.utils.BitcoinJUtils.*

class TransactionCompiler {
	
	@Inject private extension BitcoinTMTypeSystem typeSystem
    @Inject private extension ASTUtils astUtils
	@Inject private extension ExpressionCompiler expGenerator
//	@Inject private extension Optimizer optimizer
    @Inject private extension CompilerUtils
    
    def dispatch ITransactionBuilder compileTransaction(SerialTransactionDeclaration tx) {
		return ITransactionBuilder.fromSerializedTransaction(tx.networkParams, tx.bytes);
	}
    
    def dispatch ITransactionBuilder compileTransaction(UserTransactionDeclaration tx) {
   	
    	val tb =  
    		if (tx.isCoinbase) new CoinbaseTransactionBuilder	    	
	    	else new TransactionBuilder
    	
    	// free variables
    	for (param : tx.params) {
    		tb.freeVariable(param.name, param.paramType.convertType)
    	} 		
    	
    	// inputs
    	for(input : tx.body.inputs) {
    		if (tb instanceof CoinbaseTransactionBuilder) {
    			val inScript = input.compileInput
    			tb.addInput(inScript)
    		}
    		else {    			
	    		val parentTx = input.txRef.tx.compileTransaction	// recursive call
	    		val outIndex = input.outpoint
	    		val inScript = input.compileInput
	    		
	    		// relative timelock
	    		if (tx.body.tlock!==null && tx.body.tlock.containsRelative(tx)) {
					val locktime = tx.body.tlock.getRelative(tx)
					tb.addInput(parentTx, outIndex, inScript, locktime)
	    		}
	    		else {
	    			tb.addInput(parentTx, outIndex, inScript)
	    		}
    		}
    		
    	}
    	
    	// outputs
    	for (output : tx.body.outputs) {
    		val outScript = output.compileOutput
    		val satoshis = output.value.exp.interpret.first as Integer
    		tb.addOutput(outScript, satoshis)
    	}
    	
    	// absolute timelock
    	if (tx.body.tlock!==null && tx.body.tlock.containsAbsolute)
	        tb.locktime = tx.body.tlock.getAbsolute()
    	
    	return tb
    }
    

    def ScriptBuilder2 compileInput(Input input) {

		if (input.isPlaceholder) {
 			/*
             * This transaction is like a coinbase transaction.
             * You can put the input you want.
             */
            return new ScriptBuilder2().number(42)
		}
		else {
	        
	        var outIdx = input.outpoint
	        var inputTx = input.txRef.tx
	        
	        if (inputTx instanceof SerialTransactionDeclaration) {
	        	/*
	        	 * Serial transaction
	        	 */
	        	var output = input.txRef.tx.compileTransaction.toTransaction(input.networkParams).getOutput(outIdx)
	            
	            if (output.scriptPubKey.isSentToAddress) {
	                var sig = (input.exps.get(0) as Expression).simplifySafe as Signature
	                var pubkey = sig.key.value.privateKeyToPubkeyBytes(input.networkParams)
	                
	                var sb = sig.compileInputExpression
	                sb.data(pubkey)
	    
	                /* <sig> <pubkey> */
	                return sb
	            } else if (output.scriptPubKey.isPayToScriptHash) {
	                
	                val sb = new ScriptBuilder2()
	                
	                // build the list of expression pushes (actual parameters) 
	                input.exps.forEach[e|
	                	sb.append(e.simplifySafe.compileInputExpression)
	                ]
	                
	                // get the redeem script to push
	                var redeemScript = input.redeemScript.getRedeemScript
	                sb.data(redeemScript.build.program)
	                
	                /* <e1> ... <en> <serialized script> */
	                return sb
	            } else
	                throw new CompileException
	        }
	        else if (inputTx instanceof UserTransactionDeclaration) {
	        	/*
				 * User defined transaction.
				 * Return the expected inputs based on the kind of output script
				 */
	            var output = inputTx.body.outputs.get(outIdx);
	    
	            if (output.script.isP2PKH) {
	            	
	                var sig = input.exps.get(0).simplifySafe as Signature
	                var pubkey = sig.key.value.privateKeyToPubkeyBytes(input.networkParams)
	                
	                var sb = sig.compileInputExpression
	                sb.data(pubkey)
	    
	                /* <sig> <pubkey> */
	                return sb
	            } else if (output.script.isP2SH) {
	            	
	                val sb = new ScriptBuilder2()
	                
	                // build the list of expression pushes (actual parameters) 
	                input.exps.forEach[e|
	                	sb.append(e.simplifySafe.compileInputExpression)
	                ]
	                
	                // get the redeem script to push
	                var redeemScript = output.script.getRedeemScript
	                
	                sb.data(redeemScript.build.program)
	                                
	                /* <e1> ... <en> <serialized script> */
	                return sb
	            } else
	                throw new CompileException
	        }
		}
    }

	/**
	 * Compile an output based on its "type".
	 */
    def ScriptBuilder2 compileOutput(Output output) {
		
        var outScript = output.script

        if (outScript.isP2PKH) {
            var versig = outScript.exp.simplifySafe as Versig
            var pk = versig.pubkeys.get(0).value.wifToECKey(output.networkParams).toAddress(output.networkParams)

            var script = ScriptBuilder.createOutputScript(pk)

            if (script.scriptType != ScriptType.P2PKH)
                throw new CompileException

            /* OP_DUP OP_HASH160 <pkHash> OP_EQUALVERIFY OP_CHECKSIG */
            new ScriptBuilder2().append(script)
        } else if (outScript.isP2SH) {
            
            // get the redeem script to serialize
            var redeemScript = output.script.getRedeemScript
            var script = ScriptBuilder.createP2SHOutputScript(redeemScript.build)

            if (script.scriptType != ScriptType.P2SH)
                throw new CompileException

            /* OP_HASH160 <script hash-160> OP_EQUAL */
            new ScriptBuilder2().append(script)
        } else if (outScript.isOpReturn) {
            var c = outScript.exp as StringLiteral
            var script = ScriptBuilder.createOpReturnScript(c.value.bytes)

            if (script.scriptType != ScriptType.NO_TYPE)
                throw new CompileException

            /* OP_RETURN <bytes> */
            new ScriptBuilder2().append(script)
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
	def private ScriptBuilder2 getRedeemScript(Script script) {
        
        var ctx = new Context
        
        // build the redeem script to serialize
        var sb = new ScriptBuilder2()
        for (var i=script.params.size-1; i>=0; i--) {
            val Parameter p = script.params.get(i)
            var numberOfRefs = EcoreUtil2.getAllContentsOfType(script.exp, VariableReference).filter[v|v.ref==p].size 
            
            ctx.altstack.put(p, AltStackEntry.of(ctx.altstack.size, numberOfRefs))    // update the context
            
            sb.op(OP_TOALTSTACK)
        }
        
        sb.append(script.exp.compileExpression(ctx))
	}
	
	
	/**
	 * Compile an input expression. It must not have free variables
	 */
    def private ScriptBuilder2 compileInputExpression(Expression exp) {
        var refs = EcoreUtil2.getAllContentsOfType(exp, VariableReference)
        			.filter[ref|ref.eContainer instanceof TransactionDeclaration]
        
        // the altstack is used only by VariableReference(s)
        if (!refs.empty)
        	throw new CompileException("The given expression must not have free variables.")
        
        exp.compileExpression(new Context)
    }
}
