package it.unica.tcs.generator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.Transaction.SigHash;

import it.unica.tcs.bitcoinTM.Input;
import it.unica.tcs.bitcoinTM.Parameter;

/**
 * Everything that is relevant to achieve the compilation.
 */
public class Context {

	public Map<Parameter, ImmutablePair<Integer,Integer>> altstack = new HashMap<>();
	
	public SignaturesTracker signTracker = new SignaturesTracker();
		
	public void clear() {
		altstack.clear();
	}
	
    public static class SignaturesTracker extends HashMap<Input,List<SignatureUtil>>{
		public Input currentInput;
    }
    
        
    public static class SignatureUtil {
        public int index;
        public ECKey key;
        public SigHash hashType;
        public boolean anyoneCanPay;
        
        @Override
        public String toString() {
	        return "SignatureUtil [index=" + index + ", key=" + key + ", hashType=" + hashType + ", anyoneCanPay="+ anyoneCanPay + "]";
	    }
    }
}
