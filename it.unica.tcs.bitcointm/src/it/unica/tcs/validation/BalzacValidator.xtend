/*
 * Copyright 2018 Nicola Atzei
 */

package it.unica.tcs.validation

import com.google.inject.Inject
import it.unica.tcs.balzac.AbsoluteTime
import it.unica.tcs.balzac.AddressLiteral
import it.unica.tcs.balzac.Assertion
import it.unica.tcs.balzac.BalzacPackage
import it.unica.tcs.balzac.CheckBlock
import it.unica.tcs.balzac.CheckBlockDelay
import it.unica.tcs.balzac.CheckDate
import it.unica.tcs.balzac.CheckTimeDelay
import it.unica.tcs.balzac.Div
import it.unica.tcs.balzac.Expression
import it.unica.tcs.balzac.Import
import it.unica.tcs.balzac.Input
import it.unica.tcs.balzac.IsMinedCheck
import it.unica.tcs.balzac.KeyLiteral
import it.unica.tcs.balzac.Minus
import it.unica.tcs.balzac.Model
import it.unica.tcs.balzac.Modifier
import it.unica.tcs.balzac.Output
import it.unica.tcs.balzac.PackageDeclaration
import it.unica.tcs.balzac.Participant
import it.unica.tcs.balzac.Placeholder
import it.unica.tcs.balzac.Plus
import it.unica.tcs.balzac.Reference
import it.unica.tcs.balzac.Referrable
import it.unica.tcs.balzac.RelativeTime
import it.unica.tcs.balzac.Script
import it.unica.tcs.balzac.Signature
import it.unica.tcs.balzac.This
import it.unica.tcs.balzac.Times
import it.unica.tcs.balzac.Transaction
import it.unica.tcs.balzac.TransactionHexLiteral
import it.unica.tcs.balzac.TransactionInputOperation
import it.unica.tcs.balzac.TransactionOutputOperation
import it.unica.tcs.balzac.Versig
import it.unica.tcs.lib.client.BitcoinClientException
import it.unica.tcs.lib.model.ITransactionBuilder
import it.unica.tcs.lib.model.SerialTransactionBuilder
import it.unica.tcs.lib.model.TransactionBuilder
import it.unica.tcs.lib.validation.ValidationResult.InputValidationError
import it.unica.tcs.lib.validation.Validator
import it.unica.tcs.utils.ASTUtils
import it.unica.tcs.utils.BitcoinClientFactory
import it.unica.tcs.xsemantics.BalzacInterpreter
import it.unica.tcs.xsemantics.Rho
import java.util.HashMap
import java.util.HashSet
import java.util.Map
import java.util.Set
import org.apache.log4j.Logger
import org.eclipse.emf.common.util.EList
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.EStructuralFeature
import org.eclipse.emf.ecore.util.EcoreUtil
import org.eclipse.xtext.EcoreUtil2
import org.eclipse.xtext.naming.IQualifiedNameConverter
import org.eclipse.xtext.naming.QualifiedName
import org.eclipse.xtext.resource.IContainer
import org.eclipse.xtext.resource.IEObjectDescription
import org.eclipse.xtext.resource.IResourceDescription
import org.eclipse.xtext.resource.IResourceDescriptions
import org.eclipse.xtext.resource.impl.ResourceDescriptionsProvider
import org.eclipse.xtext.validation.Check
import org.eclipse.xtext.validation.CheckType

import static extension it.unica.tcs.utils.ASTExtensions.*

/**
 * This class contains custom validation rules.
 *
 * See https://www.eclipse.org/Xtext/documentation/303_runtime_concepts.html#validation
 */
class BalzacValidator extends AbstractBalzacValidator {

    static Logger logger = Logger.getLogger(BalzacValidator);

    @Inject extension IQualifiedNameConverter
    @Inject extension BalzacInterpreter
    @Inject extension ASTUtils
    @Inject ResourceDescriptionsProvider resourceDescriptionsProvider;
    @Inject IContainer.Manager containerManager;
    @Inject BitcoinClientFactory clientFactory;

    @Check
    def void checkUnusedParameters__Script(Script script){

        for (param : script.params) {
            var references = EcoreUtil.UsageCrossReferencer.find(param, script.exp);
            if (references.size==0)
                warning("Unused variable '"+param.name+"'.",
                    param,
                    BalzacPackage.Literals.PARAMETER__NAME
                );
        }
    }

    @Check
    def void checkUnusedParameters__Transaction(Transaction tx){

        for (param : tx.params) {
            var references = EcoreUtil.UsageCrossReferencer.find(param, tx);
            if (references.size==0)
                warning("Unused variable '"+param.name+"'.",
                    param,
                    BalzacPackage.Literals.PARAMETER__NAME
                );
        }
    }

