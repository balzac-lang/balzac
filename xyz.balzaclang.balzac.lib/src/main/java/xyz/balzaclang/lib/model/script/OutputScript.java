/*
 * Copyright 2019 Nicola Atzei
 */
package xyz.balzaclang.lib.model.script;

import static com.google.common.base.Preconditions.checkState;
import static org.bitcoinj.script.ScriptOpCodes.OP_CHECKSIG;
import static org.bitcoinj.script.ScriptOpCodes.OP_DUP;
import static org.bitcoinj.script.ScriptOpCodes.OP_EQUALVERIFY;
import static org.bitcoinj.script.ScriptOpCodes.OP_HASH160;

import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.script.ScriptPattern;

import xyz.balzaclang.lib.model.Address;

public abstract class OutputScript extends AbstractScriptBuilderWithVar<OutputScript> {

    private static final long serialVersionUID = 1L;

    abstract public Script getOutputScript();
    abstract public boolean isP2SH();
    abstract public boolean isP2PKH();
    abstract public boolean isOP_RETURN();

    public String getType() {
        if (isP2SH())
            return "P2SH";
        if (isP2PKH())
            return "P2PKH";
        if (isOP_RETURN())
            return "OP_RETURN";
        throw new IllegalStateException();
    }

    public static OutputScript createP2SH() {
        return new OutputScript() {
            private static final long serialVersionUID = 1L;

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

            @Override
            public Script getOutputScript() {
                checkState(isReady(), "redeemScript is not ready");
                return ScriptBuilder.createP2SHOutputScript(super.build());
            }
        };
    }

    public static OutputScript createP2PKH(Script script) {
        checkState(ScriptPattern.isP2PKH(script));
        byte[] address = script.getChunks().get(2).data;
        return createP2PKH(address);
    }

    public static OutputScript createP2PKH(Address addr) {
        return createP2PKH(addr.getBytes());
    }

    public static OutputScript createP2PKH(byte[] addressByte) {
        return new OutputScript() {
            private static final long serialVersionUID = 1L;

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

            @Override
            public Script getOutputScript() {
                return new ScriptBuilder()
                        .op(OP_DUP)
                        .op(OP_HASH160)
                        .data(addressByte)
                        .op(OP_EQUALVERIFY)
                        .op(OP_CHECKSIG).build();
            }

            @Override
            public Script build() {
                return getOutputScript();
            }
        };
    }

    public static OutputScript createOP_RETURN(Script script) {
        checkState(ScriptPattern.isOpReturn(script));
        return createOP_RETURN(script.getProgram());
    }

    public static OutputScript createOP_RETURN(byte[] bytes) {
        return new OutputScript() {
            private static final long serialVersionUID = 1L;

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

            @Override
            public Script getOutputScript() {
                return ScriptBuilder.createOpReturnScript(bytes);
            }

            @Override
            public Script build() {
                return getOutputScript();
            }
        };
    }
}
