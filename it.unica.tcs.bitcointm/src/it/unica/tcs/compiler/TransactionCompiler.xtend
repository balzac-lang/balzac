/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.compiler

import com.google.inject.Inject
import it.unica.tcs.bitcoinTM.Parameter
import it.unica.tcs.bitcoinTM.Reference
import it.unica.tcs.bitcoinTM.Transaction
import it.unica.tcs.bitcoinTM.TransactionLiteral
import it.unica.tcs.lib.CoinbaseTransactionBuilder
import it.unica.tcs.lib.ITransactionBuilder
import it.unica.tcs.lib.TransactionBuilder
import it.unica.tcs.lib.script.InputScript
import it.unica.tcs.lib.script.InputScriptImpl
import it.unica.tcs.utils.ASTUtils
import it.unica.tcs.utils.CompilerUtils
import it.unica.tcs.xsemantics.BitcoinTMInterpreter
import it.unica.tcs.xsemantics.Rho
import it.unica.tcs.bitcoinTM.Constant

class TransactionCompiler {

    @Inject private extension BitcoinTMInterpreter
    @Inject private extension ASTUtils astUtils
    @Inject private extension ScriptCompiler
    @Inject private extension CompilerUtils

    def ITransactionBuilder compileTransaction(TransactionLiteral tx) {
        val txBuilder = ITransactionBuilder.fromSerializedTransaction(tx.networkParams, tx.value);
        return txBuilder
    }