    @Check
    def void checkVerSigDuplicatedKeys(Versig versig) {

        for(var i=0; i<versig.pubkeys.size-1; i++) {
            for(var j=i+1; j<versig.pubkeys.size; j++) {

                var k1 = versig.pubkeys.get(i)
                var k2 = versig.pubkeys.get(j)

                if (k1==k2) {
                    warning("Duplicated public key.", versig, BalzacPackage.Literals.VERSIG__PUBKEYS, i);
                    warning("Duplicated public key.", versig,BalzacPackage.Literals.VERSIG__PUBKEYS, j);
                }
            }
        }
    }

    @Check
    def void checkSignatureModifiers(Signature signature) {

        var input = EcoreUtil2.getContainerOfType(signature, Input);
        for (other: EcoreUtil2.getAllContentsOfType(input, Signature)){

            if (signature!=other && signature.modifier.restrictedBy(other.modifier)) {
                warning('''This signature modifier is nullified by another one.''',
                    signature,
                    BalzacPackage.Literals.SIGNATURE__MODIFIER
                );
                warning('''This signature modifier is nullifying another one.''',
                    other,
                    BalzacPackage.Literals.SIGNATURE__MODIFIER
                );
            }
        }
    }

    def private boolean restrictedBy(Modifier _this, Modifier other) {
        false;
    }

    @Check
    def void checkConstantScripts(Script script) {

        val res = script.exp.interpretE

        if (!res.failed && (res.first instanceof Boolean)) {
            warning("Script will always evaluate to "+res.first,
                script.eContainer,
                script.eContainingFeature
            );
        }
    }

    @Check
    def void checkPackageDuplicate(PackageDeclaration pkg) {
        var Set<QualifiedName> names = new HashSet();
        var IResourceDescriptions resourceDescriptions = resourceDescriptionsProvider.getResourceDescriptions(pkg.eResource());
        var IResourceDescription resourceDescription = resourceDescriptions.getResourceDescription(pkg.eResource().getURI());
        for (IContainer c : containerManager.getVisibleContainers(resourceDescription, resourceDescriptions)) {
            for (IEObjectDescription od : c.getExportedObjectsByType(BalzacPackage.Literals.PACKAGE_DECLARATION)) {
                if (!names.add(od.getQualifiedName())) {
                    error(
                        "Duplicated package name",
                        BalzacPackage.Literals.PACKAGE_DECLARATION__NAME
                    );
                }
            }
        }
    }

    @Check
    def void checkImport(Import imp) {

        var packageName = (imp.eContainer as Model).package.name.toQualifiedName
        var importedPackage = imp.importedNamespace.toQualifiedName

        if (packageName.equals(importedPackage.skipLast(1))) {
            error(
                '''The import «importedPackage» refers to this package declaration''',
                BalzacPackage.Literals.IMPORT__IMPORTED_NAMESPACE
            );
            return
        }

        var Set<QualifiedName> names = new HashSet();
        var IResourceDescriptions resourceDescriptions = resourceDescriptionsProvider.getResourceDescriptions(imp.eResource());
        var IResourceDescription resourceDescription = resourceDescriptions.getResourceDescription(imp.eResource().getURI());

        for (IContainer c : containerManager.getVisibleContainers(resourceDescription, resourceDescriptions)) {
            for (IEObjectDescription od : c.getExportedObjectsByType(BalzacPackage.Literals.PACKAGE_DECLARATION)) {
                names.add(od.qualifiedName.append("*"))
            }
            for (IEObjectDescription od : c.getExportedObjectsByType(BalzacPackage.Literals.TRANSACTION)) {
                names.add(od.qualifiedName)
            }
        }

        if (!names.contains(importedPackage)) {
            error(
                '''The import «importedPackage» cannot be resolved''',
                BalzacPackage.Literals.IMPORT__IMPORTED_NAMESPACE
            );
        }
    }

    @Check
    def void checkShadowedReferences(Referrable r) {
        if (findReferrable(r,r)) {
            warning(
                "This declaration shadows an existing declaration",
                r,
                r.literalName
            )
        }
    }

    def private dispatch boolean findReferrable(Model model, Referrable r) {
        for (d : model.declarations.filter(Referrable)) {
            if (d !== r && d.name == r.name) return true
        }
        return false
    }

    def private dispatch boolean findReferrable(EObject ctx, Referrable r) {
        return findReferrable(ctx.eContainer, r)
    }

    def private dispatch boolean findReferrable(Participant p, Referrable r) {
        for (d : p.declarations.filter(Referrable)) {
            if (d !== r && d.name == r.name) return true
        }
        return findReferrable(p.eContainer, r)
    }

    def private dispatch boolean findReferrable(Referrable p, Referrable r) {
        if (p !== r && p.name == r.name) return true
        return findReferrable(p.eContainer, r)
    }

