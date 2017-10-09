/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.generator

import com.google.inject.Inject
import it.unica.tcs.bitcoinTM.Choice
import it.unica.tcs.bitcoinTM.PackageDeclaration
import it.unica.tcs.bitcoinTM.Parallel
import it.unica.tcs.bitcoinTM.ParticipantDeclaration
import it.unica.tcs.bitcoinTM.ProcessDeclaration
import it.unica.tcs.bitcoinTM.ProcessReference
import it.unica.tcs.bitcoinTM.ProtocolIfThenElse
import java.io.File
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.xtext.generator.AbstractGenerator
import org.eclipse.xtext.generator.IFileSystemAccess2
import org.eclipse.xtext.generator.IGeneratorContext
import org.eclipse.xtext.naming.IQualifiedNameProvider
import org.eclipse.xtext.nodemodel.util.NodeModelUtils

import it.unica.tcs.utils.CompilerUtils
import it.unica.tcs.bitcoinTM.Tau
import it.unica.tcs.bitcoinTM.Assert
import it.unica.tcs.bitcoinTM.Send
import it.unica.tcs.bitcoinTM.Receive
import it.unica.tcs.bitcoinTM.Ask
import it.unica.tcs.bitcoinTM.Put

class ParticipantGenerator extends AbstractGenerator {
	
	@Inject private extension IQualifiedNameProvider
	@Inject private extension ProtocolExpressionGenerator
	@Inject private extension CompilerUtils
	@Inject private extension TransactionFactoryGenerator
	
	override doGenerate(Resource resource, IFileSystemAccess2 fsa, IGeneratorContext context) {
		
		var packageDecl = resource.allContents.toIterable.filter(PackageDeclaration).get(0)
		var subpackage = "participant"
		var package = packageDecl.fullyQualifiedName.append(subpackage);
		var packagePath = packageDecl.fullyQualifiedName.append(subpackage).toString(File.separator) ;
		
		for (participant : resource.allContents.toIterable.filter(ParticipantDeclaration)) {
			
            fsa.generateFile(packagePath + File.separator + '''Participant_«participant.name».java''', participant.compileParticipant(package.toString))
        }
	}
	
	def private compileParticipant(ParticipantDeclaration participant, String ^package) {
		
		'''
		package «^package»;
					
		import org.bitcoinj.core.*;
		import org.bitcoinj.script.*;
		import it.unica.tcs.lib.*;
		import it.unica.tcs.lib.model.*;
		import it.unica.tcs.lib.model.*;
		import it.unica.tcs.lib.model.Process;
		import static it.unica.tcs.lib.utils.BitcoinUtils.*;
		
		@SuppressWarnings("unused")
		public class Participant_«participant.name» extends Participant {
			
			private static Participant_«participant.name» instance = new Participant_«participant.name»();
			«FOR k:participant.keys»
			private static ECKey «k.name» = wifToECKey("«k.value»", «k.compileNetworkParams»); 
			«ENDFOR»
			
			private Participant_«participant.name»() {
				super(Participant_«participant.name».class.getName());
			}
			
			public static Participant_«participant.name» instance() {
				return instance;
			}
			
			@Override
			public void run() {
				/*
				 * «NodeModelUtils.findActualNodeFor(participant.process).text.trim»
				 */
				«participant.process.compileProcess»
			}
			
			«FOR pdecl : participant.defs»
				«pdecl.compileProcessDeclaration»
			«ENDFOR»
		}
		'''
	}
	
	
	def private String compileProcessDeclaration(ProcessDeclaration decl) { '''
		/*
		 * «NodeModelUtils.findActualNodeFor(decl).text.trim»
		 */
		private static class Process_«decl.name» implements Process {
			
			«FOR p:decl.params»
			private «p.paramType.compileType» «p.name»;
			«ENDFOR»
			
			private Process_«decl.name»(«decl.params.compileFormalParams») {
				«FOR p:decl.params»
				this.«p.name» = «p.name»;
				«ENDFOR»
			}
			
			public static Process_«decl.name» instance(«decl.params.compileFormalParams») {
				return new Process_«decl.name»(«decl.params.map[p|p.name].join(",")»);
			}
			
			/*
			 * «NodeModelUtils.findActualNodeFor(decl.process).text.trim»
			 */
			public void run() {
				«decl.process.compileProcess»
			}
		}'''
	}
	
	def private dispatch String compileProcess(Choice choice) {
		if (choice.actions.size==1) {
			choice.actions.get(0).compilePrefix
		}
		else {
			
			'''
			choice(
			«choice.actions.map[p|
			
				'''«IF p.next===null»	choiceElm(«p.compilePrefix»)«ELSE»	choiceElm(«p.compilePrefix», () -> {
		«p.next.compileProcess»
	}«ENDIF»'''].join(",\n")»			
			);'''
		}
	}
	
	def private dispatch String compileProcess(ProcessReference pref) {
		'''Process_«pref.ref.name».instance(«pref.actualParams.compileActualParams»).run();'''
	}
	
	def private dispatch String compileProcess(Parallel p) '''
		«IF p.right!==null»
			parallel(() -> {
				«p.left.compileProcess»
			});
			
			«p.right.compileProcess»
		«ELSE»
			«p.left.compileProcess»
		«ENDIF»
	'''
	
	def private dispatch String compileProcess(ProtocolIfThenElse p) '''
		if(«p.exp.compileExpression») {
			«p.^then.compileProcess»
		}«IF p.^else!==null» else {
			«p.^else.compileProcess»
		}
		«ENDIF»
	'''
	
	def private dispatch String compilePrefix(Tau tau) {
		'''«IF tau.next!==null»«tau.next.compileProcess»«ENDIF»'''
		
	}
	
	def private dispatch String compilePrefix(Ask ask) {
		'''
		ask(BitcoinUtils.encode(«ask.txRefs.map[x| x.getTransactionFromFactory]».toTransaction().bitcoinSerialize()));
		«IF ask.next!==null»
		«ask.next.compileProcess»
		«ENDIF»
		'''
	}
	
	def private dispatch String compilePrefix(Put put) {
		'''
		put(BitcoinUtils.encode(«put.txRefs.map[x| x.getTransactionFromFactory]».toTransaction().bitcoinSerialize()));
		«IF put.next!==null»
		«put.next.compileProcess»
		«ENDIF»
		'''
	}

	def private dispatch String compilePrefix(Assert ass) {
		'''
		«IF ass.exp.compileExpression=="true"»
			check();
			«ELSE»
			check(()->{
				return «ass.exp.compileExpression»;
			});
		«ENDIF»
		«IF ass.next!==null»
			«ass.next.compileProcess»
		«ENDIF»
		'''
	}
	
	def private dispatch String compilePrefix(Send send) {
		'''
		send(«send.message.compileExpression», Participant_«send.p.name».instance());
		«IF send.next!==null»
			«send.next.compileProcess»
		«ENDIF»
		'''	
	}
	
	def private dispatch String compilePrefix(Receive receive) {
		'''
		// receive();
		«IF receive.next!==null»
			«receive.next.compileProcess»
		«ENDIF»
		'''	
	}
}


