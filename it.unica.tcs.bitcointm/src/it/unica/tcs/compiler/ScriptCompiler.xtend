/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.compiler

import com.google.inject.Inject
import it.unica.tcs.balzac.AndExpression
import it.unica.tcs.balzac.ArithmeticSigned
import it.unica.tcs.balzac.BalzacFactory
import it.unica.tcs.balzac.Between
import it.unica.tcs.balzac.BooleanLiteral
import it.unica.tcs.balzac.BooleanNegation
import it.unica.tcs.balzac.CheckBlock
import it.unica.tcs.balzac.CheckBlockDelay
import it.unica.tcs.balzac.CheckDate
import it.unica.tcs.balzac.CheckTimeDelay
import it.unica.tcs.balzac.Comparison
import it.unica.tcs.balzac.Constant
import it.unica.tcs.balzac.DateLiteral
import it.unica.tcs.balzac.Equals
import it.unica.tcs.balzac.Expression
import it.unica.tcs.balzac.Hash160
import it.unica.tcs.balzac.Hash256
import it.unica.tcs.balzac.HashLiteral
import it.unica.tcs.balzac.IfThenElse
import it.unica.tcs.balzac.Input
import it.unica.tcs.balzac.Interpretable
import it.unica.tcs.balzac.Literal
import it.unica.tcs.balzac.Max
import it.unica.tcs.balzac.Min
import it.unica.tcs.balzac.Minus
import it.unica.tcs.balzac.NumberLiteral
import it.unica.tcs.balzac.OrExpression
import it.unica.tcs.balzac.Output
import it.unica.tcs.balzac.Parameter
import it.unica.tcs.balzac.Plus
import it.unica.tcs.balzac.Reference
import it.unica.tcs.balzac.Referrable
import it.unica.tcs.balzac.Ripemd160
import it.unica.tcs.balzac.Script
import it.unica.tcs.balzac.ScriptParameter
import it.unica.tcs.balzac.Sha1
import it.unica.tcs.balzac.Sha256
import it.unica.tcs.balzac.Signature
import it.unica.tcs.balzac.SignatureLiteral
import it.unica.tcs.balzac.SignaturePlaceholder
import it.unica.tcs.balzac.Size
import it.unica.tcs.balzac.StringLiteral
import it.unica.tcs.balzac.TransactionParameter
import it.unica.tcs.balzac.Versig
import it.unica.tcs.lib.ECKeyStore
import it.unica.tcs.lib.ITransactionBuilder
import it.unica.tcs.lib.SerialTransactionBuilder
import it.unica.tcs.lib.TransactionBuilder
import it.unica.tcs.lib.script.AbstractScriptBuilderWithVar.ScriptBuilderWithVar
import it.unica.tcs.lib.script.InputScript
import it.unica.tcs.lib.script.OutputScript
import it.unica.tcs.lib.utils.BitcoinUtils
import it.unica.tcs.utils.ASTUtils
import it.unica.tcs.utils.CompilerUtils
import it.unica.tcs.xsemantics.BalzacInterpreter
import it.unica.tcs.xsemantics.Rho
import it.unica.tcs.xsemantics.interpreter.Address
import it.unica.tcs.xsemantics.interpreter.PrivateKey
import it.unica.tcs.xsemantics.interpreter.PublicKey
import javax.inject.Singleton
import org.eclipse.xtext.EcoreUtil2

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
class ScriptCompiler {

    @Inject extension CompilerUtils
    @Inject extension ASTUtils
    @Inject extension BalzacInterpreter