    @Check
    def void checkModelDeclarationNameIsUnique(Model model) {

        val allReferrables = model.declarations.filter(Referrable)

        for (var i=0; i<allReferrables.size-1; i++) {
            for (var j=i+1; j<allReferrables.size; j++) {
                val a = allReferrables.get(i)
                val b = allReferrables.get(j)

                if (a.name == b.name) {
                    error("Duplicated name "+a.name,
                        a,
                        a.literalName
                    )
                    error("Duplicated name "+b.name,
                        b,
                        b.literalName
                    )
                }
            }
        }
    }

    @Check
    def void checkParticipantDeclarationNameIsUnique(Participant participant) {

        val allReferrables = participant.declarations.filter(Referrable)

        for (var i=0; i<allReferrables.size-1; i++) {
            for (var j=i+1; j<allReferrables.size; j++) {
                val a = allReferrables.get(i)
                val b = allReferrables.get(j)

                if (a.name == b.name) {
                    error("Duplicated name "+a.name,
                        a,
                        a.literalName
                    )
                    error("Duplicated name "+b.name,
                        b,
                        b.literalName
                    )
                }
            }
        }
    }

    @Check
    def void checkParticipantNameIsUnique(Participant r) {

        var root = EcoreUtil2.getRootContainer(r);
        val allReferrables = EcoreUtil2.getAllContentsOfType(root, Participant)

        for (other: allReferrables){

            if (r!=other && r.pname.equals(other.pname)) {
                error("Duplicated name "+other.pname,
                    r,
                    BalzacPackage.Literals.PARTICIPANT__PNAME
                );
            }
        }
    }

    @Check
    def void checkVerSig(Versig versig) {

        checkExpressionIsWithinScript(versig)

        if (versig.pubkeys.size>15) {
            error("Cannot verify more than 15 public keys.",
                BalzacPackage.Literals.VERSIG__PUBKEYS
            )
        }

        if (versig.signatures.size > versig.pubkeys.size) {
            error("The number of signatures cannot exceed the number of public keys.",
                versig,
                BalzacPackage.Literals.VERSIG__SIGNATURES
            )
        }
    }

    @Check
    def void checkSigTransaction(Signature sig) {
        val isTxDefined = sig.isHasTx
        val isWithinInput = EcoreUtil2.getContainerOfType(sig, Input) !== null

        if (isTxDefined && isWithinInput) {
            error("You cannot specify the transaction to sign.",
                sig,
                BalzacPackage.Literals.SIGNATURE__TX
            )
            return
        }

        if (isTxDefined && sig.tx.isCoinbase) {
            error("Transaction cannot be a coinbase.",      // because you need a reference to the output script of the input i-th
                sig,
                BalzacPackage.Literals.SIGNATURE__TX
            )
            return
        }

        if (isTxDefined && sig.tx.isSerial) {
            error("Cannot sign a serialized transaction.",  // because you need a reference to the output script of the input i-th
                sig,
                BalzacPackage.Literals.SIGNATURE__TX
            )
            return
        }

        if (!isTxDefined && !isWithinInput) {
            error("You must specify the transaction to sign.",
                sig.eContainer,
                sig.eContainingFeature
            )
            return
        }

        val res = sig.tx.interpretE
        
        if (!res.failed) {
            val tx = res.first as ITransactionBuilder
            val inputSize = tx.inputs.size
            val outputSize = tx.outputs.size
            if (sig.inputIdx >= inputSize) {
                error('''Invalid input «sig.inputIdx». «IF inputSize == 1»0 expected (it can be omitted).«ELSE»Valid interval [0,«inputSize-1»].«ENDIF»''',
                    sig,
                    BalzacPackage.Literals.SIGNATURE__INPUT_IDX
                )
            }
            if (sig.modifier == Modifier.AISO || sig.modifier == Modifier.SISO) {
            	if (sig.inputIdx >= outputSize) {
	                error('''Invalid input «sig.inputIdx». Since you are signing a single output, the index must be «IF outputSize == 1»0 (it can be omitted).«ELSE» within [0,«outputSize-1»].«ENDIF»''',
	                    sig,
	                    BalzacPackage.Literals.SIGNATURE__INPUT_IDX
	                )
	            }
            }
        }
        else {
            error('''Error occurred evaluting transaction «sig.tx.nodeToString». Please report the error to the authors.''',
                sig,
                BalzacPackage.Literals.SIGNATURE__TX
            )
        }
    }


    @Check
    def void checkKeyDeclaration(KeyLiteral k) {
        val result = Validator.validatePrivateKey(k.value, k.networkParams)
        if (result.error) {
            error(result.message,
                k,
                BalzacPackage.Literals.KEY_LITERAL__VALUE
            )
        }
  	}

    @Check
    def void checkAddressDeclaration(AddressLiteral k) {
        val result = Validator.validateAddress(k.value, k.networkParams)
        if (result.error) {
            error(result.message,
                k,
                BalzacPackage.Literals.ADDRESS_LITERAL__VALUE
            )
        }
    }

