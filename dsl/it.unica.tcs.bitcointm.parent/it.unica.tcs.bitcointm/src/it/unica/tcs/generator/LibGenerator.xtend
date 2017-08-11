package it.unica.tcs.generator

import com.google.inject.Inject
import it.unica.tcs.bitcoinTM.PackageDeclaration
import it.unica.tcs.bitcoinTM.TransactionDeclaration
import it.unica.tcs.bitcoinTM.TxBody
import it.unica.tcs.bitcoinTM.UserDefinedTxBody
import it.unica.tcs.compiler.ITransactionBuilder
import java.io.File
import org.apache.commons.io.IOUtils
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.xtext.EcoreUtil2
import org.eclipse.xtext.generator.AbstractGenerator
import org.eclipse.xtext.generator.IFileSystemAccess2
import org.eclipse.xtext.generator.IGeneratorContext
import org.eclipse.xtext.naming.IQualifiedNameProvider

import static extension it.unica.tcs.utils.CompilerUtils.*

class LibGenerator extends AbstractGenerator {
	
	@Inject private extension IQualifiedNameProvider	
	
	override doGenerate(Resource resource, IFileSystemAccess2 fsa, IGeneratorContext context) {
		
		for (package : resource.allContents.toIterable.filter(PackageDeclaration)) {

			var subpackage = "lib"
			var packagePath = package.fullyQualifiedName.append(subpackage).toString(File.separator) ;
			
			val library = newArrayList(ITransactionBuilder);
			
			for (clazz : library) {
				val classFile = clazz.getName.replaceAll("\\.",File.separator).concat(".java")
				val input = IOUtils.readLines(this.getClass().getClassLoader().getResourceAsStream(classFile));
				val output = classFile
				if (!fsa.isFile(output))
					fsa.generateFile(output, input.join("\n"))
			}
						
            fsa.generateFile(packagePath + File.separator + "TransactionFactory.java", package.compileTransactionFactory(subpackage))
        }
	}
	
	def private compileTransactionFactory(PackageDeclaration pkg, String subpackage) {
		
		val txs = EcoreUtil2.eAllOfType(pkg, TransactionDeclaration)
		'''
		package «pkg.fullyQualifiedName.append(subpackage)»;
					
		import org.bitcoinj.core.*;
		import org.bitcoinj.script.*;
		
		public class TransactionFactory {
			
			«FOR tx : txs»
			public static ITransactionBuilder tx_«tx.name»(«tx.body.compileTxParameters») {
				
			}
			
			«ENDFOR»
		}
		'''
	}
	
	def private dispatch compileTxParameters(TxBody tx) ''''''
	
	def private dispatch compileTxParameters(UserDefinedTxBody tx) '''«tx.params.map[p|p.paramType.compileType+" "+p.name].join(", ")»'''
}