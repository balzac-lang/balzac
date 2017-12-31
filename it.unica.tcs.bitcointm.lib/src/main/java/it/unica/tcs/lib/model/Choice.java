/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.lib.model;

public class Choice implements Runnable {

    private final Prefix[] prefixes;
    private final static int POLLING_DELAY = 1000;
    
    public Choice(Prefix... prefixes) {
        this.prefixes = prefixes;
    }

    @Override
    public void run() {
        
        for (int i=0; ; i++) {
            Prefix p = prefixes[i];
            if (p.ready()) {
                p.run();
                break;
            }
            
            if (i==prefixes.length-1) {
                i=-1;
                silentSleep();
            }
        }
    }

    
    private void silentSleep() {
        try {
            Thread.sleep(POLLING_DELAY);
        } catch (InterruptedException e) {}
    }
}