    @Check
    def void checkUniqueParameterNames__Script(Script p) {

        for (var i=0; i<p.params.size-1; i++) {
            for (var j=i+1; j<p.params.size; j++) {
                if (p.params.get(i).name == p.params.get(j).name) {
                    error(
                        "Duplicated parameter name '"+p.params.get(j).name+"'.",
                        p.params.get(j),
                        BalzacPackage.Literals.PARAMETER__NAME, j
                    );
                }
            }
        }
    }

    @Check
    def void checkUniqueParameterNames__Transaction(Transaction p) {

        for (var i=0; i<p.params.size-1; i++) {
            for (var j=i+1; j<p.params.size; j++) {
                if (p.params.get(i).name == p.params.get(j).name) {
                    error(
                        "Duplicated parameter name '"+p.params.get(j).name+"'.",
                        p.params.get(j),
                        BalzacPackage.Literals.PARAMETER__NAME, j
                    );
                }
            }
        }
    }

    @Check
    def void checkScriptWithoutMultply(Script p) {

        val exp = p.exp

        val times = EcoreUtil2.getAllContentsOfType(exp, Times);
        val divs = EcoreUtil2.getAllContentsOfType(exp, Div);
        var signs = EcoreUtil2.getAllContentsOfType(exp, Signature);

        times.forEach[t|
            error(
                "Multiplications are not permitted within scripts.",
                t.eContainer,
                t.eContainingFeature
            );
        ]

        divs.forEach[d|
            error(
                "Divisions are not permitted within scripts.",
                d.eContainer,
                d.eContainingFeature
            );
        ]

        signs.forEach[s|
            error("Signatures are not allowed within output scripts.",
                s.eContainer,
                s.eContainmentFeature
            );
        ]
    }

    @Check
    def void checkSerialTransaction(TransactionHexLiteral tx) {
        val result = Validator.validateRawTransaction(tx.value, tx.networkParams)
        if (result.error) {
            error(
                result.message,
                tx,
                null
            )            
        }
    }

    @Check(CheckType.NORMAL)
    def void checkUserDefinedTx(Transaction tx) {
        tx.checkTx(new Rho(tx.networkParams), tx)
    }

    @Check(CheckType.NORMAL)
    def void checkTxReference(Reference ref) {
        if (!(ref.ref instanceof Transaction)) {
            return
        }

        if (ref.actualParams.empty) {
            return
        }

        if (ref.actualParams.exists[e|e instanceof Placeholder]) {
            return
        }

        val tx = ref.ref as Transaction
        val values = ref.actualParams
        val rho = new Rho(tx.networkParams)
        for(var i=0; i<tx.params.size; i++) {
            val fp = tx.params.get(i)
            val value = values.get(i).interpretE
            if (!value.failed)
                rho.put(fp, value.first)
        }
        tx.checkTx(rho, ref)
    }

    def private void checkTx(Transaction tx, Rho rho, EObject source) {
        if (tx.isCoinbase)
            return;

        var hasError = false;

        /*
         * Verify that inputs are valid
         */
        val mapInputsTx = new HashMap<Input, ITransactionBuilder>
        for (input: tx.inputs) {
            /*
             * get the transaction input
             */
            val txInput = input.txRef

            if (txInput.txVariables.empty) {

                val res = input.txRef.interpretE

                if (res.failed) {
                    res.ruleFailedException.printStackTrace
                    error("Error evaluating the transaction input, see error log for details.",
                        if (source == tx) input else source,
                        if (source == tx) BalzacPackage.Literals.INPUT__TX_REF
                    );
                    hasError = hasError || true
                }
                else {
                    val txB = res.first as ITransactionBuilder
                    mapInputsTx.put(input, txB)
                    var valid =
                        input.isPlaceholder || (
                            input.checkInputIndex(txB) &&
                            input.checkInputExpressions(txB)
                        )

                    hasError = hasError || !valid
                }
            }
        }

        if(hasError) return;  // interrupt the check

        /*
         * pairwise verify that inputs are unique
         */
        for (var i=0; i<tx.inputs.size-1; i++) {
            for (var j=i+1; j<tx.inputs.size; j++) {

                var inputA = tx.inputs.get(i)
                var inputB = tx.inputs.get(j)

                var areValid = checkInputsAreUnique(inputA, inputB, mapInputsTx)

                hasError = hasError || !areValid
            }
        }

        if(hasError) return;  // interrupt the check

        /*
         * Verify that the fees are positive
         */
        hasError = !checkFee(tx, rho, mapInputsTx, source)

        if(hasError) return;  // interrupt the check

        /*
         * Verify that the input correctly spends the output
         */
        hasError = correctlySpendsOutput(tx, rho, source)
    }


