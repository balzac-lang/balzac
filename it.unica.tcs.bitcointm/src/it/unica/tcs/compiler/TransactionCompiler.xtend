/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.compiler

import com.google.inject.Inject
import it.unica.tcs.balzac.Reference
import it.unica.tcs.balzac.Transaction
import it.unica.tcs.lib.model.CoinbaseTransactionBuilder
import it.unica.tcs.lib.model.ITransactionBuilder
import it.unica.tcs.lib.model.TransactionBuilder
import it.unica.tcs.lib.model.script.InputScript
import it.unica.tcs.utils.ASTUtils
import it.unica.tcs.utils.CompilerUtils
import it.unica.tcs.xsemantics.BalzacInterpreter
import it.unica.tcs.xsemantics.Rho
import org.apache.log4j.Logger
import org.eclipse.xtext.util.OnChangeEvictingCache

class TransactionCompiler {

    static final Logger logger = Logger.getLogger(TransactionCompiler);

    @Inject extension BalzacInterpreter
    @Inject extension ASTUtils astUtils
    @Inject extension ScriptCompiler
    @Inject extension CompilerUtils
    @Inject OnChangeEvictingCache cache;

    def ITransactionBuilder compileTransaction(Transaction tx, Rho rho) {
        logger.debug('''Compiling «tx.name». Rho «rho.entrySet.map[e|'''«e.key.name -> e.value.toString»''']»''')
        var key = 1;
        val prime = 31;
        key = prime * key + tx.hashCode 
        key = prime * key + rho.hashCode 
        return cache.get(key, tx.eResource, [ internalCompileTransaction(tx,rho) ])
    }

