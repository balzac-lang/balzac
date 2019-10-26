/*
 * Copyright 2019 Nicola Atzei
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
