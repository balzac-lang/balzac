package it.unica.tcs.bitcointm.lib;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static org.bitcoinj.script.ScriptOpCodes.OP_0;
import static org.bitcoinj.script.ScriptOpCodes.OP_1;
import static org.bitcoinj.script.ScriptOpCodes.OP_16;
import static org.bitcoinj.script.ScriptOpCodes.OP_1NEGATE;
import static org.bitcoinj.script.ScriptOpCodes.OP_INVALIDOPCODE;
import static org.bitcoinj.script.ScriptOpCodes.getOpCodeName;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.Transaction.SigHash;
import org.bitcoinj.core.UnsafeByteArrayOutputStream;
import org.bitcoinj.core.Utils;
import org.bitcoinj.crypto.TransactionSignature;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.script.ScriptChunk;
import org.bitcoinj.script.ScriptOpCodes;

import com.google.common.collect.Lists;

public class ScriptBuilder2 extends ScriptBuilder {

	private static final String SIGNATURE_PLACEHOLDER = "\u03C3"; 		// σ
	private static final String FREEVAR_PREFIX_PLACEHOLDER = "\u03F0"; 	// ϰ;
	
	private final Map<ScriptChunk, ECKey> keys;
	private final Map<ScriptChunk, SigHash> hashTypes;
	private final Map<ScriptChunk, Boolean> anyoneCanPay;
	private final Map<String,Class<?>> freeVariables;
	
	private ScriptBuilder2(
			Script script,
			Map<ScriptChunk, ECKey> keys, 
			Map<ScriptChunk, SigHash> hashTypes,
			Map<ScriptChunk, Boolean> anyoneCanPay, 
			Map<String,Class<?>> freeVariablesTypes) {
		super(script);
		this.keys = keys;
		this.hashTypes = hashTypes;
		this.anyoneCanPay = anyoneCanPay;
		this.freeVariables = freeVariablesTypes;
	}

	private ScriptBuilder2(
			Map<ScriptChunk, ECKey> keys, 
			Map<ScriptChunk, SigHash> hashTypes,
			Map<ScriptChunk, Boolean> anyoneCanPay, 
			Map<String,Class<?>> freeVariablesTypes) {
		this(new Script(new byte[]{}), keys, hashTypes, anyoneCanPay, freeVariablesTypes);
	}
	
	public ScriptBuilder2() {
		this(new Script(new byte[]{}));
	}

	public ScriptBuilder2(Script script) {
		this(script,new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>());
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
		checkNotNull(name);
		checkNotNull(clazz);
		checkArgument(!freeVariables.containsKey(name) || clazz.equals(freeVariables.get(name)));
		ScriptChunk chunk = new ScriptBuilder().data((FREEVAR_PREFIX_PLACEHOLDER+name).getBytes()).build().getChunks().get(0);
		super.addChunk(chunk);
		this.freeVariables.put(name, clazz);
		return this;
	}
	
	public ScriptBuilder2 signaturePlaceholder(ECKey key, SigHash hashType, boolean anyoneCanPay) {
		checkNotNull(key);
		checkNotNull(hashType);
		ScriptChunk chunk = new ScriptBuilder().data(SIGNATURE_PLACEHOLDER.getBytes()).build().getChunks().get(0);
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
		checkState(keys.size() == hashTypes.size());
		checkState(keys.size() == anyoneCanPay.size());
		return keys.size();
	}
	
