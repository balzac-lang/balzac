package it.unica.tcs.generator;

import static org.bitcoinj.script.ScriptOpCodes.OP_0;

import java.util.HashMap;
import java.util.Map;

import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.Transaction.SigHash;
import org.bitcoinj.crypto.TransactionSignature;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.script.ScriptChunk;
import org.bitcoinj.script.ScriptOpCodes;

import it.xsemantics.runtime.Result;

public class ScriptBuilder2 extends ScriptBuilder {

	private Map<ScriptChunk, ECKey> keys = new HashMap<>();
	private Map<ScriptChunk, SigHash> hashTypes = new HashMap<>();
	private Map<ScriptChunk, Boolean> anyoneCanPay = new HashMap<>();
	private Map<ScriptChunk, Class<?>> freeVariables = new HashMap<>();
	
	public ScriptBuilder2() {}
	
	private ScriptBuilder2(
			Map<ScriptChunk, ECKey> keys, 
			Map<ScriptChunk, SigHash> hashTypes,
			Map<ScriptChunk, Boolean> anyoneCanPay, 
			Map<ScriptChunk, Class<?>> freeVariables) {
		this.keys = keys;
		this.hashTypes = hashTypes;
		this.anyoneCanPay = anyoneCanPay;
		this.freeVariables = freeVariables;
	}

	public ScriptBuilder2 data(byte[] data) {
		super.data(data);
		return this;
	}
	
	public ScriptBuilder2 number(long num) {
		super.number(num);
		return this;
	}
	
	public ScriptBuilder2 op(int op) {
		super.op(op);
		return this;
	}
	
	public ScriptBuilder2 freeVariable(String name, Class<?> clazz) {
		ScriptChunk chunk = new ScriptBuilder().data(("$"+name).getBytes()).build().getChunks().get(0);
		super.addChunk(chunk);
		this.freeVariables.put(chunk, clazz);
		return this;
	}
	
	public ScriptBuilder2 signaturePlaceholder(ECKey key, SigHash hashType, boolean anyoneCanPay) {
		ScriptChunk chunk = new ScriptChunk(OP_0, null);
		super.addChunk(chunk);
		this.keys.put(chunk, key);
		this.hashTypes.put(chunk, hashType);
		this.anyoneCanPay.put(chunk, anyoneCanPay);
		return this;
	}
	
	public int getScriptSize() {
		return super.build().getChunks().size();
	}
	
	public int freeVariableSize() {
		return freeVariables.size();
	}
	
	public int signaturesSize() {
		return keys.size();
	}
	
	public ScriptBuilder2 setFreeVariable(String name, Object obj) {
		ScriptBuilder2 sb = new ScriptBuilder2(keys,hashTypes,anyoneCanPay,freeVariables);
		
		for (ScriptChunk chunk : keys.keySet()) {
			
			if (chunk.data.equals(("$"+name).getBytes())) {				
				Class<?> expectedClass = sb.freeVariables.remove(chunk);
				if (obj.getClass().isInstance(expectedClass)) {
					if (obj instanceof String){
			            sb.data(((String) obj).getBytes());
			        }
			        else if (obj instanceof Integer) {
			        	sb.number((Integer) obj);
			        }
			        else if (obj instanceof Boolean) {
			            if ((Boolean) obj) this.op(ScriptOpCodes.OP_TRUE);
			            else sb.op(ScriptOpCodes.OP_FALSE);
			        }
			        else if (obj instanceof byte[]) {
			        	sb.data((byte[]) obj);
			        }
			        else throw new IllegalArgumentException("Unxpected type "+obj.getClass());
				}
				else throw new IllegalArgumentException("expected class "+expectedClass);
			}
			else {
				sb.addChunk(chunk);
			}
		}
		return sb;
	}
	
	public ScriptBuilder2 setSignatures(Transaction tx, int inputIndex, byte[] outScript) {
		ScriptBuilder2 sb = new ScriptBuilder2(keys,hashTypes,anyoneCanPay,freeVariables);
		
		for (ScriptChunk chunk : keys.keySet()) {
			
			if (chunk.opcode == OP_0 && chunk.data==null) {				
				ECKey key = sb.keys.remove(chunk);
				SigHash hashType = sb.hashTypes.remove(chunk);
				boolean anyoneCanPay = sb.anyoneCanPay.remove(chunk);
				
				TransactionSignature sig = tx.calculateSignature(inputIndex, key, outScript, hashType, anyoneCanPay);
				sb.data(sig.encodeToBitcoin());
			}
			else {
				sb.addChunk(chunk);
			}
		}
		return sb;
	}
	
	
	public ScriptBuilder2 append(Script append) {
		for (ScriptChunk ch : append.getChunks()) {
			this.addChunk(ch);
		}
		return this;
	}
	
	public ScriptBuilder2 append(ScriptBuilder append) {
		return append(append.build());
	}
	
	public ScriptBuilder2 append(Result<Object> res) {
		
		if (res.getFirst() instanceof String){
            this.data(((String) res.getFirst()).getBytes());
        }
        else if (res.getFirst() instanceof Integer) {
        	this.number((Integer) res.getFirst());
        }
        else if (res.getFirst() instanceof Boolean) {
            if ((Boolean)res.getFirst()) this.op(ScriptOpCodes.OP_TRUE);
            else this.number(ScriptOpCodes.OP_FALSE);
        }
        else if (res.getFirst() instanceof byte[]) {
        	this.data((byte[])res.getFirst());
        }
        else throw new IllegalArgumentException("Unxpected type "+res.getFirst().getClass());
		return this;
	}
	
	
	/* ScriptBuilder extensions */
	public static ScriptBuilder append(ScriptBuilder sb, Script append) {
		for (ScriptChunk ch : append.getChunks()) {
			sb.addChunk(ch);
		}
		return sb;
	}

	public static ScriptBuilder append(ScriptBuilder sb, ScriptBuilder append) {
		return append(sb, append.build());
	}

	public static ScriptBuilder append(ScriptBuilder sb, Result<Object> res) {
		
		if (res.getFirst() instanceof String){
            sb.data(((String) res.getFirst()).getBytes());
        }
        else if (res.getFirst() instanceof Integer) {
            sb.number((Integer) res.getFirst());
        }
        else if (res.getFirst() instanceof Boolean) {
            if ((Boolean)res.getFirst()) {
                sb.op(ScriptOpCodes.OP_TRUE);
            }
            else sb.op(ScriptOpCodes.OP_FALSE);
        }
        else if (res.getFirst() instanceof byte[]) {
            sb.data((byte[])res.getFirst());
        }
        else throw new IllegalArgumentException("Unxpected type "+res.getFirst().getClass());
		return sb;
	}
}
