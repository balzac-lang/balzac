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
		import it.unica.tcs.bitcointm.lib.prefix.*;
		import it.unica.tcs.bitcointm.lib.process.*;
		import it.unica.tcs.bitcointm.lib.process.Process;		
		
		@SuppressWarnings("unused")
		public class Participant_«participant.name» extends Participant {
			
			private static Participant_«participant.name» instance = new Participant_«participant.name»();
			
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
		private static class Process_«decl.name» extends Process {
			
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
		""
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
		}
		else {
			«p.^else.compileProcess»
		}
	'''
	
}


