/*
za * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.lib.model.script;

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
import java.security.KeyStoreException;
import java.util.ArrayList;
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

import it.unica.tcs.lib.ECKeyStore;
import it.unica.tcs.lib.model.Hash;
import it.unica.tcs.lib.model.Signature;
import it.unica.tcs.lib.utils.BitcoinUtils;
import it.unica.tcs.lib.utils.Env;
import it.unica.tcs.lib.utils.EnvI;

public abstract class AbstractScriptBuilderWithVar<T extends AbstractScriptBuilderWithVar<T>>
    extends AbstractScriptBuilder<T>
    implements EnvI<Object,T> {
    
    private static final long serialVersionUID = 1L;
    private static final String SIGNATURE_PREFIX = "[$sig$]";
    private static final String FREEVAR_PREFIX = "[$var$]";

    private final Env<Object> env = new Env<>();

    protected final Map<String, SignatureUtil> signatures = new HashMap<>();

    public static class ScriptBuilderWithVar extends AbstractScriptBuilderWithVar<ScriptBuilderWithVar> {
        private static final long serialVersionUID = 1L;
        public ScriptBuilderWithVar() {}
        public ScriptBuilderWithVar(Script s) {super(s);}
        public ScriptBuilderWithVar(String serial) {super(serial);}
    }

    private static class SignatureUtil implements Serializable {
        private static final long serialVersionUID = 1L;
        private final String keyID;
        private final SigHash hashType;
        private final Boolean anyoneCanPay;
        public SignatureUtil(String keyID, SigHash hashType, boolean anyoneCanPay) {
            checkNotNull(keyID);
            this.keyID = keyID;
            this.hashType = hashType;
            this.anyoneCanPay = anyoneCanPay;
        }
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((anyoneCanPay == null) ? 0 : anyoneCanPay.hashCode());
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
            if (anyoneCanPay == null) {
                if (other.anyoneCanPay != null)
                    return false;
            } else if (!anyoneCanPay.equals(other.anyoneCanPay))
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

    protected AbstractScriptBuilderWithVar() {
        this(new Script(new byte[]{}));
    }

    protected AbstractScriptBuilderWithVar(Script script) {
        super(script);
    }

    protected AbstractScriptBuilderWithVar(String serializedScript) {
        this.deserialize(serializedScript);
    }

    @Override
    public Script build() {
        checkState(isReady(), "there exist some free-variables or signatures that need to be set before building");
        return substituteAllBinding();
    }

    public T signaturePlaceholderKeyFree(String keyVarname, SigHash hashType, boolean anyoneCanPay) {
        this.addVariable(keyVarname, String.class);
        return this.signaturePlaceholder(FREEVAR_PREFIX+keyVarname, hashType, anyoneCanPay);
    }

    @SuppressWarnings("unchecked")
    public T signaturePlaceholder(String keyID, SigHash hashType, boolean anyoneCanPay) {
        checkNotNull(keyID, "'keyID' cannot be null");
        checkNotNull(hashType, "'hashType' cannot be null");
        SignatureUtil sig = new SignatureUtil(keyID, hashType, anyoneCanPay);
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
     * @throws KeyStoreException if an error occurs retrieving private keys
     */
    @SuppressWarnings("unchecked")
    public T setAllSignatures(ECKeyStore keystore, Transaction tx, int inputIndex, byte[] outScript, boolean isP2PKH) throws KeyStoreException {

        List<ScriptChunk> newChunks = new ArrayList<>();

        for (ScriptChunk chunk : getChunks()) {

            ScriptBuilderWithVar sb = new ScriptBuilderWithVar();
            if (isSignature(chunk)) {
                String mapKey = getMapKey(chunk);
                SignatureUtil sig = this.signatures.get(mapKey);

                // check if the private key is a variable
                String keyID = sig.keyID;
                if (keyID.startsWith(FREEVAR_PREFIX)) {
                    // check that the variable is bound
                    String varName = keyID.substring(FREEVAR_PREFIX.length());
                    checkState(isBound(varName), "variable "+varName+" must be bound to retrieve the key");
                    keyID = getValue(varName, String.class);
                }

                checkState(keystore != null, "keystore must be set to retrieve the private keys");
                checkState(keystore.containsKey(keyID), "key "+keyID+" not found on the specified keystore");

                ECKey key = keystore.getKey(keyID);
                SigHash hashType = sig.hashType;
                boolean anyoneCanPay = sig.anyoneCanPay;

                // create the signature
                TransactionSignature txSig = tx.calculateSignature(inputIndex, key, outScript, hashType, anyoneCanPay);
                Sha256Hash hash = tx.hashForSignature(inputIndex, outScript, (byte) txSig.sighashFlags);
                boolean isValid =  ECKey.verify(hash.getBytes(), txSig, key.getPubKey());
                checkState(isValid);
                checkState(txSig.isCanonical());
                sb.data(txSig.encodeToBitcoin());
                if (isP2PKH) {
                	sb.data(key.getPubKey());
                }
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
        ScriptBuilderWithVar sb = new ScriptBuilderWithVar(append);
        return this.append(sb);
    }

    /**
     * Append the given script builder to this one.
     * If it contains some free variables or signatures placeholder, they are merged ensuring consistency.
     * @param <U> the concrete type of the given builder
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
     */
    public String serialize() {
        StringBuilder str = new StringBuilder();
        for (ScriptChunk ch : getChunks()) {
            str.append(serializeChunk(ch)).append(" ");
        }
        return str.toString().trim();
    }

    /**
     * Parse the given string to initialize this builder.
     * @param str a string representation of this builder
     * @return this builder
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

    protected String serializeChunk(ScriptChunk ch) {

        StringBuilder str = new StringBuilder();

        if (isSignature(ch)) {
            String mapKey = getMapKey(ch);
            str.append("[");
            str.append("sig");
            str.append(",");
            str.append(this.signatures.get(mapKey).keyID);
            str.append(",");
            str.append(encodeModifier(this.signatures.get(mapKey).hashType, this.signatures.get(mapKey).anyoneCanPay));
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
        return getChunks().equals(other.getChunks());
    }

    @Override
    public String toString() {
        return this.serialize();
    }

    //////////////////////////////////////////////////////////////////////////
    //                           EnvI interface                             //
    //////////////////////////////////////////////////////////////////////////

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
        checkArgument(Number.class.isAssignableFrom(type) || String.class.equals(type) || Boolean.class.equals(type)
                || Hash.class.isAssignableFrom(type) || Signature.class.isAssignableFrom(type), "invalid type "+type);
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
}
