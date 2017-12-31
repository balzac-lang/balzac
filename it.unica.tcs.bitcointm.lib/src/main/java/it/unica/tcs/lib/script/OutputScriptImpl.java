/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.lib.script;

import org.bitcoinj.script.Script;

public class OutputScriptImpl extends OutputScript {

    private static final long serialVersionUID = 1L;

    public OutputScriptImpl() {
        super();
    }

    public OutputScriptImpl(Script script) {
        super(script);
    }

    public OutputScriptImpl(String serializedScript) {
        super(serializedScript);
    }

    @Override
    public boolean isP2PKH() {
        return build().isSentToAddress();
    }

    @Override
    public boolean isOP_RETURN() {
        return build().isOpReturn();
    }

    @Override
    public boolean isP2SH() {
        return build().isPayToScriptHash();
    }

}