    def private ITransactionBuilder internalCompileTransaction(Transaction tx, Rho rho) {
    
        if (rho.isAlreadyVisited(tx)) {
            logger.error('''Transaction «tx.name» already visited. Cyclic dependency.''')
            throw new CompileException('''Transaction «tx.name» already visited. Cyclic dependency.''')
        }

        rho.addVisited(tx)

        logger.debug('''START . compiling «tx.name». Rho «rho.entrySet.map[e|'''«e.key.name -> e.value.toString»''']» ''')

        val tb =
            if (tx.isCoinbase) new CoinbaseTransactionBuilder(tx.networkParams)
            else new TransactionBuilder(tx.networkParams)

        // free variables
        for (param : tx.params) {
            if (!rho.containsKey(param)) {
                logger.debug('''freevar «param.name» : «param.type»''')
                tb.addVariable(param.name, param.type.convertType)
            }
        }

        // inputs
        for(input : tx.inputs) {
            if (tb instanceof CoinbaseTransactionBuilder) {
                /*
                 * This transaction is like a coinbase transaction.
                 * You can put the input you want.
                 */
                val inScript = InputScript.create().number(42)
                tb.addInput(inScript)
            }
            else {
                /*
                 * interpret parent transaction
                 */
                val res = input.txRef.interpret(rho)
                if (!res.failed) {
                    /*
                     * It succeed in three case. The parent is a
                     * - literal
                     * - constant reference
                     * - parameter reference (bound in rho)
                     * - tx reference (actual parameters bound in rho)
                     */
                    val parentTx = res.first as ITransactionBuilder
                    val outIndex = input.outpoint
                    val inScript = input.compileInputScript(parentTx, rho)

                    // relative timelock
                    if (tx.relLocks.containsRelativeForTx(parentTx, rho)) {
                        val locktime = tx.relLocks.getRelativeForTx(parentTx, rho)
                        val resL = locktime.exp.interpret(rho)
                        if (resL.failed || !(resL.first instanceof Long)) {
                            logger.error("Unable to interpret relative timelock "+locktime.nodeToString+" with rho "+rho)
                            throw new CompileException("Unable to interpret relative timelock "+locktime.nodeToString+" with rho "+rho)
                        }
                        val value = resL.first as Long
                        tb.addInput(parentTx, outIndex, inScript, value.getSequenceNumber(locktime.isBlock, rho))
                    }
                    else {
                        tb.addInput(parentTx, outIndex, inScript)
                    }
                }
                else {
                    /*
                     * Transaction reference (with free parameters)
                     */

                    if (!((input.txRef instanceof Reference) &&                      // for sure it is a Reference
                        ((input.txRef as Reference).ref instanceof Transaction))     // and it must be a Transaction
                    ) {
                        logger.error("Input transaction expected to be a Transaction Reference")
                        throw new CompileException("Input transaction expected to be a Transaction Reference")
                    }

                    val parentTxRef = input.txRef as Reference
                    val parentTx = parentTxRef.ref as Transaction

                    /*
                     * recursively compile using this method, that allows free variables for tx builder
                     */

                    // we use a fresh rho to avoid confusion, since some actual parameters might be bound and other not 
                    val parentTxB = compileTransaction(parentTx, rho.fresh)
                    logger.trace('''input tx compiled: name=«parentTx.name» vars=«parentTxB.variables», fv=«parentTxB.freeVariables»''')

                    // now iterate over the actual parameters, in order to bound them into the builder
                    for (var j=0; j<parentTxRef.actualParams.size; j++) {
                        val formalP = parentTx.params.get(j)
                        val actualP = parentTxRef.actualParams.get(j)

                        val resP = actualP.interpret(rho)

                        if (!resP.failed) {
                            val actualPvalue = resP.first
                            logger.trace('''binding «formalP.name» -> «actualPvalue»''')
                            parentTxB.bindVariable(formalP.name, actualPvalue)
                        }
                    }

                    // The unbound actual parameters must be references (or expression containing references)
                    // to unbound tx parameters.
                    // For each actual parameters, get the txs parameters it depends on, and set an hook.
                    for (var j=0; j<parentTxRef.actualParams.size; j++) {
                        val formalP = parentTx.params.get(j)
                        val actualP = parentTxRef.actualParams.get(j)

                        // get the tx parameters of actualP
                        val vs = actualP.getTxVariables

                        // set hooks for all unbound variables
                        val fvs = vs.filter[v | tb.isFree(v.name)]

                        // hook for unbound variables
                        val fvsNames = fvs.map[p|p.name].toSet

                        logger.trace('''expression #«j» depends on «fvsNames»''')

                        if (!fvsNames.empty) {
                            logger.trace('''setting hook for variables «fvsNames», to then evaluate a value for '«formalP.name»' of parent tx «parentTx.name»''')

                            // this hook will be executed when all the tx variables will have been bound
                            // 'values' contains the bound values, we are now able to evaluate 'actualPvalue'
                            tb.addHookToVariableBinding(fvsNames, [ values |
                                logger.trace('''«tx.name»: executing hook for variables '«fvsNames»'. Binding variable '«formalP.name»' parent tx «parentTx.name»''')
                                logger.trace('''«tx.name»: values «values»''')

                                // create a rho for the evaluation
                                val newHookRho = rho.fresh
                                for(fp : tx.params) {
                                    newHookRho.put( fp, values.get(fp.name) )
                                }
                                logger.trace('''rho «newHookRho»''')
                                // re-interpret actualP
                                val resP = actualP.interpret(newHookRho)

                                if (resP.failed) {
                                    logger.error("expecting an evaluation to Literal")
                                    throw new CompileException("expecting an evaluation to Literal")
                                }

                                val v = resP.first
                                parentTxB.bindVariable(formalP.name, v)
                            ])
                        }
                    }

                    val outIndex = input.outpoint
                    val inScript = input.compileInputScript(parentTxB, rho)

                    // relative timelock
                    if (tx.relLocks.containsRelativeForTx(parentTxB, rho)) {
                        val locktime = tx.relLocks.getRelativeForTx(parentTxB, rho)
                        val resL = locktime.exp.interpret(rho)
                        if (resL.failed || !(resL.first instanceof Long)) {
                            logger.error("Unable to interpret relative timelock "+locktime.nodeToString+" with rho "+rho)
                            throw new CompileException("Unable to interpret relative timelock "+locktime.nodeToString+" with rho "+rho)
                        }
                        val value = resL.first as Long
                        tb.addInput(parentTxB, outIndex, inScript, value.getSequenceNumber(locktime.isBlock, rho))
                    }
                    else {
                        tb.addInput(parentTxB, outIndex, inScript)
                    }
                }
            }
        }

        // outputs
        for (output : tx.outputs) {
            val outScript = output.compileOutputScript(rho)
            val res = output.value.interpret(rho)
            if (res.failed || !(res.first instanceof Long)) {
                logger.error("Unable to interpret output value "+output.value.nodeToString+" with rho "+rho)
                throw new CompileException("Unable to interpret output value "+output.value.nodeToString+" with rho "+rho)
            }
            val satoshis = res.first as Long
            tb.addOutput(outScript, satoshis)
        }

        // absolute timelock
        if (tx.absLock !== null) {
            val res = tx.absLock.exp.interpret(rho)

            if (res.failed || !(res.first instanceof Long)) {
                logger.error("Unable to interpret absolute timelock "+tx.absLock.exp.nodeToString)                
                throw new CompileException("Unable to interpret absolute timelock "+tx.absLock.exp.nodeToString)
            }

            tb.locktime = res.first as Long
        }

        // remove unused tx variables
//      tb.removeUnusedVariables()

        logger.debug('''END . «tx.name» compiled. vars=«tb.variables», fv=«tb.freeVariables», bv=«tb.boundVariables»''')

        rho.removeVisited(tx)

        return tb
    }
}
