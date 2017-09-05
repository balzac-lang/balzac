package it.unica.tcs.compiler

import com.google.inject.Inject
import it.unica.tcs.bitcoinTM.Expression
import it.unica.tcs.bitcoinTM.Input
import it.unica.tcs.bitcoinTM.Output
import it.unica.tcs.bitcoinTM.Parameter
import it.unica.tcs.bitcoinTM.Script
import it.unica.tcs.bitcoinTM.SerialTxBody
import it.unica.tcs.bitcoinTM.Signature
import it.unica.tcs.bitcoinTM.StringLiteral
import it.unica.tcs.bitcoinTM.TransactionDeclaration
import it.unica.tcs.bitcoinTM.UserDefinedTxBody
import it.unica.tcs.bitcoinTM.VariableReference
import it.unica.tcs.bitcoinTM.Versig
import it.unica.tcs.bitcointm.lib.CoinbaseTransactionBuilder
import it.unica.tcs.bitcointm.lib.ITransactionBuilder
import it.unica.tcs.bitcointm.lib.ScriptBuilder2
import it.unica.tcs.bitcointm.lib.TransactionBuilder
import it.unica.tcs.utils.ASTUtils
import it.unica.tcs.xsemantics.BitcoinTMTypeSystem
import org.bitcoinj.script.Script.ScriptType
import org.bitcoinj.script.ScriptBuilder
import org.eclipse.xtext.EcoreUtil2

import static org.bitcoinj.script.ScriptOpCodes.*

import static extension it.unica.tcs.utils.ASTUtils.*
import static extension it.unica.tcs.utils.BitcoinJUtils.*
import static extension it.unica.tcs.utils.CompilerUtils.*

class TransactionCompiler {
	
	@Inject private extension BitcoinTMTypeSystem typeSystem
    @Inject private extension ASTUtils astUtils
	@Inject private extension ExpressionCompiler expGenerator
	@Inject private extension Optimizer optimizer
	
	def ITransactionBuilder compileTransaction(TransactionDeclaration txDecl) {
    	return txDecl.body.compileTransactionBody;
    }
    
    def dispatch ITransactionBuilder compileTransactionBody(UserDefinedTxBody tx) {
    	
    	val tb =  
    		if (tx.isCoinbase) new CoinbaseTransactionBuilder	    	
	    	else new TransactionBuilder
    	
    	// free variables
    	for (param : tx.params) {
    		tb.freeVariable(param.name, param.paramType.convertType)
    	} 		
    	
    	// inputs
    	for(input : tx.inputs) {
    		if (tb instanceof CoinbaseTransactionBuilder) {
    			val inScript = input.compileInput
    			tb.addInput(inScript)
    		}
    		else {    			
	    		val parentTx = input.txRef.tx.compileTransaction	// recursive call
	    		val outIndex = input.txRef.idx
	    		val inScript = input.compileInput
	    		
	    		// relative timelock
	    		if (tx.tlock!==null && tx.tlock.containsRelative(tx.eContainer as TransactionDeclaration)) {
					val locktime = tx.tlock.getRelative(tx.eContainer as TransactionDeclaration)
					tb.addInput(parentTx, outIndex, inScript, locktime)
	    		}
	    		else {
	    			tb.addInput(parentTx, outIndex, inScript)
	    		}
    		}
    		
    	}
    	
    	// outputs
    	for (output : tx.outputs) {
    		val outScript = output.compileOutput
    		val satoshis = output.value.exp.interpret.first as Integer
    		tb.addOutput(outScript, satoshis)
    	}
    	
    	// absolute timelock
    	if (tx.tlock!==null && tx.tlock.containsAbsolute)
	        tb.locktime = tx.tlock.getAbsolute()
    	
    	return tb
    }
    
    def dispatch ITransactionBuilder compileTransactionBody(SerialTxBody body) {
    	return ITransactionBuilder.fromSerializedTransaction(body.networkParams, body.bytes);
    }

