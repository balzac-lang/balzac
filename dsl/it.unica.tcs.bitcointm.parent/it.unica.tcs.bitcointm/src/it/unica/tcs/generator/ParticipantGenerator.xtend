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

class ParticipantGenerator extends AbstractGenerator {
	
	@Inject private extension IQualifiedNameProvider
	@Inject private extension ExpressionGenerator
	@Inject private extension CompilerUtils
	
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
		import it.unica.tcs.bitcointm.lib.*;
		import it.unica.tcs.bitcointm.lib.model.prefix.*;
		import it.unica.tcs.bitcointm.lib.model.process.*;
		import it.unica.tcs.bitcointm.lib.model.process.Process;
		import static it.unica.tcs.bitcointm.lib.model.prefix.PrefixFactory.*;
		import static it.unica.tcs.bitcointm.lib.model.process.ProcessFactory.*;
		import static it.unica.tcs.bitcointm.lib.utils.BitcoinUtils.*;
		
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
			public void start() {
				/*
				 * «NodeModelUtils.findActualNodeFor(participant.process).text.trim»
				 */
				«participant.process.compileProcess».run();
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
			'''choice(«choice.actions.get(0).compilePrefix»)'''
		}
		else {
			'''
			choice(
			«choice.actions.map[p|p.compilePrefix].join(",\n")»)'''
		}
	}
	
	def private dispatch String compileProcess(ProcessReference pref) {
		'''Process_«pref.ref.name».instance(«pref.actualParams.compileActualParams»)'''
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
		if(«p.exp.compileExpression» ) {
			«p.^then.compileProcess» 
		}«IF p.^else!==null» else {
			() -> { «p.^else.compileProcess» 
		}
		«ENDIF»
	'''
	
	def private dispatch String compilePrefix(Tau tau) {
		'''«tau.next.compileProcess»'''
	}
	
	def private dispatch String compilePrefix(Ask ask) {
		if (ask.next===null){
			'''createAsk()'''	
		}
		else {
			'''createAsk(«ask.next.compileProcess»)'''
		}
	}

	def private dispatch String compilePrefix(Assert ass) {
		if (ass.exp.compileExpression=="true"){
			if (ass.next===null){
				'''createAssert()'''	
			}
			else {
				'''createAssert(«ass.next.compileProcess»)'''
			}
		}		
		else
			if (ass.next===null){
				'''
				createAssert(()->{
					return «ass.exp.compileExpression»;
				});'''	
			}
			else {
				'''
				createAssert(()->{
					return «ass.exp.compileExpression»;
				},
				«ass.next.compileProcess»
				);'''
			}
	}
	
	def private dispatch String compilePrefix(Send send) {
		if (send.next===null){
			'''createSend()'''	
		}
		else {
			'''createSend(«send.next.compileProcess»)'''
		}
	}
	
	def private dispatch String compilePrefix(Receive receive) {
		if (receive.next===null){
			'''createSend()'''	
		}
		else {
			'''createSend(«receive.next.compileProcess»)'''
		}
	}
}


