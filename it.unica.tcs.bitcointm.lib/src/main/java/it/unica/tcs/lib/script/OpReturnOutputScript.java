/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.lib.script;

import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import static  com.google.common.base.Preconditions.checkState;

public class OpReturnOutputScript extends OutputScript {

    private static final long serialVersionUID = 1L;

    public OpReturnOutputScript() {
        super();
    }

    public OpReturnOutputScript(Script script) {
        super(script);
        checkState(script.isOpReturn());
    }

    public OpReturnOutputScript(byte[] bytes) {
        super(ScriptBuilder.createOpReturnScript(bytes));
    }

    @Override
    public boolean isP2SH() {
        return false;
    }

    @Override
    public boolean isP2PKH() {
        return false;
    }

    @Override
    public boolean isOP_RETURN() {
        return true;
    }
}