    def ScriptBuilder2 compileInput(Input stmt) {

		val Context ctx = new Context

		if (stmt.isPlaceholder) {
 			/*
             * This transaction is like a coinbase transaction.
             * You can put the input you want.
             */
            return new ScriptBuilder2().number(42)
		}
		else {
	        var outIdx = stmt.txRef.idx
			switch stmt.txRef.tx.body {
				
				/*
				 * User defined transaction.
				 * Return the expected inputs based on the kind of output script
				 */
				UserDefinedTxBody: {
					var inputTx = stmt.txRef.tx.body as UserDefinedTxBody       
		            var output = inputTx.outputs.get(outIdx);
		    
		            if (output.script.isP2PKH) {
		                var sig = stmt.exps.get(0).simplifySafe as Signature
		                var pubkey = sig.key.body.pvt.value.privateKeyToPubkeyBytes(stmt.networkParams)
		                
		                var sb = sig.compileInputExpression(ctx)
		                sb.data(pubkey)
		    
		                /* <sig> <pubkey> */
		                sb
		            } else if (output.script.isP2SH) {
		            	
		                val sb = new ScriptBuilder2()
		                
		                // build the list of expression pushes (actual parameters) 
		                stmt.exps.forEach[e|
		                	sb.append(e.simplifySafe.compileInputExpression(ctx))
		                ]
		                
		                // get the redeem script to push
		                var redeemScript = output.script.getRedeemScript(ctx)
		                
		                sb.data(redeemScript.build.program)
		                                
		                /* <e1> ... <en> <serialized script> */
		                sb
		            } else
		                throw new CompileException
	            }
	            
				/*
				 * Serialized transaction.
				 * Return the expected inputs based on the kind of output script
				 */
	            SerialTxBody: {
	            	var output = stmt.txRef.tx.compileTransaction.toTransaction(stmt.networkParams).getOutput(outIdx)
	            
		            if (output.scriptPubKey.isSentToAddress) {
		                var sig = (stmt.exps.get(0) as Expression).simplifySafe as Signature
		                var pubkey = sig.key.body.pvt.value.privateKeyToPubkeyBytes(stmt.networkParams)
		                
		                var sb = sig.compileInputExpression(ctx)
		                sb.data(pubkey)
		    
		                /* <sig> <pubkey> */
		                sb
		            } else if (output.scriptPubKey.isPayToScriptHash) {
		                
		                val sb = new ScriptBuilder2()
		                
		                // build the list of expression pushes (actual parameters) 
		                stmt.exps.forEach[e|
		                	sb.append(e.simplifySafe.compileInputExpression(ctx))
		                ]
		                
		                // get the redeem script to push
		                var redeemScript = stmt.redeemScript.getRedeemScript(ctx)
		                sb.data(redeemScript.build.program)
		                
		                /* <e1> ... <en> <serialized script> */
		                sb
		            } else
		                throw new CompileException
	            }
	            default: throw new CompileException("Unexpected class "+stmt.txRef.tx.body.class)
			}			
		}
    }

	/**
	 * Compile an output based on its "type".
	 */
    def private ScriptBuilder2 compileOutput(Output output) {
		
		val Context ctx = new Context
        var outScript = output.script

        if (outScript.isP2PKH) {
            var versig = outScript.exp.simplifySafe as Versig
            var pk = versig.pubkeys.get(0).body.pub.value.wifToAddress(output.networkParams)

            var script = ScriptBuilder.createOutputScript(pk)

            if (script.scriptType != ScriptType.P2PKH)
                throw new CompileException

            /* OP_DUP OP_HASH160 <pkHash> OP_EQUALVERIFY OP_CHECKSIG */
            new ScriptBuilder2().append(script)
        } else if (outScript.isP2SH) {
            
            // get the redeem script to serialize
            var redeemScript = output.script.getRedeemScript(ctx)
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
	def private ScriptBuilder2 getRedeemScript(Script script, Context ctx) {
        
        if (!ctx.altstack.isEmpty)
        	throw new CompileException("Altstack must be empty.")
        
        // build the redeem script to serialize
        var sb = new ScriptBuilder2()
        for (var i=script.params.size-1; i>=0; i--) {
            val Parameter p = script.params.get(i)
            var numberOfRefs = EcoreUtil2.getAllContentsOfType(script.exp, VariableReference).filter[v|v.ref==p].size 
            
            ctx.altstack.put(p, AltStackEntry.of(ctx.altstack.size, numberOfRefs))    // update the context
            
            sb.op(OP_TOALTSTACK)
        }
        
        sb.append(script.exp.simplifySafe.compileExpression(ctx)).optimize
	}
	
	
	/**
	 * Compile an input expression. It must not have free variables
	 */
    def private ScriptBuilder2 compileInputExpression(Expression exp, Context ctx) {
        var refs = EcoreUtil2.getAllContentsOfType(exp, VariableReference)
        
        // the altstack is used only by VariableReference(s)
        if (!refs.empty)
        	throw new CompileException("The given expression must not have free variables.")
        
        if (!ctx.altstack.isEmpty)
        	throw new CompileException("Altstack must be empty.")
        
        exp.simplifySafe.compileExpression(ctx).optimize
    }
}
