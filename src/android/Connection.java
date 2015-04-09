package com.tlantic.plugins.socket;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.Exception;
import java.lang.String;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import org.apache.cordova.CallbackContext;


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
	private BufferedReader reader;

	private Boolean mustClose;
	private String host;
	private int port;
    private CallbackContext callbackContext; //This is a bad practice. should be using handlers to send this to Socket Plugin
    private String buildKey;

    public String getBuildKey() {
        return buildKey;
    }

    public void setBuildKey(String buildKey) {
        this.buildKey = buildKey;
    }

    public CallbackContext getCallbackContext() {
        return callbackContext;
    }

    public void setCallbackContext(CallbackContext callbackContext) {
        this.callbackContext = callbackContext;
    }

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
     * Creates a TCP socket connection object.
     *
     * @param pool Object containing "sendMessage" method to be called as a callback for data receive.
     * @param host Target host for socket connection.
     * @param port Target port for socket connection
     * @param callbackContext for sending callbacks
     */
    public Connection(SocketPlugin pool, String host, int port, CallbackContext callbackContext, String buildKey) {
        this(pool, host, port);
        this.callbackContext = callbackContext;
        this.buildKey = buildKey;
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
	public boolean write(String data) {
		this.writer.println(data);
        this.writer.flush();
        return !this.writer.checkError();
	}



	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	public void run() {
		String chunk = null;

		// creating connection
		try {
            //This is a bad practice. should be using handlers to send this to SocketPlugin
			this.callbackSocket = new Socket();
            this.callbackSocket.connect(new InetSocketAddress(this.host, this.port), 20000);
            this.hook.sendConnectedEvent(this.buildKey, this.host, this.port, this);
            this.callbackContext.success(this.buildKey);
            this.writer = new PrintWriter(this.callbackSocket.getOutputStream(), true);
			this.reader = new BufferedReader(new InputStreamReader(callbackSocket.getInputStream()));

			// receiving data chunk
			while(!this.mustClose){

				try {

					if (this.isConnected()) {
						chunk = reader.readLine();

						if (chunk != null) {
							chunk = chunk.replaceAll("\"\"", "null");
							System.out.print("## RECEIVED DATA: " + chunk);
							hook.sendMessage(this.host, this.port, chunk);
						}
					}
				} catch (Exception e) {
                    try {
                        this.callbackSocket.close();
                    } catch (Exception closeException) {
                        closeException.printStackTrace();
                    }

                    hook.sendDisconnectedEvent(this.buildKey);
                    this.mustClose = true;
				}
			}

		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
            this.callbackContext.error(this.buildKey+" did not connect: unknown host");
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
            this.callbackContext.error(this.buildKey+" did not connect: io host");
			e1.printStackTrace();
		} catch (Exception el) {
            this.callbackContext.error(this.buildKey+" did not connect: unknown error");
            el.printStackTrace();
        }

	}

}
