/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.utils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.Base58;
import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.Transaction.SigHash;
import org.bitcoinj.crypto.TransactionSignature;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.RegTestParams;
import org.bitcoinj.params.TestNet3Params;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.EcoreUtil2;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import it.unica.tcs.bitcoinTM.AbsoluteTime;
import it.unica.tcs.bitcoinTM.BitcoinTMFactory;
import it.unica.tcs.bitcoinTM.BitcoinTMPackage;
import it.unica.tcs.bitcoinTM.BooleanLiteral;
import it.unica.tcs.bitcoinTM.Constant;
import it.unica.tcs.bitcoinTM.Delay;
import it.unica.tcs.bitcoinTM.Expression;
import it.unica.tcs.bitcoinTM.Hash160Literal;
import it.unica.tcs.bitcoinTM.Hash256Literal;
import it.unica.tcs.bitcoinTM.Interpretable;
import it.unica.tcs.bitcoinTM.KeyLiteral;
import it.unica.tcs.bitcoinTM.Literal;
import it.unica.tcs.bitcoinTM.Modifier;
import it.unica.tcs.bitcoinTM.Network;
import it.unica.tcs.bitcoinTM.NumberLiteral;
import it.unica.tcs.bitcoinTM.Parameter;
import it.unica.tcs.bitcoinTM.Reference;
import it.unica.tcs.bitcoinTM.Referrable;
import it.unica.tcs.bitcoinTM.RelativeTime;
import it.unica.tcs.bitcoinTM.Ripemd160Literal;
import it.unica.tcs.bitcoinTM.Script;
import it.unica.tcs.bitcoinTM.Sha256Literal;
import it.unica.tcs.bitcoinTM.SignatureLiteral;
import it.unica.tcs.bitcoinTM.SignatureType;
import it.unica.tcs.bitcoinTM.StringLiteral;
import it.unica.tcs.bitcoinTM.Timelock;
import it.unica.tcs.bitcoinTM.Versig;
import it.unica.tcs.lib.Hash.Hash160;
import it.unica.tcs.lib.Hash.Hash256;
import it.unica.tcs.lib.Hash.Ripemd160;
import it.unica.tcs.lib.Hash.Sha256;
import it.unica.tcs.lib.ITransactionBuilder;
import it.unica.tcs.lib.SerialTransactionBuilder;
import it.unica.tcs.lib.client.BitcoinClientI;
import it.unica.tcs.lib.client.TransactionNotFoundException;
import it.unica.tcs.lib.utils.BitcoinUtils;
import it.unica.tcs.validation.ValidationResult;
import it.unica.tcs.xsemantics.BitcoinTMInterpreter;
import it.unica.tcs.xsemantics.Rho;
import it.xsemantics.runtime.Result;

@Singleton
public class ASTUtils {

    @Inject private BitcoinClientI bitcoin;
    @Inject private BitcoinTMInterpreter interpreter;

    public boolean isAddress(String wif) {
        try {
            Address.fromBase58(null, wif);
            return true;
        }
        catch(AddressFormatException e) {
            return false;
        }
    }

    public boolean isPrivateKey(String wif) {
        try {
            DumpedPrivateKey.fromBase58(null, wif);
            return true;
        }
        catch(AddressFormatException e) {
            return false;
        }
    }

    public String getName(Referrable ref) {
        if (ref instanceof Parameter)
            return ((Parameter) ref).getName();
        if (ref instanceof it.unica.tcs.bitcoinTM.Transaction)
            return ((it.unica.tcs.bitcoinTM.Transaction) ref).getName();
        if (ref instanceof Constant)
            return ((Constant) ref).getName();
        throw new IllegalStateException("Unexpected class "+ref.getClass());
    }

    public EAttribute getLiteralName(Referrable ref) {
        if (ref instanceof Parameter)
            return BitcoinTMPackage.Literals.PARAMETER__NAME;
        if (ref instanceof it.unica.tcs.bitcoinTM.Transaction)
            return BitcoinTMPackage.Literals.TRANSACTION__NAME;
        if (ref instanceof Constant)
            return BitcoinTMPackage.Literals.CONSTANT__NAME;
        throw new IllegalStateException("Unexpected class "+ref.getClass());
    }

    public Set<Parameter> getTxVariables(Expression exp) {
        Set<Parameter> refs =
                EcoreUtil2.getAllContentsOfType(exp.eContainer(), Reference.class)
                .stream()
                .map( v -> v.getRef() )
                .filter(this::isTxParameter)
                .map( r -> (Parameter) r )
                .collect(Collectors.toSet());

        return refs;
    }


    public boolean isTxParameter(Referrable p) {
        return (p instanceof Parameter) && (p.eContainer() instanceof it.unica.tcs.bitcoinTM.Transaction);
    }

