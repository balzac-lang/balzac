/*
 * Copyright 2019 Nicola Atzei
 */

package xyz.balzaclang.utils;

import java.security.KeyStoreException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xsemantics.runtime.Result;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.util.OnChangeEvictingCache;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import xyz.balzaclang.balzac.AddressLiteral;
import xyz.balzaclang.balzac.BalzacFactory;
import xyz.balzaclang.balzac.BooleanLiteral;
import xyz.balzaclang.balzac.Constant;
import xyz.balzaclang.balzac.Expression;
import xyz.balzaclang.balzac.HashLiteral;
import xyz.balzaclang.balzac.Interpretable;
import xyz.balzaclang.balzac.KeyLiteral;
import xyz.balzaclang.balzac.Literal;
import xyz.balzaclang.balzac.Network;
import xyz.balzaclang.balzac.NumberLiteral;
import xyz.balzaclang.balzac.PubKeyLiteral;
import xyz.balzaclang.balzac.Reference;
import xyz.balzaclang.balzac.RelativeTime;
import xyz.balzaclang.balzac.Script;
import xyz.balzaclang.balzac.SignatureLiteral;
import xyz.balzaclang.balzac.StringLiteral;
import xyz.balzaclang.balzac.TransactionExpression;
import xyz.balzaclang.balzac.TransactionParameter;
import xyz.balzaclang.balzac.Versig;
import xyz.balzaclang.lib.ECKeyStore;
import xyz.balzaclang.lib.model.Address;
import xyz.balzaclang.lib.model.Hash;
import xyz.balzaclang.lib.model.ITransactionBuilder;
import xyz.balzaclang.lib.model.NetworkType;
import xyz.balzaclang.lib.model.PlaceholderUtils;
import xyz.balzaclang.lib.model.PrivateKey;
import xyz.balzaclang.lib.model.PublicKey;
import xyz.balzaclang.lib.model.SerialTransactionBuilder;
import xyz.balzaclang.lib.model.Signature;
import xyz.balzaclang.lib.utils.BitcoinUtils;
import xyz.balzaclang.xsemantics.BalzacInterpreter;
import xyz.balzaclang.xsemantics.Rho;

@Singleton
public class ASTUtils {

    private static Logger logger = Logger.getLogger(ASTUtils.class);

    @Inject private BalzacInterpreter interpreter;
    @Inject private OnChangeEvictingCache cache;

    private static final String cacheECKeyStoreID = "eckeystore";

    public ECKeyStore getECKeyStore(EObject obj) throws KeyStoreException {
    	Resource resource = obj.eResource();
    	logger.debug("Get the ECKeyStore for resource "+resource);

    	try {
        	ECKeyStore value = cache.get(cacheECKeyStoreID, resource, new Provider<ECKeyStore>() {

        		@Override
        		public ECKeyStore get() {
        		    logger.debug("Generating new ECKeyStore for resource "+resource);
                    ECKeyStore kstore;
        			try {
        				kstore = new ECKeyStore();
        				EObject root = EcoreUtil2.getRootContainer(obj);
        				List<KeyLiteral> keys = EcoreUtil2.getAllContentsOfType(root, KeyLiteral.class);
        				keys.add(getPlaceholderPrivateKey(obj));
        				for (KeyLiteral k : keys) {
        					String uniqueID = kstore.addKey(k.getValue());
        					logger.info("keystore: added key "+uniqueID);
        				}
        				return kstore;
        			} catch (KeyStoreException e) {
        				logger.error("Error when creating the ECKeyStore for resource "+resource);
        				return null;
        			}
        		}
        	});
        	return value;
    	}
    	catch (RuntimeException e) {
    	    if (e.getCause() instanceof KeyStoreException) {
    	        throw (KeyStoreException) e.getCause();
    	    }
    	    else throw e;
    	}
    }

    private KeyLiteral getPlaceholderPrivateKey(EObject obj) {
    	PrivateKey privateKey = PlaceholderUtils.KEY(networkParams(obj));
    	KeyLiteral key = BalzacFactory.eINSTANCE.createKeyLiteral();
    	key.setValue(privateKey.getWif());
    	return key;
    }

