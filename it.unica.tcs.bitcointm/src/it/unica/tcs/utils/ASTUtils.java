/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.utils;

import java.security.KeyStoreException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.Base58;
import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.Transaction.SigHash;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.RegTestParams;
import org.bitcoinj.params.TestNet3Params;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xsemantics.runtime.Result;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.util.OnChangeEvictingCache;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import it.unica.tcs.balzac.AddressLiteral;
import it.unica.tcs.balzac.BalzacFactory;
import it.unica.tcs.balzac.BalzacPackage;
import it.unica.tcs.balzac.BooleanLiteral;
import it.unica.tcs.balzac.Constant;
import it.unica.tcs.balzac.Delay;
import it.unica.tcs.balzac.Expression;
import it.unica.tcs.balzac.HashLiteral;
import it.unica.tcs.balzac.Interpretable;
import it.unica.tcs.balzac.KeyLiteral;
import it.unica.tcs.balzac.Literal;
import it.unica.tcs.balzac.Modifier;
import it.unica.tcs.balzac.Network;
import it.unica.tcs.balzac.NumberLiteral;
import it.unica.tcs.balzac.Parameter;
import it.unica.tcs.balzac.PubKeyLiteral;
import it.unica.tcs.balzac.Reference;
import it.unica.tcs.balzac.Referrable;
import it.unica.tcs.balzac.RelativeTime;
import it.unica.tcs.balzac.Script;
import it.unica.tcs.balzac.SignatureLiteral;
import it.unica.tcs.balzac.StringLiteral;
import it.unica.tcs.balzac.TransactionExpression;
import it.unica.tcs.balzac.TransactionParameter;
import it.unica.tcs.balzac.Versig;
import it.unica.tcs.lib.ECKeyStore;
import it.unica.tcs.lib.ITransactionBuilder;
import it.unica.tcs.lib.SerialTransactionBuilder;
import it.unica.tcs.lib.client.TransactionNotFoundException;
import it.unica.tcs.lib.model.Address;
import it.unica.tcs.lib.model.Hash;
import it.unica.tcs.lib.model.PlaceholderUtils;
import it.unica.tcs.lib.model.PrivateKey;
import it.unica.tcs.lib.model.PublicKey;
import it.unica.tcs.lib.model.Signature;
import it.unica.tcs.lib.utils.BitcoinUtils;
import it.unica.tcs.validation.ValidationResult;
import it.unica.tcs.xsemantics.BalzacInterpreter;
import it.unica.tcs.xsemantics.Rho;

@Singleton
public class ASTUtils {
	
    private static Logger logger = Logger.getLogger(ASTUtils.class);

