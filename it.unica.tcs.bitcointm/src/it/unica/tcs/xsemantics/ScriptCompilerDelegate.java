/*
 * Copyright 2019 Nicola Atzei
 */
package it.unica.tcs.xsemantics;

import com.google.inject.Inject;

import it.unica.tcs.balzac.Input;
import it.unica.tcs.balzac.Output;
import it.unica.tcs.compiler.ScriptCompiler;
import it.unica.tcs.lib.model.ITransactionBuilder;
import it.unica.tcs.lib.model.script.InputScript;
import it.unica.tcs.lib.model.script.OutputScript;

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
