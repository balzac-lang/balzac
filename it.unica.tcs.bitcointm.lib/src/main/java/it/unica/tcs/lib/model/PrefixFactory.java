/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.lib.model;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class PrefixFactory {

    static Prefix ask(String... txsid) {
        return ask(Arrays.asList(txsid));
    }

    static Prefix ask(List<String> txsid) {
        return new Ask(txsid);
    }

    static Prefix check(Supplier<Boolean> condition) {
        return new Check(condition);
    }

    static Prefix check() {
        return check(() ->true);
    }

    static Prefix put(String txhex) {
        return new Put(txhex);
    }

}
