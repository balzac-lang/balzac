/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.lib.model;

import static com.google.common.base.Preconditions.checkState;

public class Receive extends AbstractPrefix {

    Receive() {}

    @Override
    public boolean ready() {
        return false;
    }

    @Override
    public void run() {
        checkState(ready());
    }

}
