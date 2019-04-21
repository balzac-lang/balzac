/*
 * Copyright 2018 Nicola Atzei
 */

/**
 *
 */
package xyz.balzaclang.xsemantics;

import xyz.balzaclang.balzac.BalzacFactory;
import xyz.balzaclang.balzac.TypeVariable;

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
