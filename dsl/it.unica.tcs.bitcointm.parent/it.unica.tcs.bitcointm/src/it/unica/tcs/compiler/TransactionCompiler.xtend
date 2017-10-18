/*
 * Copyright 2017 Nicola Atzei
 */

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
import it.unica.tcs.lib.CoinbaseTransactionBuilder
import it.unica.tcs.lib.ITransactionBuilder
import it.unica.tcs.lib.InputScript
import it.unica.tcs.lib.InputScriptImpl
import it.unica.tcs.lib.OpReturnOutputScript
import it.unica.tcs.lib.OutputScript
import it.unica.tcs.lib.P2PKHOutputScript
import it.unica.tcs.lib.P2SHInputScript
import it.unica.tcs.lib.P2SHOutputScript
import it.unica.tcs.lib.TransactionBuilder
import it.unica.tcs.lib.Wrapper.NetworkParametersWrapper
import it.unica.tcs.lib.client.BitcoinClientI
import it.unica.tcs.lib.utils.BitcoinUtils
import it.unica.tcs.utils.ASTUtils
import it.unica.tcs.utils.CompilerUtils
import it.unica.tcs.xsemantics.BitcoinTMTypeSystem
import org.eclipse.xtext.EcoreUtil2

import static org.bitcoinj.script.ScriptOpCodes.*

class TransactionCompiler {
	
	@Inject private BitcoinClientI bitcoin;
	@Inject private extension BitcoinTMTypeSystem typeSystem
    @Inject private extension ASTUtils astUtils
	@Inject private extension ExpressionCompiler expGenerator
//	@Inject private extension Optimizer optimizer
    @Inject private extension CompilerUtils
    
    def dispatch ITransactionBuilder compileTransaction(SerialTransactionDeclaration tx) {
    	val txBuilder = 
	    	if (tx.bytes!==null) {
				ITransactionBuilder.fromSerializedTransaction(tx.networkParams, tx.bytes);
			}
			else {
				ITransactionBuilder.fromSerializedTransaction(tx.networkParams, bitcoin.getRawTransaction(tx.id));
			}
		println()
		println('''::: Compiling '«tx.name»' ''')
		println('''«txBuilder.toTransaction»''')
		return txBuilder			
	}
    
    def dispatch ITransactionBuilder compileTransaction(UserTransactionDeclaration tx) {
   	
    	println()
		println('''::: Compiling '«tx.name»' ''')
		
		val tb =  
    		if (tx.isCoinbase) new CoinbaseTransactionBuilder(NetworkParametersWrapper.wrap(tx.networkParams))    	
	    	else new TransactionBuilder(NetworkParametersWrapper.wrap(tx.networkParams))
    	
    	// free variables
    	for (param : tx.params) {
    		println('''freevar «param.name» : «param.paramType»''')
    		tb.addVariable(param.name, param.paramType.convertType)
    	} 		
    	
    	// inputs
    	for(input : tx.body.inputs) {
    		if (tb instanceof CoinbaseTransactionBuilder) {
    			/*
	             * This transaction is like a coinbase transaction.
	             * You can put the input you want.
	             */
    			val inScript = new InputScriptImpl().number(42) as InputScript
    			tb.addInput(inScript)
    		}
    		else {    			
	    		val parentTx = input.txRef.tx.compileTransaction	// recursive call
	    		val outIndex = input.outpoint
	    		val inScript = input.compileInput(parentTx)
	    		
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
    

    def private InputScript compileInput(Input input, ITransactionBuilder parentTx) {

        var outIdx = input.outpoint
        var inputTx = input.txRef.tx
        
        
        if (parentTx.getOutputs().get(outIdx).script.isP2PKH) {
        	/*
        	 * P2PKH
        	 */
        	var sig = input.exps.get(0).simplifySafe as Signature
            var pubkey = sig.key.value.privateKeyToPubkeyBytes(input.networkParams)
            
            var sb = sig.compileInputExpression
            sb.data(pubkey)

            /* <sig> <pubkey> */
            return new InputScriptImpl().append(sb) as InputScript
        }
        else if (parentTx.getOutputs().get(outIdx).script.isP2SH) {
        	/*
        	 * P2SH
        	 */
        	val redeemScript = 
	        	if (inputTx instanceof SerialTransactionDeclaration) {
	        		// get the redeem script from the AST (specified by the user)
	                val s = input.redeemScript.getRedeemScript
	                
	                if (!s.ready)
		                throw new CompileException("This redeem script cannot have free variables")
		            
		            s
	        	}
	        	else if (inputTx instanceof UserTransactionDeclaration) {
	        		parentTx.getOutputs.get(outIdx).script as P2SHOutputScript
	        	}
	        	
	        val p2sh = new P2SHInputScript(redeemScript)
                
            // build the list of expression pushes (actual parameters) 
            input.exps.forEach[e|
            	p2sh.append(e.simplifySafe.compileInputExpression)
            ]
            
            /* <e1> ... <en> <serialized script> */
            return p2sh
        }
        else
            throw new CompileException("cannot redeem OP_RETURN outputs")
    }

	/**
	 * Compile an output based on its "type".
	 */
    def private OutputScript compileOutput(Output output) {
		
        var outScript = output.script

        if (outScript.isP2PKH) {
            var versig = outScript.exp.simplifySafe as Versig
            var pk = BitcoinUtils.wifToECKey(versig.pubkeys.get(0).value, output.networkParams).toAddress(output.networkParams)

            /* OP_DUP OP_HASH160 <pkHash> OP_EQUALVERIFY OP_CHECKSIG */
            new P2PKHOutputScript(pk)
        } else if (outScript.isP2SH) {
            
            // get the redeem script to serialize
            var redeemScript = output.script.getRedeemScript

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
	def private P2SHOutputScript getRedeemScript(Script script) {
        
        var ctx = new Context
        
        // build the redeem script to serialize
        var redeemScript = new P2SHOutputScript()
        for (var i=script.params.size-1; i>=0; i--) {
            val Parameter p = script.params.get(i)
            var numberOfRefs = EcoreUtil2.getAllContentsOfType(script.exp, VariableReference).filter[v|v.ref==p].size 
            
            ctx.altstack.put(p, AltStackEntry.of(ctx.altstack.size, numberOfRefs))    // update the context
            
            redeemScript.op(OP_TOALTSTACK)
        }
        
        redeemScript.append(script.exp.compileExpression(ctx)) as P2SHOutputScript
	}
	
	
	/**
	 * Compile an input expression. It must not have free variables
	 */
    def private InputScript compileInputExpression(Expression exp) {
        var refs = EcoreUtil2.getAllContentsOfType(exp, VariableReference)
        			.filter[ref|ref.eContainer instanceof TransactionDeclaration]
        
        // the altstack is used only by VariableReference(s)
        if (!refs.empty)
        	throw new CompileException("The given expression must not have free variables.")
        
        new InputScriptImpl().append(exp.compileExpression(new Context)) as InputScript
    }
}