    def boolean checkInputIndex(Input input, ITransactionBuilder inputTx) {

        var numOfOutputs = inputTx.outputs.size
        var outIndex = input.outpoint

        if (outIndex>=numOfOutputs) {
            error("This input is pointing to an undefined output script.",
                input,
                BalzacPackage.Literals.INPUT__TX_REF
            );
            return false
        }

        return true
    }

    def boolean checkInputExpressions(Input input, ITransactionBuilder inputTx) {

        var outputIdx = input.outpoint

        if (inputTx instanceof SerialTransactionBuilder) {
            if (inputTx.outputs.get(outputIdx).script.isP2SH) {
                input.failIfRedeemScriptIsMissing
            }
            else {
                input.failIfRedeemScriptIsDefined
            }
        }
        else if (inputTx instanceof TransactionBuilder) {
            input.failIfRedeemScriptIsDefined
        }
        return true
    }


    def boolean failIfRedeemScriptIsMissing(Input input) {
        if (input.redeemScript===null) {
            error(
                "You must specify the redeem script when referring to a P2SH output of a serialized transaction.",
                input,
                BalzacPackage.Literals.INPUT__EXPS,
                input.exps.size-1
            );
            return false
        }
        else {
            // free variables are not allowed
            var ok = true
            for (v : EcoreUtil2.getAllContentsOfType(input.redeemScript, Reference)) {
                if (v.ref.eContainer instanceof org.bitcoinj.core.Transaction) {
                    error(
                        "Cannot reference transaction parameters from the redeem script.",
                        v,
                        BalzacPackage.Literals.REFERENCE__REF
                    );
                    ok = false;
                }
            }
            return ok
        }
    }

    def boolean failIfRedeemScriptIsDefined(Input input) {
        if (input.redeemScript!==null) {
            error(
                "You must not specify the redeem script when referring to a user-defined transaction.",
                input.redeemScript,
                BalzacPackage.Literals.INPUT__EXPS,
                input.exps.size-1
            );
            return false
        }
        return true;
    }

    def boolean checkInputsAreUnique(Input inputA, Input inputB, Map<Input, ITransactionBuilder> mapInputsTx) {

        val txA = mapInputsTx.get(inputA)
        val txB = mapInputsTx.get(inputB)

        if (txA===null || txB===null)
            return true

        if (!txA.ready || !txB.ready)
            return true

        if (txA.toTransaction(inputA.ECKeyStore)==txB.toTransaction(inputB.ECKeyStore) && inputA.outpoint==inputB.outpoint
        ) {
            error(
                "Double spending. You cannot redeem the output twice.",
                inputA,
                BalzacPackage.Literals.INPUT__TX_REF
            );

            error(
                "Double spending. You cannot redeem the output twice.",
                inputB,
                BalzacPackage.Literals.INPUT__TX_REF
            );
            return false
        }
        return true
    }

    def boolean checkFee(Transaction tx, Rho rho, Map<Input, ITransactionBuilder> mapInputsTx, EObject source) {

        if (tx.isCoinbase)
            return true;

        var amount = 0L

        for (in : tx.inputs) {
            val inTx = mapInputsTx.get(in)
            amount += inTx.outputs.get(in.outpoint).value
        }

        for (output : tx.outputs) {
            val res = output.value.interpret(rho)
            if (!res.failed) {
                val value = res.first as Long                
                amount -= value
            }
        }

        if (amount<0) {
            error("The transaction spends more than expected.",
                source,
                if (source == tx) BalzacPackage.Literals.TRANSACTION__NAME
            );
            return false;
        }

        return true;
    }

    def boolean correctlySpendsOutput(Transaction tx, Rho rho, EObject source) {

        
        var txStr = tx.nodeToString
        txStr = txStr.substring(0, txStr.indexOf("{")).trim

        logger.debug("witness check: interpreting "+txStr+" with rho="+rho)
        var res = tx.interpret(rho)

        if (!res.failed) {
            var txBuilder = res.first as ITransactionBuilder

            val validationResult = Validator.checkWitnessesCorrecltySpendsOutputs(txBuilder, tx.ECKeyStore)

            if (validationResult.error) {
                if (validationResult instanceof InputValidationError) {
                    warning('''
                        Input «validationResult.index» does not redeem the specified output script.
                        Reason: «validationResult.message»
                        
                        INPUT:   «validationResult.inputScript»
                        OUTPUT:  «validationResult.outputScript»
                        «IF validationResult.reedemScript !== null»
                        REDEEM SCRIPT:  «validationResult.reedemScript»
                        «ENDIF»''',
                        source,
                        if (source === tx) BalzacPackage.Literals.TRANSACTION__INPUTS,
                        validationResult.index
                    )
                }
                else {
                    warning('''
                        Something went wrong verifying the input witnesses.
                        «validationResult.message»''',
                        tx,
                        BalzacPackage.Literals.TRANSACTION__INPUTS
                    )
                }
            }
        }
        else {
            logger.debug("witness check: unable to interpret tx "+txStr+" with rho="+rho+" (skip)")
        }

        return true
    }

