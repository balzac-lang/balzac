/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.compiler

import com.google.inject.Inject
import it.unica.tcs.bitcoinTM.Input
import it.unica.tcs.bitcoinTM.KeyLiteral
import it.unica.tcs.bitcoinTM.Literal
import it.unica.tcs.bitcoinTM.Output
import it.unica.tcs.bitcoinTM.Reference
import it.unica.tcs.bitcoinTM.Referrable
import it.unica.tcs.bitcoinTM.Script
import it.unica.tcs.bitcoinTM.ScriptExpression
import it.unica.tcs.bitcoinTM.Signature
import it.unica.tcs.bitcoinTM.StringLiteral
import it.unica.tcs.bitcoinTM.TransactionBody
import it.unica.tcs.bitcoinTM.TransactionDeclaration
import it.unica.tcs.bitcoinTM.TransactionLiteral
import it.unica.tcs.bitcoinTM.Versig
import it.unica.tcs.lib.CoinbaseTransactionBuilder
import it.unica.tcs.lib.ITransactionBuilder
import it.unica.tcs.lib.TransactionBuilder
import it.unica.tcs.lib.script.InputScript
import it.unica.tcs.lib.script.InputScriptImpl
import it.unica.tcs.lib.script.OpReturnOutputScript
import it.unica.tcs.lib.script.OutputScript
import it.unica.tcs.lib.script.P2PKHOutputScript
import it.unica.tcs.lib.script.P2SHInputScript
import it.unica.tcs.lib.script.P2SHOutputScript
import it.unica.tcs.lib.utils.BitcoinUtils
import it.unica.tcs.utils.ASTUtils
import it.unica.tcs.utils.CompilerUtils
import it.unica.tcs.xsemantics.BitcoinTMInterpreter
import java.util.Map
import org.bitcoinj.core.DumpedPrivateKey
import org.eclipse.xtext.EcoreUtil2

import static com.google.common.base.Preconditions.*
import static org.bitcoinj.script.ScriptOpCodes.*

class TransactionCompiler {
	
	@Inject private extension BitcoinTMInterpreter
    @Inject private extension ASTUtils astUtils
	@Inject private extension ScriptCompiler expGenerator
    @Inject private extension CompilerUtils
    
	def dispatch ITransactionBuilder compileTransaction(TransactionLiteral tx) {    	
    	val txBuilder = ITransactionBuilder.fromSerializedTransaction(tx.networkParams, tx.value);
		return txBuilder			
	}
	    
