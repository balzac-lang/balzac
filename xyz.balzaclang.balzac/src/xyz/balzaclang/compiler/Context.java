/*
 * Copyright 2020 Nicola Atzei
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

package xyz.balzaclang.compiler;

import java.util.HashMap;

import xyz.balzaclang.balzac.Parameter;
import xyz.balzaclang.xsemantics.Rho;

/**
 * Everything that is relevant to achieve the compilation.
 */
public class Context {

    public final AltStack altstack = new AltStack();
    public final Rho rho;
    public final boolean isP2SH;

    public Context(Rho rho, boolean isP2SH) {
        this.rho = rho;
        this.isP2SH = isP2SH;
    }
}

class AltStack extends HashMap<Parameter, AltStackEntry> {

    private static final long serialVersionUID = 1L;
}

class AltStackEntry {

    public final Integer position;

    public AltStackEntry(Integer position) {
        this.position = position;
    }

    public static AltStackEntry of(Integer position) {
        return new AltStackEntry(position);
    }
}
