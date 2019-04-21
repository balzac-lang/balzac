/*
 * Copyright 2019 Nicola Atzei
 */
package xyz.balzaclang.utils;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;

import xyz.balzaclang.balzac.BalzacPackage;
import xyz.balzaclang.balzac.Constant;
import xyz.balzaclang.balzac.Modifier;
import xyz.balzaclang.balzac.Parameter;
import xyz.balzaclang.balzac.Referrable;
import xyz.balzaclang.lib.model.SignatureModifier;

public class ASTExtensions {

    public static String nodeToString(EObject eobj) {
        return NodeModelUtils.getTokenText(NodeModelUtils.getNode(eobj));
    }

    public static SignatureModifier toSignatureModifier(Modifier mod) {
        switch (mod) {
        case AIAO: return SignatureModifier.ALL_INPUT_ALL_OUTPUT;
        case AISO: return SignatureModifier.ALL_INPUT_SINGLE_OUTPUT;
        case AINO: return SignatureModifier.ALL_INPUT_NO_OUTPUT;
        case SIAO: return SignatureModifier.SINGLE_INPUT_ALL_OUTPUT;
        case SISO: return SignatureModifier.SINGLE_INPUT_SINGLE_OUTPUT;
        case SINO: return SignatureModifier.SINGLE_INPUT_NO_OUTPUT;
        default: throw new IllegalStateException();
        }
    }

    public static String getName(Referrable ref) {
        if (ref instanceof Parameter)
            return ((Parameter) ref).getName();
        if (ref instanceof xyz.balzaclang.balzac.Transaction)
            return ((xyz.balzaclang.balzac.Transaction) ref).getName();
        if (ref instanceof Constant)
            return ((Constant) ref).getName();
        throw new IllegalStateException("Unexpected class "+ref.getClass());
    }

    public static EAttribute getLiteralName(Referrable ref) {
        if (ref instanceof Parameter)
            return BalzacPackage.Literals.PARAMETER__NAME;
        if (ref instanceof xyz.balzaclang.balzac.Transaction)
            return BalzacPackage.Literals.TRANSACTION__NAME;
        if (ref instanceof Constant)
            return BalzacPackage.Literals.CONSTANT__NAME;
        throw new IllegalStateException("Unexpected class "+ref.getClass());
    }
}
