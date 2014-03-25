package com.tlantic.plugins.socket;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

/*
 * 
 */
public class Connection extends Thread {

	private Socket callbackSocket;
	private Boolean mustClose;
	
	/*
	 * 
	 */
	public Connection(String host, int port) throws UnknownHostException, IOException {
		super("Establishing connection with " + host + " on port " + port);
		setDaemon(true);
		
		this.mustClose = false;
		this.callbackSocket = new Socket(host, port);
	}
	
	/*
	 * 
	 */
	public boolean isConnected() {
		return this.callbackSocket.isConnected();
	}
	
	/*
	 * 
	 */
	public void close() {
		this.mustClose = true;
	}
	/*
	 * 
	 */
	public void run() {
		
		// receiving data chunk
		while(!this.mustClose){
		}
		
		
		// closing connection
		try {
			callbackSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
}