    public Set<TransactionParameter> getTxVariables(EObject exp) {
        List<Reference> list = new ArrayList<>(EcoreUtil2.getAllContentsOfType(exp, Reference.class));
        if (exp instanceof Reference)
            list.add((Reference) exp);
        Set<TransactionParameter> refs =
                list
                .stream()
                .map( v -> v.getRef() )
                .filter( r -> r instanceof TransactionParameter )
                .map( r -> (TransactionParameter) r )
                .collect(Collectors.toSet());
        return refs;
    }

    public boolean hasTxVariables(EObject exp) {
        return !getTxVariables(exp).isEmpty();
    }

    public <T extends Interpretable> T interpretSafe(T exp) {
        return interpretSafe(exp, new Rho(networkParams(exp)));
    }

    @SuppressWarnings("unchecked")
    public <T extends Interpretable> T interpretSafe(T exp, Rho rho) {
        // returns the same type of exp
        if (exp instanceof Literal)
            return exp;

        Result<Object> interpreted = interpreter.interpret(exp, rho);
        if (interpreted.failed())
            return exp;
        else {
            Object value = interpreted.getFirst();
            return (T) objectToExpression(value);
        }
    }

    public Expression objectToExpression(Object value) {
        if (value instanceof Long) {
            NumberLiteral res = BalzacFactory.eINSTANCE.createNumberLiteral();
            res.setValue((Long) value);
            return res;
        }
        else if (value instanceof String) {
            StringLiteral res = BalzacFactory.eINSTANCE.createStringLiteral();
            res.setValue((String) value);
            return res;
        }
        else if (value instanceof Boolean) {
            BooleanLiteral res = BalzacFactory.eINSTANCE.createBooleanLiteral();
            res.setTrue((Boolean) value);
            return res;
        }
        else if (value instanceof Hash) {
            HashLiteral res = BalzacFactory.eINSTANCE.createHashLiteral();
            res.setValue(((Hash) value).getBytes());
            return res;
        }
        else if (value instanceof PrivateKey) {
            KeyLiteral res = BalzacFactory.eINSTANCE.createKeyLiteral();
            res.setValue(((PrivateKey) value).getWif());
            return res;
        }
        else if (value instanceof PublicKey) {
            PubKeyLiteral res = BalzacFactory.eINSTANCE.createPubKeyLiteral();
            res.setValue(((PublicKey) value).getBytesAsString());
            return res;
        }
        else if (value instanceof Address) {
            AddressLiteral res = BalzacFactory.eINSTANCE.createAddressLiteral();
            res.setValue(((Address) value).getWif());
            return res;
        }
        else if (value instanceof Signature) {
        	Signature sig = (Signature) value;
            SignatureLiteral res = BalzacFactory.eINSTANCE.createSignatureLiteral();
            res.setValue(BitcoinUtils.encode(sig.getSignature()));
            if (sig.getPubkey().isPresent()) {
            	PubKeyLiteral pubkey = BalzacFactory.eINSTANCE.createPubKeyLiteral();
            	pubkey.setValue(sig.getPubkey().get().getBytesAsString());
            	res.setPubkey(pubkey);
            }
            return res;
        }
        else {
            throw new IllegalStateException("Unexpected type "+value.getClass());
        }
    }

    public boolean isCoinbase(xyz.balzaclang.balzac.Transaction tx) {
        return tx.getInputs().size()==1 && tx.getInputs().get(0).isPlaceholder();
    }

    public boolean isCoinbase(TransactionExpression tx) {
        Result<Object> res = this.interpreter.interpretE(tx);

        if (res.failed())
            return false;
        else {

            if (res.getFirst() instanceof ITransactionBuilder) {
                ITransactionBuilder t = (ITransactionBuilder) res.getFirst();
                return t.isCoinbase();
            }
            else {
                return false;
            }
        }
    }

    public boolean isSerial(TransactionExpression tx) {
        Result<Object> res = this.interpreter.interpretE(tx);

        if (res.failed())
            return false;
        else {
            return res.getFirst() instanceof SerialTransactionBuilder;
        }
    }