    @Check
    def void checkPositiveOutValue(Output output) {

        var value = output.value.interpretE.first as Long
        var script = output.script as Script

        if (script.isOpReturn(new Rho(output.networkParams)) && value>0) {
            error("OP_RETURN output scripts must have 0 value.",
                output,
                BalzacPackage.Literals.OUTPUT__VALUE
            );
        }

        // https://github.com/bitcoin/bitcoin/commit/6a4c196dd64da2fd33dc7ae77a8cdd3e4cf0eff1
        if (!script.isOpReturn(new Rho(output.networkParams)) && value<546) {
            error("Output (except OP_RETURN scripts) must spend at least 546 satoshis.",
                output,
                BalzacPackage.Literals.OUTPUT__VALUE
            );
        }
    }

    @Check
    def void checkJustOneOpReturn(Transaction tx) {
        /*
         * https://en.bitcoin.it/wiki/Script
         * "Currently it is usually considered non-standard (though valid) for a transaction to have more than one OP_RETURN output or an OP_RETURN output with more than one pushdata op."
         */

        var boolean[] error = newBooleanArrayOfSize(tx.outputs.size);

        for (var i=0; i<tx.outputs.size-1; i++) {
            for (var j=i+1; j<tx.outputs.size; j++) {

                var outputA = tx.outputs.get(i)
                var outputB = tx.outputs.get(j)

                // these checks need to be executed in this order
                if ((outputA.script as Script).isOpReturn(new Rho(tx.networkParams)) && (outputB.script as Script).isOpReturn(new Rho(tx.networkParams))
                ) {
                    if (!error.get(i) && (error.set(i,true) && true))
                        warning(
                            "Currently it is usually considered non-standard (though valid) for a transaction to have more than one OP_RETURN output or an OP_RETURN output with more than one pushdata op.",
                            outputA.eContainer,
                            outputA.eContainingFeature,
                            i
                        );

                    if (!error.get(j) && (error.set(j,true) && true))
                        warning(
                            "Currently it is usually considered non-standard (though valid) for a transaction to have more than one OP_RETURN output or an OP_RETURN output with more than one pushdata op.",
                            outputB.eContainer,
                            outputB.eContainingFeature,
                            j
                        );
                }
            }
        }
    }

    @Check
    def void checkUniqueRelativeTimelock(RelativeTime tlock) {

        val isScriptTimelock = EcoreUtil2.getContainerOfType(tlock, Script) !== null;

        if (isScriptTimelock)
            return

        var tx = EcoreUtil2.getContainerOfType(tlock, Transaction);
        for (other: tx.relLocks){

            if (tlock!=other && tlock.class==other.class) {
                val tx1 = tlock.tx.interpretE.first
                val tx2 = other.tx.interpretE.first

                if (tx1==tx2)
                    error(
                    	"Duplicated relative timelock",
                        tlock,
                        null
                    );
            }
        }
    }

    @Check
    def void checkRelativeTimelockFromTxIsInput(RelativeTime tlock) {

        if (tlock.tx !== null) {
            val tx = tlock.tx.interpretE.first
            val containingTx = EcoreUtil2.getContainerOfType(tlock, Transaction);

            for (in : containingTx.inputs) {
                val inTx = in.txRef.interpretE.first
                if (tx==inTx) {
                    return
                }
            }

            error(
                'Relative timelocks must refer to an input transaction',
                tlock,
                BalzacPackage.Literals.RELATIVE_TIME__TX
            );
        }
    }

    @Check
    def void checkCheckBlock(CheckBlock check) {
        checkExpressionIsWithinScript(check)
        checkTimeExpression(check.exp, true, true, check, BalzacPackage.Literals.CHECK_BLOCK__EXP)
    }

    @Check
    def void checkCheckDate(CheckDate check) {
        checkExpressionIsWithinScript(check)
        checkTimeExpression(check.exp, true, false, check, BalzacPackage.Literals.CHECK_DATE__EXP)
    }

    @Check
    def void checkCheckBlockDelay(CheckBlockDelay check) {
        checkExpressionIsWithinScript(check)
        checkTimeExpression(check.exp, false, true, check, BalzacPackage.Literals.CHECK_BLOCK_DELAY__EXP)
    }

    @Check
    def void checkCheckTimeDelay(CheckTimeDelay check) {
        checkExpressionIsWithinScript(check)        
        checkTimeExpression(check.exp, false, false, check, BalzacPackage.Literals.CHECK_TIME_DELAY__EXP)
    }

    @Check
    def void checkAbsoluteTime(AbsoluteTime tlock) {
        checkTimeExpression(tlock.exp, true, tlock.isBlock, tlock, BalzacPackage.Literals.ABSOLUTE_TIME__EXP)
    }

