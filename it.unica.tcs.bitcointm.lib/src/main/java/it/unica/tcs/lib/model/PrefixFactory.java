/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.lib.model;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import com.google.inject.Inject;

import it.unica.tcs.lib.client.BitcoinClientI;

public class PrefixFactory {

    @Inject BitcoinClientI client;

    Prefix ask(String... txsid) {
        return ask(Arrays.asList(txsid));
    }

    Prefix ask(List<String> txsid) {
        return new Ask(txsid, client);
    }

    Prefix check(Supplier<Boolean> condition) {
        return new Check(condition);
    }

    Prefix check() {
        return check(() ->true);
    }

    Prefix put(String txhex) {
        return new Put(txhex, client);
    }

}
