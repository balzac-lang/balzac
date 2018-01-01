/*
 * Copyright 2017 Nicola Atzei
 */

package it.unica.tcs.lib.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerSocketDaemon implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(ServerSocketDaemon.class);

    private BlockingQueue<String> buffer = new SynchronousQueue<>(true);
    private int port;
    private boolean online;

    public ServerSocketDaemon(int port) {
        this.port = port;
        this.online = false;
    }

    /**
     * Return the server port. If might be different from the {@code port} parameter
     * given in the constructor.
     * @return the server port
     */
    public int getPort() {
        return port;
    }

    /**
     * Block until the server is online and waiting for connections.
     * @throws InterruptedException if the thread is interrupted
     */
    public void waitUntilOnline() throws InterruptedException {
        while(!online) {
            Thread.sleep(500);
        }
    }

    /**
     * Read a value. If there is no value to read,
     * it blocks until another thread will send a value through a socket connection.
     * @return the read value
     * @throws InterruptedException if the thread is interrupted
     */
    public String read() throws InterruptedException {
        return buffer.take();
    }

    /**
     * Read a value. If there is no value to read,
     * it blocks until another thread will send a value through a socket connection
     * or the given timeout expires.
     * @param timeout timeout value
     * @param unit time-unit for the given timeout
     * @return the read value
     * @throws InterruptedException if the thread is interrupted
     * @throws TimeoutException if the value cannot be read before the timeout
     */
    public String read(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        String value = buffer.poll(timeout, unit);
        if (value == null)
            throw new TimeoutException();
        return value;
    }

    public boolean isReady() {
        return !buffer.isEmpty();
    }

    @Override
    public void run() {

        try (
            ServerSocket server = new ServerSocket(port);
        ) {
            server.setSoTimeout(3000);
            this.port = server.getLocalPort();  // if the port is 0, a free port is assigned by the system
            this.online = true;

            logger.trace("daemon server started at port "+port);

            while (true) {

                if (Thread.currentThread().isInterrupted()) {
                    logger.trace("received interrupt signal, exiting... ");
                    return;
                }

                logger.trace("["+this.toString()+"] waiting for connection");
                try (
                        Socket clientSocket = server.accept();
                        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                        PrintWriter out = new PrintWriter(clientSocket.getOutputStream())
                ) {
                    logger.trace("connected to remote port "+clientSocket.getPort());
                    logger.trace("waiting for input");

                    String inputLine = in.readLine();

                    logger.trace("writing '"+inputLine+"' buffer");
                    buffer.put(inputLine.toString());

                    logger.trace("connection to port "+clientSocket.getPort()+" closed");
                }
                catch (InterruptedIOException e) {
                    logger.trace("Timeout...");
                }
                catch (IOException e) {
                    logger.error("Exception caught when trying to listen on port " + server.getLocalPort() + " or listening for a connection. Error message: "+e.getMessage());
                }
                catch (InterruptedException e) {
                    logger.trace("received interrupt signal, exiting... ");
                    return;
                }
            }

        } catch (IOException e) {
            logger.error("unable to start the server: "+e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
