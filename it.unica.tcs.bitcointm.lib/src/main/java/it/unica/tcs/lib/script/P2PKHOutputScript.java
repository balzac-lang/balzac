/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.lib.script;

import static com.google.common.base.Preconditions.checkState;

import org.bitcoinj.core.Address;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;

public class P2PKHOutputScript extends OutputScript {

    private static final long serialVersionUID = 1L;

    public P2PKHOutputScript() {
        super();
    }
    
    public P2PKHOutputScript(Script script) {
        super(script);
        checkState(script.isSentToAddress());
    }
    
    public P2PKHOutputScript(Address addr) {
        super(ScriptBuilder.createOutputScript(addr));
    }
    
    @Override
    public boolean isP2SH() {
        return false;
    }

    @Override
    public boolean isP2PKH() {
        return true;
    }

    @Override
    public boolean isOP_RETURN() {
        return false;
    }
}
