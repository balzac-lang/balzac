/*
 * Copyright 2019 Nicola Atzei
 */
package xyz.balzaclang.xsemantics;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;

import xyz.balzaclang.balzac.Referrable;
import xyz.balzaclang.lib.model.NetworkType;

public class Rho extends HashMap<Referrable,Object> {

    private static final long serialVersionUID = 1L;

    private final Set<EObject> visited = new HashSet<>();
    public final NetworkType networkParams;
    private boolean evaluateWitnesses = true;

    public Rho(NetworkType params) {
        this.networkParams = params;
    }

    public Rho addVisited(EObject tx) {
        visited.add(tx);
        return this;
    }

    public Rho setEvaluateWitnesses(boolean value) {
        evaluateWitnesses = value;
        return this;
    }

    public boolean getEvaluateWitnesses() {
        return evaluateWitnesses;
    }

    public Rho removeVisited(EObject tx) {
        visited.remove(tx);
        return this;
    }

    public boolean isAlreadyVisited(EObject tx) {
        return visited.contains(tx);
    }

    public Rho fresh() {
        Rho rho = new Rho(networkParams);
        rho.visited.addAll(this.visited);
        return rho;
    }
}
