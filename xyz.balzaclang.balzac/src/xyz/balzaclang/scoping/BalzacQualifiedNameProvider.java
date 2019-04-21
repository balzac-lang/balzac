/*
 * Copyright 2018 Nicola Atzei
 */

package xyz.balzaclang.scoping;

import org.eclipse.xtext.naming.DefaultDeclarativeQualifiedNameProvider;
import org.eclipse.xtext.naming.QualifiedName;

import xyz.balzaclang.balzac.Model;

public class BalzacQualifiedNameProvider extends DefaultDeclarativeQualifiedNameProvider {

    protected QualifiedName qualifiedName(Model ele){
        if (ele.getPackage()!=null)
            return getConverter().toQualifiedName(ele.getPackage().getName());
        return null;
    }
}
