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

package xyz.balzaclang.xsemantics;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;

import xyz.balzaclang.balzac.Type;

public class TypeSubstitutions {

    protected Map<String, Type> substitutions = new HashMap<>();

    protected Set<EObject> visited = new HashSet<>();

    public void reset() {
        substitutions.clear();
    }

    public void add(String typeVariableName, Type mapped) {
        substitutions.put(typeVariableName, mapped);
    }

    public Type mapped(String typeVariableName) {
        return substitutions.get(typeVariableName);
    }

    public Set<Entry<String, Type>> getSubstitutions() {
        return substitutions.entrySet();
    }

    public TypeSubstitutions addVisited(EObject tx) {
        visited.add(tx);
        return this;
    }

    public TypeSubstitutions removeVisited(EObject tx) {
        visited.remove(tx);
        return this;
    }

    public boolean isAlreadyVisited(EObject tx) {
        return visited.contains(tx);
    }
}