    def ITransactionBuilder compileTransaction(Transaction tx, Rho rho) {

        println()
        println('''::: Compiling '«tx.name»'. Rho «rho.entrySet.map[e|'''«e.key.name -> e.value.toString»''']» ''')

        val tb =
            if (tx.isCoinbase) new CoinbaseTransactionBuilder(tx.networkParams)
            else new TransactionBuilder(tx.networkParams)

        // free variables
        for (param : tx.params) {
            if (!rho.containsKey(param)) {
                println('''freevar «param.name» : «param.type»''')
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
                val inScript = new InputScriptImpl().number(42) as InputScript
                tb.addInput(inScript)
            }
            else {
                /*
                 * compile parent transaction
                 */
                val parentTxExp = input.txRef

                // transaction literal
                if (parentTxExp instanceof TransactionLiteral) {
                    val parentTxCompiled = parentTxExp.compileTransaction   // recursive call
                    val outIndex = new Long(input.outpoint).intValue
                    val inScript = input.compileInput(parentTxCompiled, rho)

                    // relative timelock
                    if (tx.timelocks.containsRelative(parentTxCompiled, rho)) {
                        val locktime = tx.timelocks.getRelative(parentTxCompiled, rho)
                        tb.addInput(parentTxCompiled, outIndex, inScript, locktime.getSequenceNumber(rho))
                    }
                    else {
                        tb.addInput(parentTxCompiled, outIndex, inScript)
                    }
                }
                else if (parentTxExp instanceof Reference) {

                    val parentTxRef = parentTxExp.ref

                    /*
                     * Constant reference
                     */
                    if (parentTxRef instanceof Constant) {
                        val parentTxCompiled = (parentTxRef.exp as TransactionLiteral).compileTransaction   // recursive call
                        val outIndex = new Long(input.outpoint).intValue
                        val inScript = input.compileInput(parentTxCompiled, rho)


                        // relative timelock
                        if (tx.timelocks.containsRelative(parentTxCompiled, rho)) {
                            val locktime = tx.timelocks.getRelative(parentTxCompiled, rho)
                            tb.addInput(parentTxCompiled, outIndex, inScript, locktime.getSequenceNumber(rho))
                        }
                        else {
                            tb.addInput(parentTxCompiled, outIndex, inScript)
                        }
                    }
                    /*
                     * Parameter reference (input transaction is a parameter of the transaction)
                     */
                    else if (parentTxRef instanceof Parameter) {

                        println('''«tx.name»: input tx is a transaction parameter '«parentTxRef.name»' ''')

                        // rho contains the actual value
                        if (rho.containsKey(parentTxRef)) {
                            val parentTx = rho.get(parentTxRef) as ITransactionBuilder
                            val outIndex = new Long(input.outpoint).intValue
                            val inScript = input.compileInput(parentTx, rho)


                            // relative timelock
                            if (tx.timelocks.containsRelative(parentTx, rho)) {
                                val locktime = tx.timelocks.getRelative(parentTx, rho)
                                tb.addInput(parentTx, outIndex, inScript, locktime.getSequenceNumber(rho))
                            }
                            else {
                                tb.addInput(parentTx, outIndex, inScript)
                            }
                        }
                        else {
                            // compilation it's finalized within the hook
                            throw new CompileException("Rho must contain the actual value for "+parentTxRef.name)
                        }

                    }
                    /*
                     * Transaction reference
                     */
                    else if (parentTxRef instanceof Transaction) {

                        // prepare a new rho for the evaluation of the parent transaction
                        val newRho = new Rho

                        /*
                         * iterate over the actual parameters, in order to set the corresponding values
                         */

                        val parentTxFormalparams = parentTxRef.params

                        for (var j=0; j<parentTxExp.actualParams.size; j++) {
                            val formalP = parentTxFormalparams.get(j)
                            val actualP = parentTxExp.actualParams.get(j)

                            val res = actualP.interpret(rho)

                            if (!res.failed) {
                                val actualPvalue = res.first

                                println('''adding «formalP.name» -> «actualPvalue» to rho''')
                                newRho.put(formalP, actualPvalue)
                            }
                            else
                                throw new CompileException("Cannot interpret actual parameter", res.ruleFailedException)
                        }

                        /*
                         * Compile the parent tx reference with newRho
                         */
                        val parentTxCompiled = parentTxRef.compileTransaction(newRho)
                        println('''parent compiled («parentTxRef.name») vars=«parentTxCompiled.variables», fv=«parentTxCompiled.freeVariables»''')


                        for (var j=0; j<parentTxExp.actualParams.size; j++) {
                            val formalP = parentTxFormalparams.get(j)
                            val actualP = parentTxExp.actualParams.get(j)

                            // the tx parameters
                            val vs = actualP.getTxVariables

                            // set hooks for all unbound variables
                            val fvs = vs.filter[v|!rho.containsKey(v)]

                            // hook for unbound variables
                            val fvsNames = fvs.map[p|p.name].toSet

                            if (!fvsNames.empty) {
                                println('''«tx.name»: setting hook for variables «fvs»: variable '«formalP.name»' of parent tx «parentTxRef.name»''')

                                // this hook will be executed when all the tx variables will have been bound
                                // 'values' contains the bound values, we are now able to evaluate 'actualPvalue'
                                tb.addHookToVariableBinding(fvsNames, [ values |
                                    println('''«tx.name»: executing hook for variables '«fvsNames»'. Binding variable '«formalP.name»' parent tx «parentTxRef.name»''')
                                    println('''«tx.name»: values «values»''')

                                    // create a rho for the evaluation
                                    val newHookRho = new Rho
                                    for(fp : tx.params) {
                                        rho.put( fp, values.get(fp.name) )
                                    }
                                    println('''rho «newHookRho»''')
                                    // re-interpret actualP
                                    val res = actualP.interpret(newHookRho)

                                    if (res.failed)
                                        throw new CompileException("expecting an evaluation to Literal")

                                    val v = res.first
                                    parentTxCompiled.bindVariable(formalP.name, v)
                                ])
                            }
                        }

                        val outIndex = new Long(input.outpoint).intValue
                        val inScript = input.compileInput(parentTxCompiled, rho)

                        // relative timelock
                        if (tx.timelocks.containsRelative(parentTxCompiled, rho)) {
                            val locktime = tx.timelocks.getRelative(parentTxCompiled, rho)
                            tb.addInput(parentTxCompiled, outIndex, inScript, locktime.getSequenceNumber(rho))
                        }
                        else {
                            tb.addInput(parentTxCompiled, outIndex, inScript)
                        }
                    }
                    else
                        throw new CompileException("Unexpected class "+parentTxExp.class)
                }
                else
                    throw new CompileException("Unexpected class "+parentTxExp.class)
            }
        }

        // outputs
        for (output : tx.outputs) {
            val outScript = output.compileOutputScript(rho)
            val satoshis = output.value.exp.interpretE.first as Long
            tb.addOutput(outScript, satoshis)
        }

        // absolute timelock
        if (tx.timelocks.containsAbsolute) {
            val res = tx.timelocks.absolute.value.interpret(rho)

            if (res.failed)
                throw new CompileException("Cannot evaluate the absolute timelock.")

            tb.locktime = res.first as Long
        }


        // remove unused tx variables
//      tb.removeUnusedVariables()

        println('''::: Transaction '«tx.name»' compiled''')
        return tb
    }
}
