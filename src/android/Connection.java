package com.tlantic.plugins.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;


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
	private String charset;


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
	 * Get the charset name used in OutputStreamWriter
	 * @return String return charset name if socket was instantiated with that charset
	 */
	public String getCharset() {
		return this.charset;
	}

	/**
	 * Set the charset name for create an OutputStreamWriter that uses that charset
	 * 
	 * @param charset String the charset to use
	 */
	public void setCharset(String charset) {
		this.charset = charset;
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



	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	public void run() {
		String chunk = null;

		// creating connection
		try {
			this.callbackSocket = new Socket(this.host, this.port);

			if (this.charset != null) {
				this.writer = new PrintWriter(new OutputStreamWriter(callbackSocket.getOutputStream(), Charset.forName(this.charset)), true);
			} else {
				this.writer = new PrintWriter(callbackSocket.getOutputStream(), true);
			}

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