    /**
     * Compile the given input (AST) to an input script class (lib).
     * It could include the redeem-script.
     * @param input the input to compile
     * @param parentTx the (compiled) parent transaction of the input
     * @param rho the environment for the interpreter
     * @return the compiled input
     * @see InputScript
     */
    def InputScript compileInputScript(Input input, ITransactionBuilder parentTx, Rho rho) {
        val outpoint = input.outpoint
        val outScript = parentTx.outputs.get(outpoint).script
        
        if (outScript.isP2PKH) {
            /*
             * P2PKH
             */
            var sigE = input.exps.get(0)
            if (sigE instanceof Signature) {
                var sig = input.exps.get(0) as Signature
                var sb = sig.compileInputExpression(rho)

                val resKey = sig.privkey.interpret(rho)

                if (resKey.failed)
                    throw new CompileException('''Unable to evaluate to a private key''')

                val key = resKey.first
                if (key instanceof PrivateKey) {
                    sb.data(key.publicKeyByte)
                }
                else {
                    throw new CompileException('''Unable to evaluate to a private key: «key»''')
                }

                /* <sig> <pubkey> */
                return InputScript.create().append(sb)
            }
            else {
                val sigI = input.exps.get(0).interpret(rho)
                val pubkeyI = input.exps.get(1).interpret(rho)
                
                if (sigI.failed)
                    throw new CompileException('''Unable to evaluate to a valid signature''')

                if (pubkeyI.failed)
                    throw new CompileException('''Unable to evaluate to a valid pubkey''')

                if (!(sigI.first instanceof it.unica.tcs.xsemantics.interpreter.Signature))
                    throw new CompileException('''Unexpected result evaluating the signature. Result is «sigI.first»''')
                
                if (!(pubkeyI.first instanceof PublicKey))
                    throw new CompileException('''Unexpected result evaluating the public key. Result is «pubkeyI.first»''')
                
                val sig = sigI.first as it.unica.tcs.xsemantics.interpreter.Signature
                val pubkey = pubkeyI.first as PublicKey
                
                val sb = InputScript.create()
                sb.data(sig.getSignature)
                sb.data(pubkey.publicKeyByte)
                return InputScript.create().append(sb)
            }

        }
        else if (outScript.isP2SH) {
            /*
             * P2SH
             */
            val redeemScript = 
                if (parentTx instanceof SerialTransactionBuilder) {
    
                    // get the redeem script from the AST (specified by the user)
                    val s = (input.redeemScript as Script).compileRedeemScript(rho)
    
                    if (!s.ready)
                        throw new CompileException("This redeem script cannot have free variables")
    
                    s
                }
                else if (parentTx instanceof TransactionBuilder) {
                    outScript
                }
                else
                    throw new CompileException('''Unexpected class «parentTx»''')

            val p2sh = InputScript.createP2SH(redeemScript)

            // build the list of expression pushes (actual parameters)
            input.exps.forEach[e|
                p2sh.append(e.compileInputExpression(rho))
            ]

            /* <e1> ... <en> <serialized script> */
            return p2sh
        }
        else
            throw new CompileException("cannot redeem OP_RETURN outputs")
    }

    def private InputScript compileInputExpression(Expression exp, Rho rho) {
        var ctx = new Context(rho)
        InputScript.create().append(exp.compileExpression(ctx))
    }

