/*
 * Copyright 2019 Nicola Atzei
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package xyz.balzaclang.compiler

import com.google.inject.Inject
import java.util.ArrayList
import java.util.Collection
import org.apache.log4j.Logger
import org.eclipse.xtext.util.OnChangeEvictingCache
import xyz.balzaclang.balzac.Reference
import xyz.balzaclang.lib.model.Address
import xyz.balzaclang.lib.model.script.InputScript
import xyz.balzaclang.lib.model.script.primitives.Primitive
import xyz.balzaclang.lib.model.transaction.CoinbaseTransactionBuilder
import xyz.balzaclang.lib.model.transaction.ITransactionBuilder
import xyz.balzaclang.lib.model.transaction.SerialTransactionBuilder
import xyz.balzaclang.lib.model.transaction.TransactionBuilder
import xyz.balzaclang.utils.ASTUtils
import xyz.balzaclang.xsemantics.BalzacInterpreter
import xyz.balzaclang.xsemantics.Rho

import static extension xyz.balzaclang.utils.ASTExtensions.*
import xyz.balzaclang.lib.model.Hash
import xyz.balzaclang.lib.model.Signature
import xyz.balzaclang.lib.model.PrivateKey
import xyz.balzaclang.lib.model.PublicKey
import xyz.balzaclang.balzac.Transaction

class TransactionCompiler {

    static final Logger logger = Logger.getLogger(TransactionCompiler);

    @Inject extension BalzacInterpreter
    @Inject extension ASTUtils astUtils
    @Inject extension ScriptCompiler
    @Inject OnChangeEvictingCache cache;

    def ITransactionBuilder compileTransaction(Transaction tx, Rho rho) {
        logger.debug('''Compiling «tx.name». Rho «rho.entrySet.map[e|'''«e.key.name -> e.value.toString»''']»''')
        var key = 1;
        val prime = 31;
        key = prime * key + tx.hashCode
        key = prime * key + rho.hashCode
        return cache.get(key, tx.eResource, [
            logger.debug('''Cache hit. Generating...''')
            internalCompileTransaction(tx,rho)
        ])
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
                            logger.error("Unable to interpret relative timelock '"+locktime.nodeToString+"' with rho "+rho)
                            throw new CompileException("Unable to interpret relative timelock '"+locktime.nodeToString+"' with rho "+rho)
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
                logger.error("Unable to interpret output value '"+output.value.nodeToString+"' with rho "+rho+". Transaction: "+tx.name+".")
                throw new CompileException("Unable to interpret output value '"+output.value.nodeToString+"' with rho "+rho+". Transaction: "+tx.name+".")
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

    def private dispatch Collection<String> variables(TransactionBuilder builder) {
        return builder.variables
    }

    def private dispatch Collection<String> variables(ITransactionBuilder builder) {
        return new ArrayList<String>()
    }

    def private dispatch Collection<String> variables(SerialTransactionBuilder builder) {
        return new ArrayList<String>()
    }

    def private dispatch Collection<String> freeVariables(TransactionBuilder builder) {
        return builder.freeVariables
    }

    def private dispatch Collection<String> freeVariables(CoinbaseTransactionBuilder builder) {
        return new ArrayList<String>()
    }

    def private dispatch Collection<String> freeVariables(SerialTransactionBuilder builder) {
        return new ArrayList<String>()
    }

    def private dispatch ITransactionBuilder bindVariable(TransactionBuilder builder, String name, Object value) {
        return builder.bindVariable(name, value.toPrimitive)
    }

    def private dispatch ITransactionBuilder bindVariable(CoinbaseTransactionBuilder builder, String name, Primitive value) {
        logger.warn('''Unable to bind variable '«name»' and value '«value»' for CoinbaseTransactionBuilder''')
        return builder
    }

    def private dispatch ITransactionBuilder bindVariable(SerialTransactionBuilder builder, String name, Primitive value) {
        logger.warn('''Unable to bind variable '«name»' and value '«value»' for SerialTransactionBuilder''')
        return builder
    }

    def private dispatch Primitive toPrimitive(Number obj) {
        return Primitive.of(obj)
    }

    def private dispatch Primitive toPrimitive(String obj) {
        return Primitive.of(obj)
    }

    def private dispatch Primitive toPrimitive(Boolean obj) {
        return Primitive.of(obj)
    }

    def private dispatch Primitive toPrimitive(Hash obj) {
        return Primitive.of(obj)
    }

    def private dispatch Primitive toPrimitive(Signature obj) {
        return Primitive.of(obj)
    }

    def private dispatch Primitive toPrimitive(PrivateKey obj) {
        return Primitive.of(obj)
    }

    def private dispatch Primitive toPrimitive(PublicKey obj) {
        return Primitive.of(obj)
    }

    def private dispatch Primitive toPrimitive(Address obj) {
        return Primitive.of(obj)
    }

    def private dispatch Primitive toPrimitive(ITransactionBuilder obj) {
        return Primitive.of(obj)
    }

    def private dispatch Primitive toPrimitive(Object obj) {
        throw new RuntimeException('''Unable to convert object «obj» to a Primitive type''')
    }
}
