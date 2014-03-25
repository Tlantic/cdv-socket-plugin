package com.tlantic.plugins.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import org.apache.cordova.CordovaWebView;

/*
 * 
 */
public class Connection extends Thread {
	private SocketPlugin hook;
	
	private Socket callbackSocket;
	private PrintWriter writer;
	private BufferedReader reader;
	
	private Boolean mustClose;
	private String host;
	private int port;
	
	/*
	 * 
	 */
	public Connection(SocketPlugin pool, String host, int port) {
		super();
		setDaemon(true);
		
		this.mustClose = false;
		this.host = host;
		this.port = port;
		this.hook = pool;
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
	public void write(String data) {
		this.writer.println(data);
	}
	
	/*
	 * 
	 */
	public void run() {
		String chunk = null;
		
		// creating connection
		try {
			
			this.callbackSocket = new Socket(this.host, this.port);
			this.writer = new PrintWriter(this.callbackSocket.getOutputStream(), true);
			this.reader = new BufferedReader(new InputStreamReader(callbackSocket.getInputStream()));
			
		} catch (IOException e) {
			e.printStackTrace();
			this.close();
		}
		
		// receiving data chunk
		while(!this.mustClose){
			
			try {
				chunk = reader.readLine().replaceAll("\"\"", "null");
				System.out.print("## RECEIVED DATA: " + chunk);
				hook.sendMessage(this.host, this.port, chunk);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		
		// closing connection
		try {
			callbackSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
}