    def dispatch ITransactionBuilder compileTransaction(TransactionDeclaration tx) {
   	
    	println()
		println('''::: Compiling '«tx.left.name»' ''')
		
		val txBody = tx.right.value as TransactionBody
		
		val tb =  
    		if (tx.isCoinbase) new CoinbaseTransactionBuilder(tx.networkParams)
	    	else new TransactionBuilder(tx.networkParams)
    	
    	// free variables
    	for (param : tx.left.params) {
    		println('''freevar «param.name» : «param.type»''')
    		tb.addVariable(param.name, param.type.convertType)
    	} 		
    	
    	// inputs
    	for(input : txBody.inputs) {
    		if (tb instanceof CoinbaseTransactionBuilder) {
    			/*
	             * This transaction is like a coinbase transaction.
	             * You can put the input you want.
	             */
    			val inScript = new InputScriptImpl().number(42) as InputScript
    			tb.addInput(inScript)
    		}
    		else { 
    			/*
    			 * compile parent transaction
    			 */
    			val parentTx = input.txRef
    			
    			// transaction literal
    			if (parentTx instanceof TransactionLiteral) {
    				val parentTxCompiled = parentTx.compileTransaction	// recursive call
    				val outIndex = new Long(input.outpoint).intValue
		    		val inScript = input.compileInput(parentTxCompiled)
		    		
		    		// relative timelock
		    		if (txBody.tlock!==null && txBody.tlock.containsRelative(tx)) {
						val locktime = txBody.tlock.getRelative(tx)
						tb.addInput(parentTxCompiled, outIndex, inScript, locktime)
		    		}
		    		else {
		    			tb.addInput(parentTxCompiled, outIndex, inScript)
		    		}
    			}
    			else if (parentTx instanceof Reference) {
    				
    				if (parentTx.ref.isTx) {
    					
    					val parentTxCompiled = parentTx.ref.txDeclaration.compileTransaction	// recursive call
			    		println('''parent compiled («parentTx.ref.txDeclaration.left.name») vars=«parentTxCompiled.variables», fv=«parentTxCompiled.freeVariables»''')
			    		
		    			val parentTx2 = parentTx.ref.eContainer as TransactionDeclaration
		    			val parentTxFormalparams = parentTx2.left.params
		    			
			    		/*
			    		 * iterate over the actual parameters, in order to set the corresponding values
			    		 */
						for (var j=0; j<parentTx.actualParams.size; j++) {
							val formalP = parentTxFormalparams.get(j)
							val actualP = parentTx.actualParams.get(j)
							
							if (parentTxCompiled.hasVariable(formalP.name)) { // unused free-variables have been removed 
								
								val fvs = actualP.getTxVariables
								if (fvs.empty) {
									// the expression can be interpret to obtain a value (Literal or TransactionRef)
									val actualPvalue = actualP.interpret(newHashMap).first
									println('''«tx.left.name»: setting value «actualPvalue» for variable '«formalP.name»' of parent tx «parentTx2.left.name»''')
									parentTxCompiled.bindVariable(formalP.name, actualPvalue)
								}
								else {
									val fvsNames = fvs.map[p|p.name].toSet
									println('''«tx.left.name»: setting hook for variables «fvs»: variable '«formalP.name»' of parent tx «parentTx2.left.name»''')
									
									// this hook will be executed when all the tx variables will have been bound
									// 'values' contains the bound values, we are now able to evaluate 'actualPvalue' 
									tb.addHookToVariableBinding(fvsNames, [ values |
										println('''«tx.left.name»: executing hook for variables '«fvsNames»'. Binding variable '«formalP.name»' parent tx «parentTx2.left.name»''')
										println('''«tx.left.name»: values «values»''')
		
										// create a rho for the evaluation
										val Map<Referrable,Object> rho = newHashMap
										for(fp : tx.left.params) {
											rho.put( fp, values.get(fp.name) )	
										}
										println('''rho «rho»''')
										// re-interpret actualP
										val actualPvalue2 = actualP.interpretSafe(rho)
										
										if (!(actualPvalue2 instanceof Literal))
											throw new CompileException("expecting an evaluation to Literal")
										
										val v = actualP.interpret(rho).first
										parentTxCompiled.bindVariable(formalP.name, v)
									])
								}
							}
		    			}
    					val outIndex = new Long(input.outpoint).intValue
			    		val inScript = input.compileInput(parentTxCompiled)
			    		
			    		// relative timelock
			    		if (txBody.tlock!==null && txBody.tlock.containsRelative(tx)) {
							val locktime = txBody.tlock.getRelative(tx)
							tb.addInput(parentTxCompiled, outIndex, inScript, locktime)
			    		}
			    		else {
			    			tb.addInput(parentTxCompiled, outIndex, inScript)
			    		}
    				}
    				else if (parentTx.ref.isTxParameter) {
    					
    					val param = parentTx.ref.getTxParameter
			    		println('''«tx.left.name»: input tx is a transaction paramenter '«param.name»' ''')
									
			    		// compilation it's finalized within the hook
			    		tb.addHookToVariableBinding(newHashSet(param.name), [ values |
							println('''«tx.left.name»: executing hook for variable '«param.name»' ''')
							println('''«tx.left.name»: values «values»''')
							
							val paramTx = values.get(param.name)
							checkState(paramTx !== null)
							checkState(paramTx instanceof ITransactionBuilder)
							
							val parentTxCompiled = paramTx as ITransactionBuilder
			    			val outIndex = new Long(input.outpoint).intValue
				    		val inScript = input.compileInput(parentTxCompiled)
				    		
				    		// relative timelock
				    		if (txBody.tlock!==null && txBody.tlock.containsRelative(tx)) {
								val locktime = txBody.tlock.getRelative(tx)
								tb.addInput(parentTxCompiled, outIndex, inScript, locktime)
				    		}
				    		else {
				    			tb.addInput(parentTxCompiled, outIndex, inScript)
				    		}
						])
					}
					else if (parentTx.ref.isTxLiteral){
			            val parentTxCompiled = parentTx.ref.getTxLiteral.compileTransaction	// recursive call
	    				val outIndex = new Long(input.outpoint).intValue
			    		val inScript = input.compileInput(parentTxCompiled)
			    		
			    		// relative timelock
			    		if (txBody.tlock!==null && txBody.tlock.containsRelative(tx)) {
							val locktime = txBody.tlock.getRelative(tx)
							tb.addInput(parentTxCompiled, outIndex, inScript, locktime)
			    		}
			    		else {
			    			tb.addInput(parentTxCompiled, outIndex, inScript)
			    		}
			        }
    			}
    		}
    	}
    	
    	// outputs
    	for (output : txBody.outputs) {
    		val outScript = output.compileOutput
    		val satoshis = output.value.exp.interpret(newHashMap).first as Long
    		tb.addOutput(outScript, satoshis)
    	}
    	
    	// absolute timelock
    	if (txBody.tlock!==null && txBody.tlock.containsAbsolute)
	        tb.locktime = txBody.tlock.getAbsolute()
    	
    	
    	// remove unused tx variables
//    	tb.removeUnusedVariables()	// TODO il bug è qui, viene rimosso il parametro tx
    	
		println('''::: Transaction '«tx.left.name»' compiled''')    	
    	return tb
    }
    