    @Check
    def void checkRelativeTime(RelativeTime tlock) {
        checkTimeExpression(tlock.exp, false, tlock.isBlock, tlock, BalzacPackage.Literals.RELATIVE_TIME__EXP)
    }

    def private boolean checkExpressionIsWithinScript(EObject obj) {
        val script = EcoreUtil2.getContainerOfType(obj, Script)

        if (script === null) {
            error(
                "This expression is permitted only in output scripts",
                obj.eContainer,
                obj.eContainmentFeature
            )
            return false
        }
        return true
    }

    def private checkTimeExpression(Expression exp, boolean isAbsolute, boolean isBlock, EObject obj, EStructuralFeature feature) {
        val res = exp.interpretE

        if (res.failed)
            return;

        val value = res.first as Long

        if (value<0) {
            error(
                "Negative timelock is not permitted.",
                obj,
                feature
            )
            return
        }

        if (isAbsolute && isBlock && value>=org.bitcoinj.core.Transaction.LOCKTIME_THRESHOLD) {
            error(
                "Block number must be lower than 500_000_000.",
                obj,
                feature
            )
            return
        }

        if (isAbsolute && !isBlock && value<org.bitcoinj.core.Transaction.LOCKTIME_THRESHOLD) {
            error(
                "Block number must be greater or equal than 500_000_000 (1985-11-05 00:53:20). Found "+value,
                obj,
                feature
            )
        }

        /*
         * tlock.value must fit in 16-bit
         */
        if (!isAbsolute && !value.delayValue.fitIn16bits) {
            error(
                '''Relative timelocks must fit within unsigned 16-bits. «IF isBlock»Block«ELSE»Delay«ENDIF» value is «value», max allowed is «0xFFFF»''',
                obj,
                feature
            )
        }
    }

    @Check
    def boolean checkTransactionChecksOndemand(Transaction tx) {
        var hasError = false
        for (var i=0; i<tx.checks.size-1; i++) {
            for (var j=i; i<tx.checks.size; j++) {
                val one = tx.checks.get(i)
                val other = tx.checks.get(j)

                if (one.class == other.class) {
                    error(
                        "Duplicated annotation",
                        tx,
                        BalzacPackage.Literals.TRANSACTION__CHECKS,
                        i
                    );
                    error(
                        "Duplicated annotation",
                        tx,
                        BalzacPackage.Literals.TRANSACTION__CHECKS,
                        j
                    );
                    hasError = true;
                }
            }
        }

        return !hasError;
    }

    @Check(CheckType.NORMAL)
    def void checkTransactionOndemand(IsMinedCheck check) {

        val tx = EcoreUtil2.getContainerOfType(check, Transaction)

        if (!checkTransactionChecksOndemand(tx)) {
            return
        }

        val checkIdx = tx.checks.indexOf(check)
        val res = tx.interpretE

        if (res.failed) {
            warning(
                '''Cannot check if «tx.name» is mined. Cannot interpret the transaction.''',
                tx,
                BalzacPackage.Literals.TRANSACTION__CHECKS,
                checkIdx
            );
        }
        else {
            val txBuilder = res.first as ITransactionBuilder
            val txid = txBuilder.toTransaction(tx.ECKeyStore).hashAsString

            try {
                val client = clientFactory.getBitcoinClient(tx.networkParams)
                val mined = client.isMined(txid)

                if (check.isMined && !mined) {
                    warning(
                        "Transaction is not mined",
                        tx,
                        BalzacPackage.Literals.TRANSACTION__CHECKS,
                        checkIdx
                    );
                }

                if (!check.isMined && mined) {
                    warning(
                        "Transaction is already mined",
                        tx,
                        BalzacPackage.Literals.TRANSACTION__CHECKS,
                        checkIdx
                    );
                }

            }
            catch(BitcoinClientException e) {
                warning(
                    "Cannot check if the transaction is mined due to network problems: "+e.message,
                    tx,
                    BalzacPackage.Literals.TRANSACTION__CHECKS,
                    checkIdx
                );
            }
        }
    }

    @Check
    def void checkThis(This ref) {
        // 'this' reference is allowed only inside transactions
        val containingTx = EcoreUtil2.getContainerOfType(ref, Transaction);
        val isInsideTx = containingTx !== null
        
        if (!isInsideTx) {
            error(
                "Reference 'this' is allowed only within transactions.",
                ref.eContainer,
                ref.eContainingFeature
            );
            return
        }
    }

    @Check
    def void checkTransactionInputOperation(TransactionInputOperation op) {
        // each idx is unique
        val limit = computeInputSize(op.tx);
        checkTransactionOperationIndexes(op.indexes, limit, op, BalzacPackage.Literals.TRANSACTION_INPUT_OPERATION__INDEXES)
    }

