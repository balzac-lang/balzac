/*
 * Copyright 2018 Nicola Atzei
 */

package it.unica.tcs.scoping

import com.google.inject.Inject
import it.unica.tcs.balzac.Constant
import it.unica.tcs.balzac.Model
import it.unica.tcs.balzac.Participant
import it.unica.tcs.balzac.Reference
import it.unica.tcs.balzac.Referrable
import it.unica.tcs.balzac.Script
import it.unica.tcs.balzac.Transaction
import it.unica.tcs.utils.ASTUtils
import org.apache.log4j.Logger
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.EReference
import org.eclipse.xtext.EcoreUtil2
import org.eclipse.xtext.naming.QualifiedName
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

    static final Logger logger = Logger.getLogger(BalzacScopeProvider);
    @Inject extension ASTUtils

    def IScope scope_Referrable(Reference v, EReference ref) {
        logger.trace("resolving reference: "+v)
        val scope = 
            getIScopeForParameters(v,
                getParticipantIScope(v,
                    getGlobalParticipantIScope(v,
                        getGlobalIScope(v)
                    )
                )
            )
        logger.trace("scope: "+scope.allElements)
        scope
    }

    /**
     * Returns all global variables, Transactions and Constants
     */
    def IScope getGlobalIScope(EObject ctx){
        getGlobalIScope(ctx, Constant,
            getGlobalIScope(ctx, Transaction))
    }

    def IScope getGlobalIScope(EObject ctx, Class<? extends EObject> clazz){
        getGlobalIScope(ctx, clazz, IScope.NULLSCOPE)
    }

    def IScope getGlobalIScope(EObject ctx, Class<? extends EObject> clazz, IScope outer){
        logger.trace("fetching all global objects of type "+clazz.name)
        val model = EcoreUtil2.getContainerOfType(ctx, Model)               // get the model
        val candidates = model.declarations.filter(clazz)                   // take only instance of 'clazz', not recursively
        return Scopes.scopeFor(candidates, outer)                           // return the scope
    }

    /**
     * Return participant variables, Transaction and Constants, with fully qualified names
     */
    def IScope getGlobalParticipantIScope(EObject ctx, IScope outer){
        getGlobalParticipantIScope(ctx, Constant,
            getGlobalParticipantIScope(ctx, Transaction, outer))
    }

    def IScope getGlobalParticipantIScope(EObject ctx, Class<? extends EObject> clazz){
        getGlobalIScope(ctx, clazz, IScope.NULLSCOPE)
    }

    def IScope getGlobalParticipantIScope(EObject ctx, Class<? extends Referrable> clazz, IScope outer){
        logger.trace("fetching all fullt qualified participant objects of type "+clazz.name)
        val model = EcoreUtil2.getContainerOfType(ctx, Model)               // get the model
        val participants = model.declarations.filter(Participant)           // get all participants
        
        val candidates = newHashMap
        for (p : participants) {
            val pcandidates = p.declarations
                .filter(clazz)
                .filter[c | !(c instanceof Constant && (c as Constant).private)]    // remove private constants
            pcandidates.forEach[c | candidates.put(c, p.pname)]                     // map each declaration with the participant name
        }
        val qualifiedNameProvider = [Referrable p| QualifiedName.create(candidates.get(p), p.name)]
        return Scopes.scopeFor(candidates.keySet, qualifiedNameProvider, outer)
    }

    /**
     * Return participant variables, Transaction and Constants
     */
    def IScope getParticipantIScope(EObject ctx, IScope outer){
        getParticipantIScope(ctx, Constant,
            getParticipantIScope(ctx, Transaction, outer))
    }

    def IScope getParticipantIScope(EObject ctx, Class<? extends Referrable> clazz){
        getParticipantIScope(ctx, clazz, IScope.NULLSCOPE)
    }

    def IScope getParticipantIScope(EObject ctx, Class<? extends Referrable> clazz, IScope outer){
        logger.trace("fetching all participant objects of type "+clazz.name)
        val participant = EcoreUtil2.getContainerOfType(ctx, Participant)   // get the participant
        if (participant === null) return outer
        val candidates = participant.declarations.filter(clazz)             // take only instance of 'clazz', not recursively
        return Scopes.scopeFor(candidates, outer)    // return the scope
    }

    /**
     * Recursively get all Parameter declarations until Transaction definition
     */
    def dispatch IScope getIScopeForParameters(EObject cont, IScope outer) {
        logger.trace("skipping: "+cont)
        return getIScopeForParameters(cont.eContainer, outer)
    }

    def dispatch IScope getIScopeForParameters(Script obj, IScope outer) {
        logger.trace('''adding script params: [«obj.params.map[p|p.name+":"+p.type].join(",")»]''')
        return Scopes.scopeFor(
            obj.params,
            getIScopeForParameters(obj.eContainer, outer)
        )
    }

    def dispatch IScope getIScopeForParameters(Transaction obj, IScope outer) {
        logger.trace('''adding transaction params: [«obj.params.map[p|p.name+":"+p.type].join(",")»]''')
        return Scopes.scopeFor(obj.params, outer)
        // stop recursion
    }
    
    def dispatch IScope getIScopeForParameters(Model obj, IScope outer) {
        return outer
        // stop recursion
    }

    // unused
    def IScope getIScopeForAllContentsOfClass(EObject ctx, Class<? extends EObject> clazz, IScope outer){
        logger.trace("fetching all objects of type "+clazz.name)
        val root = EcoreUtil2.getRootContainer(ctx)                         // get the root
        val candidates = EcoreUtil2.getAllContentsOfType(root, clazz)       // get all contents of type clazz
        return Scopes.scopeFor(candidates, outer)                           // return the scope
    }
}
