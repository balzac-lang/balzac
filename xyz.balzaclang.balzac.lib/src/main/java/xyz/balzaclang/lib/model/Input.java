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

package xyz.balzaclang.lib.model;

import java.io.Serializable;

import xyz.balzaclang.lib.model.script.InputScript;

public class Input implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final int UNSET_OUTINDEX = -1;
    private static final int UNSET_LOCKTIME = -1;

    private final ITransactionBuilder parentTx;
    private final int outIndex;
    private final InputScript script;
    private final long locktime;

    private Input(ITransactionBuilder parentTx, int outIndex, InputScript script, long locktime) {
        this.parentTx = parentTx;
        this.script = script;
        this.outIndex = outIndex;
        this.locktime = locktime;
    }

    static Input of(InputScript script){
        return of((ITransactionBuilder) null, UNSET_OUTINDEX, script, UNSET_LOCKTIME);
    }

    static Input of(InputScript script, long locktime){
        return of((ITransactionBuilder) null, UNSET_OUTINDEX, script, locktime);
    }

    static Input of(int index, InputScript script){
        return of((ITransactionBuilder) null, index, script, UNSET_LOCKTIME);
    }

    static Input of(ITransactionBuilder tx, int index, InputScript script){
        return of(tx, index, script, UNSET_LOCKTIME);
    }

    static Input of(ITransactionBuilder tx, int index, InputScript script, long locktime){
        return new Input(tx, index, script, locktime);
    }

    public boolean hasParentTx() {
        return getParentTx()!=null;
    };

    public boolean hasLocktime() {
        return locktime!=UNSET_LOCKTIME;
    };

    public ITransactionBuilder getParentTx() {
        return parentTx;
    }

    public int getOutIndex() {
        return outIndex;
    }

    public InputScript getScript() {
        return script;
    }

    public long getLocktime() {
        return locktime;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (locktime ^ (locktime >>> 32));
        result = prime * result + outIndex;
        result = prime * result + ((parentTx == null) ? 0 : parentTx.hashCode());
        result = prime * result + ((script == null) ? 0 : script.hashCode());
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
        Input other = (Input) obj;
        if (locktime != other.locktime)
            return false;
        if (outIndex != other.outIndex)
            return false;
        if (parentTx == null) {
            if (other.parentTx != null)
                return false;
        } else if (!parentTx.equals(other.parentTx))
            return false;
        if (script == null) {
            if (other.script != null)
                return false;
        } else if (!script.equals(other.script))
            return false;
        return true;
    }

}
