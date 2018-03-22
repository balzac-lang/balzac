/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.generator

import com.google.inject.Inject
import it.unica.tcs.bitcoinTM.Compile
import it.unica.tcs.bitcoinTM.Model
import it.unica.tcs.bitcoinTM.PackageDeclaration
import it.unica.tcs.lib.ITransactionBuilder
import it.unica.tcs.lib.utils.BitcoinUtils
import it.unica.tcs.xsemantics.BitcoinTMInterpreter
import java.io.File
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.xtext.EcoreUtil2
import org.eclipse.xtext.generator.AbstractGenerator
import org.eclipse.xtext.generator.IFileSystemAccess2
import org.eclipse.xtext.generator.IGeneratorContext
import org.eclipse.xtext.generator.InMemoryFileSystemAccess
import org.eclipse.xtext.naming.IQualifiedNameProvider
import org.eclipse.xtext.nodemodel.util.NodeModelUtils

class RawTransactionGenerator extends AbstractGenerator {

    @Inject private extension IQualifiedNameProvider
    @Inject private extension BitcoinTMInterpreter

    override doGenerate(Resource resource, IFileSystemAccess2 fsa, IGeneratorContext context) {
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

        val compiles = EcoreUtil2.getAllContentsOfType(model, Compile)

        if (compiles.isEmpty)
            return "";

        val sb = new StringBuilder

        compiles.get(0).txs
            .forEach[r |

                val res = r.interpretE

                if (!res.failed) {

                    val obj = res.first

                    if (obj instanceof ITransactionBuilder) {
                        val tx = obj.toTransaction(astUtils.getECKeyStore(model))
                        sb.append(tx).append("\n")
                        sb.append(BitcoinUtils.encode(tx.bitcoinSerialize)).append("\n\n\n")
                    }
                    else {
                        sb.append(obj.toString).append("\n\n\n")
                    }
                }
                else {
                    res.ruleFailedException.printStackTrace
                    sb.append("Cannot compile expression "+NodeModelUtils.getTokenText(NodeModelUtils.getNode(r))).append("\n\n\n")
                }
            ]

        sb.toString
    }
}