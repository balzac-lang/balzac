/*
 * Copyright 2018 Nicola Atzei
 */
package it.unica.tcs.lib.model;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import it.unica.tcs.lib.model.ServerSocketDaemon.ServerSocketClient;

public class ServerSocketDaemonTest {

    @Test
    public void test_read_write() throws IOException, InterruptedException {
        ServerSocketDaemon server = new ServerSocketDaemon();

        // start the server
        server.start();

        List<Integer> expectedExecution = new ArrayList<>();
        
        // producer
        Thread t1 = new Thread(()-> {
            ServerSocketClient client;
            try {
                client = server.getClient();
                expectedExecution.add(1);
                client.write("1");
                expectedExecution.add(2);
                client.write("2");
                expectedExecution.add(3);
                client.write("3");
            } catch (IOException e) {} 
        });
        t1.start();

        // consumer
        Thread t2 = new Thread(()-> {
            try {
                expectedExecution.add(1);
                server.read();
                expectedExecution.add(2);
                server.read();
                expectedExecution.add(3);
                server.read();
            } catch (InterruptedException e) {}
        });
        t2.start();

        t1.join();
        t2.join();
        int prev = 0;
        for (int i : expectedExecution) {
            assertTrue("prev "+prev+", i "+i,i>=prev);
            prev = i;
        }
        
        server.stop();
    }
}
