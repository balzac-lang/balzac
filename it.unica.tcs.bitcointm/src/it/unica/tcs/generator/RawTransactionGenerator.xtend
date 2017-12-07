/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.generator

import com.google.inject.Inject
import it.unica.tcs.bitcoinTM.Compile
import it.unica.tcs.bitcoinTM.Declaration
import it.unica.tcs.bitcoinTM.PackageDeclaration
import it.unica.tcs.compiler.TransactionCompiler
import it.unica.tcs.lib.utils.BitcoinUtils
import it.unica.tcs.xsemantics.BitcoinTMInterpreter
import java.io.File
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.xtext.EcoreUtil2
import org.eclipse.xtext.generator.AbstractGenerator
import org.eclipse.xtext.generator.IFileSystemAccess2
import org.eclipse.xtext.generator.IGeneratorContext
import org.eclipse.xtext.naming.IQualifiedNameProvider
import it.unica.tcs.bitcoinTM.Model

class RawTransactionGenerator extends AbstractGenerator {
	
	@Inject private extension IQualifiedNameProvider	
	@Inject private extension TransactionCompiler	
	@Inject private extension BitcoinTMInterpreter
	
	override doGenerate(Resource resource, IFileSystemAccess2 fsa, IGeneratorContext context) {
		val model = resource.allContents.toIterable.filter(Model).get(0)
		val packages = EcoreUtil2.getAllContentsOfType(model, PackageDeclaration)
		val packagePath =
			if (packages.isEmpty) {
				""
			}
			else {
				var package = packages.get(0)
				package.fullyQualifiedName.toString(File.separator)
			}
		
		
        fsa.generateFile(packagePath + File.separator + "transactions", model.compileTransactions)
        fsa.generateFile('/DEFAULT_ARTIFACT', model.compileTransactions)
	}
	
	def private compileTransactions(Model model) {
		
		val compiles = EcoreUtil2.getAllContentsOfType(model, Compile)
		
		if (compiles.isEmpty)
			return "";
			
		val sb = new StringBuilder
			
		compiles.get(0).txs
			.forEach[d | 
				val txDecl = d.ref.eContainer as Declaration
				val txBuilder = txDecl.compileTransaction
				
				for (var i=0 ; i<d.actualParams.size; i++) {
					val actualP = d.actualParams.get(i).interpret(newHashMap).first
					val formalP = txDecl.left.params.get(i)
					
					txBuilder.bindVariable(formalP.name, actualP)
				}
				
				val tx = txBuilder.toTransaction
				
				sb.append("transaction ").append(txDecl.left.name).append("\n")
				sb.append(tx).append("\n")
				sb.append(BitcoinUtils.encode(tx.bitcoinSerialize)).append("\n\n\n")
			]
			
		sb.toString
	}
}