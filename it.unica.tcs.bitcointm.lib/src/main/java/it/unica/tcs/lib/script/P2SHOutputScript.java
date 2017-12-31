/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.lib.script;

import static com.google.common.base.Preconditions.checkState;

import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;

public class P2SHOutputScript extends OutputScript {

    private static final long serialVersionUID = 1L;

    public P2SHOutputScript() {
        super();
    }

    public P2SHOutputScript(Script script) {
        super(script);
    }

    public Script getOutputScript() {
        checkState(isReady(), "redeemScript is not ready");
        return ScriptBuilder.createP2SHOutputScript(super.build());
    }

    @Override
    public boolean isP2SH() {
        return true;
    }

    @Override
    public boolean isP2PKH() {
        return false;
    }

    @Override
    public boolean isOP_RETURN() {
        return false;
    }

}
