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

package xyz.balzaclang.validation

import com.google.inject.Inject
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
import xyz.balzaclang.balzac.AbsoluteTime
import xyz.balzaclang.balzac.AddressLiteral
import xyz.balzaclang.balzac.Assertion
import xyz.balzaclang.balzac.BalzacPackage
import xyz.balzaclang.balzac.Between
import xyz.balzaclang.balzac.CheckBlock
import xyz.balzaclang.balzac.CheckBlockDelay
import xyz.balzaclang.balzac.CheckDate
import xyz.balzaclang.balzac.CheckTimeDelay
import xyz.balzaclang.balzac.Div
import xyz.balzaclang.balzac.Expression
import xyz.balzaclang.balzac.Import
import xyz.balzaclang.balzac.Input
import xyz.balzaclang.balzac.KeyLiteral
import xyz.balzaclang.balzac.Minus
import xyz.balzaclang.balzac.Model
import xyz.balzaclang.balzac.Modifier
import xyz.balzaclang.balzac.PackageDeclaration
import xyz.balzaclang.balzac.Participant
import xyz.balzaclang.balzac.Placeholder
import xyz.balzaclang.balzac.Plus
import xyz.balzaclang.balzac.Reference
import xyz.balzaclang.balzac.Referrable
import xyz.balzaclang.balzac.RelativeTime
import xyz.balzaclang.balzac.Script
import xyz.balzaclang.balzac.Signature
import xyz.balzaclang.balzac.This
import xyz.balzaclang.balzac.Times
import xyz.balzaclang.balzac.Transaction
import xyz.balzaclang.balzac.TransactionHexLiteral
import xyz.balzaclang.balzac.TransactionInputOperation
import xyz.balzaclang.balzac.TransactionOutputOperation
import xyz.balzaclang.balzac.Versig
import xyz.balzaclang.lib.ECKeyStore
import xyz.balzaclang.lib.model.transaction.ITransactionBuilder
import xyz.balzaclang.lib.model.transaction.SerialTransactionBuilder
import xyz.balzaclang.lib.model.transaction.TransactionBuilder
import xyz.balzaclang.lib.validation.ValidationResult.InputValidationError
import xyz.balzaclang.lib.validation.Validator
import xyz.balzaclang.utils.ASTUtils
import xyz.balzaclang.xsemantics.BalzacInterpreter
import xyz.balzaclang.xsemantics.Rho

