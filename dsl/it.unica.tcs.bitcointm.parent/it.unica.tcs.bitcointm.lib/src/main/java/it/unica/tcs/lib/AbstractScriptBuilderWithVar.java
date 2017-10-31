/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.lib;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static org.bitcoinj.script.ScriptOpCodes.OP_0;
import static org.bitcoinj.script.ScriptOpCodes.OP_1;
import static org.bitcoinj.script.ScriptOpCodes.OP_16;
import static org.bitcoinj.script.ScriptOpCodes.OP_1NEGATE;
import static org.bitcoinj.script.ScriptOpCodes.OP_INVALIDOPCODE;
import static org.bitcoinj.script.ScriptOpCodes.OP_PUSHDATA1;
import static org.bitcoinj.script.ScriptOpCodes.getOpCodeName;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.Transaction.SigHash;
import org.bitcoinj.core.UnsafeByteArrayOutputStream;
import org.bitcoinj.core.Utils;
import org.bitcoinj.crypto.TransactionSignature;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.script.ScriptChunk;
import org.bitcoinj.script.ScriptOpCodes;

import it.unica.tcs.lib.Wrapper.SigHashWrapper;
import it.unica.tcs.lib.utils.BitcoinUtils;

public abstract class AbstractScriptBuilderWithVar<T extends AbstractScriptBuilderWithVar<T>> 
	extends AbstractScriptBuilder<T> 
	implements EnvI<Object,T> {

	private static final long serialVersionUID = 1L;
	private static final String SIGNATURE_PREFIX = "\u03C3"; 	// σ
	private static final String FREEVAR_PREFIX = "\u03F0"; 		// ϰ;
	
	private final Env<Object> env = new Env<>();
	
	protected final Map<String, SignatureUtil> signatures = new HashMap<>();
	
	private static class SignatureUtil implements Serializable {
		private static final long serialVersionUID = 1L;
		private final String keyID;
		private final SigHashWrapper hashType;
		private final Boolean anyoneCanPay;
		public SignatureUtil(String keyID, SigHashWrapper hashType, boolean anyoneCanPay) {
			checkNotNull(keyID);
			this.keyID = keyID;
			this.hashType = hashType;
			this.anyoneCanPay = anyoneCanPay;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (anyoneCanPay ? 1231 : 1237);
			result = prime * result + ((hashType == null) ? 0 : hashType.hashCode());
			result = prime * result + ((keyID == null) ? 0 : keyID.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			SignatureUtil other = (SignatureUtil) obj;
			if (anyoneCanPay != other.anyoneCanPay)
				return false;
			if (hashType != other.hashType)
				return false;
			if (keyID == null) {
				if (other.keyID != null)
					return false;
			} else if (!keyID.equals(other.keyID))
				return false;
			return true;
		}
		@Override
		public String toString() {
			return "SignatureUtil [key=" + keyID + ", hashType=" + hashType + ", anyoneCanPay=" + anyoneCanPay + "]";
		}
		
		public String getUniqueKey() {
			return Utils.HEX.encode(this.keyID.concat(this.hashType.toString()).concat(this.anyoneCanPay.toString()).getBytes());
		}
	}
	
	public AbstractScriptBuilderWithVar() {
		this(new Script(new byte[]{}));
	}

	public AbstractScriptBuilderWithVar(Script script) {
		super(script);
	}
	
	public AbstractScriptBuilderWithVar(String serializedScript) {
		this.deserialize(serializedScript);
	}

	@Override
	public Script build() {
		checkState(isReady(), "there exist some free-variables or signatures that need to be set before building");
		return substituteAllBinding();
	}
	
	public T signaturePlaceholder(String keyID, SigHash hashType, boolean anyoneCanPay) {
		ECKey key = KeyStoreFactory.getInstance().getKey(keyID);
		return signaturePlaceholder(key, hashType, anyoneCanPay);
	}
	
	@SuppressWarnings("unchecked")
	public T signaturePlaceholder(ECKey key, SigHash hashType, boolean anyoneCanPay) {
		checkNotNull(key, "'key' cannot be null");
		checkNotNull(hashType, "'hashType' cannot be null");
		String keyID = KeyStoreFactory.getInstance().addKey(key);
		SignatureUtil sig = new SignatureUtil(keyID, SigHashWrapper.wrap(hashType), anyoneCanPay);
		String mapKey = sig.getUniqueKey();
		byte[] data = (SIGNATURE_PREFIX+mapKey).getBytes();
		checkState(data.length<256, "data too long: "+data.length);
		ScriptChunk chunk = new ScriptChunk(OP_PUSHDATA1, data);
		super.addChunk(chunk);
		this.signatures.put(mapKey, sig);
		return (T) this;
	}
	
	public int signatureSize() {
		return signatures.size();
	}
	
	/*
	 * Return a Script, binding the variables.
	 * This builder is assumed to be ready 
	 */
	private Script substituteAllBinding() {
		
		ScriptBuilder sb = new ScriptBuilder();

		for (ScriptChunk chunk : getChunks()) {

			if (isVariable(chunk)) {
				String name = getVariableName(chunk);
				Object obj = getValue(name);
				Class<?> expectedClass = getType(name);

				if (expectedClass.isInstance(obj)) {
					for (ScriptChunk ch : BitcoinUtils.toScript(obj).getChunks()) {
						sb.addChunk(ch);
					}
				} else
					throw new IllegalArgumentException("expected class " + expectedClass.getName() + ", got " + obj.getClass().getName());
			}
			else {
				sb.addChunk(chunk);
			}
		}
		
		return sb.build();
	}
	
	/**
	 * Replace all the signatures placeholder with the actual signatures.
	 * Each placeholder is already associated with the key and the modifiers.
	 * @param tx the transaction to be signed
	 * @param inputIndex the index of the input that will contain this script
	 * @param outScript the redeemed output script 
	 * @return a <b>copy</b> of this builder
	 */
	@SuppressWarnings("unchecked")
	public T setAllSignatures(Transaction tx, int inputIndex, byte[] outScript) {
		
		List<ScriptChunk> newChunks = new ArrayList<>();

		for (ScriptChunk chunk : getChunks()) {
			
			ScriptBuilder2 sb = new ScriptBuilder2();
			if (isSignature(chunk)) {
				String mapKey = getMapKey(chunk);
				SignatureUtil sig = this.signatures.get(mapKey);
				ECKey key = KeyStoreFactory.getInstance().getKey(sig.keyID);
				SigHash hashType = sig.hashType.get();
				boolean anyoneCanPay = sig.anyoneCanPay;
				
				// check the key is correct when P2PKH
				Script s = new Script(outScript);
				if (s.isSentToAddress()) {
					checkState(Arrays.equals(s.getPubKeyHash(), key.getPubKeyHash()));
				}
				
				TransactionSignature txSig = tx.calculateSignature(inputIndex, key, outScript, hashType, anyoneCanPay);
				Sha256Hash hash = tx.hashForSignature(inputIndex, outScript, (byte) txSig.sighashFlags);
	            boolean isValid =  ECKey.verify(hash.getBytes(), txSig, key.getPubKey());
	            checkState(isValid);
	            checkState(txSig.isCanonical());
	            sb.data(txSig.encodeToBitcoin());
			}
			else {
				sb.addChunk(chunk);
			}
			
			newChunks.addAll(sb.getChunks());
		}
		super.getChunks().clear();
		super.getChunks().addAll(newChunks);
		
		this.signatures.clear();
		return (T) this;
	}
	
	/**
	 * Append the given script to this builder.
	 * @param append the script to append
	 * @return this builder
	 */
	public T append(Script append) {
		ScriptBuilder2 sb = new ScriptBuilder2(append);
		return this.append(sb);
	}
	
	/**
	 * Append the given script builder to this one.
	 * If it contains some free variables or signatures placeholder, they are merged ensuring consistency.
	 * @param append the script builder to append
	 * @return this builder
	 */
	@SuppressWarnings("unchecked")
	public <U extends AbstractScriptBuilderWithVar<U>> T append(AbstractScriptBuilderWithVar<U> append) {
		for (ScriptChunk ch : append.getChunks()) {
			
			if (isVariable(ch)) {
				// merge free variables
				String name = getVariableName(ch);

				// check they are consistent
				if (hasVariable(name)) {
					checkState(getType(name).equals(append.getType(name)), 
							"Inconsitent state: variable '%s' is bound to type '%s' (this) and type '%s' (append)",
							name, this.getType(name), append.getType(name));
					
					if (isBound(name) && append.isBound(name)) {
						checkState(getValue(name).equals(append.getValue(name)), 
								"Inconsitent state: variable '%s' is bound to value '%s' (this) and value '%s' (append)",
								name, this.getValue(name), append.getValue(name));
					}
				}
				
				this.addVariable(name, append.getType(name));
				if (!isBound(name) && append.isBound(name)) {
					this.bindVariable(name, append.getValue(name));
				}				
			}
			else if (isSignature(ch)) {
				// merge signatures
				String mapKey = getMapKey(ch);
				checkNotNull(append.signatures.containsKey(mapKey));
				if (this.signatures.containsKey(mapKey)) {
					// check they are consistent
					checkState(this.signatures.get(mapKey).equals(append.signatures.get(mapKey)), 
							"Inconsitent state: sig placeholder '%s' is bound to '%s' (this) and '%s' (append)",
							mapKey, this.signatures.get(mapKey), append.signatures.get(mapKey));
				}
				else {
					this.signatures.put(mapKey, append.signatures.get(mapKey));
				}
				this.addChunk(ch);
			}
			else {
				this.addChunk(ch);
			}
		}
		
		return (T) this;
	}
	
	/**
	 * Extract a string representation of this builder.
	 * @return a string representing this builder
	 * @see #parse(String)
	 */
	public String serialize() {
		StringBuilder str = new StringBuilder();
		for (ScriptChunk ch : getChunks()) {
			str.append(serializeChunk(ch)).append(" ");
		}
		return str.toString().trim();
	}
	
	/**
	 * Parse the given string to initialize this {@link ScriptBuilder}
	 * @param str
	 * @return 
	 * @return
	 * @see #serialize()
	 */
	@SuppressWarnings("unchecked")
	public T deserialize(String str) {
		for (String ch : str.split(" ")) {
			this.deserializeChunk(ch);
		}
		return (T) this;
	}
	
	private static boolean isSignature(ScriptChunk ch) {
		return ch.data != null && new String(ch.data).startsWith(SIGNATURE_PREFIX);
	}
	
	private static boolean isVariable(ScriptChunk ch) {
		return ch.data != null && new String(ch.data).startsWith(FREEVAR_PREFIX);
	}
	
	private static boolean isVariable(ScriptChunk ch, String name) {
		return ch.data != null && new String(ch.data).equals(FREEVAR_PREFIX+name);
	}
	
	private static String getVariableName(ScriptChunk ch) {
		return new String(ch.data).substring(FREEVAR_PREFIX.length());
	}
	
	private static String getMapKey(ScriptChunk ch) {
		return new String(ch.data).substring(SIGNATURE_PREFIX.length());
	}
	
	@Override
	public String toString() {
		return this.serialize();
	}
	

	protected String serializeChunk(ScriptChunk ch) {
		
		StringBuilder str = new StringBuilder();

		if (isSignature(ch)) {
			String mapKey = getMapKey(ch);
			str.append("[");
			str.append("sig");
			str.append(",");
			str.append(this.signatures.get(mapKey).keyID);
			str.append(",");
			str.append(encodeModifier(this.signatures.get(mapKey).hashType.get(), this.signatures.get(mapKey).anyoneCanPay));
			str.append("]");
			str.append(" ");
		}
		else if (isVariable(ch)) {
			String name = getVariableName(ch);
			str.append("[");
			str.append("var");
			str.append(",");
			str.append(name);
			str.append(",");
			str.append(getType(name).getCanonicalName());
			str.append("]");
			str.append(" ");
		}
		else if (ch.isOpCode()) {
            str.append(getOpCodeName(ch.opcode));
        } else if (ch.data != null) {
            // Data chunk
            str.append("PUSHDATA").append("[").append(Utils.HEX.encode(ch.data)).append("]");
        } else {
            // Small num
            str.append(decodeFromOpN(ch.opcode));
        }
        return str.toString();
	}
	
	protected void deserializeChunk(String w) {
        	
    	if (w.startsWith("[")) {
			String[] vals = w.substring(1, w.length()-1).split(",");
			if (vals[0].equals("sig")) {
				String keyID = vals[1];
				Object[] modifier = decodeModifier(vals[2]);
				this.signaturePlaceholder(keyID, (SigHash) modifier[0], (Boolean) modifier[1]);
			}
			else if (vals[0].equals("var")){
				try {
					String name = vals[1];
					Class<?> clazz = Class.forName(vals[2]);
					this.addVariable(name, clazz);
				} catch (ClassNotFoundException e) {
					throw new RuntimeException("Error retrieving the class "+vals[2], e);
				}
			}
			else throw new IllegalStateException();
		}
    	else {
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
		        
		        this.getChunks().addAll(new Script(out.toByteArray()).getChunks());
		    } catch (IOException e) {
		    	throw new RuntimeException("Unexpected IO error for word "+w, e);
			}
        }
	}
	
	@SuppressWarnings("incomplete-switch")
	protected static String encodeModifier(SigHash sigHash, boolean anyoneCanPay) {
		switch (sigHash) {
		case ALL: return (anyoneCanPay?"1":"*")+"*";
		case NONE: return (anyoneCanPay?"1":"*")+"0";
		case SINGLE: return (anyoneCanPay?"1":"*")+"1";
		}
		throw new IllegalStateException();
	}
	
	protected static Object[] decodeModifier(String modifier) {
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
	
	protected static int decodeFromOpN(int opcode) {
        checkArgument((opcode == OP_0 || opcode == OP_1NEGATE) || (opcode >= OP_1 && opcode <= OP_16), "decodeFromOpN called on non OP_N opcode");
        if (opcode == OP_0)
            return 0;
        else if (opcode == OP_1NEGATE)
            return -1;
        else
            return opcode + 1 - OP_1;
    }

	protected static int encodeToOpN(int value) {
        checkArgument(value >= -1 && value <= 16, "encodeToOpN called for " + value + " which we cannot encode in an opcode.");
        if (value == 0)
            return OP_0;
        else if (value == -1)
            return OP_1NEGATE;
        else
            return value - 1 + OP_1;
    }
	
	
	
	private void addVariableChunk(String name) {
		byte[] data = (FREEVAR_PREFIX+name).getBytes();
		checkState(data.length<256, "data too long: "+data.length);
		ScriptChunk chunk = new ScriptChunk(OP_PUSHDATA1, data);
		super.addChunk(chunk);
	}
	
	private void removeVariableChunk(String name) {
		ListIterator<ScriptChunk> it = getChunks().listIterator();
		
		while(it.hasNext()) {
			ScriptChunk next = it.next();
			
			if (isVariable(next, name)) {
				it.remove();
			}
		}
	}
	
	@Override
	public boolean hasVariable(String name) {
		return env.hasVariable(name);
	}

	@Override
	public boolean isFree(String name) {
		return env.isFree(name);
	}

	@Override
	public boolean isBound(String name) {
		return env.isBound(name);
	}

	@Override
	public Class<?> getType(String name) {
		return env.getType(name);
	}
	
	@Override
	public Object getValue(String name) {
		return env.getValue(name);
	}

	@Override
	public <E> E getValue(String name, Class<E> clazz) {
		return env.getValue(name, clazz);
	}
	
	@Override
	public Object getValueOrDefault(String name, Object defaultValue) {
		return env.getValueOrDefault(name, defaultValue);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public T addVariable(String name, Class<?> type) {
		checkArgument(Integer.class.equals(type) || String.class.equals(type) || Boolean.class.equals(type) || Hash.class.isAssignableFrom(type), "invalid type "+type);
		addVariableChunk(name);
		env.addVariable(name, type);
		return (T) this;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public T removeVariable(String name) {
		removeVariableChunk(name);
		env.removeVariable(name);
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T bindVariable(String name, Object value) {
		env.bindVariable(name, value);
		return (T) this;
	}

	@Override
	public Collection<String> getVariables() {
		return env.getVariables();
	}

	@Override
	public Collection<String> getFreeVariables() {
		return env.getFreeVariables();
	}

	@Override
	public Collection<String> getBoundVariables() {
		return env.getBoundVariables();
	}
	
	@Override
	public boolean isReady() {
		return env.isReady();
	}
	
	@Override
	public void clear() {
		env.clear();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((env == null) ? 0 : env.hashCode());
		result = prime * result + ((signatures == null) ? 0 : signatures.hashCode());
		result = prime * result + ((getChunks() == null) ? 0 : getChunks().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractScriptBuilderWithVar<?> other = (AbstractScriptBuilderWithVar<?>) obj;
		if (env == null) {
			if (other.env != null)
				return false;
		} else if (!env.equals(other.env))
			return false;
		if (signatures == null) {
			if (other.signatures != null)
				return false;
		} else if (!signatures.equals(other.signatures))
			return false;
		return getChunks().equals(super.getChunks());
	}
}
