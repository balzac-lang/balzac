/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.xsemantics;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import it.unica.tcs.bitcoinTM.Referrable;
import it.unica.tcs.bitcoinTM.Type;

public class TypeSubstitutions {
	protected Map<String, Type> substitutions = new HashMap<>();
	
	protected Map<Referrable, Type> support = new HashMap<>();

    public void reset() {
        substitutions.clear();
    }

    public void add(String typeVariableName, Type mapped) {
		substitutions.put(typeVariableName, mapped);
    }

    public Type mapped(String typeVariableName) {
		return substitutions.get(typeVariableName);
    }

    public Set<Entry<String, Type>> getSubstitutions() {
        return substitutions.entrySet();
    }
}
