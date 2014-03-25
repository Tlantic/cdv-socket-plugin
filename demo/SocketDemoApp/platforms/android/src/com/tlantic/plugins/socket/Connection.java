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
	private String host;
	private int port;
	
	/*
	 * 
	 */
	public Connection(String host, int port) {
		super();
		setDaemon(true);
		
		this.mustClose = false;
		this.host = host;
		this.port = port;
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
		
		// creating connection
		try {
			this.callbackSocket = new Socket(this.host, this.port);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
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
