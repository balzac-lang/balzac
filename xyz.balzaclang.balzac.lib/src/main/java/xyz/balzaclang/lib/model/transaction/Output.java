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

package xyz.balzaclang.lib.model.transaction;

import java.io.Serializable;

import xyz.balzaclang.lib.model.script.OutputScript;

public class Output implements Serializable {

    private static final long serialVersionUID = 1L;
    private final OutputScript script;
    private final long value;

    private Output(OutputScript script, long value) {
        this.script = script;
        this.value = value;
    }

    public static Output of(OutputScript script, long value) {
        return new Output(script, value);
    }

    public OutputScript getScript() {
        return script;
    }

    public long getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((script == null) ? 0 : script.hashCode());
        result = prime * result + (int) (value ^ (value >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Output other = (Output) obj;
        if (script == null) {
            if (other.script != null)
                return false;
        }
        else if (!script.equals(other.script))
            return false;
        if (value != other.value)
            return false;
        return true;
    }
}
