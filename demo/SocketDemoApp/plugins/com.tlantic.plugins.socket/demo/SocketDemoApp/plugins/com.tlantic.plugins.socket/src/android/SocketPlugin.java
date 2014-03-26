package com.tlantic.plugins.socket;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;

public class SocketPlugin extends CordovaPlugin {

	private Map<String, Connection> pool = new HashMap<String,Connection>();
	
	
	@Override
	public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {

		if (action.equals("connect")) {
			this.connect(args, callbackContext);
			return true;
			
		}else if(action.equals("send")) {
			return true;
			
		} else if (action.equals("disconnect")) {
			this.disconnect(args, callbackContext);
			return true;
			
		} else if (action.equals("disconnectAll")) {
			return true;
			
		}  else {
			return false;
		}
	}
	
	private String buildKey(String host, int port) {
		return (host.toLowerCase() + ":" + port);
	}
	
	private void connect (JSONArray args, CallbackContext callbackContext) {
		String key;
		String host;
		int port;
		Connection socket;
		
		// validating parameters
		if (args.length() < 2) {
			callbackContext.error("Missing arguments when calling 'connect' action.");
		} else {
			
			// opening connection and adding into pool
			try {
				
				// preparing parameters
				host = args.getString(0);
				port = args.getInt(1);
				key = this.buildKey(host, port);
				
				// creating connection
				socket = new Connection(host, port);
				
				// checking connection
				if (socket.isConnected()) {
					// adding to pool
					this.pool.put(key, socket);
					callbackContext.success("Established connection with " + key);
				} else {
					callbackContext.error("Unable to establish connection with " + key);
				}
				
			} catch (JSONException e) {
				callbackContext.error("Invalid parameters for 'connect' action:" + e.getMessage());
				
			} catch (UnknownHostException e) {
				callbackContext.error("Unable to connect because the host is unknown: " + e.getMessage());
				
			} catch (IOException e) {
				callbackContext.error("Unexpected error when establishing connection: " + e.getMessage());	
			}
		}
	}
	
	private void send() {
	}
	
	private void disconnect (JSONArray args, CallbackContext callbackContext) {
		String key;
		String host;
		int port;
		Connection socket;
		
		// validating parameters
		if (args.length() < 2) {
			callbackContext.error("Missing arguments when calling 'disconnect' action.");
		} else {
			
			try {
				// preparing parameters
				host = args.getString(0);
				port = args.getInt(1);
				key = this.buildKey(host, port);
				
				// getting connection from pool
				socket = pool.get(key);
				
				// closing socket
				if (socket != null) {
					socket.close();
					pool.remove(key);
				}
				
				// ending with success
				callbackContext.success("Disconnected from " + key);
				
			} catch (JSONException e) {
				callbackContext.error("Invalid parameters for 'connect' action:" + e.getMessage());
			}
		}		
	}
	
	private void disconnectAll () {
	}
}