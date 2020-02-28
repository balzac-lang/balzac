/*
 * Copyright 2019 Nicola Atzei
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package xyz.balzaclang.lib.utils;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import xyz.balzaclang.lib.utils.ServerSocketDaemon;
import xyz.balzaclang.lib.utils.ServerSocketDaemon.ServerSocketClient;

@Ignore
public class ServerSocketDaemonTest {

    @Test
    public void test_read_write() throws IOException, InterruptedException {
        ServerSocketDaemon server = new ServerSocketDaemon();

        // start the server
        server.start();

        List<Integer> expectedExecution = new ArrayList<>();

        // producer
        Thread t1 = new Thread(() -> {
            ServerSocketClient client;
            try {
                client = server.getClient();
                expectedExecution.add(1);
                client.write("1");
                expectedExecution.add(2);
                client.write("2");
                expectedExecution.add(3);
                client.write("3");
            } catch (IOException e) {
            }
        });
        t1.start();

        // consumer
        Thread t2 = new Thread(() -> {
            try {
                expectedExecution.add(1);
                server.read();
                expectedExecution.add(2);
                server.read();
                expectedExecution.add(3);
                server.read();
            } catch (InterruptedException e) {
            }
        });
        t2.start();

        t1.join();
        t2.join();
        int prev = 0;
        for (int i : expectedExecution) {
            assertTrue("prev " + prev + ", i " + i, i >= prev);
            prev = i;
        }

        server.stop();
    }
}
