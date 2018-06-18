/*
 * Copyright 2018 Nicola Atzei
 */

package it.unica.tcs.scoping

import it.unica.tcs.balzac.Constant
import it.unica.tcs.balzac.Reference
import it.unica.tcs.balzac.Script
import it.unica.tcs.balzac.Transaction
import org.apache.log4j.Logger
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.EReference
import org.eclipse.xtext.EcoreUtil2
import org.eclipse.xtext.scoping.IScope
import org.eclipse.xtext.scoping.Scopes
import org.eclipse.xtext.scoping.impl.AbstractDeclarativeScopeProvider

/**
 * This class contains custom scoping description.
 *
 * See https://www.eclipse.org/Xtext/documentation/303_runtime_concepts.html#scoping
 * on how and when to use it.
 */
class BalzacScopeProvider extends AbstractDeclarativeScopeProvider {

    private static final Logger logger = Logger.getLogger(BalzacScopeProvider);

    def IScope scope_Referrable(Reference v, EReference ref) {
        logger.trace("resolving reference: "+v)
        getScopeForParameters(v,
            getIScopeForAllContentsOfClass(v, Constant,
                getIScopeForAllContentsOfClass(v, Transaction)
            )
        )
    }

    def static IScope getIScopeForAllContentsOfClass(EObject ctx, Class<? extends EObject> clazz){
        getIScopeForAllContentsOfClass(ctx, clazz, IScope.NULLSCOPE)
    }

    def static IScope getIScopeForAllContentsOfClass(EObject ctx, Class<? extends EObject> clazz, IScope outer){
        logger.trace("fetching all objects of type "+clazz.name)
        var root = EcoreUtil2.getRootContainer(ctx);                        // get the root
        var candidates = EcoreUtil2.getAllContentsOfType(root, clazz);      // get all contents of type clazz
        return Scopes.scopeFor(candidates, outer);                          // return the scope
    }

    //utils: recursively get all free-names declarations until Transaction definition
    def static dispatch IScope getScopeForParameters(EObject cont, IScope outer) {
        logger.trace("skipping: "+cont)
        return getScopeForParameters(cont.eContainer, outer);
    }

    def static dispatch IScope getScopeForParameters(Script obj, IScope outer) {
        logger.trace('''adding script params: [«obj.params.map[p|p.name+":"+p.type].join(",")»]''')
        return Scopes.scopeFor(
            obj.params,
            getScopeForParameters(obj.eContainer, outer)
        );
    }

    def static dispatch IScope getScopeForParameters(Transaction obj, IScope outer) {
        logger.trace('''adding transaction params: [«obj.params.map[p|p.name+":"+p.type].join(",")»]''')
        return Scopes.scopeFor(obj.params, outer);
        // stop recursion
    }

}
