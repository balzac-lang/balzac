/*
 * Copyright 2017 Nicola Atzei
 */
package it.unica.tcs.xsemantics;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import it.unica.tcs.balzac.Referrable;

public class Rho extends HashMap<Referrable,Object> {

    private static final long serialVersionUID = 1L;

    private final Set<Referrable> visited = new HashSet<>();

    public Rho addVisited(Referrable tx) {
        visited.add(tx);
        return this;
    }

    public Rho removeVisited(Referrable tx) {
        visited.remove(tx);
        return this;
    }

    public boolean isAlreadyVisited(Referrable tx) {
        return visited.contains(tx);
    }

    public Rho fresh() {
        Rho rho = new Rho();
        rho.visited.addAll(this.visited);
        return rho;
    }
}
