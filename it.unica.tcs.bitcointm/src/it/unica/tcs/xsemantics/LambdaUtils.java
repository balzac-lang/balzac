/*
 * Copyright 2017 Nicola Atzei
 */

/**
 *
 */
package it.unica.tcs.xsemantics;

import it.unica.tcs.balzac.BalzacFactory;
import it.unica.tcs.balzac.TypeVariable;

/**
 * @author bettini
 *
 */
public class LambdaUtils {

    protected int counter = 0;

    public void resetCounter() {
        counter = 0;
    }

    public TypeVariable createTypeVariable(String name) {
        TypeVariable typeVariable = BalzacFactory.eINSTANCE
                .createTypeVariable();
        typeVariable.setValue(name);
        return typeVariable;
    }

    public TypeVariable createFreshTypeVariable() {
        return createTypeVariable("X" + counter++);
    }

}
