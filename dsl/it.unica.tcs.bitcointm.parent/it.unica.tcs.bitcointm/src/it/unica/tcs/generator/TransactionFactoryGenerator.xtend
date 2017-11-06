/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.generator

import com.google.inject.Inject
import it.unica.tcs.bitcoinTM.Expression
import it.unica.tcs.bitcoinTM.PackageDeclaration
import it.unica.tcs.bitcoinTM.ProtocolTransactionReference
import it.unica.tcs.bitcoinTM.SerialTransactionDeclaration
import it.unica.tcs.bitcoinTM.TransactionDeclaration
import it.unica.tcs.bitcoinTM.TransactionReference
import it.unica.tcs.bitcoinTM.UserTransactionDeclaration
import it.unica.tcs.compiler.TransactionCompiler
import it.unica.tcs.lib.utils.ObjectUtils
import it.unica.tcs.utils.CompilerUtils
import java.io.File
import java.util.List
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.xtext.EcoreUtil2
import org.eclipse.xtext.generator.AbstractGenerator
import org.eclipse.xtext.generator.IFileSystemAccess2
import org.eclipse.xtext.generator.IGeneratorContext
import org.eclipse.xtext.naming.IQualifiedNameProvider

class TransactionFactoryGenerator extends AbstractGenerator {
	
	@Inject private extension IQualifiedNameProvider	
	@Inject private extension TransactionCompiler	
	@Inject private extension CompilerUtils
//	@Inject private extension ASTUtils
	
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
		import it.unica.tcs.lib.utils.*;
		
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
		«val txBuilder = tx.compileTransaction»
		return ObjectUtils.deserializeObjectFromStringQuietly("«ObjectUtils.serializeObjectToString(txBuilder)»", ITransactionBuilder.class);
«««		TransactionBuilder tb = new «IF txBuilder.isCoinbase»CoinbaseTransactionBuilder«ELSE»TransactionBuilder«ENDIF»(«tx.compileNetworkParams»);
«««		«IF !tx.body.inputs.isEmpty»
«««			
«««			// inputs
«««			ITransactionBuilder parentTx;
«««			int outIndex;
«««			InputScript inScript;
«««			int relativeLocktime;
«««		«ENDIF»
«««		
«««		«FOR i : 0 ..< tx.body.inputs.size»
«««			«val input = tx.body.inputs.get(i)»
«««			«val inputB = txBuilder.inputs.get(i)»
«««			«IF tx.isCoinbase»
«««				inScript = (InputScript) new InputScriptImpl().number(42);
«««				((CoinbaseTransactionBuilder)tb).addInput(inScript);
«««			«ELSE»
«««				parentTx = «getTransactionFromFactory(input.txRef.tx.name, input.txRef.actualParams)»;
«««				outIndex = «inputB.outIndex»;
«««				inScript = (InputScript) new InputScriptImpl("«inputB.script.serialize»");
«««				«IF (tx.body.tlock!==null && tx.body.tlock.containsRelative(tx))»
«««					// relative timelock
«««					relativeLocktime = «tx.body.tlock.getRelative(tx)»;
«««					tb.addInput(parentTx, outIndex, inScript, relativeLocktime);
«««				«ELSE»
«««					tb.addInput(parentTx, outIndex, inScript);
«««				«ENDIF»				
«««			«ENDIF»
«««		«ENDFOR»
«««		«IF !tx.body.outputs.isEmpty»
«««					
«««			// outputs
«««			ScriptBuilder2 outScript;
«««			int satoshis;
«««		«ENDIF»
«««		«FOR i : 0 ..< tx.body.outputs.size»
«««			«val outputB = txBuilder.outputs.get(i)»
«««			outScript = new ScriptBuilder2("«outputB.script.serialize»");
«««			satoshis = «outputB.value»;
«««			tb.addOutput(outScript, satoshis);
«««		«ENDFOR»
«««		«IF (tx.body.tlock!==null && tx.body.tlock.containsAbsolute)»
«««			// absolute timelock
«««			tb.setLocktime(«tx.body.tlock.getAbsolute()»);
«««		«ENDIF»
«««		«IF !tx.params.isEmpty»
«««			
«««			// free variables
«««		«ENDIF»
«««		«FOR param:tx.params»
«««			tb.setFreeVariable("«param.name»", «param.name»);
«««		«ENDFOR»  	
«««		
«««		return tb;
	'''
	
	def public getTransactionFromFactory(ProtocolTransactionReference tx) {
		return getTransactionFromFactory(tx.txRef)
	}
	
	def public getTransactionFromFactory(TransactionReference txRef) {
		return getTransactionFromFactory(txRef.ref.name, txRef.actualParams)
	}
	
	def public getTransactionFromFactory(String name, List<Expression> actualParams) {
		'''TransactionFactory.tx_«name»(«actualParams.compileActualParams»)'''
	}	
}