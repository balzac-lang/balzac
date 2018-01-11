/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.lib.model;

import static com.google.common.base.Preconditions.checkState;

import java.util.List;
import java.util.stream.Collectors;

import it.unica.tcs.lib.client.BitcoinClientI;

public class Ask extends AbstractPrefix {

    private BitcoinClientI client;
    private final List<String> txsid;

    Ask(List<String> txsid, BitcoinClientI client) {
        this.txsid = txsid;
        this.client = client;
    }

    @Override
    public boolean ready() {
        return txsid.stream().allMatch((tx) -> client.isMined(tx));
    }

    @Override
    public void run() {
        checkState(ready());
    }

    @Override
    public String toString(){
        return "ask {"+txsid.stream().collect(Collectors.joining(","))+"}";
    }
}
