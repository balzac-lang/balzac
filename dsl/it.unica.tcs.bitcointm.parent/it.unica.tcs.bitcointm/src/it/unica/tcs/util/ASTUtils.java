package it.unica.tcs.util;

import java.util.stream.Collectors;

import it.unica.tcs.bitcoinTM.AbsoluteTime;
import it.unica.tcs.bitcoinTM.RelativeTime;
import it.unica.tcs.bitcoinTM.Time;
import it.unica.tcs.bitcoinTM.Tlock;
import it.unica.tcs.bitcoinTM.TransactionDeclaration;

public class ASTUtils {
	
	
//	public static boolean allAbsoluteAreBlock(Tlock tlock) {
//    	return tlock.getTimes().stream()
//    			.filter(x -> x instanceof AbsoluteTime)
//    			.map(x -> (AbsoluteTime) x)
//    			.allMatch(ASTUtils::isAbsoluteBlock);
//    }
//	
//	public static boolean allRelativeAreBlock(Tlock tlock) {
//    	return tlock.getTimes().stream()
//    			.filter(x -> x instanceof RelativeTime)
//    			.map(x -> (RelativeTime) x)
//    			.allMatch(ASTUtils::isRelativeBlock);
//    }
//	
//	public static boolean allAbsoluteAreDate(Tlock tlock) {
//    	return tlock.getTimes().stream()
//    			.filter(x -> x instanceof AbsoluteTime)
//    			.map(x -> (AbsoluteTime) x)
//    			.allMatch(ASTUtils::isAbsoluteDate);
//    }
//	
//	public static boolean allRelativeAreDate(Tlock tlock) {
//    	return tlock.getTimes().stream()
//    			.filter(x -> x instanceof RelativeTime)
//    			.map(x -> (RelativeTime) x)
//    			.allMatch(ASTUtils::isRelativeDate);
//    }
	
	public static boolean containsAbsolute(Tlock tlock) {
    	return tlock.getTimes().stream().filter(x -> x instanceof AbsoluteTime).count()>0;
    }
    
    public static boolean containsRelative(Tlock tlock, TransactionDeclaration tx) {
    	return tlock.getTimes().stream()
    			.filter(x -> x instanceof RelativeTime && ((RelativeTime) x).getTx()==tx)
    			.count()>0;
    }
	
    public static int getAbsolute(Tlock tlock) {
    	return tlock.getTimes().stream()
    			.filter(x -> x instanceof AbsoluteTime)
    			.collect(Collectors.toList()).get(0).getValue();
    }
    
    public static int getRelative(Tlock tlock, TransactionDeclaration tx) {
    	return tlock.getTimes().stream()
    			.filter(x -> x instanceof RelativeTime && ((RelativeTime) x).getTx()==tx)
    			.collect(Collectors.toList()).get(0).getValue();
    }
    
    
    
    
    
	public static boolean isBlock(Time time) {
    	if (isRelative(time)) return isRelativeBlock(time);
    	if (isAbsolute(time)) return isAbsoluteBlock(time);
    	throw new IllegalArgumentException();
    }
	
	public static boolean isDate(Time time) {
    	if (isRelative(time)) return isRelativeDate(time);
    	if (isAbsolute(time)) return isAbsoluteDate(time);
    	throw new IllegalArgumentException();
    }
    
    public static boolean isAbsolute(Time time) {
    	return time instanceof AbsoluteTime;
    }
    
    public static boolean isRelative(Time time) {
    	return time instanceof RelativeTime;
    }
    
    public static boolean isAbsoluteBlock(Time time) {
    	return isAbsolute(time) && ((AbsoluteTime) time).isBlock();
    }
    
    public static boolean isAbsoluteDate(Time time) {
    	return isAbsolute(time) && ((AbsoluteTime)time).isDate();
    }
    
    public static boolean isRelativeBlock(Time time) {
    	// true if the 22th bit is UNSET
    	int mask = 0b0000_0000__0100_0000__0000_0000__0000_0000;
    	return isRelative(time) && (((RelativeTime) time).getValue() & mask) == 0;
    }
    
    public static boolean isRelativeDate(Time time) {
    	return isRelative(time) && !isRelativeBlock(time);   
	}
    
    public static int setRelativeDate(int i) {
    	// true if the 22th bit is UNSET
    	int mask = 0b0000_0000__0100_0000__0000_0000__0000_0000;
    	return i | mask;
    }
    
    public static int castUnsignedShort(int i) {
		int mask = 0x0000FFFF;
		return i & mask;
	}
    
    public static int safeCastUnsignedShort(int i) {
		int value = castUnsignedShort(i);
		
		if (value!=i)
			throw new NumberFormatException("The number does not fit in 16 bits");
		
		return value;
	}
}
