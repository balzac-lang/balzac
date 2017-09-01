package it.unica.tcs.generator

import com.google.inject.Inject
import it.unica.tcs.bitcoinTM.PackageDeclaration
import it.unica.tcs.bitcoinTM.SerialTxBody
import it.unica.tcs.bitcoinTM.TransactionDeclaration
import it.unica.tcs.bitcoinTM.TxBody
import it.unica.tcs.bitcoinTM.UserDefinedTxBody
import it.unica.tcs.xsemantics.BitcoinTMTypeSystem
import java.io.File
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.xtext.EcoreUtil2
import org.eclipse.xtext.generator.AbstractGenerator
import org.eclipse.xtext.generator.IFileSystemAccess2
import org.eclipse.xtext.generator.IGeneratorContext
import org.eclipse.xtext.naming.IQualifiedNameProvider

import static extension it.unica.tcs.utils.ASTUtils.*
import static extension it.unica.tcs.utils.CompilerUtils.*
import it.unica.tcs.bitcoinTM.Declaration

class TransactionFactoryGenerator extends AbstractGenerator {
	
	@Inject private extension IQualifiedNameProvider	
	@Inject private extension BitcoinTMTypeSystem typeSystem
	
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
			public static ITransactionBuilder tx_«tx.name»(«tx.body.compileTxParameters») {
				«tx.body.compileTxBody»
			}
			
			«ENDFOR»
		}
		'''
	}
	
	def private dispatch compileTxParameters(TxBody tx) ''''''
	
	def private dispatch compileTxParameters(UserDefinedTxBody tx) '''«tx.params.map[p|p.paramType.compileType+" "+p.name].join(", ")»'''
	
	def private dispatch compileTxBody(TxBody tx) ''''''
	
	def private dispatch compileTxBody(UserDefinedTxBody tx) '''
		TransactionBuilder tb = new «IF tx.isCoinbase»CoinbaseTransactionBuilder«ELSE»TransactionBuilder«ENDIF»();
		«IF !tx.params.isEmpty»
			
			// free variables
		«ENDIF»
		«FOR param:tx.params»
			tb.freeVariable(«param.name», «param.paramType.compileType».class);
		«ENDFOR»
		«IF !tx.inputs.isEmpty»
			
			// inputs
			ITransactionBuilder parentTx;
			int outIndex;
			ScriptBuilder2 inScript;
			int relativeLocktime;
		«ENDIF»
		«FOR input:tx.inputs»
			parentTx = null;
			outIndex = «if (input.txRef!==null) input.txRef.idx else 0»;
			inScript = null;
			«IF (tx.tlock!==null && tx.tlock.containsRelative(tx.eContainer as TransactionDeclaration))»
				// relative timelock
				relativeLocktime = «tx.tlock.getRelative(tx.eContainer as TransactionDeclaration)»;
				tb.addInput(parentTx, outIndex, inScript, relativeLocktime);
			«ELSE»
				tb.addInput(parentTx, outIndex, inScript);
			«ENDIF»
		«ENDFOR»
		«IF !tx.outputs.isEmpty»
					
			// outputs
			ScriptBuilder2 outScript;
			int satoshis;
		«ENDIF»
		«FOR output:tx.outputs»
			outScript = null;
			satoshis = «output.value.exp.interpret.first as Integer»;
			tb.addOutput(outScript, satoshis);
		«ENDFOR»
		«IF (tx.tlock!==null && tx.tlock.containsAbsolute)»
			// absolute timelock
			tb.setLocktime(«tx.tlock.getAbsolute()»);
		«ENDIF»		    	
		return tb;
	'''
	
	def private dispatch compileTxBody(SerialTxBody tx) '''
		return new ITransactionBuilder() {
			private final Transaction tx = new Transaction(«tx.compileNetworkParams», Utils.HEX.decode("«tx.bytes»"));
			@Override public boolean isReady() { return true; }
			@Override public Transaction toTransaction(NetworkParameters params) { return tx; }
			@Override public int getInputsSize() { return tx.getInputs().size(); }
			@Override public int getOutputsSize() { return tx.getOutputs().size(); }
		};
	'''
	
}