    @Check
    def void checkTransactionOutputOperation(TransactionOutputOperation op) {
        // expression is allowed only inside transactions
        val limit = computeOutputSize(op.tx);
        checkTransactionOperationIndexes(op.indexes, limit, op, BalzacPackage.Literals.TRANSACTION_OUTPUT_OPERATION__INDEXES)
    }

    def dispatch private int computeInputSize(This thiz) {
        val tx = EcoreUtil2.getContainerOfType(thiz, Transaction)
        computeInputSize(tx)
    }

    def dispatch private int computeInputSize(Transaction tx) {
        return tx.inputs.size
    }

    def dispatch private int computeInputSize(Expression exp) {
        val txRes = exp.interpretE
        if (!txRes.failed && txRes.first instanceof ITransactionBuilder) {
            val tx = txRes.first as ITransactionBuilder
            return tx.inputs.size
        }
        return 0
    }

    def dispatch private int computeOutputSize(This thiz) {
        computeOutputSize(EcoreUtil2.getContainerOfType(thiz, Transaction))
    }

    def dispatch private int computeOutputSize(Transaction tx) {
        return tx.outputs.size
    }

    def dispatch private int computeOutputSize(Expression exp) {
        val txRes = exp.interpretE
        if (!txRes.failed && txRes.first instanceof ITransactionBuilder) {
            val tx = txRes.first as ITransactionBuilder
            return tx.outputs.size
        }
        return 0
    }

    def private checkTransactionOperationIndexes(EList<Integer> indexes, int limit, EObject source, EStructuralFeature feature) {
        // each idx is in range
        var hasError = false;
        for (var i=0; i<indexes.size; i++) {
            val idx = indexes.get(i)
            if (idx >= limit) {
                error("Index out of range.", source, feature, i);
                hasError = true;
            }
        }
        if (hasError)
            return;

        // each idx is unique
        for (var i=0; i<indexes.size-1; i++) {
            for (var j=i+1; j<indexes.size; j++) {
                val idx1 = indexes.get(i)
                val idx2 = indexes.get(j)
                if (idx1 == idx2) {
                    error("Duplicated index.", source, feature, i);
                    error("Duplicated index.", source, feature, j);
                }
            }
        }
    }

    @Check
    def void checkDivisionByZero(Div divide) {
        val left = divide.left.interpretE
        val right = divide.right.interpretE

        if (!right.failed && right.first instanceof Long) {
            val value = right.first as Long
            if (value == 0) {
                error(
                    "Division by 0",
                    divide,
                    BalzacPackage.Literals.DIV__RIGHT
                );
            }
        }

        if (!left.failed && !right.failed &&
            left.first instanceof Long && right.first instanceof Long
        ) {
            val a = left.first as Long
            val b = right.first as Long
            if (a % b != 0) {
                warning(
                    "Division with remainder",
                    divide,
                    null
                );
            }
        }
    }

    @Check
    def void checkIntegerOverflows(Plus plus) {
        val left = plus.left.interpretE
        val right = plus.right.interpretE

        if (!left.failed && !right.failed &&
            left.first instanceof Long && right.first instanceof Long
        ) {
            try {
                Math.addExact(left.first as Long, right.first as Long)
            }
            catch (ArithmeticException e) {
                error(
                    "Result overflows 64-bit integer",
                    plus,
                    null
                );
            }
        }
    }

    @Check
    def void checkIntegerOverflows(Minus minus) {
        val left = minus.left.interpretE
        val right = minus.right.interpretE

        if (!left.failed && !right.failed &&
            left.first instanceof Long && right.first instanceof Long
        ) {
            try {
                Math.subtractExact(left.first as Long, right.first as Long)
            }
            catch (ArithmeticException e) {
                error(
                    "Result overflows 64-bit integer",
                    minus,
                    null
                );
            }
        }
    }

    @Check
    def void checkIntegerOverflows(Times times) {
        val left = times.left.interpretE
        val right = times.right.interpretE

        if (!left.failed && !right.failed &&
            left.first instanceof Long && right.first instanceof Long
        ) {
            try {
                Math.multiplyExact(left.first as Long, right.first as Long)
            }
            catch (ArithmeticException e) {
                error(
                    "Result overflows 64-bit integer",
                    times,
                    null
                );
            }
        }
    }

    @Check
    def void checkAssertions(Assertion assertion) {
        val res = assertion.exp.interpretE
        if (!res.failed && res.first instanceof Boolean) {
            val assertOk = res.first as Boolean
            if (!assertOk) {
                val errorString =
                    if (assertion.err === null) "Assertion failed"
                    else {
                        val resErr = assertion.err.interpretE
                        if (!resErr.failed && resErr.first instanceof String) {
                            resErr.first as String
                        }
                        else "Assertion failed"
                    }
                error(
                    errorString,
                    assertion.exp,
                    null
                );
            }
        }
        else {
            warning(
                    "Cannot evaluate expression "+assertion.exp.nodeToString,
                    assertion.exp,
                    null
                );
        }
    }
}