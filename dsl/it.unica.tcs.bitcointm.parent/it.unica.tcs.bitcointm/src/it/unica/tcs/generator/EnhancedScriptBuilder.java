package it.unica.tcs.generator;

import static org.bitcoinj.script.ScriptOpCodes.OP_RESERVED;
import static org.bitcoinj.script.ScriptOpCodes.OP_RESERVED1;
import static org.bitcoinj.script.ScriptOpCodes.OP_RESERVED2;

import java.util.HashMap;
import java.util.Map;

import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.script.ScriptChunk;

public class EnhancedScriptBuilder extends ScriptBuilder {

	private int signatureCount = 0;
	private final Map<String, Class<?>> freeVariables;
	
	public EnhancedScriptBuilder() {
		this(new HashMap<>());
	}
	
	// private
	private EnhancedScriptBuilder(Map<String, Class<?>> freeVariables) {
		this.freeVariables = freeVariables;
	}
	
	private void addParameter(String name, Class<?> type) {
		if (this.freeVariables.containsKey(name))
			throw new IllegalStateException("parameter with name '"+name+"' already present");
		this.freeVariables.put(name, type);
	}
	
	public EnhancedScriptBuilder freeVariable(String name, Class<?> clazz) {
		super.op(OP_RESERVED1).data(name.getBytes()).op(OP_RESERVED2);
		this.addParameter(name, clazz);
		return this;
	}
	
	public EnhancedScriptBuilder signaturePlaceholder() {
		String paramName = "sig_"+signatureCount++;
		super.op(OP_RESERVED);
		this.addParameter(paramName, byte[].class);
		return this;
	}
		
	public int getScriptSize() {
		return super.build().getChunks().size();
	}
	
	public EnhancedScriptBuilder setSignatures(byte[] signature) {
		EnhancedScriptBuilder sb = new EnhancedScriptBuilder(new HashMap<>(freeVariables));
        Script script = build();
        for (int j=0; j<script.getChunks().size(); j++) {
        	ScriptChunk chunk = script.getChunks().get(j);
        	
        	if (chunk.equalsOpCode(OP_RESERVED))
        		sb.data(signature);
        	else
        		sb.addChunk(chunk); // copy
        }
        return sb;
	}
}