    @Inject private BitcoinClientFactory bitcoinClientFactory;
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
    	key.setValue(privateKey.getPrivateKeyWif());
    	return key;
    }

    public String nodeToString(EObject eobj) {
    	return NodeModelUtils.getTokenText(NodeModelUtils.getNode(eobj));
    }

    public String getName(Referrable ref) {
        if (ref instanceof Parameter)
            return ((Parameter) ref).getName();
        if (ref instanceof it.unica.tcs.balzac.Transaction)
            return ((it.unica.tcs.balzac.Transaction) ref).getName();
        if (ref instanceof Constant)
            return ((Constant) ref).getName();    
        throw new IllegalStateException("Unexpected class "+ref.getClass());
    }

    public EAttribute getLiteralName(Referrable ref) {
        if (ref instanceof Parameter)
            return BalzacPackage.Literals.PARAMETER__NAME;
        if (ref instanceof it.unica.tcs.balzac.Transaction)
            return BalzacPackage.Literals.TRANSACTION__NAME;
        if (ref instanceof Constant)
            return BalzacPackage.Literals.CONSTANT__NAME;
        throw new IllegalStateException("Unexpected class "+ref.getClass());
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

    @SuppressWarnings("unchecked")
    public <T extends Interpretable> T interpretSafe(T exp, Rho rho) {
        // returns the same type of exp
        if (exp instanceof Literal)
            return exp;

        if (exp instanceof Transaction)
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
            res.setValue(((PrivateKey) value).getPrivateKeyWif());
            return res;
        }
        else if (value instanceof PublicKey) {
            PubKeyLiteral res = BalzacFactory.eINSTANCE.createPubKeyLiteral();
            res.setValue(((PublicKey) value).getPublicKeyByteString());
            return res;
        }
        else if (value instanceof Address) {
            AddressLiteral res = BalzacFactory.eINSTANCE.createAddressLiteral();
            res.setValue(((Address) value).getAddressWif());
            return res;
        }
        else if (value instanceof ECKey) {	// TODO remove this case
            PubKeyLiteral res = BalzacFactory.eINSTANCE.createPubKeyLiteral();
            res.setValue(((ECKey) value).getPublicKeyAsHex());
            return res;
        }
        else if (value instanceof Signature) {
        	Signature sig = (Signature) value;
            SignatureLiteral res = BalzacFactory.eINSTANCE.createSignatureLiteral();
            res.setValue(BitcoinUtils.encode(sig.getSignature()));
            if (sig.getPubkey().isPresent()) {
            	PubKeyLiteral pubkey = BalzacFactory.eINSTANCE.createPubKeyLiteral();
            	pubkey.setValue(BitcoinUtils.encode(sig.getPubkey().get()));
            	res.setPubkey(pubkey);
            }
            return res;
        }
        else {
            throw new IllegalStateException("Unexpected type "+value.getClass());
        }
    }

