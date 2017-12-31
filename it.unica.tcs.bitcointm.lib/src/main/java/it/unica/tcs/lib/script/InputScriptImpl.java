/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.lib.script;

import org.bitcoinj.script.Script;

public class InputScriptImpl extends InputScript {

    private static final long serialVersionUID = 1L;

    public InputScriptImpl() {
        super();
    }

    public InputScriptImpl(Script script) {
        super(script);
    }

    public InputScriptImpl(String serializedScript) {
        super(serializedScript);
    }

    @Override
    public boolean isP2SH() {
        return false;
    }

}
