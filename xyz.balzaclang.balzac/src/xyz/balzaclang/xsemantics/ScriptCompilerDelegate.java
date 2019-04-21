/*
 * Copyright 2019 Nicola Atzei
 */
package xyz.balzaclang.xsemantics;

import com.google.inject.Inject;

import xyz.balzaclang.balzac.Input;
import xyz.balzaclang.balzac.Output;
import xyz.balzaclang.compiler.ScriptCompiler;
import xyz.balzaclang.lib.model.ITransactionBuilder;
import xyz.balzaclang.lib.model.script.InputScript;
import xyz.balzaclang.lib.model.script.OutputScript;

/**
 * This class solves maven-build problems of interpreter.xsemantics
 */
class ScriptCompilerDelegate {

    @Inject private ScriptCompiler compiler;

    public InputScript compileInputScript(Input input, ITransactionBuilder parentTx, Rho rho) {
        return compiler.compileInputScript(input, parentTx, rho);
    }

    public OutputScript compileOutputScript(Output output, Rho rho) {
        return compiler.compileOutputScript(output, rho);
    }
}
