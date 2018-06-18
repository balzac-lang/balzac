/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.scoping;

import org.eclipse.xtext.naming.DefaultDeclarativeQualifiedNameProvider;
import org.eclipse.xtext.naming.QualifiedName;

import it.unica.tcs.balzac.Model;

public class BalzacQualifiedNameProvider extends DefaultDeclarativeQualifiedNameProvider {

    protected QualifiedName qualifiedName(Model ele){
        if (ele.getPackage()!=null)
            return getConverter().toQualifiedName(ele.getPackage().getName());
        return null;
    }
}