	public ScriptBuilder2 setFreeVariable(String name, Object obj) {
		ScriptBuilder2 sb = new ScriptBuilder2(keys,hashTypes,anyoneCanPay,freeVariables);
		
		for (ScriptChunk chunk : build().getChunks()) {
			
			if (Arrays.equals(chunk.data, (FREEVAR_PREFIX_PLACEHOLDER+name).getBytes())) {
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
			
			if (Arrays.equals(chunk.data, (SIGNATURE_PLACEHOLDER).getBytes())) {				
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
		ScriptBuilder2 sb = new ScriptBuilder2(append);
		return this.append(sb);
	}
	
	public ScriptBuilder2 append(ScriptBuilder2 append) {
		for (ScriptChunk ch : append.build().getChunks()) {
			this.addChunk(ch);
			
			// merge free variables
			if (isFreeVariable(ch)) {
				String name = getFreeVariableName(ch);
				if (this.freeVariables.containsKey(name)) {
					// check they are consistent
					checkState(this.freeVariables.get(name).equals(append.freeVariables.get(name)), 
							"Inconsitent state: free variable '%s' is bound to '%s' (this) and '%s' (append)",
							name, this.freeVariables.get(name), append.freeVariables.get(name));
				}
				else {
					this.freeVariables.put(name, append.freeVariables.get(name));
				}
			}
			// merge signatures
			if (isSignature(ch)) {
				ECKey key = append.keys.get(ch);
				SigHash hashType = append.hashTypes.get(ch);
				Boolean anyoneCanPay = append.anyoneCanPay.get(ch);
				checkNotNull(key);
				checkNotNull(hashType);
				checkNotNull(anyoneCanPay);
				if (this.keys.containsKey(ch)) {
					checkState(this.hashTypes.containsKey(ch));
					checkState(this.anyoneCanPay.containsKey(ch));
					// check they are consistent
					checkState(this.keys.get(ch).equals(append.keys.get(ch)), 
							"Inconsitent state: chunk '%s' is bound to '%s' (this) and '%s' (append)",
							ch, this.keys.get(ch), append.keys.get(ch));
					checkState(this.hashTypes.get(ch).equals(append.hashTypes.get(ch)), 
							"Inconsitent state: chunk '%s' is bound to '%s' (this) and '%s' (append)",
							ch, this.hashTypes.get(ch), append.hashTypes.get(ch));
					checkState(this.anyoneCanPay.get(ch).equals(append.anyoneCanPay.get(ch)), 
							"Inconsitent state: chunk '%s' is bound to '%s' (this) and '%s' (append)",
							ch, this.anyoneCanPay.get(ch), append.anyoneCanPay.get(ch));
				}
				else {
					this.keys.put(ch, append.keys.get(ch));
					this.hashTypes.put(ch, append.hashTypes.get(ch));
					this.anyoneCanPay.put(ch, append.anyoneCanPay.get(ch));
				}
			}
		}
		
		return this;
	}
	
	/**
	 * Extract a string representation of this builder.
	 * @return a string representing this builder
	 * @see #parse(String)
	 */
	public static String serialize(ScriptBuilder2 sb) {
		StringBuilder str = new StringBuilder();
		for (ScriptChunk ch : sb.build().getChunks()) {
			if (isSignature(ch)) {
				str.append("[");
				str.append("sig");
				str.append(",");
				str.append(encodeKey(sb.keys.get(ch)));
				str.append(",");
				str.append(encodeModifier(sb.hashTypes.get(ch), sb.anyoneCanPay.get(ch)));
				str.append("]");
				str.append(" ");
			}
			else if (isFreeVariable(ch)) {
				String name = getFreeVariableName(ch);
				str.append("[");
				str.append("var");
				str.append(",");
				str.append(name);
				str.append(",");
				str.append(sb.freeVariables.get(name).getCanonicalName());
				str.append("]");
				str.append(" ");
			}
			else {
				str.append(serializeChunk(ch)).append(" ");
			}
		}
		return str.toString().trim();
	}
	
	/**
	 * Parse the given string to initialize this {@link ScriptBuilder}
	 * @param str
	 * @return
	 * @see #serialize()
	 */
	public static ScriptBuilder2 deserialize(String str) {
		ScriptBuilder2 sb = new ScriptBuilder2();
		for (String ch : str.split(" ")) {
			if (ch.startsWith("[")) {
				String[] vals = ch.substring(1, ch.length()-1).split(",");
				if (vals[0].equals("sig")) {
					ECKey key = decodeKey(vals[1]);
					Object[] modifier = decodeModifier(vals[2]);
					sb.signaturePlaceholder(key, (SigHash) modifier[0], (Boolean) modifier[1]);
				}
				else if (vals[0].equals("var")){
					try {
						String name = vals[1];
						Class<?> clazz = Class.forName(vals[2]);
						sb.freeVariable(name, clazz);
					} catch (ClassNotFoundException e) {
						throw new RuntimeException("Error retrieving the class "+vals[2], e);
					}
				}
				else throw new IllegalStateException();
			}
			else {
				sb.addChunk(deserializeChunk(ch));
			}
		}
		return sb;
	}
	
	private static boolean isSignature(ScriptChunk ch) {
		return ch.data != null && new String(ch.data).equals(SIGNATURE_PLACEHOLDER);
	}
	
	private static boolean isFreeVariable(ScriptChunk ch) {
		return ch.data != null && new String(ch.data).startsWith(FREEVAR_PREFIX_PLACEHOLDER);
	}
	
	private static String getFreeVariableName(ScriptChunk ch) {
		return new String(ch.data).substring(FREEVAR_PREFIX_PLACEHOLDER.length());
	}
	
	private static String encodeKey(ECKey key) {
		return key.getPrivateKeyAsHex();
	}
	
	private static ECKey decodeKey(String key) {
		return ECKey.fromPrivate(Utils.HEX.decode(key));
	}
	
	@SuppressWarnings("incomplete-switch")
	private static String encodeModifier(SigHash sigHash, boolean anyoneCanPay) {
		switch (sigHash) {
		case ALL: return (anyoneCanPay?"1":"*")+"*";
		case NONE: return (anyoneCanPay?"1":"*")+"0";
		case SINGLE: return (anyoneCanPay?"1":"*")+"1";
		}
		throw new IllegalStateException();
	}
	
	private static Object[] decodeModifier(String modifier) {
		switch (modifier) {
		case "**": return new Object[]{SigHash.ALL, Boolean.FALSE};
		case "1*": return new Object[]{SigHash.ALL, Boolean.TRUE};
		case "*0": return new Object[]{SigHash.NONE, Boolean.FALSE};
		case "10": return new Object[]{SigHash.NONE, Boolean.TRUE};
		case "*1": return new Object[]{SigHash.SINGLE, Boolean.FALSE};
		case "11": return new Object[]{SigHash.SINGLE, Boolean.TRUE};
		}
		throw new IllegalStateException(modifier);
	}

	
	/* ---------------------------------------------------------------------------------------------- */
	/*
	 * below there are some methods customized from bitcoinJ
	 */
	
	private static String serializeChunk(ScriptChunk ch) {
		StringBuilder buf = new StringBuilder();
        if (ch.isOpCode()) {
            buf.append(getOpCodeName(ch.opcode));
        } else if (ch.data != null) {
            // Data chunk
            buf.append("PUSHDATA").append("[").append(Utils.HEX.encode(ch.data)).append("]");
        } else {
            // Small num
            buf.append(decodeFromOpN(ch.opcode));
        }
        return buf.toString();
	}
	
	private static ScriptChunk deserializeChunk(String w) {
        try(UnsafeByteArrayOutputStream out = new UnsafeByteArrayOutputStream()) {
	        if (w.matches("^-?[0-9]*$")) {
	            // Small Number
	            long val = Long.parseLong(w);
                out.write(encodeToOpN((int)val));
	        } else if (ScriptOpCodes.getOpCode(w) != OP_INVALIDOPCODE) {
	            // opcode, e.g. OP_ADD or OP_1:
	            out.write(ScriptOpCodes.getOpCode(w));
	        } 
	        else if (w.startsWith("PUSHDATA")) {
	        	String data = w.substring("PUSHDATA".length()+1, w.length()-1);
	        	Script.writeBytes(out, Utils.HEX.decode(data));
	        }
	        else {
	            throw new RuntimeException("Invalid word: '" + w + "'");
	        }                        
	        
	        return new Script(out.toByteArray()).getChunks().get(0);
        } catch (IOException e) {
        	throw new RuntimeException("Unexpected IO error for word "+w, e);
		}
	}
	
    static int decodeFromOpN(int opcode) {
        checkArgument((opcode == OP_0 || opcode == OP_1NEGATE) || (opcode >= OP_1 && opcode <= OP_16), "decodeFromOpN called on non OP_N opcode");
        if (opcode == OP_0)
            return 0;
        else if (opcode == OP_1NEGATE)
            return -1;
        else
            return opcode + 1 - OP_1;
    }

    static int encodeToOpN(int value) {
        checkArgument(value >= -1 && value <= 16, "encodeToOpN called for " + value + " which we cannot encode in an opcode.");
        if (value == 0)
            return OP_0;
        else if (value == -1)
            return OP_1NEGATE;
        else
            return value - 1 + OP_1;
    }
}
