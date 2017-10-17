/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.compiler

import org.bitcoinj.script.Script
import org.bitcoinj.script.ScriptBuilder
import org.bitcoinj.script.ScriptChunk
import org.bitcoinj.script.ScriptOpCodes
import it.unica.tcs.lib.AbstractScriptBuilderWithVar

class Optimizer {


	def private AbstractScriptBuilderWithVar optimize(ScriptBuilder script) {
		return new AbstractScriptBuilderWithVar().append(script.build.optimize)
	}
	
	def private Script optimize(Script script) {
		var oldScript = script
		var newScript = script
		do {
			oldScript = newScript			
			newScript = oldScript.optimizationStep
		} while( newScript!=oldScript )
		
		newScript;
	}
	
	def private Script optimizationStep(Script script) {
		
		if (script.chunks.isEmpty || script.chunks.size==1)
			return script
			
		var a = script.chunks.get(0)
		var b = script.chunks.get(1)
		var tail = script.subscript(2)
		
		if (a.equalsOpCode(ScriptOpCodes.OP_TOALTSTACK) && 
			b.equalsOpCode(ScriptOpCodes.OP_FROMALTSTACK)
		) {
			return tail.optimizationStep;
		}

		if (a.equalsOpCode(ScriptOpCodes.OP_TRUE) && 
			b.equalsOpCode(ScriptOpCodes.OP_BOOLAND)
		) {
			return tail.optimizationStep;
		}
		
		if (a.equalsOpCode(ScriptOpCodes.OP_FALSE) && 
			b.equalsOpCode(ScriptOpCodes.OP_BOOLOR)
		) {
			return tail.optimizationStep;
		}
		
		a + (b + tail).optimizationStep
	}
	
	
	def private Script operator_plus(ScriptChunk ch, Script script) {
		return new ScriptBuilder(script).addChunk(0,ch).build
	}

	def private Script subscript(Script script, int startIndex) {
		subscript(script, startIndex, script.chunks.size)
	}

	def private Script subscript(Script script, int startIndex, int endIndex) {
		var sb = new ScriptBuilder();
		var i=0;
		for (ch : script.chunks) {
			if (startIndex<=i && i<endIndex)
				sb.addChunk(ch)
			i++
		}
		return sb.build
	}	
	
}
