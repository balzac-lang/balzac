/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.compiler

import com.google.inject.Inject
import it.unica.tcs.bitcoinTM.Literal
import it.unica.tcs.bitcoinTM.Reference
import it.unica.tcs.bitcoinTM.Referrable
import it.unica.tcs.bitcoinTM.TransactionBody
import it.unica.tcs.bitcoinTM.TransactionDeclaration
import it.unica.tcs.bitcoinTM.TransactionLiteral
import it.unica.tcs.lib.CoinbaseTransactionBuilder
import it.unica.tcs.lib.ITransactionBuilder
import it.unica.tcs.lib.TransactionBuilder
import it.unica.tcs.lib.script.InputScript
import it.unica.tcs.lib.script.InputScriptImpl
import it.unica.tcs.utils.ASTUtils
import it.unica.tcs.utils.CompilerUtils
import it.unica.tcs.xsemantics.BitcoinTMInterpreter
import java.util.Map

import static com.google.common.base.Preconditions.*

class TransactionCompiler {
	
	@Inject private extension BitcoinTMInterpreter
    @Inject private extension ASTUtils astUtils
	@Inject private extension ScriptCompiler
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
    				val outScript = parentTxCompiled.getOutputs().get(outIndex).script
		    		val inScript = input.compileInput(outScript)
		    		
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
	    				val outScript = parentTxCompiled.getOutputs().get(outIndex).script
			    		val inScript = input.compileInput(outScript)
				    		
			    		
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
		    				val outScript = parentTxCompiled.getOutputs().get(outIndex).script
				    		val inScript = input.compileInput(outScript)
				    		
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
	    				val outScript = parentTxCompiled.getOutputs().get(outIndex).script
			    		val inScript = input.compileInput(outScript)
				    		
			    		
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
    		val outScript = output.compileOutputScript
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
    
}
