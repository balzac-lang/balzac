/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.generator

import com.google.inject.Inject
import it.unica.tcs.balzac.Eval
import it.unica.tcs.balzac.Model
import it.unica.tcs.balzac.PackageDeclaration
import it.unica.tcs.lib.ITransactionBuilder
import it.unica.tcs.lib.utils.BitcoinUtils
import it.unica.tcs.xsemantics.BalzacInterpreter
import java.io.File
import org.apache.log4j.Logger
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.xtext.EcoreUtil2
import org.eclipse.xtext.generator.AbstractGenerator
import org.eclipse.xtext.generator.IFileSystemAccess2
import org.eclipse.xtext.generator.IGeneratorContext
import org.eclipse.xtext.generator.InMemoryFileSystemAccess
import org.eclipse.xtext.naming.IQualifiedNameProvider
import it.unica.tcs.utils.ASTUtils

class RawTransactionGenerator extends AbstractGenerator {

    static final Logger logger = Logger.getLogger(RawTransactionGenerator)
    @Inject extension IQualifiedNameProvider
    @Inject extension BalzacInterpreter
    @Inject extension ASTUtils

    override doGenerate(Resource resource, IFileSystemAccess2 fsa, IGeneratorContext context) {
        logger.info("Evaluating expressions for resource "+resource.URI)
        val models = resource.allContents.toIterable.filter(Model)
        val model = models.get(0)
        val packages = EcoreUtil2.getAllContentsOfType(model, PackageDeclaration)
        val packagePath =
            if (packages.isEmpty) {
                ""
            }
            else {
                var package = packages.get(0)
                package.fullyQualifiedName.toString(File.separator)
            }

        if (fsa instanceof InMemoryFileSystemAccess) {
            fsa.generateFile('/DEFAULT_ARTIFACT', model.compileTransactions)
        }
        else {
            fsa.generateFile(packagePath + File.separator + "transactions", model.compileTransactions)
        }
    }

    def private compileTransactions(Model model) {

        val compiles = EcoreUtil2.getAllContentsOfType(model, Eval)

        if (compiles.isEmpty)
            return "";

        val sb = new StringBuilder

        compiles.get(0).exps
            .forEach[e |
                val nodeString = e.nodeToString

                logger.info("interpret expression "+nodeString)
                val res = e.interpretE

                if (!res.failed) {

                    val obj = res.first
                    
                    sb.append(nodeString).append("\n")
                    
                    if (obj instanceof ITransactionBuilder) {
                        val tx = obj.toTransaction(astUtils.getECKeyStore(model))
                        sb.append(tx).append("\n")
                        sb.append(BitcoinUtils.encode(tx.bitcoinSerialize)).append("\n\n\n")
                    }
                    else {
                        sb.append("    ").append(obj.toString).append("\n\n\n")
                    }
                }
                else {
                    res.ruleFailedException.printStackTrace
                    sb.append("Cannot evaluate expression ").append(nodeString).append("\n\n\n")
                    logger.warn("cannot evaluate expression "+nodeString)
                }
            ]

        sb.toString
    }
}