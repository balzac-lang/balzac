/*
 * Copyright 2017 Nicola Atzei
 */
package it.unica.tcs.xsemantics;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;

import it.unica.tcs.balzac.Referrable;

public class Rho extends HashMap<Referrable,Object> {

    private static final long serialVersionUID = 1L;

    private final Set<EObject> visited = new HashSet<>();

    public Rho addVisited(EObject tx) {
        visited.add(tx);
        return this;
    }

    public Rho removeVisited(EObject tx) {
        visited.remove(tx);
        return this;
    }

    public boolean isAlreadyVisited(EObject tx) {
        return visited.contains(tx);
    }

    public Rho fresh() {
        Rho rho = new Rho();
        rho.visited.addAll(this.visited);
        return rho;
    }
}
