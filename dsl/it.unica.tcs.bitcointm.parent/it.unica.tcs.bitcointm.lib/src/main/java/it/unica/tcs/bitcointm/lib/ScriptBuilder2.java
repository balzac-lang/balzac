package it.unica.tcs.bitcointm.lib;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static org.bitcoinj.core.Utils.HEX;
import static org.bitcoinj.script.ScriptOpCodes.OP_0;
import static org.bitcoinj.script.ScriptOpCodes.OP_1;
import static org.bitcoinj.script.ScriptOpCodes.OP_16;
import static org.bitcoinj.script.ScriptOpCodes.OP_1NEGATE;
import static org.bitcoinj.script.ScriptOpCodes.OP_INVALIDOPCODE;
import static org.bitcoinj.script.ScriptOpCodes.getOpCodeName;
import static org.bitcoinj.script.ScriptOpCodes.getPushDataName;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.UnsafeByteArrayOutputStream;
import org.bitcoinj.core.Transaction.SigHash;
import org.bitcoinj.core.Utils;
import org.bitcoinj.crypto.TransactionSignature;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.script.ScriptChunk;
import org.bitcoinj.script.ScriptOpCodes;

public class ScriptBuilder2 extends ScriptBuilder {

	private static final String SIGNATURE_PLACEHOLDER = "\u03C3"; 		// σ
	private static final String FREEVAR_PREFIX_PLACEHOLDER = "\u03F0"; 	// ϰ;
	
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
		ScriptChunk chunk = new ScriptBuilder().data((FREEVAR_PREFIX_PLACEHOLDER+name).getBytes()).build().getChunks().get(0);
		super.addChunk(chunk);
		this.freeVariables.put(name, clazz);
		return this;
	}
	
	public ScriptBuilder2 signaturePlaceholder(ECKey key, SigHash hashType, boolean anyoneCanPay) {
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
		for (ScriptChunk ch : append.getChunks()) {
			this.addChunk(ch);
		}
		return this;
	}
	
	public ScriptBuilder2 append(ScriptBuilder2 append) {
		checkState(this.keys.entrySet().containsAll(append.keys.entrySet()));
		checkState(this.hashTypes.entrySet().containsAll(append.hashTypes.entrySet()));
		checkState(this.anyoneCanPay.entrySet().containsAll(append.anyoneCanPay.entrySet()));
		checkState(this.freeVariables.entrySet().containsAll(append.freeVariables.entrySet()));
		for (ScriptChunk ch : append.build().getChunks()) {
			this.addChunk(ch);
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
				String name = new String(ch.data).substring(FREEVAR_PREFIX_PLACEHOLDER.length());
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
	
	private static String serializeChunk(ScriptChunk ch) {
		StringBuilder buf = new StringBuilder();
        if (ch.isOpCode()) {
            buf.append(getOpCodeName(ch.opcode));
        } else if (ch.data != null) {
            // Data chunk
            buf.append(getPushDataName(ch.opcode)).append("[").append(Utils.HEX.encode(ch.data)).append("]");
        } else {
            // Small num
            buf.append(decodeFromOpN(ch.opcode));
        }
        return buf.toString();
	}
	
	private static ScriptChunk deserializeChunk(String w) {
        try(UnsafeByteArrayOutputStream out = new UnsafeByteArrayOutputStream()) {
	        if (w.matches("^-?[0-9]*$")) {
	            // Number
	            long val = Long.parseLong(w);
	            if (val >= -1 && val <= 16)
	                out.write(encodeToOpN((int)val));
	            else
	                Script.writeBytes(out, Utils.reverseBytes(Utils.encodeMPI(BigInteger.valueOf(val), false)));
	        } else if (w.matches("^0x[0-9a-fA-F]*$")) {
	            // Raw hex data, inserted NOT pushed onto stack:
	            out.write(HEX.decode(w.substring(2).toLowerCase()));
	        } else if (w.length() >= 2 && w.startsWith("'") && w.endsWith("'")) {
	            // Single-quoted string, pushed as data. NOTE: this is poor-man's
	            // parsing, spaces/tabs/newlines in single-quoted strings won't work.
	            Script.writeBytes(out, w.substring(1, w.length() - 1).getBytes(Charset.forName("UTF-8")));
	        } else if (ScriptOpCodes.getOpCode(w) != OP_INVALIDOPCODE) {
	            // opcode, e.g. OP_ADD or OP_1:
	            out.write(ScriptOpCodes.getOpCode(w));
	        } else if (w.startsWith("OP_") && ScriptOpCodes.getOpCode(w.substring(3)) != OP_INVALIDOPCODE) {
	            // opcode, e.g. OP_ADD or OP_1:
	            out.write(ScriptOpCodes.getOpCode(w.substring(3)));
	        } else {
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
	
	private static boolean isSignature(ScriptChunk ch) {
		return ch.data != null && new String(ch.data).equals(SIGNATURE_PLACEHOLDER);
	}
	
	private static boolean isFreeVariable(ScriptChunk ch) {
		return ch.data != null && new String(ch.data).startsWith(FREEVAR_PREFIX_PLACEHOLDER);
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
}
