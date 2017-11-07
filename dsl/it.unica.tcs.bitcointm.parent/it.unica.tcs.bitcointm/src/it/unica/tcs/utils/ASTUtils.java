/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.Base58;
import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.Transaction.SigHash;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.RegTestParams;
import org.bitcoinj.params.TestNet3Params;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.EcoreUtil2;

import static com.google.common.base.Preconditions.*;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import it.unica.tcs.bitcoinTM.AbsoluteTime;
import it.unica.tcs.bitcoinTM.BitcoinTMFactory;
import it.unica.tcs.bitcoinTM.BooleanLiteral;
import it.unica.tcs.bitcoinTM.ExpressionI;
import it.unica.tcs.bitcoinTM.Hash160Literal;
import it.unica.tcs.bitcoinTM.Hash256Literal;
import it.unica.tcs.bitcoinTM.KeyLiteral;
import it.unica.tcs.bitcoinTM.Literal;
import it.unica.tcs.bitcoinTM.Modifier;
import it.unica.tcs.bitcoinTM.Network;
import it.unica.tcs.bitcoinTM.NumberLiteral;
import it.unica.tcs.bitcoinTM.Parameter;
import it.unica.tcs.bitcoinTM.RelativeTime;
import it.unica.tcs.bitcoinTM.Ripemd160Literal;
import it.unica.tcs.bitcoinTM.Script;
import it.unica.tcs.bitcoinTM.Sha256Literal;
import it.unica.tcs.bitcoinTM.SignatureType;
import it.unica.tcs.bitcoinTM.StringLiteral;
import it.unica.tcs.bitcoinTM.Time;
import it.unica.tcs.bitcoinTM.Tlock;
import it.unica.tcs.bitcoinTM.TransactionBody;
import it.unica.tcs.bitcoinTM.TransactionDeclaration;
import it.unica.tcs.bitcoinTM.UserTransactionDeclaration;
import it.unica.tcs.bitcoinTM.VariableReference;
import it.unica.tcs.bitcoinTM.Versig;
import it.unica.tcs.lib.Hash.Hash160;
import it.unica.tcs.lib.Hash.Hash256;
import it.unica.tcs.lib.Hash.Ripemd160;
import it.unica.tcs.lib.Hash.Sha256;
import it.unica.tcs.lib.client.BitcoinClientI;
import it.unica.tcs.lib.client.TransactionNotFoundException;
import it.unica.tcs.lib.utils.BitcoinUtils;
import it.unica.tcs.validation.ValidationResult;
import it.unica.tcs.xsemantics.BitcoinTMTypeSystem;
import it.xsemantics.runtime.Result;

@Singleton
public class ASTUtils {
	
	@Inject private BitcoinClientI bitcoin;
	@Inject private BitcoinTMTypeSystem typeSystem;
	
	public Set<Parameter> getTxVariables(ExpressionI exp) {
        Set<Parameter> refs = 
        		EcoreUtil2.getAllContentsOfType(exp, VariableReference.class)
        		.stream()
        		.filter(v -> v.getRef() instanceof Parameter)
        		.map(v -> (Parameter) v.getRef())
    			.filter(v -> v.eContainer() instanceof TransactionDeclaration)
    			.collect(Collectors.toSet());
    
        return refs;
	}
	
	public boolean hasTxVariables(ExpressionI exp) {
        return !getTxVariables(exp).isEmpty();
	}
	
	
	public <T extends ExpressionI> T interpretSafe(ExpressionI exp, Class<T> expectedType) {
		return interpretSafe(exp, new HashMap<>(), expectedType);
	}
	
	public <T extends ExpressionI> T interpretSafe(ExpressionI exp, Map<Parameter,Object> rho, Class<T> expectedType) {
		ExpressionI res = interpretSafe(exp, rho);
		checkState(expectedType.isInstance(res), "expected type "+expectedType+", got "+res.getClass());
		return expectedType.cast(res);
	}
	
	public <T extends ExpressionI> T interpretSafe(T exp) {
		// returns the same type of exp
		return interpretSafe(exp, new HashMap<>());
	}
	
