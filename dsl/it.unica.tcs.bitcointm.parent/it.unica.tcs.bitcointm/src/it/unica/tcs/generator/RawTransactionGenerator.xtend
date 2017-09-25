package it.unica.tcs.generator

import com.google.inject.Inject
import it.unica.tcs.bitcoinTM.PackageDeclaration
import it.unica.tcs.bitcoinTM.TransactionDeclaration
import java.io.File
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.xtext.EcoreUtil2
import org.eclipse.xtext.generator.AbstractGenerator
import org.eclipse.xtext.generator.IFileSystemAccess2
import org.eclipse.xtext.generator.IGeneratorContext
import org.eclipse.xtext.naming.IQualifiedNameProvider
import it.unica.tcs.compiler.TransactionCompiler

import static extension it.unica.tcs.utils.Utils2.*
import static extension it.unica.tcs.bitcointm.lib.utils.BitcoinJUtils.*

class RawTransactionGenerator extends AbstractGenerator {
	
	@Inject private extension IQualifiedNameProvider	
	@Inject private extension TransactionCompiler	
	
	override doGenerate(Resource resource, IFileSystemAccess2 fsa, IGeneratorContext context) {
		var package = resource.allContents.toIterable.filter(PackageDeclaration).get(0)
		var packagePath = package.fullyQualifiedName.toString(File.separator) ;
        fsa.generateFile(packagePath + File.separator + "transactions", package.compileTransactions)
	}
	
	def private compileTransactions(PackageDeclaration pkg) {
		
		val txs = EcoreUtil2.eAllOfType(pkg, TransactionDeclaration)
		
		'''
		«FOR tx:txs»
			«val txBuilder = tx.compileTransaction»
			«IF txBuilder.isReady»
				«val txJ = txBuilder.toTransaction(tx.networkParams)»
				TX «tx.name»
				serial: «txJ.bitcoinSerialize.encode»
				«txJ.toString»
				
			«ENDIF»
		«ENDFOR»
		'''
	}
}