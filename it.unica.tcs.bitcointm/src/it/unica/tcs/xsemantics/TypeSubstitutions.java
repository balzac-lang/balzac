/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.xsemantics;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;

import it.unica.tcs.balzac.Type;

public class TypeSubstitutions {
    protected Map<String, Type> substitutions = new HashMap<>();

    protected Set<EObject> visited = new HashSet<>();

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

    public TypeSubstitutions addVisited(EObject tx) {
        visited.add(tx);
        return this;
    }

    public TypeSubstitutions removeVisited(EObject tx) {
        visited.remove(tx);
        return this;
    }

    public boolean isAlreadyVisited(EObject tx) {
        return visited.contains(tx);
    }
}