    public boolean isP2PKH(Script script) {
        boolean isVersig = script.getExp() instanceof Versig;
        boolean onlyOnePubkey = isVersig && ((Versig) script.getExp()).getPubkeys().size() == 1;

        return isVersig && onlyOnePubkey;
    }

    public boolean isOpReturn(Script script, Rho rho) {
        boolean noParam = script.getParams().size() == 0;
        Result<Object> res = this.interpreter.interpret(script.getExp(), rho);
        boolean onlyString = !res.failed() && res.getFirst() instanceof String;
        return noParam && onlyString;
    }

    public boolean isP2SH(Script script, Rho rho) {
        return !isP2PKH(script) && !isOpReturn(script, rho);
    }



    public boolean containsRelativeForTx(List<RelativeTime> timelocks, ITransactionBuilder tx, Rho rho) {
        return timelocks.stream()
                .filter( x ->
                    tx.equals(interpreter.interpret(x.getTx(), rho).getFirst())
                )
                .count()>0;
    }

    public RelativeTime getRelativeForTx(List<RelativeTime> timelocks, ITransactionBuilder tx, Rho rho) {
        return timelocks.stream()
                .filter( x ->
                    tx.equals(interpreter.interpret(x.getTx(), rho).getFirst())
                ).map( x -> (RelativeTime) x)
                .collect(Collectors.toList()).get(0);
    }

    public long getDelayValue(long seconds) {
        return seconds / 512;
    }

    public boolean isDelayTruncated(long seconds) {
        return seconds % 512 > 0;
    }
    
    public long setRelativeTimelockFlag(long i) {
        // true if the 22th bit is UNSET
        long mask = 0b0000_0000__0100_0000__0000_0000__0000_0000;
        return i | mask;
    }

    public boolean isRelativeTimelockFlag(long n) {
        // true if the 22th bit is SET
        int mask = 0b0000_0000__0100_0000__0000_0000__0000_0000;
        return (n & mask) != 0;
    }

    /**
     * Cast the given number to unsigned-short (16 bit)
     * @param i the number to cast
     * @return the number itself
     * @throws NumberFormatException if the number does not fit in 16-bit
     */
    public long castUnsignedShort(long i) throws NumberFormatException {
        long mask = 0x0000FFFF;
        long value = i & mask;

        if (value!=i)
            throw new NumberFormatException("The number does not fit in 16 bits");

        return value;
    }

    public boolean fitIn16bits (long i) {
        try {
            castUnsignedShort((int)i);
            return true;
        }
        catch (NumberFormatException e) {
            return false;
        }
    }

    public long getSequenceNumber(long value, boolean isBlock, Rho rho) {
        return isBlock? castUnsignedShort(value): setRelativeTimelockFlag(getDelayValue(value));
    }

    public NetworkType networkParams(EObject obj) {
        List<Network> list = EcoreUtil2.getAllContentsOfType(EcoreUtil2.getRootContainer(obj), Network.class);

        if (list.size()==0) // network undeclared, assume testnet
            return NetworkType.TESTNET;

        if (list.size()==1) {
            Network net = list.get(0);

            if (net.isTestnet())
                return NetworkType.TESTNET;

            if (net.isMainnet())
                return NetworkType.MAINNET;
        }

        throw new IllegalStateException();
    }

    public Optional<xyz.balzaclang.balzac.Transaction> getTransactionFromReference(TransactionExpression txExp) {
        if (txExp instanceof Reference) {
            Reference ref = (Reference) txExp;
            if (ref.getRef() instanceof xyz.balzaclang.balzac.Transaction) {
                xyz.balzaclang.balzac.Transaction tx = (xyz.balzaclang.balzac.Transaction) ref.getRef();
                return Optional.of(tx);
            }
            if (ref.getRef() instanceof Constant) {
                Constant c = (Constant) ref.getRef();
                if (c.getExp() instanceof TransactionExpression) {
                    return getTransactionFromReference((TransactionExpression) c.getExp());
                }
            }
        }
        return Optional.<xyz.balzaclang.balzac.Transaction>absent();
    }
}