	@SuppressWarnings("unchecked")
	public <T extends ExpressionI> T interpretSafe(T exp, Map<Parameter,Object> rho) {
		// returns the same type of exp
		Result<Object> interpreted = typeSystem.interpret(exp, rho);
		if (interpreted.failed()) 
			return exp;
		else {
			if (exp instanceof Literal)
				return exp;
			
			Object value = interpreted.getFirst();
			if (value instanceof Long) {
				NumberLiteral res = BitcoinTMFactory.eINSTANCE.createNumberLiteral();
	    		res.setValue((Long) value);
	    		return (T) res;
	    	}
	    	else if (value instanceof String) {
	    		StringLiteral res = BitcoinTMFactory.eINSTANCE.createStringLiteral();
	    		res.setValue((String) value);
	    		return (T) res;
	    	}	
	    	else if (value instanceof Boolean) {
	    		BooleanLiteral res = BitcoinTMFactory.eINSTANCE.createBooleanLiteral();
	    		res.setTrue((Boolean) value);
	    		return (T) res;	
	    	}	
	    	else if (value instanceof Hash160) {
	    		Hash160Literal res = BitcoinTMFactory.eINSTANCE.createHash160Literal();
	    		res.setValue(((Hash160) value).getBytes());
	    		return (T) res;	
	    	}
	    	else if (value instanceof Hash256) {
	    		Hash256Literal res = BitcoinTMFactory.eINSTANCE.createHash256Literal();
	    		res.setValue(((Hash256) value).getBytes());
	    		return (T) res;    		
	    	}
	    	else if (value instanceof Ripemd160) {
	    		Ripemd160Literal res = BitcoinTMFactory.eINSTANCE.createRipemd160Literal();
	    		res.setValue(((Ripemd160) value).getBytes());
	    		return (T) res;    		
	    	}
	    	else if (value instanceof Sha256) {
	    		Sha256Literal res = BitcoinTMFactory.eINSTANCE.createSha256Literal();
	    		res.setValue(((Sha256) value).getBytes());
	    		return (T) res;    		
	    	}
	    	else if (value instanceof DumpedPrivateKey) {
	    		KeyLiteral res = BitcoinTMFactory.eINSTANCE.createKeyLiteral();
	    		res.setValue(((DumpedPrivateKey) value).toBase58());
	    		return (T) res;    		
	    	}
	    	else {
	    		throw new IllegalStateException("Unexpected type "+value.getClass());
	    	}	
		}
	}
	
//	public boolean allAbsoluteAreBlock(Tlock tlock) {
//    	return tlock.getTimes().stream()
//    			.filter(x -> x instanceof AbsoluteTime)
//    			.map(x -> (AbsoluteTime) x)
//    			.allMatch(ASTUtils::isAbsoluteBlock);
//    }
//	
//	public boolean allRelativeAreBlock(Tlock tlock) {
//    	return tlock.getTimes().stream()
//    			.filter(x -> x instanceof RelativeTime)
//    			.map(x -> (RelativeTime) x)
//    			.allMatch(ASTUtils::isRelativeBlock);
//    }
//	
//	public boolean allAbsoluteAreDate(Tlock tlock) {
//    	return tlock.getTimes().stream()
//    			.filter(x -> x instanceof AbsoluteTime)
//    			.map(x -> (AbsoluteTime) x)
//    			.allMatch(ASTUtils::isAbsoluteDate);
//    }
//	
//	public boolean allRelativeAreDate(Tlock tlock) {
//    	return tlock.getTimes().stream()
//    			.filter(x -> x instanceof RelativeTime)
//    			.map(x -> (RelativeTime) x)
//    			.allMatch(ASTUtils::isRelativeDate);
//    }
	
	public boolean isCoinbase(UserTransactionDeclaration tx) {
		return isCoinbase(tx.getBody());
	}
	
	public boolean isCoinbase(TransactionBody tx) {
		return tx.getInputs().size()==1 && tx.getInputs().get(0).isPlaceholder();
	}

	public boolean isP2PKH(Script script) {
        boolean onlyOneSignatureParam = 
        		script.getParams().size() == 1 && script.getParams().get(0).getType() instanceof SignatureType;
        boolean onlyOnePubkey = (interpretSafe(script.getExp()) instanceof Versig) 
        		&& ((Versig) interpretSafe(script.getExp())).getPubkeys().size() == 1;

        return onlyOneSignatureParam && onlyOnePubkey;
    }

