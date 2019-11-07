package com.tlantic.plugins.socket;

import android.util.Base64;

import java.net.Socket;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

import org.apache.cordova.CallbackContext;

/**
 * @author diogoqueiros
 *
 * This class represents a socket connection, behaving like a thread to listen
 * a TCP port and receive data
 */
public class ConnectionExpress extends Thread {
    private SocketPlugin socketPlugin;

    private Socket callbackSocket;
    private PrintWriter writer;

    private String host;
    private int port;
    private String data;
    private String charset;
    private String format;
    private CallbackContext callbackContext;

    /**
     * Creates a TCP socket connection object.
     */
    public ConnectionExpress(SocketPlugin pool, String host, int port, String data, String charset, String format, CallbackContext callbackContext) {
        super();
        setDaemon(true);

        this.host = host;
        this.port = port;
        this.data = data;
        this.charset = charset;
        this.format = format;
        this.callbackContext = callbackContext;
        this.socketPlugin = pool;
    }

    /**
     * Returns socket connection state.
     *
     * @return true if socket connection is established or false case else.
     */
    public boolean isConnected() {
        return (
            this.callbackSocket == null ? false : this.callbackSocket.isConnected() &&
            this.callbackSocket.isBound() &&
            !this.callbackSocket.isClosed() &&
            !this.callbackSocket.isOutputShutdown()
        );
    }

    /**
     * Closes socket connection.
     */
    public void close() {
        // closing connection
        try {
            this.callbackSocket.shutdownOutput();
            this.callbackSocket.close();
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

    /* (non-Javadoc)
     * @see java.lang.Thread#run()
     */
    public void run() {
        // creating connection
        try {
            this.callbackSocket = new Socket(this.host, this.port);

            if (this.charset != null) {
                this.writer = new PrintWriter(new OutputStreamWriter(callbackSocket.getOutputStream(), Charset.forName(this.charset)), true);
            } else {
                this.writer = new PrintWriter(callbackSocket.getOutputStream(), true);
            }

            if (!this.isConnected()) {
                this.socketPlugin.disconnectExpress(this.host, this.port, this.callbackContext, false);
                this.callbackContext.error("Socket not connected");
            } else {
                if (this.format != null && this.format.equals("base64")) {
                    if (this.charset == null) {
                        this.charset = "UTF-8";
                    }

                    byte[] decodedData = Base64.decode(this.data, Base64.DEFAULT);
                    CharBuffer charBuffer = Charset.forName(this.charset).decode(ByteBuffer.wrap(decodedData));
                    this.data = String.valueOf(charBuffer);
                }

                this.write(this.data);
            }

            this.socketPlugin.disconnectExpress(this.host, this.port, this.callbackContext, true);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            this.socketPlugin.disconnectExpress(this.host, this.port, this.callbackContext, false);
            this.callbackContext.error("Invalid parameters for 'connect' action:" + e.getMessage());
        }
    }

}
