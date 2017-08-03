package it.unica.tcs.generator;

import static com.google.common.base.Preconditions.*;

import java.util.Arrays;
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

	private final Map<ScriptChunk, ECKey> keys;
	private final Map<ScriptChunk, SigHash> hashTypes;
	private final Map<ScriptChunk, Boolean> anyoneCanPay;
	private final Map<String,Class<?>> freeVariables;
	
	public ScriptBuilder2() {
		this.keys = new HashMap<>();
		this.hashTypes = new HashMap<>();
		this.anyoneCanPay = new HashMap<>();
		this.freeVariables = new HashMap<>();
	}
	
	private ScriptBuilder2(
			Map<ScriptChunk, ECKey> keys, 
			Map<ScriptChunk, SigHash> hashTypes,
			Map<ScriptChunk, Boolean> anyoneCanPay, 
			Map<String,Class<?>> freeVariablesTypes) {
		this.keys = keys;
		this.hashTypes = hashTypes;
		this.anyoneCanPay = anyoneCanPay;
		this.freeVariables = freeVariablesTypes;
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
	
	public Map<String,Class<?>> getFreeVariables() {
		return new HashMap<>(freeVariables);
	}
	
	public ScriptBuilder2 freeVariable(String name, Class<?> clazz) {
		checkArgument(!freeVariables.containsKey(name) || clazz.equals(freeVariables.get(name)));
		ScriptChunk chunk = new ScriptBuilder().data(("$"+name).getBytes()).build().getChunks().get(0);
		super.addChunk(chunk);
		this.freeVariables.put(name, clazz);
		return this;
	}
	
	public ScriptBuilder2 signaturePlaceholder(ECKey key, SigHash hashType, boolean anyoneCanPay) {
		ScriptChunk chunk = new ScriptBuilder().data(("$sig$").getBytes()).build().getChunks().get(0);
		super.addChunk(chunk);
		this.keys.put(chunk, key);
		this.hashTypes.put(chunk, hashType);
		this.anyoneCanPay.put(chunk, anyoneCanPay);
		return this;
	}
	
	public int size() {
		return super.build().getChunks().size();
	}
	
	public int freeVariableSize() {
		return freeVariables.size();
	}
	
	public int signatureSize() {
		return keys.size();
	}
	
	public ScriptBuilder2 setFreeVariable(String name, Object obj) {
		ScriptBuilder2 sb = new ScriptBuilder2(keys,hashTypes,anyoneCanPay,freeVariables);
		
		for (ScriptChunk chunk : build().getChunks()) {
			
			if (Arrays.equals(chunk.data, ("$"+name).getBytes())) {
				Class<?> expectedClass = sb.freeVariables.remove(name);
				if (expectedClass.isInstance(obj)) {
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
				else throw new IllegalArgumentException("expected class "+expectedClass.getName()+", got "+obj.getClass().getName());
			}
			else {
				sb.addChunk(chunk);
			}
		}
		return sb;
	}
	
	public ScriptBuilder2 setSignatures(Transaction tx, int inputIndex, byte[] outScript) {
		ScriptBuilder2 sb = new ScriptBuilder2(keys,hashTypes,anyoneCanPay,freeVariables);
		
		for (ScriptChunk chunk : build().getChunks()) {
			
			if (Arrays.equals(chunk.data, ("$sig$").getBytes())) {				
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
		return ScriptBuilder2.append(this, append);
	}
	
	public ScriptBuilder2 append(ScriptBuilder2 append) {
		return ScriptBuilder2.append(this, append);
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
	public static ScriptBuilder2 append(ScriptBuilder2 sb, Script append) {
		for (ScriptChunk ch : append.getChunks()) {
			sb.addChunk(ch);
		}
		return sb;
	}

	public static ScriptBuilder2 append(ScriptBuilder2 sb, ScriptBuilder2 append) {
		checkState(sb.keys.entrySet().containsAll(append.keys.entrySet()));
		checkState(sb.hashTypes.entrySet().containsAll(append.hashTypes.entrySet()));
		checkState(sb.anyoneCanPay.entrySet().containsAll(append.anyoneCanPay.entrySet()));
		checkState(sb.freeVariables.entrySet().containsAll(append.freeVariables.entrySet()));
		return append(sb, append.build());
	}

	public static ScriptBuilder2 append(ScriptBuilder2 sb, Result<Object> res) {
		
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