//  public boolean allAbsoluteAreBlock(Tlock tlock) {
//      return tlock.getTimes().stream()
//              .filter(x -> x instanceof AbsoluteTime)
//              .map(x -> (AbsoluteTime) x)
//              .allMatch(ASTUtils::isAbsoluteBlock);
//    }
//
//  public boolean allRelativeAreBlock(Tlock tlock) {
//      return tlock.getTimes().stream()
//              .filter(x -> x instanceof RelativeTime)
//              .map(x -> (RelativeTime) x)
//              .allMatch(ASTUtils::isRelativeBlock);
//    }
//
//  public boolean allAbsoluteAreDate(Tlock tlock) {
//      return tlock.getTimes().stream()
//              .filter(x -> x instanceof AbsoluteTime)
//              .map(x -> (AbsoluteTime) x)
//              .allMatch(ASTUtils::isAbsoluteDate);
//    }
//
//  public boolean allRelativeAreDate(Tlock tlock) {
//      return tlock.getTimes().stream()
//              .filter(x -> x instanceof RelativeTime)
//              .map(x -> (RelativeTime) x)
//              .allMatch(ASTUtils::isRelativeDate);
//    }

    public boolean isCoinbase(it.unica.tcs.balzac.Transaction tx) {
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
//        return false;   // TODO: temporarily disabled
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





//  public boolean isBlock(Time time) {
//      if (isRelative(time)) return isRelativeBlock(time);
//      if (isAbsolute(time)) return isAbsoluteBlock(time);
//      throw new IllegalArgumentException();
//    }
//
//  public boolean isDate(Time time) {
//      if (isRelative(time)) return isRelativeDate(time);
//      if (isAbsolute(time)) return isAbsoluteDate(time);
//      throw new IllegalArgumentException();
//    }
//
//    public boolean isAbsolute(Time time) {
//      return time instanceof AbsoluteTime;
//    }
//
//    public boolean isRelative(Time time) {
//      return time instanceof RelativeTime;
//    }
//
//    public boolean isAbsoluteBlock(Time time) {
//      return isAbsolute(time) && ((AbsoluteTime) time).isBlock();
//    }
//
//    public boolean isAbsoluteDate(Time time) {
//      return isAbsolute(time) && ((AbsoluteTime)time).isDate();
//    }
//
//
    public long getDelayValue(long seconds) {
        return convertSeconds(seconds);
    }

    public long getDelayValue(Delay delay) {
        long result = 0;
        result += convertMinutes(delay.getMinutes());
        result += convertHours(delay.getHours());
        result += convertDays(delay.getDays());
        return result;
    }

    private long convertSeconds(long secs) {
        return secs / 512;
    }

    private long convertMinutes(long min) {
        return convertSeconds(min*60);
    }

    private long convertHours(long hours) {
        return convertMinutes(hours*60);
    }

    private long convertDays(long days) {
        return convertHours(days*24);
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

    public ValidationResult isBase58WithChecksum(String key) {
        try {
            Base58.decodeChecked(key);
            return ValidationResult.VALIDATION_OK;
        } catch (AddressFormatException e1) {
            return new ValidationResult(false, e1.getMessage());
        }
    }

    public ValidationResult isValidPrivateKey(String key, NetworkParameters params) {
        try {
            DumpedPrivateKey.fromBase58(params, key);
            return ValidationResult.VALIDATION_OK;
        } catch (AddressFormatException e2) {
            return new ValidationResult(false, e2.getMessage());
        }
    }

    public ValidationResult isValidPublicKey(String key, NetworkParameters params) {
        try {
            org.bitcoinj.core.Address.fromString(params, key);
            return ValidationResult.VALIDATION_OK;
        } catch (AddressFormatException e2) {
            return new ValidationResult(false, e2.getMessage());
        }
    }

    public NetworkParameters networkParams(EObject obj) {
        List<Network> list = EcoreUtil2.getAllContentsOfType(EcoreUtil2.getRootContainer(obj), Network.class);

        if (list.size()==0) // network undeclared, assume testnet
            return TestNet3Params.get();

        if (list.size()==1) {
            Network net = list.get(0);

            if (net.isTestnet())
                return TestNet3Params.get();

            if (net.isMainnet())
                return MainNetParams.get();

            if (net.isRegtest())
                return RegTestParams.get();
        }

        throw new IllegalStateException();
    }

    public Transaction getTransactionById(String txid, NetworkParameters params) throws TransactionNotFoundException {
        byte[] payloadBytes = BitcoinUtils.decode(bitcoinClientFactory.getBitcoinClient(params).getRawTransaction(txid));
        return new Transaction(params, payloadBytes);
    }

    public long getOutputAmount(String txString, NetworkParameters params, int index) {
        try {
            Transaction tx = new Transaction(params, BitcoinUtils.decode(txString));
            return tx.getOutput(index).getValue().value;
        }
        catch (Exception e) {
            return -1;
        }
    }

    public SigHash toHashType(Modifier mod) {
        switch (mod) {
        case AIAO:
        case SIAO: return SigHash.ALL;
        case AISO:
        case SISO: return SigHash.SINGLE;
        case AINO:
        case SINO: return SigHash.NONE;
        default: throw new IllegalStateException();
        }
    }

    public boolean toAnyoneCanPay(Modifier mod) {
        switch (mod) {
        case SIAO:
        case SISO:
        case SINO: return true;
        case AIAO:
        case AISO:
        case AINO: return false;
        default: throw new IllegalStateException();
        }
    }

    public Optional<it.unica.tcs.balzac.Transaction> getTransactionFromReference(TransactionExpression txExp) {
        if (txExp instanceof Reference) {
            Reference ref = (Reference) txExp;
            if (ref.getRef() instanceof it.unica.tcs.balzac.Transaction) {
                it.unica.tcs.balzac.Transaction tx = (it.unica.tcs.balzac.Transaction) ref.getRef();
                return Optional.of(tx);
            }
            if (ref.getRef() instanceof Constant) {
                Constant c = (Constant) ref.getRef();
                if (c.getExp() instanceof TransactionExpression) {
                    return getTransactionFromReference((TransactionExpression) c.getExp());
                }
            }
        }
        return Optional.<it.unica.tcs.balzac.Transaction>absent();
    }
}
