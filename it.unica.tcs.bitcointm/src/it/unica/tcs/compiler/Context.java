/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.compiler;

import java.util.HashMap;

import it.unica.tcs.balzac.Parameter;
import it.unica.tcs.xsemantics.Rho;

/**
 * Everything that is relevant to achieve the compilation.
 */
public class Context {
    public final AltStack altstack = new AltStack();
    public final Rho rho;

    public Context(Rho rho) {
        this.rho = rho;
    }
}

class AltStack extends HashMap<Parameter, AltStackEntry>{
    private static final long serialVersionUID = 1L;
}

class AltStackEntry {
    public final Integer position;

    public AltStackEntry(Integer position) {
        this.position = position;
    }

    public static AltStackEntry of(Integer position) {
        return new AltStackEntry(position);
    }
}