	public boolean isOpReturn(Script script) {
        boolean noParam = script.getParams().size() == 0;
        boolean onlyString = interpretSafe(script.getExp()) instanceof StringLiteral;
        return noParam && onlyString;
    }

	public boolean isP2SH(Script script) {
        return !isP2PKH(script) && !isOpReturn(script);
    }

	
	
	public boolean containsAbsolute(Tlock tlock) {
    	return tlock.getTimes().stream().filter(x -> x instanceof AbsoluteTime).count()>0;
    }
    
    public boolean containsRelative(Tlock tlock, TransactionDeclaration tx) {
    	return tlock.getTimes().stream()
    			.filter(x -> x instanceof RelativeTime && ((RelativeTime) x).getTx()==tx)
    			.count()>0;
    }
	
    public long getAbsolute(Tlock tlock) {
    	return tlock.getTimes().stream()
    			.filter(x -> x instanceof AbsoluteTime)
    			.collect(Collectors.toList()).get(0).getValue();
    }
    
    public long getRelative(Tlock tlock, TransactionDeclaration tx) {
    	return tlock.getTimes().stream()
    			.filter(x -> x instanceof RelativeTime && ((RelativeTime) x).getTx()==tx)
    			.collect(Collectors.toList()).get(0).getValue();
    }
    
    
    
    
    
	public boolean isBlock(Time time) {
    	if (isRelative(time)) return isRelativeBlock(time);
    	if (isAbsolute(time)) return isAbsoluteBlock(time);
    	throw new IllegalArgumentException();
    }
	
	public boolean isDate(Time time) {
    	if (isRelative(time)) return isRelativeDate(time);
    	if (isAbsolute(time)) return isAbsoluteDate(time);
    	throw new IllegalArgumentException();
    }
    
    public boolean isAbsolute(Time time) {
    	return time instanceof AbsoluteTime;
    }
    
    public boolean isRelative(Time time) {
    	return time instanceof RelativeTime;
    }
    
    public boolean isAbsoluteBlock(Time time) {
    	return isAbsolute(time) && ((AbsoluteTime) time).isBlock();
    }
    
    public boolean isAbsoluteDate(Time time) {
    	return isAbsolute(time) && ((AbsoluteTime)time).isDate();
    }
    
    public boolean isRelativeBlock(Time time) {
    	// true if the 22th bit is UNSET
    	int mask = 0b0000_0000__0100_0000__0000_0000__0000_0000;
    	return isRelative(time) && (((RelativeTime) time).getValue() & mask) == 0;
    }
    
    public boolean isRelativeDate(Time time) {
    	return isRelative(time) && !isRelativeBlock(time);   
	}
    
    public long setRelativeDate(int i) {
    	// true if the 22th bit is UNSET
    	int mask = 0b0000_0000__0100_0000__0000_0000__0000_0000;
    	return i | mask;
    }
    
    public int castUnsignedShort(int i) {
		int mask = 0x0000FFFF;
		return i & mask;
	}
    
    public int safeCastUnsignedShort(int i) {
		int value = castUnsignedShort(i);
		
		if (value!=i)
			throw new NumberFormatException("The number does not fit in 16 bits");
		
		return value;
	}
    
    public byte[] wifToAddressHash(String wif, NetworkParameters params) {
		return wifToAddress(wif, params).getHash160();
	}
	
	public Address wifToAddress(String wif, NetworkParameters params) {
		Address pubkeyAddr = Address.fromBase58(params, wif);
		return pubkeyAddr;
	}
	
	public byte[] privateWifToPubkeyBytes(String wif, NetworkParameters params) {
		return DumpedPrivateKey.fromBase58(params, wif).getKey().getPubKey();
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
			Address.fromBase58(params, key);
			return ValidationResult.VALIDATION_OK;
		} catch (AddressFormatException e2) {
			return new ValidationResult(false, e2.getMessage());
		}
	}
	
	public NetworkParameters networkParams(EObject obj) {
		List<Network> list = EcoreUtil2.getAllContentsOfType(EcoreUtil2.getRootContainer(obj), Network.class);
			
		if (list.size()==0)	// network undeclared, assume testnet
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
		byte[] payloadBytes = BitcoinUtils.decode(bitcoin.getRawTransaction(txid));
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
}