import static extension xyz.balzaclang.utils.ASTExtensions.*

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

    @Check
    def void checkUnusedParameters__Script(Script script){

        for (param : script.params) {
            var references = EcoreUtil.UsageCrossReferencer.find(param, script.exp);
            if (references.size==0)
                warning("Unused variable '"+param.name+"'.",
                    param,
                    null,
                    BalzacValidatorCodes.WARNING_UNUSED_PARAM
                )
        }
    }

    @Check
    def void checkUnusedParameters__Transaction(Transaction tx){

        for (param : tx.params) {
            var references = EcoreUtil.UsageCrossReferencer.find(param, tx);
            if (references.size==0)
                warning("Unused variable '"+param.name+"'.",
                    param,
                    null,
                    BalzacValidatorCodes.WARNING_UNUSED_PARAM
                )
        }
    }

    @Check
    def void checkVerSigDuplicatedKeys(Versig versig) {

        for(var i=0; i<versig.pubkeys.size-1; i++) {
            for(var j=i+1; j<versig.pubkeys.size; j++) {

                var k1 = versig.pubkeys.get(i).interpretE
                var k2 = versig.pubkeys.get(j).interpretE

                if (!k1.failed && !k2.failed && k1.value == k2.value) {
                    warning("Duplicated public key.", versig, BalzacPackage.Literals.VERSIG__PUBKEYS, i, BalzacValidatorCodes.WARNING_VERSIG_DUPLICATED_PUBKEY, String.valueOf(i))
                    warning("Duplicated public key.", versig, BalzacPackage.Literals.VERSIG__PUBKEYS, j, BalzacValidatorCodes.WARNING_VERSIG_DUPLICATED_PUBKEY, String.valueOf(j))
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

    @Check(NORMAL)
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

        if (!isTxDefined && isWithinInput) {
            // nothing to check here
            return
        }

        logger.debug("[checkSigTransaction] interpreting transaction " + sig.tx.nodeToString)
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
            logger.error('''Failed to evaluate '«sig.tx.nodeToString»' [error]''', res.ruleFailedException)
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
        // check only transactions without parameters
        if (!tx.params.empty) {
            return
        }

        tx.checkTx(new Rho(tx.networkParams), tx, true)
    }

    @Check(CheckType.NORMAL)
    def void checkTxReference(Reference ref) {
        // check only transactions
        if (!(ref.ref instanceof Transaction)) {
            return
        }

        // check only parametric transactions
        if (ref.actualParams.empty) {
            return
        }

        // do not check transactions that have placeholders as actual parameters
        val isInsideASignature = EcoreUtil2.getContainerOfType(ref, Signature) !== null
        if (ref.actualParams.exists[e|e instanceof Placeholder] && isInsideASignature) {
            return
        }

        val tx = ref.ref as Transaction
        logger.debug('''Checking transaction reference '«ref.nodeToString»' ''')
        val values = ref.actualParams
        val rho = new Rho(tx.networkParams)
        for(var i=0; i<tx.params.size; i++) {
            val fp = tx.params.get(i)
            val ap = values.get(i)
            val value = ap.interpretE
            if (!value.failed)
                rho.put(fp, value.first)
            else
                logger.warn('''Failed to evaluate '«ap.nodeToString»' [warn]''')
        }
        tx.checkTx(rho, ref, false)
    }

    def private boolean checkTx(Transaction tx, Rho rho, EObject source, boolean sourceIsTx) {

        logger.debug("checkTx: " + tx.nodeToString + " " + rho)

        var hasError = false;

        /*
         * Verify that inputs are valid
         */
        if (!tx.isCoinbase) {

            val mapInputsTx = new HashMap<Input, ITransactionBuilder>
            for (input: tx.inputs) {
                /*
                 * get the transaction input
                 */
                val txInput = input.txRef

                if (txInput.txVariables.empty) {

                    logger.debug("[input.txRef]: " + input.txRef.nodeToString + " " + rho)
                    val res = input.txRef.interpret(rho)

                    if (res.failed) {
                        logger.warn('''Failed to evaluate '«input.txRef.nodeToString»' [warn]''', res.ruleFailedException)
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

            if(hasError) return false;  // interrupt the check

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

            if(hasError) return false;  // interrupt the check

            /*
             * Verify that the fees are positive
             */
            hasError = !checkFee(tx, rho, mapInputsTx, source, sourceIsTx)

            if(hasError) return false;  // interrupt the check
        }

        logger.debug("[tx]: " + tx.nodeToString + " " + rho)
        val res = tx.interpret(rho)

        if (res.failed || !(res.value instanceof TransactionBuilder))
            return false;

        val txBuilder = res.value as TransactionBuilder

        /*
         * Verify that the input correctly spends the output
         */
        hasError = !correctlySpendsOutput(txBuilder, tx.ECKeyStore, source, sourceIsTx)

        if(hasError) return false;  // interrupt the check

        /*
         * Verify that the output scripts does not exceeds 520 bytes
         */
        hasError = !checkOutputScriptSize(txBuilder, source, sourceIsTx)

        if(hasError) return false;  // interrupt the check

        /*
         * Verify that the output values are positive
         */
        hasError = !checkPositiveOutputValue(txBuilder, source, sourceIsTx)
        
        return hasError
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
                if (v.ref.eContainer instanceof Transaction) {
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

        if (txA.toTransaction(inputA.ECKeyStore) == txB.toTransaction(inputB.ECKeyStore) && inputA.outpoint==inputB.outpoint
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

    def boolean checkFee(Transaction tx, Rho rho, Map<Input, ITransactionBuilder> mapInputsTx, EObject source, boolean sourceIsTx) {

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
                if (sourceIsTx) BalzacPackage.Literals.TRANSACTION__NAME
            );
            return false;
        }

        return true;
    }

    def boolean correctlySpendsOutput(TransactionBuilder txBuilder, ECKeyStore keystore, EObject source, boolean sourceIsTx) {

        val validationResult = Validator.checkWitnessesCorrecltySpendsOutputs(txBuilder, keystore)

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
                    if (sourceIsTx) BalzacPackage.Literals.TRANSACTION__INPUTS,
                    validationResult.index
                )
            }
            else {
                warning('''
                    Something went wrong verifying the input witnesses.
                    «validationResult.message»''',
                    source,
                    BalzacPackage.Literals.TRANSACTION__INPUTS
                )
            }
        }

        return true
    }

    def boolean checkOutputScriptSize(TransactionBuilder txBuilder, EObject source, boolean sourceIsTx) {

        for (var i = 0 ; i < txBuilder.outputs.size; i++) {
            val output = txBuilder.outputs.get(i)

            val validationResult = Validator.checkOutputScriptSize(output.script)

            if (validationResult.error) {
                warning('''The output script at index «i» exceeds the size limit of 520 bytes. The transaction will be refused by the network.''',
                    source,
                    if (sourceIsTx) BalzacPackage.Literals.TRANSACTION__OUTPUTS,
                    i
                )
            }
        }

        return true
    }

    def boolean checkPositiveOutputValue(TransactionBuilder txBuilder, EObject source, boolean sourceIsTx) {

        for (var i = 0 ; i < txBuilder.outputs.size; i++) {
            val output = txBuilder.outputs.get(i)
            val value = output.value

            if (output.script.isOP_RETURN && value > 0) {
                error("OP_RETURN output scripts must have 0 value.",
                    source,
                    if (sourceIsTx) BalzacPackage.Literals.OUTPUT__VALUE,
                    i
                );
                return false;
            }

            if (!output.script.isOP_RETURN && value < 546) {
                // https://github.com/bitcoin/bitcoin/commit/6a4c196dd64da2fd33dc7ae77a8cdd3e4cf0eff1
                error("Output (except OP_RETURN scripts) must spend at least 546 satoshis.",
                    source,
                    if (sourceIsTx) BalzacPackage.Literals.OUTPUT__VALUE,
                    i
                );
                return false;
            }
        }

        return true
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
                logger.debug("checkUniqueRelativeTimelock")
                val tx1 = tlock.tx.interpretE
                val tx2 = other.tx.interpretE

                if (tx1.failed) {
                    logger.debug('''Failed to evaluate '«tlock.tx.nodeToString»' [skip]''')
                    return;
                }

                if (tx2.failed) {
                    logger.debug('''Failed to evaluate '«other.tx.nodeToString»' [skip]''')
                    return;
                }

                if ( ITransactionBuilder.equals(tx1.first as ITransactionBuilder, tx2.first as ITransactionBuilder, tlock.ECKeyStore) )
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
            logger.debug("checkRelativeTimelockFromTxIsInput")
            val tx = tlock.tx.interpretE

            if (tx.failed) {
                logger.debug('''Failed to evaluate '«tlock.tx.nodeToString»' [skip]''')
                return;
            }

            val containingTx = EcoreUtil2.getContainerOfType(tlock, Transaction);

            for (in : containingTx.inputs) {
                val inTx = in.txRef.interpretE

                if (inTx.failed) {
                    logger.debug('''Failed to evaluate '«in.txRef.nodeToString»' [skip]''')
                    return;
                }

                if ( ITransactionBuilder.equals(tx.first as ITransactionBuilder, inTx.first as ITransactionBuilder, tlock.ECKeyStore) ) {
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

        if (res.failed) {
            logger.debug('''Failed to evaluate '«exp.nodeToString»' [skip]''')
            return;
        }

        val value = res.first as Long

        if (value<0) {
            error(
                "Negative timelock is not permitted.",
                obj,
                feature
            )
            return
        }

        if (isAbsolute && isBlock && value >= 500_000_000) {
            error(
                "Block number must be lower than 500_000_000.",
                obj,
                feature
            )
            return
        }

        if (isAbsolute && !isBlock && value < 500_000_000) {
            error(
                "Block number must be greater or equal than 500_000_000 (1985-11-05 00:53:20). Found "+value,
                obj,
                feature
            )
        }

        if (!isAbsolute) {
            if (!isBlock && !value.delayValue.fitIn16bits) {
                error(
                    '''Relative timelocks must fit within unsigned 16-bits. Delay value is «value» seconds, max allowed is «0xFFFF*512»''',
                    obj,
                    feature
                )
            }
            if (!isBlock && value.isDelayTruncated) {
                warning(
                    '''Delay value of «value» seconds is truncated to «value.delayValue*512» seconds''',
                    obj,
                    feature
                )
            }
            if (isBlock && !value.fitIn16bits) {
                error(
                    '''Relative timelocks must fit within unsigned 16-bits. Block value is «value», max allowed is «0xFFFF»''',
                    obj,
                    feature
                )
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
            logger.debug('''Failed to evaluate '«assertion.exp.nodeToString»' [warn]''')
            warning(
                    "Cannot evaluate expression "+assertion.exp.nodeToString,
                    assertion.exp,
                    null
                );
        }
    }

    @Check
    def void checkBetween(Between between) {
        val leftRes = between.left.interpretE
        val rightRes = between.right.interpretE

        if (!leftRes.failed && (leftRes.value instanceof Long) &&
            !rightRes.failed && (rightRes.value instanceof Long)
        ) {
            val left = leftRes.value as Long
            val right = rightRes.value as Long

            if (left >= right) {
                error(
                    '''Invalid range. Value «left» must be lower than «right»''',
                    between,
                    BalzacPackage.Literals.BETWEEN__LEFT
                )
            }
            if (left + 1 == right) {
                warning(
                    '''Unnecessary usage of between. Replace with «between.value.nodeToString» == «left»''',
                    between,
                    null,
                    BalzacValidatorCodes.WARNING_USELESS_BETWEEN,
                    '''«between.value.nodeToString» == «left»'''
                )
            }

        }
    }
}
