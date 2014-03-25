package com.tlantic.plugins.socket;

import android.annotation.SuppressLint;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

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
			this.send(args, callbackContext);
			return true;

		} else if (action.equals("disconnect")) {
			this.disconnect(args, callbackContext);
			return true;

		} else if (action.equals("disconnectAll")) {
			this.disconnectAll(callbackContext);
			return true;

		}  else {
			return false;
		}
	}

	@SuppressLint("DefaultLocale")
	private String buildKey(String host, int port) {
		return (host.toLowerCase() + ":" + port);
	}
	
	private Connection getSocket(String host, int port) {
		String key = this.buildKey(host, port);
		return this.pool.get(key);
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
				socket = new Connection(this, host, port);
				socket.start();

				// adding to pool
				this.pool.put(key, socket);
				callbackContext.success("Established connection with " + key);

			} catch (JSONException e) {
				callbackContext.error("Invalid parameters for 'connect' action:" + e.getMessage());
			} 
		}
	}

	private void send(JSONArray args, CallbackContext callbackContext) {
		Connection socket;
		
		// validating parameters
		if (args.length() < 3) {
			callbackContext.error("Missing arguments when calling 'send' action.");
		} else {
			try {
				// getting socket and writting on output stream
				socket = this.getSocket(args.getString(0), args.getInt(1));
				socket.write(args.getString(2));
				
				// ending send process
				callbackContext.success();
				
			} catch (JSONException e) {
				callbackContext.error("Unexpected error sending information: " + e.getMessage());
			}
		}
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
					
					// checking connection
					if (socket.isConnected()) {
						socket.close();
					}
					
					// removing from pool
					pool.remove(key);
				}

				// ending with success
				callbackContext.success("Disconnected from " + key);

			} catch (JSONException e) {
				callbackContext.error("Invalid parameters for 'connect' action:" + e.getMessage());
			}
		}		
	}

	private void disconnectAll (CallbackContext callbackContext) {
		// building iterator
		Iterator<Entry<String, Connection>> it = this.pool.entrySet().iterator();
		
		while( it.hasNext() ) {
			
			// retrieving object
			Map.Entry<String, Connection> pairs = (Entry<String, Connection>) it.next();
			Connection socket = pairs.getValue();
			
			// checking connection
			if (socket.isConnected()) {
				socket.close();
			}
			
			// removing from pool
			this.pool.remove(pairs.getKey());
		}
		
		callbackContext.success("All connections were closed.");
	}

	/*
	 * 
	 */
	public synchronized void sendMessage(String host, int port, String chunk) {
		final String receiveHook = "window.tlantic.plugins.socket.receive('" + host + "'," + port + ",'" + chunk + "');";
		
		cordova.getActivity().runOnUiThread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				webView.loadUrl("javascript:" + receiveHook);
			}
			
		});
	}
	
}