    public boolean hasTxVariables(Expression exp) {
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
            NumberLiteral res = BitcoinTMFactory.eINSTANCE.createNumberLiteral();
            res.setValue((Long) value);
            return res;
        }
        else if (value instanceof String) {
            StringLiteral res = BitcoinTMFactory.eINSTANCE.createStringLiteral();
            res.setValue((String) value);
            return res;
        }
        else if (value instanceof Boolean) {
            BooleanLiteral res = BitcoinTMFactory.eINSTANCE.createBooleanLiteral();
            res.setTrue((Boolean) value);
            return res;
        }
        else if (value instanceof Hash160) {
            Hash160Literal res = BitcoinTMFactory.eINSTANCE.createHash160Literal();
            res.setValue(((Hash160) value).getBytes());
            return res;
        }
        else if (value instanceof Hash256) {
            Hash256Literal res = BitcoinTMFactory.eINSTANCE.createHash256Literal();
            res.setValue(((Hash256) value).getBytes());
            return res;
        }
        else if (value instanceof Ripemd160) {
            Ripemd160Literal res = BitcoinTMFactory.eINSTANCE.createRipemd160Literal();
            res.setValue(((Ripemd160) value).getBytes());
            return res;
        }
        else if (value instanceof Sha256) {
            Sha256Literal res = BitcoinTMFactory.eINSTANCE.createSha256Literal();
            res.setValue(((Sha256) value).getBytes());
            return res;
        }
        else if (value instanceof DumpedPrivateKey) {
            KeyLiteral res = BitcoinTMFactory.eINSTANCE.createKeyLiteral();
            res.setValue(((DumpedPrivateKey) value).toBase58());
            return res;
        }
        else if (value instanceof TransactionSignature) {
            SignatureLiteral res = BitcoinTMFactory.eINSTANCE.createSignatureLiteral();
            res.setValue(BitcoinUtils.encode(((TransactionSignature) value).encodeToBitcoin()));
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

    public boolean isCoinbase(it.unica.tcs.bitcoinTM.Transaction tx) {
        return tx.getInputs().size()==1 && tx.getInputs().get(0).isPlaceholder();
    }

    public boolean isCoinbase(Expression tx) {
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

    public boolean isSerial(Expression tx) {
        Result<Object> res = this.interpreter.interpretE(tx);

        if (res.failed())
            return false;
        else {
            return res.getFirst() instanceof SerialTransactionBuilder;
        }
    }

    public boolean isP2PKH(Script script, Rho rho) {
        boolean onlyOneSignatureParam = script.getParams().size() == 1 && script.getParams().get(0).getType() instanceof SignatureType;
        boolean onlyOnePubkey = (interpretSafe(script.getExp(), rho) instanceof Versig) && ((Versig) interpretSafe(script.getExp(), rho)).getPubkeys().size() == 1;

        return onlyOneSignatureParam && onlyOnePubkey;
    }

    public boolean isOpReturn(Script script, Rho rho) {
        boolean noParam = script.getParams().size() == 0;
        boolean onlyString = interpretSafe(script.getExp(), rho) instanceof StringLiteral;
        return noParam && onlyString;
    }

    public boolean isP2SH(Script script, Rho rho) {
        return !isP2PKH(script, rho) && !isOpReturn(script, rho);
    }



    public boolean containsAbsolute(List<Timelock> timelocks) {
        return timelocks.stream().filter(x -> x instanceof AbsoluteTime).count()>0;
    }

    public boolean containsRelative(List<Timelock> timelocks, ITransactionBuilder tx, Rho rho) {
        return timelocks.stream()
                .filter( x ->
                    x instanceof RelativeTime
                    && tx.equals(interpreter.interpret(((RelativeTime) x).getTx(), rho).getFirst())
                )
                .count()>0;
    }

    public AbsoluteTime getAbsolute(List<Timelock> timelocks) {
        return timelocks.stream()
                .filter(x -> x instanceof AbsoluteTime)
                .map( x -> (AbsoluteTime) x)
                .collect(Collectors.toList()).get(0);
    }

    public RelativeTime getRelative(List<Timelock> timelocks, ITransactionBuilder tx, Rho rho) {
        return timelocks.stream()
                .filter( x ->
                    x instanceof RelativeTime
                    && tx.equals(interpreter.interpret(((RelativeTime) x).getTx(), rho).getFirst())
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
//    public boolean isRelativeBlock(Time time) {
//      // true if the 22th bit is UNSET
//      int mask = 0b0000_0000__0100_0000__0000_0000__0000_0000;
//      return isRelative(time) && (((RelativeTime) time).getValue() & mask) == 0;
//    }
//
//    public boolean isRelativeDate(Time time) {
//      return isRelative(time) && !isRelativeBlock(time);
//  }

    public long getDelayValue(Delay delay) {
        long result = 0;
        result += convertMinutes(delay.getMinutes());
        result += convertHours(delay.getHours());
        result += convertDays(delay.getDays());
        return result;
    }

    private long convertMinutes(long min) {
        return (min*60) / 512;
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

    /**
     * Cast the given number to unsigned-short (16 bit)
     * @param i
     * @return
     */
    public long castUnsignedShort(long i) {
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

    public long getSequenceNumber(RelativeTime reltime, Rho rho) {
        if (reltime.isBlock()) {

            Result<Object> res = interpreter.interpret(reltime.getValue(), rho);

            if (res.failed())
                throw new IllegalStateException("Cannot interpret relative time");

            Long value = (Long) res.getFirst();

            return castUnsignedShort(value);
        }
        else {
            return setRelativeTimelockFlag(getDelayValue(reltime.getDelay()));
        }
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
