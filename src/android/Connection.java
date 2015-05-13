package com.tlantic.plugins.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;


/**
 * @author viniciusl
 *
 * This class represents a socket connection, behaving like a thread to listen
 * a TCP port and receive data
 */
public class Connection extends Thread {
    private SocketPlugin hook;

    private Socket callbackSocket;
    private PrintWriter writer;
    private OutputStream outputStream;
    private BufferedReader reader;

    private Boolean mustClose;
    private String host;
    private int port;


    /**
     * Creates a TCP socket connection object.
     *
     * @param pool Object containing "sendMessage" method to be called as a callback for data receive.
     * @param host Target host for socket connection.
     * @param port Target port for socket connection
     */
    public Connection(SocketPlugin pool, String host, int port) {
        super();
        setDaemon(true);

        this.mustClose = false;
        this.host = host;
        this.port = port;
        this.hook = pool;
    }


    /**
     * Returns socket connection state.
     *
     * @return true if socket connection is established or false case else.
     */
    public boolean isConnected() {

        boolean result =  (
                this.callbackSocket == null ? false :
                    this.callbackSocket.isConnected() &&
                    this.callbackSocket.isBound() &&
                    !this.callbackSocket.isClosed() &&
                    !this.callbackSocket.isInputShutdown() &&
                    !this.callbackSocket.isOutputShutdown());

        // if everything apparently is fine, time to test the streams
        if (result) {
            try {
                this.callbackSocket.getInputStream().available();
            } catch (IOException e) {
                // connection lost
                result = false;
            }
        }

        return result;
    }

    /**
     * Closes socket connection.
     */
    public void close() {
        // closing connection
        try {
            //this.writer.close();
            //this.reader.close();
            callbackSocket.shutdownInput();
            callbackSocket.shutdownOutput();
            callbackSocket.close();
            this.mustClose = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Writes on socket output stream to send data to target host.
     *
     * @param data information to be sent
     */
    public void write(String data) {
        this.writer.println(data);
    }



    /**
     * Outputs to socket output stream to send binary data to target host.
     *
     * @param data information to be sent
     */
    public void writeBinary(byte[] data) throws IOException {
        this.outputStream.write(data);
    }


    /* (non-Javadoc)
     * @see java.lang.Thread#run()
     */
    public void run() {
    byte[] chunk = new byte[512];

        // creating connection
        try {
            this.callbackSocket = new Socket(this.host, this.port);
            this.writer = new PrintWriter(this.callbackSocket.getOutputStream(), true);
            this.outputStream = this.callbackSocket.getOutputStream();
            this.reader = new BufferedReader(new InputStreamReader(callbackSocket.getInputStream()));

            // receiving data chunk
            while(!this.mustClose){

                try {

                    if (this.isConnected()) {
                        int bytesRead = callbackSocket.getInputStream().read(chunk);
                        byte[] line = new byte[bytesRead];
                        System.arraycopy(chunk, 0, line, 0, bytesRead);

                        if (bytesRead > 0) {
                            hook.sendMessage(this.host, this.port, line);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } catch (UnknownHostException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

    }

}
