/*
 * Copyright 2018 Nicola Atzei
 */

package xyz.balzaclang.lib.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerSocketDaemon  implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(ServerSocketDaemon.class);

    private BlockingQueue<String> buffer = new SynchronousQueue<>(true);
    private int port;
    private boolean online;
    private Thread daemonThread;

    /**
     * Create a new daemon. The port is chosen randomly and will be set after the startup process.
     * When {@link #isOnline()} returns true, then the port is set and cannot change during the daemon lifecycle.
     */
    public ServerSocketDaemon() {
        this(0);
    }

    /**
     * Create a new daemon a the specified port number.
     * @param port the port that the daemon will use.
     */
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
     * Check if the server is running.
     * @return true if the server started correctly, false otherwise.
     */
    public boolean isOnline() {
        return online;
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
        logger.trace("reading value");
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

    /**
     * Start a new thread that executes the server.
     */
    public void start() {
        logger.info("Starting...");
        if (daemonThread != null)
            throw new IllegalArgumentException("Daemon already started");

        daemonThread = new Thread(this, "daemon");
        daemonThread.start();
        try {
            waitUntilOnline();
        } catch (InterruptedException e) {
            logger.info("Interrupt signal!");
            stop();
            return;
        }
        logger.info("Server started");
    }

    /**
     * Stop the server through an interrupt signal.
     */
    public void stop() {
        logger.info("Shutting down...");
        daemonThread.interrupt();
        logger.info("Stop");
    }

    /**
     * Creates a new ServerSocket.
     * Repeatedly wait for a new connection.
     * <p>One connection allows to receive <b>just one string</b> at once.</p>
     * <p>No multiple connections are allowed.</p>
     * <p>The communication is synchronous. Once a connection is established,
     * it waits indefinitely to receive a message. When a message is received,
     * the server waits that someone read that value.</p>
     */
    @Override
    public void run() {

        try (
            ServerSocket server = new ServerSocket(port);
        ) {
            server.setSoTimeout(3000);
            this.port = server.getLocalPort();  // if the port is 0, a free port is assigned by the system
            this.online = true;

            logger.trace("server started at port "+port);

            while (true) {

                if (Thread.currentThread().isInterrupted()) {
                    logger.trace("received interrupt signal, exiting... ");
                    return;
                }

                logger.trace("waiting for connection");
                try (
                        Socket clientSocket = server.accept();
                        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                ) {
                    logger.trace("new connection from {}:{} ", clientSocket.getInetAddress(), clientSocket.getPort());
                    logger.trace("waiting for input");

                    String inputLine = in.readLine();
                    logger.trace("writing '"+inputLine+"' to buffer");
                    buffer.put(inputLine.toString());

                    logger.trace("<-/- "+clientSocket.getPort()+" : connection closed");
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

    /**
     * Return a Socket for this daemon. The socket is creating using
     * {@code InetAddress.getLocalHost()} as host.
     * @return a socket.
     * @throws IOException if an I/O error occurs when creating the socket.
     */
    public Socket getSocket() throws IOException {
        Socket socket = new Socket(InetAddress.getLocalHost(), getPort());
        socket.setKeepAlive(true);
        return socket;
    }

    /**
     * Return a client that allows to write to this server.
     * @return the client.
     * @throws IOException if an I/O error occurs creating the socket.
     */
    public ServerSocketClient getClient() throws IOException {
        return new ServerSocketClient(this);
    }

    public static class ServerSocketClient {

        private static final Logger logger = LoggerFactory.getLogger(ServerSocketClient.class);
        private final ServerSocketDaemon daemon;

        private ServerSocketClient(ServerSocketDaemon daemon) throws IOException {
            this.daemon = daemon;
        }

        public void write(String str) throws IOException {
            Socket socket = daemon.getSocket();

            logger.trace("connected to {}:{}, exit port {} ", socket.getInetAddress(), socket.getPort(), socket.getLocalPort());
            try (
                    PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    ) {
                writer.println(str);
                reader.readLine();      // wait until the server close the connection
            }
        }
    }
}