    /**
     * Compile the given output (AST) to an output script class (lib).
     * @param output the output to compile
     * @param rho the environment for the interpreter
     * @return the compiled output
     * @see OutputScript
     */
    def OutputScript compileOutputScript(Output output, Rho rho) {

        var outScript = output.script as Script

        if (outScript.isP2PKH) {
            var versig = outScript.exp as Versig

            val resAddr = versig.pubkeys.get(0).interpret(rho)

            if (resAddr.failed)
                throw new CompileException('''Unable to evaluate to an address''')

            val addr = resAddr.first
            if (addr instanceof Address) {
                /* OP_DUP OP_HASH160 <pkHash> OP_EQUALVERIFY OP_CHECKSIG */
                return OutputScript.createP2PKH(addr.addressByte)
            }
            else {
                throw new CompileException('''Unable to evaluate to a an address. Result is: «addr»''')
            }

        } else if (outScript.isP2SH(rho)) {

            // get the redeem script to serialize
            var redeemScript = (output.script as Script).compileRedeemScript(rho)

            /* OP_HASH160 <script hash-160> OP_EQUAL */
            redeemScript
        } else if (outScript.isOpReturn(rho)) {
            var c = outScript.exp as StringLiteral
            var data = c.value.bytes

            /* OP_RETURN <bytes> */
            OutputScript.createOP_RETURN(data)
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
    def private OutputScript compileRedeemScript(Script script, Rho rho) {

        var ctx = new Context(rho)

        // build the redeem script to serialize
        var redeemScript = OutputScript.createP2SH()
        for (var i=script.params.size-1; i>=0; i--) {
            val p = script.params.get(i)
            var numberOfRefs = EcoreUtil2.getAllContentsOfType(script.exp, Reference).filter[v|v.ref==p].size

            ctx.altstack.put(p, AltStackEntry.of(ctx.altstack.size, numberOfRefs))    // update the context

            redeemScript.op(OP_TOALTSTACK)
        }

        return redeemScript.append(script.exp.compileExpression(ctx)).optimize()
    }







    def private ScriptBuilderWithVar compileExpression(Expression exp, Context ctx) {
        return exp.interpretSafe(ctx.rho).compileExpressionInternal(ctx)
    }

    // default
    def private dispatch ScriptBuilderWithVar compileExpressionInternal(Expression exp, Context ctx) {
        throw new CompileException("Unable to compile expression "+exp)
    }

    def private dispatch ScriptBuilderWithVar compileExpressionInternal(SignaturePlaceholder p, Context ctx) {
        new ScriptBuilderWithVar().data(#[])
    }

    /*
     * Literals
     */
    def private dispatch ScriptBuilderWithVar compileExpressionInternal(NumberLiteral n, Context ctx) {
        new ScriptBuilderWithVar().number(n.value)
    }

    def private dispatch ScriptBuilderWithVar compileExpressionInternal(BooleanLiteral n, Context ctx) {
        if(n.isTrue) new ScriptBuilderWithVar().op(OP_TRUE)
        else         new ScriptBuilderWithVar().number(OP_FALSE)
    }

    def private dispatch ScriptBuilderWithVar compileExpressionInternal(StringLiteral s, Context ctx) {
        new ScriptBuilderWithVar().data(s.value.bytes)
    }

    def private dispatch ScriptBuilderWithVar compileExpressionInternal(HashLiteral s, Context ctx) {
        new ScriptBuilderWithVar().data(s.value)
    }

    def private dispatch ScriptBuilderWithVar compileExpressionInternal(DateLiteral s, Context ctx) {
        new ScriptBuilderWithVar().number(s.value)
    }

    def private dispatch ScriptBuilderWithVar compileExpressionInternal(SignatureLiteral s, Context ctx) {
        new ScriptBuilderWithVar().data(BitcoinUtils.decode(s.value))
    }

    def private dispatch ScriptBuilderWithVar compileExpressionInternal(Signature stmt, Context ctx) {

        val hashType = stmt.modifier.toHashType
        val anyoneCanPay = stmt.modifier.toAnyoneCanPay
        val resKey = stmt.privkey.interpret(ctx.rho)

        if (resKey.failed) {
            // check if the privkey is a tx parameter 
            if (stmt.privkey instanceof Reference) {
                val ref = stmt.privkey as Reference
                if (ref.ref instanceof TransactionParameter) {
                    val param = ref.ref as TransactionParameter
                    // the signature placeholder will evaluate to the actual parameter when signing
                    var sb = new ScriptBuilderWithVar().signaturePlaceholderKeyFree(param.name, hashType, anyoneCanPay)
                    return sb
                }
            }
            throw new CompileException('''Unable to evaluate to a private key''')
        }

        val key = resKey.first
        if (key instanceof PrivateKey) {
            var sb = new ScriptBuilderWithVar().signaturePlaceholder(ECKeyStore.getUniqueID(key.privateKeyWif), hashType, anyoneCanPay)
            sb
        }
        else {
            throw new CompileException('''Unable to evaluate to a private key: «key»''')
        }
    }

    def private dispatch ScriptBuilderWithVar compileExpressionInternal(Hash160 hash, Context ctx) {
        var sb = hash.value.compileExpression(ctx)
        sb.op(OP_HASH160)
    }

    def private dispatch ScriptBuilderWithVar compileExpressionInternal(Hash256 hash, Context ctx) {
        var sb = hash.value.compileExpression(ctx)
        sb.op(OP_HASH256)
    }

        def private dispatch ScriptBuilderWithVar compileExpressionInternal(Ripemd160 hash, Context ctx) {
        var sb = hash.value.compileExpression(ctx)
        sb.op(OP_RIPEMD160)
    }

    def private dispatch ScriptBuilderWithVar compileExpressionInternal(Sha256 hash, Context ctx) {
        var sb = hash.value.compileExpression(ctx)
        sb.op(OP_SHA256)
    }


    def private dispatch ScriptBuilderWithVar compileExpressionInternal(Sha1 hash, Context ctx) {
        var sb = hash.value.compileExpression(ctx)
        sb.op(OP_SHA1)
    }

    def private dispatch ScriptBuilderWithVar compileExpressionInternal(CheckBlock stmt, Context ctx) {
        var sb = stmt.exp.compileExpression(ctx)
        sb.op(OP_CHECKLOCKTIMEVERIFY)
        sb.op(OP_DROP)
        sb.append(stmt.continuation.compileExpression(ctx))        
    }

    def private dispatch ScriptBuilderWithVar compileExpressionInternal(CheckDate stmt, Context ctx) {
        var sb = stmt.exp.compileExpression(ctx)
        sb.op(OP_CHECKLOCKTIMEVERIFY)
        sb.op(OP_DROP)
        sb.append(stmt.continuation.compileExpression(ctx))                
    }

    def private dispatch ScriptBuilderWithVar compileExpressionInternal(CheckBlockDelay stmt, Context ctx) {
        val res = stmt.exp.interpret(ctx.rho)
        if (res.failed || !(res.first instanceof Long))
            throw new CompileException('''Unable to interpret relative time from «stmt.nodeToString»''')
        val reltime = res.first as Long
        var sb = new ScriptBuilderWithVar
        sb.number(reltime.getSequenceNumber(true, ctx.rho))
        sb.op(OP_CHECKSEQUENCEVERIFY)
        sb.op(OP_DROP)
        sb.append(stmt.continuation.compileExpression(ctx))
    }

    def private dispatch ScriptBuilderWithVar compileExpressionInternal(CheckTimeDelay stmt, Context ctx) {
        val res = stmt.exp.interpret(ctx.rho)
        if (res.failed || !(res.first instanceof Long))
            throw new CompileException('''Unable to interpret relative time from «stmt.nodeToString»''')
        val reltime = res.first as Long
        var sb = new ScriptBuilderWithVar
        sb.number(reltime.getSequenceNumber(false, ctx.rho))
        sb.op(OP_CHECKSEQUENCEVERIFY)
        sb.op(OP_DROP)
        sb.append(stmt.continuation.compileExpression(ctx))
    }

    def private dispatch ScriptBuilderWithVar compileExpressionInternal(AndExpression stmt, Context ctx) {
        var sb = stmt.left.compileExpression(ctx)
        sb.op(OP_IF)
        sb.append(stmt.right.compileExpression(ctx))
        sb.op(OP_ELSE)  // short circuit
        val f = BalzacFactory.eINSTANCE.createBooleanLiteral
        f.setTrue(false)
        sb.append(compileExpression(f,ctx))
        sb.op(OP_ENDIF)
    }

    def private dispatch ScriptBuilderWithVar compileExpressionInternal(OrExpression stmt, Context ctx) {
        var sb = stmt.left.compileExpression(ctx)
        sb.op(OP_IF) // short circuit
        val f = BalzacFactory.eINSTANCE.createBooleanLiteral
        f.setTrue(true)
        sb.append(compileExpression(f,ctx))
        sb.op(OP_ELSE)
        sb.append(stmt.right.compileExpression(ctx))
        sb.op(OP_ENDIF)
    }

    def private dispatch ScriptBuilderWithVar compileExpressionInternal(Plus stmt, Context ctx) {
        var sb = stmt.left.compileExpression(ctx)
        sb.append(stmt.right.compileExpression(ctx))
        sb.op(OP_ADD)
    }

    def private dispatch ScriptBuilderWithVar compileExpressionInternal(Minus stmt, Context ctx) {
        var sb = stmt.left.compileExpression(ctx)
        sb.append(stmt.right.compileExpression(ctx))
        sb.op(OP_SUB)
    }

    def private dispatch ScriptBuilderWithVar compileExpressionInternal(Max stmt, Context ctx) {
        var sb = stmt.left.compileExpression(ctx)
        sb.append(stmt.right.compileExpression(ctx))
        sb.op(OP_MAX)
    }

    def private dispatch ScriptBuilderWithVar compileExpressionInternal(Min stmt, Context ctx) {
        var sb = stmt.left.compileExpression(ctx)
        sb.append(stmt.right.compileExpression(ctx))
        sb.op(OP_MIN)
    }

    def private dispatch ScriptBuilderWithVar compileExpressionInternal(Size stmt, Context ctx) {
        var sb = stmt.value.compileExpression(ctx)
        sb.op(OP_SIZE)
    }

    def private dispatch ScriptBuilderWithVar compileExpressionInternal(BooleanNegation stmt, Context ctx) {
        var sb = stmt.exp.compileExpression(ctx)
        sb.op(OP_NOT)
    }

    def private dispatch ScriptBuilderWithVar compileExpressionInternal(ArithmeticSigned stmt, Context ctx) {
        var sb = stmt.exp.compileExpression(ctx)
        sb.op(OP_NOT)
    }

    def private dispatch ScriptBuilderWithVar compileExpressionInternal(Between stmt, Context ctx) {
        var sb = stmt.value.compileExpression(ctx)
        sb.append(stmt.left.compileExpression(ctx))
        sb.append(stmt.right.compileExpression(ctx))
        sb.op(OP_WITHIN)
    }

    def private dispatch ScriptBuilderWithVar compileExpressionInternal(Comparison stmt, Context ctx) {
        var sb = stmt.left.compileExpression(ctx)
        sb.append(stmt.right.compileExpression(ctx))

        switch (stmt.op) {
            case "<": sb.op(OP_LESSTHAN)
            case ">": sb.op(OP_GREATERTHAN)
            case "<=": sb.op(OP_LESSTHANOREQUAL)
            case ">=": sb.op(OP_GREATERTHANOREQUAL)
        }
    }

    def private dispatch ScriptBuilderWithVar compileExpressionInternal(Equals stmt, Context ctx) {
        var sb = stmt.left.compileExpression(ctx)
        sb.append(stmt.right.compileExpression(ctx))

        switch (stmt.op) {
            case "==": sb.op(OP_EQUAL)
            case "!=": sb.op(OP_EQUAL).op(OP_NOT)
        }
    }

    def private dispatch ScriptBuilderWithVar compileExpressionInternal(IfThenElse stmt, Context ctx) {
        var sb = stmt.^if.compileExpression(ctx)
        sb.op(OP_IF)
        sb.append(stmt.then.compileExpression(ctx))
        sb.op(OP_ELSE)
        sb.append(stmt.^else.compileExpression(ctx))
        sb.op(OP_ENDIF)
    }

    def private dispatch ScriptBuilderWithVar compileExpressionInternal(Versig stmt, Context ctx) {
        if (stmt.pubkeys.size == 1) {
            var sb = stmt.signatures.get(0).compileExpression(ctx)

            val resKey = stmt.pubkeys.get(0).interpret(ctx.rho)

            if (resKey.failed)
                throw new CompileException('''Unable to evaluate the key''')

            val key = resKey.first
            if (key instanceof PublicKey) {
                sb.data(key.publicKeyByte)
            }
            else {
                throw new CompileException('''Unable to evaluate key «key»''')
            }

            sb.op(OP_CHECKSIG)
        } else {
            val sb = new ScriptBuilderWithVar().number(OP_0)
            stmt.signatures.forEach[s|sb.append(s.compileExpression(ctx))]
            sb.number(stmt.signatures.size)
            stmt.pubkeys.forEach[k|
                val resKey = k.interpret(ctx.rho)

                if (resKey.failed)
                    throw new CompileException('''Unable to evaluate key «k»''')

                val key = resKey.first
                if (key instanceof PublicKey) {
                    sb.data(key.publicKeyByte)
                }
                else {
                    throw new CompileException('''Unable to evaluate key «key»''')
                }
            ]
            sb.number(stmt.pubkeys.size)
            sb.op(OP_CHECKMULTISIG)
        }
    }


    def private dispatch ScriptBuilderWithVar compileExpressionInternal(Reference varRef, Context ctx) {
        return compileReferrable(varRef.ref, ctx)
    }


    def private dispatch ScriptBuilderWithVar compileReferrable(Referrable obj, Context ctx) {
        throw new CompileException('''Cannot compile «obj.class»''')
    }

    def private dispatch ScriptBuilderWithVar compileReferrable(Constant const, Context ctx) {
                
        val value = (const.exp as Interpretable).interpretSafe(ctx.rho)

        if (value instanceof Literal) {
            return value.compileExpression(ctx)
        }
        else
            throw new CompileException('''Constant «const.name» does not evaluate to Literal value. This should be checked by validation.''')
    }

    def private dispatch ScriptBuilderWithVar compileReferrable(Parameter param, Context ctx) {

        if (ctx.rho.containsKey(param)) {
            val exp = objectToExpression(ctx.rho.get(param))
            return exp.compileExpression(ctx)
        }
        else {
            if (param instanceof ScriptParameter) {

                /*
                 * N: altezza dell'altstack
                 * i: posizione della variabile interessata
                 *
                 * OP_FROMALTSTACK( N - i )                svuota l'altstack fino a raggiungere x
                 *                                         x ora è in cima al main stack
                 *
                 * OP_DUP OP_TOALTSTACK                    duplica x e lo rimanda sull'altstack
                 *
                 * (OP_SWAP OP_TOALTSTACK)( N - i - 1 )    prende l'elemento sotto x e lo sposta sull'altstack
                 *
                 */
                // script parameter
                val sb = new ScriptBuilderWithVar()
                val pos = ctx.altstack.get(param).position
                var count = ctx.altstack.get(param).occurrences

                if(pos === null) throw new CompileException;

                (1 .. ctx.altstack.size - pos).forEach[x|sb.op(OP_FROMALTSTACK)]

                if (count==1) {
                    // this is the last usage of the variable
                    ctx.altstack.remove(param)                          // remove the reference to its altstack position
                    for (e : ctx.altstack.entrySet.filter[e|e.value.position>pos]) {    // update all the positions of the remaing elements
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
            else if (param instanceof TransactionParameter) {
                // transaction parameter
                return new ScriptBuilderWithVar().addVariable(param.name, param.type.convertType)
            }
            else
                throw new CompileException('''unexpected class «param»''')
        }
    }
}