    def private InputScript compileInput(Input input, ITransactionBuilder parentTx) {

        var outIdx = new Long(input.outpoint).intValue
        
        if (parentTx.getOutputs().get(outIdx).script.isP2PKH) {
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
        else if (parentTx.getOutputs().get(outIdx).script.isP2SH) {
        	
        	var inputTx = input.txRef
        	
        	/*
        	 * P2SH
        	 */
        	val redeemScript = 
	        	if (inputTx instanceof TransactionLiteral) {
	        		// get the redeem script from the AST (specified by the user)
	                val s = input.redeemScript.getRedeemScript
	                
	                if (!s.ready)
		                throw new CompileException("This redeem script cannot have free variables")
		            
		            s
	        	}
	        	else if (inputTx instanceof Reference) {
	        		
				if (inputTx.ref.isTx || inputTx.ref.isTxParameter) {
					parentTx.getOutputs.get(outIdx).script as P2SHOutputScript
				}
					else if (inputTx.ref.isTxLiteral){
						// get the redeem script from the AST (specified by the user)
		                val s = input.redeemScript.getRedeemScript
		                
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
	 * Compile an output based on its "type".
	 */
    def private OutputScript compileOutput(Output output) {
		
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
            val p = script.params.get(i)
            var numberOfRefs = EcoreUtil2.getAllContentsOfType(script.exp, Reference).filter[v|v.ref==p].size 
            
            ctx.altstack.put(p, AltStackEntry.of(ctx.altstack.size, numberOfRefs))    // update the context
            
            redeemScript.op(OP_TOALTSTACK)
        }
        
        redeemScript.append(script.exp.compileExpression(ctx)).optimize() as P2SHOutputScript
	}
	
	
	/**
	 * Compile an input expression. It must not have free variables
	 */
    def private InputScript compileInputExpression(ScriptExpression exp) {
        // the altstack is used only by VariableReference(s)
		var ctx = new Context
        new InputScriptImpl().append(exp.compileExpression(ctx)) as InputScript
    }
}
