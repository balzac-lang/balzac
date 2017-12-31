/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.lib;

import java.io.Serializable;

import it.unica.tcs.lib.script.OutputScript;

/*
 * Output internal representation (not visible outside)
 */
public class Output implements Serializable {
    
    private static final long serialVersionUID = 1L;
    private final OutputScript script;
    private final long value;
    
    private Output(OutputScript script, long value) {
        this.script = script;
        this.value = value;
    }
    
    public static Output of(OutputScript script, long value) {
        return new Output(script,value);
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
        } else if (!script.equals(other.script))
            return false;
        if (value != other.value)
            return false;
        return true;
    }
}