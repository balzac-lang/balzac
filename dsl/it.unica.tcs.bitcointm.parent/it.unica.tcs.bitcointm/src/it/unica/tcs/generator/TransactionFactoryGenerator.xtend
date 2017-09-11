package it.unica.tcs.generator

import com.google.inject.Inject
import it.unica.tcs.bitcoinTM.BooleanLiteral
import it.unica.tcs.bitcoinTM.Expression
import it.unica.tcs.bitcoinTM.HashLiteral
import it.unica.tcs.bitcoinTM.NumberLiteral
import it.unica.tcs.bitcoinTM.PackageDeclaration
import it.unica.tcs.bitcoinTM.StringLiteral
import it.unica.tcs.bitcoinTM.TransactionDeclaration
import it.unica.tcs.bitcoinTM.VariableReference
import it.unica.tcs.bitcointm.lib.ScriptBuilder2
import it.unica.tcs.compiler.TransactionCompiler
import it.unica.tcs.xsemantics.BitcoinTMTypeSystem
import java.io.File
import org.bitcoinj.core.Utils
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.xtext.EcoreUtil2
import org.eclipse.xtext.generator.AbstractGenerator
import org.eclipse.xtext.generator.IFileSystemAccess2
import org.eclipse.xtext.generator.IGeneratorContext
import org.eclipse.xtext.naming.IQualifiedNameProvider

import static extension it.unica.tcs.utils.ASTUtils.*
import static extension it.unica.tcs.utils.CompilerUtils.*
import it.unica.tcs.bitcoinTM.UserTransactionDeclaration
import it.unica.tcs.bitcoinTM.SerialTransactionDeclaration

class TransactionFactoryGenerator extends AbstractGenerator {
	
	@Inject private extension IQualifiedNameProvider	
	@Inject private extension BitcoinTMTypeSystem
	@Inject private extension TransactionCompiler	
	
	override doGenerate(Resource resource, IFileSystemAccess2 fsa, IGeneratorContext context) {
		
		for (package : resource.allContents.toIterable.filter(PackageDeclaration)) {

			var subpackage = "factory"
			var packagePath = package.fullyQualifiedName.append(subpackage).toString(File.separator) ;
            fsa.generateFile(packagePath + File.separator + "TransactionFactory.java", package.compileTransactionFactory(subpackage))
        }
	}
	
	
	def private compileTransactionFactory(PackageDeclaration pkg, String subpackage) {
		
		val txs = EcoreUtil2.eAllOfType(pkg, TransactionDeclaration)
		'''
		package «pkg.fullyQualifiedName.append(subpackage)»;
					
		import org.bitcoinj.core.*;
		import org.bitcoinj.script.*;
		import it.unica.tcs.bitcointm.lib.*;
		
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
	def private dispatch compileTxParameters(UserTransactionDeclaration tx) '''«tx.params.map[p|p.paramType.compileType+" "+p.name].join(", ")»'''
	
	def private dispatch compileTxBody(SerialTransactionDeclaration tx) '''
		return ITransactionBuilder.fromSerializedTransaction(«tx.compileNetworkParams», "«tx.bytes»");
	'''
	
	def private dispatch compileTxBody(UserTransactionDeclaration tx) '''
		TransactionBuilder tb = new «IF tx.isCoinbase»CoinbaseTransactionBuilder«ELSE»TransactionBuilder«ENDIF»();
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
				parentTx = TransactionFactory.tx_«input.txRef.tx.name»(«input.txRef.actualParams.map[e|e.compileActualParam].join(",")»);
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
	
	def private dispatch String compileActualParam(Expression s) '''/* error */'''
	def private dispatch String compileActualParam(VariableReference v) { v.ref.name }
	def private dispatch String compileActualParam(StringLiteral s) { '"'+s.value+'"' }
	def private dispatch String compileActualParam(NumberLiteral n) { n.value.toString }
	def private dispatch String compileActualParam(BooleanLiteral b) { b.isTrue.toString }
	def private dispatch String compileActualParam(HashLiteral h) '''Utils.HEX.decode("«Utils.HEX.encode(h.value)»")'''
	
	
	
}