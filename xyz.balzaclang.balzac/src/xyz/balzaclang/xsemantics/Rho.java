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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;

import xyz.balzaclang.balzac.Referrable;
import xyz.balzaclang.lib.model.NetworkType;

public class Rho extends HashMap<Referrable, Object> {

    private static final long serialVersionUID = 1L;

    private final Set<EObject> visited = new HashSet<>();
    public final NetworkType networkParams;

    public Rho(NetworkType params) {
        this.networkParams = params;
    }

    public Rho addVisited(EObject tx) {
        visited.add(tx);
        return this;
    }

    public Rho removeVisited(EObject tx) {
        visited.remove(tx);
        return this;
    }

    public boolean isAlreadyVisited(EObject tx) {
        return visited.contains(tx);
    }

    public Rho fresh() {
        Rho rho = new Rho(networkParams);
        rho.visited.addAll(this.visited);
        return rho;
    }
}
