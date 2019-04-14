/*
 * Copyright 2019 Nicola Atzei
 */
package it.unica.tcs.lib.model.script;

import static com.google.common.base.Preconditions.checkState;

import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;

public abstract class InputScript extends AbstractScriptBuilderWithVar<InputScript> {

    private static final long serialVersionUID = 1L;

    public boolean isP2SH() {
        return this instanceof P2SHInputScript;
    }

    abstract public OutputScript getRedeemScript();

    public String getType() {
        return isP2SH()? "P2SH": "STANDARD";
    }

    public static InputScript create() {
        return new InputScript() {
            private static final long serialVersionUID = 1L;

            @Override
            public OutputScript getRedeemScript() {
                throw new IllegalStateException("Not a P2SH. Use InputScript.createP2SH");
            }
        };
    }

    public static InputScript createP2SH(OutputScript redeemScript) {
        return new P2SHInputScript(redeemScript);
    }

    private static class P2SHInputScript extends InputScript {

        private static final long serialVersionUID = 1L;

        private final OutputScript redeemScript;

        private P2SHInputScript(OutputScript redeemScript) {
            this.redeemScript = redeemScript;
        }

        @Override
        public Script build() {
            checkState(redeemScript.isReady(), "redeemScript is not ready");
            return new ScriptBuilder(super.build()).data(redeemScript.build().getProgram()).build();
        }

        @Override
        public boolean isReady() {
            return super.isReady() && redeemScript.isReady();
        }

        public OutputScript getRedeemScript() {
            return redeemScript;
        }

        @Override
        public boolean isP2SH() {
            return true;
        }

        @Override
        public String toString() {
            return super.toString()+" <"+redeemScript.toString()+">";
        }
    }
}
