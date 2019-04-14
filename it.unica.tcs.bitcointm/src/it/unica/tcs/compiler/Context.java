/*
 * Copyright 2018 Nicola Atzei
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
    public final boolean isP2SH;

    public Context(Rho rho, boolean isP2SH) {
        this.rho = rho;
        this.isP2SH = isP2SH;
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
