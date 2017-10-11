/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.generator

import com.google.inject.Inject
import it.unica.tcs.bitcoinTM.PackageDeclaration
import it.unica.tcs.bitcoinTM.ProtocolExpression
import it.unica.tcs.bitcoinTM.ProtocolTransactionReference
import it.unica.tcs.bitcoinTM.SerialTransactionDeclaration
import it.unica.tcs.bitcoinTM.TransactionDeclaration
import it.unica.tcs.bitcoinTM.TransactionReference
import it.unica.tcs.bitcoinTM.UserTransactionDeclaration
import it.unica.tcs.compiler.TransactionCompiler
import it.unica.tcs.lib.ScriptBuilder2
import it.unica.tcs.utils.ASTUtils
import it.unica.tcs.utils.CompilerUtils
import it.unica.tcs.xsemantics.BitcoinTMTypeSystem
import java.io.File
import java.util.List
import org.eclipse.emf.common.util.EList
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.xtext.EcoreUtil2
import org.eclipse.xtext.generator.AbstractGenerator
import org.eclipse.xtext.generator.IFileSystemAccess2
import org.eclipse.xtext.generator.IGeneratorContext
import org.eclipse.xtext.naming.IQualifiedNameProvider

class TransactionFactoryGenerator extends AbstractGenerator {
	
	@Inject private extension IQualifiedNameProvider	
	@Inject private extension BitcoinTMTypeSystem
	@Inject private extension TransactionCompiler	
	@Inject private extension CompilerUtils
	@Inject private extension ASTUtils
	
	override doGenerate(Resource resource, IFileSystemAccess2 fsa, IGeneratorContext context) {
		
		var package = resource.allContents.toIterable.filter(PackageDeclaration).get(0)
		var subpackage = "factory"
		var packagePath = package.fullyQualifiedName.append(subpackage).toString(File.separator) ;
        fsa.generateFile(packagePath + File.separator + "TransactionFactory.java", package.compileTransactionFactory(subpackage))
	}
	
	
	def private compileTransactionFactory(PackageDeclaration pkg, String subpackage) {
		
		val txs = EcoreUtil2.eAllOfType(pkg, TransactionDeclaration)
		'''
		package «pkg.fullyQualifiedName.append(subpackage)»;
					
		import org.bitcoinj.core.*;
		import org.bitcoinj.script.*;
		import it.unica.tcs.lib.*;
		
		@SuppressWarnings("unused")
		public class TransactionFactory {
			
			«FOR tx : txs»
			public static ITransactionBuilder tx_«tx.name»(«tx.compileTxParameters») {
				«tx.compileTxBody»
			}
			
			«ENDFOR»
		}
		'''
	}
	
	def private dispatch compileTxParameters(SerialTransactionDeclaration tx) ''''''
	def private dispatch compileTxParameters(UserTransactionDeclaration tx) '''«tx.params.compileFormalParams»'''
	
	def private dispatch compileTxBody(SerialTransactionDeclaration tx) '''
		return ITransactionBuilder.fromSerializedTransaction(«tx.compileNetworkParams», "«tx.bytes»");
	'''
	
	def private dispatch compileTxBody(UserTransactionDeclaration tx) '''
		TransactionBuilder tb = new «IF tx.isCoinbase»CoinbaseTransactionBuilder«ELSE»TransactionBuilder«ENDIF»(«tx.compileNetworkParams»);
		«IF !tx.params.isEmpty»
			
			// free variables
		«ENDIF»
		«FOR param:tx.params»
			tb.freeVariable(«param.name», «param.paramType.compileType».class);
		«ENDFOR»
		«IF !tx.body.inputs.isEmpty»
			
			// inputs
			ITransactionBuilder parentTx;
			int outIndex;
			ScriptBuilder2 inScript;
			int relativeLocktime;
		«ENDIF»
		«FOR input:tx.body.inputs»
			«IF tx.isCoinbase»
				inScript = new ScriptBuilder2().number(42);
				((CoinbaseTransactionBuilder)tb).addInput(inScript);
			«ELSE»
				parentTx = «getTransactionFromFactory(input.txRef.tx.name, input.txRef.actualParams)»;
				outIndex = «input.outpoint»;
				inScript = ScriptBuilder2.deserialize("«ScriptBuilder2.serialize(input.compileInput)»");
				«IF (tx.body.tlock!==null && tx.body.tlock.containsRelative(tx))»
					// relative timelock
					relativeLocktime = «tx.body.tlock.getRelative(tx)»;
					tb.addInput(parentTx, outIndex, inScript, relativeLocktime);
				«ELSE»
					tb.addInput(parentTx, outIndex, inScript);
				«ENDIF»				
			«ENDIF»
		«ENDFOR»
		«IF !tx.body.outputs.isEmpty»
					
			// outputs
			ScriptBuilder2 outScript;
			int satoshis;
		«ENDIF»
		«FOR output:tx.body.outputs»
			outScript = ScriptBuilder2.deserialize("«ScriptBuilder2.serialize(output.compileOutput)»");
			satoshis = «output.value.exp.interpret.first as Integer»;
			tb.addOutput(outScript, satoshis);
		«ENDFOR»
		«IF (tx.body.tlock!==null && tx.body.tlock.containsAbsolute)»
			// absolute timelock
			tb.setLocktime(«tx.body.tlock.getAbsolute()»);
		«ENDIF»		    	
		return tb;
	'''
	
	def public getTransactionFromFactory(ProtocolTransactionReference tx) {
		return getTransactionFromFactory(tx.txRef)
	}
	
	def public getTransactionFromFactory(TransactionReference txRef) {
		return getTransactionFromFactory(txRef.tx.name, txRef.actualParams)
	}
	
	def public getTransactionFromFactory(String name, EList<?> actualParams) {
		return getTransactionFromFactory(name, actualParams.map[x|x as ProtocolExpression])
	}
	
	def public getTransactionFromFactory(String name, List<ProtocolExpression> actualParams) {
		'''TransactionFactory.tx_«name»(«actualParams.compileActualParams»)'''
	}
}