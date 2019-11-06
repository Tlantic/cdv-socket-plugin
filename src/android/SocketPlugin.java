package com.tlantic.plugins.socket;

import android.telecom.Call;
import android.util.Base64;
import android.annotation.SuppressLint;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;

/**
 * @author viniciusl
 * <p>
 * Plugin to handle TCP socket connections.
 */

/**
 * @author viniciusl
 */
public class SocketPlugin extends CordovaPlugin {

    // pool of "active" connections
    private Map<String, Connection> pool = new HashMap<String, Connection>();
    private Map<String, ConnectionExpress> poolExpress = new HashMap<String, ConnectionExpress>();

    /* (non-Javadoc)
     * @see org.apache.cordova.CordovaPlugin#execute(java.lang.String, org.json.JSONArray, org.apache.cordova.CallbackContext)
     */
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("connect")) {
            this.connect(args, callbackContext);
            return true;

        } else if (action.equals("send")) {
            this.send(args, callbackContext);
            return true;

        } else if (action.equals("sendExpress")) {
            this.sendExpress(args, callbackContext);
            return true;

        } else if (action.equals("disconnect")) {
            this.disconnect(args, callbackContext);
            return true;

        } else if (action.equals("disconnectAll")) {
            this.disconnectAll(callbackContext);
            return true;

        } else {
            return false;
        }
    }

    /**
     * Closes an existing connection express
     */
    public void disconnectExpress(String host, int port, CallbackContext callbackContext, boolean ending) {
        String key = this.buildKey(host, port);
        ConnectionExpress socket;

        try {
            // getting connection from pool
            socket = poolExpress.get(key);

            // closing socket
            if (socket != null) {

                // checking connection
                if (socket.isConnected()) {
                    socket.close();
                }

                // removing from pool
                poolExpress.remove(key);
            }

            if (ending) {
                // ending with success
                callbackContext.success("Disconnected!");
            }
        } catch (Exception e) {
            callbackContext.error("Invalid parameters for 'connect' action:" + e.getMessage());
        }
    }

    /**
     * Build a key to identify a socket connection based on host and port information.
     *
     * @param host Target host
     * @param port Target port
     * @return connection key
     */
    @SuppressLint("DefaultLocale")
    private String buildKey(String host, int port) {
        return (host.toLowerCase() + ":" + port);
    }

    /**
     * Opens a socket connection.
     *
     * @param args
     * @param callbackContext
     */
    private void connect(JSONArray args, CallbackContext callbackContext) {
        String key;
        String host;
        int port;
        String charset = null;
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

                if (!args.isNull(2)) {
                    charset = args.getString(2);
                }

                // creating connection
                if (this.pool.get(key) == null) {
                    socket = new Connection(this, host, port);

                    if (charset != null) {
                        socket.setCharset(charset);
                    }

                    socket.start();
                    this.pool.put(key, socket);
                }

                // adding to pool
                callbackContext.success(key);

            } catch (JSONException e) {
                callbackContext.error("Invalid parameters for 'connect' action: " + e.getMessage());
            }
        }
    }

    /**
     * Send information to target host
     *
     * @param args
     * @param callbackContext
     */
    private void send(JSONArray args, CallbackContext callbackContext) {
        Connection socket;

        // validating parameters
        if (args.length() < 2) {
            callbackContext.error("Missing arguments when calling 'send' action.");
        } else {
            try {
                // retrieving parameters
                String key = args.getString(0);
                String data = args.getString(1);
                String format = args.getString(2);

                // getting socket
                socket = this.pool.get(key);

                // checking if socket was not found and his connectivity
                if (socket == null) {
                    callbackContext.error("No connection found with host " + key);

                } else if (!socket.isConnected()) {
                    callbackContext.error("Invalid connection with host " + key);

                } else if (data.length() == 0) {
                    callbackContext.error("Cannot send empty data to " + key);

                } else {
                    if (format.equals("base64")) {
                        String charset = socket.getCharset();

                        if (charset == null) {
                            charset = "UTF-8";
                        }

                        byte[] decodedData = Base64.decode(data, Base64.DEFAULT);
                        CharBuffer charBuffer = Charset.forName(charset).decode(ByteBuffer.wrap(decodedData));
                        data = String.valueOf(charBuffer);
                    }

                    // write on output stream
                    socket.write(data);

                    // ending send process
                    callbackContext.success();
                }
            } catch (JSONException e) {
                callbackContext.error("Unexpected error sending information: " + e.getMessage());
            }
        }
    }

    /**
     * Opens a socket connection, send information to target host and closes the connection
     *
     * @param args
     * @param callbackContext
     */
    private void sendExpress(JSONArray args, CallbackContext callbackContext) {
        try {
            String host = args.getString(0);
            int port = args.getInt(1);
            String key = this.buildKey(host, port);
            String data = args.getString(2);
            String charset = null;
            String format = null;
            ConnectionExpress socket;

            // validating parameters
            if (args.length() < 3) {
                callbackContext.error("Missing arguments when calling 'connect' action.");
            } else if (data.length() == 0) {
                callbackContext.error("Cannot send empty data to " + key);
            } else {
                // opening connection and adding into poolExpress
                if (!args.isNull(3)) {
                    charset = args.getString(3);
                }

                if (!args.isNull(4)) {
                    format = args.getString(4);
                }

                // creating connection
                if (this.poolExpress.get(key) == null) {
                    socket = new ConnectionExpress(this, host, port, data, charset, format, callbackContext);
                    socket.start();
                    this.poolExpress.put(key, socket);
                }
            }
        } catch (Exception e) {
            callbackContext.error("Invalid parameters for 'connect' action: " + e.getMessage());
        }
    }

    /**
     * Closes an existing connection
     *
     * @param args
     * @param callbackContext
     */
    private void disconnect(JSONArray args, CallbackContext callbackContext) {
        String key;
        Connection socket;

        // validating parameters
        if (args.length() < 1) {
            callbackContext.error("Missing arguments when calling 'disconnect' action.");
        } else {

            try {
                // preparing parameters
                key = args.getString(0);

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

    /**
     * Closes all existing connections
     *
     * @param callbackContext
     */
    private void disconnectAll(CallbackContext callbackContext) {
        // building iterator
        Iterator<Entry<String, Connection>> it = this.pool.entrySet().iterator();

        while (it.hasNext()) {

            // retrieving object
            Entry<String, Connection> pairs = (Entry<String, Connection>) it.next();
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


    /**
     * Callback for Connection object data receive. Relay information to javascript object method: window.tlantic.plugins.socket.receive();
     *
     * @param host
     * @param port
     * @param chunk
     */
    public synchronized void sendMessage(String host, int port, String chunk) {
        final String receiveHook = "window.tlantic.plugins.socket.receive('" + host + "'," + port + ",'" + this.buildKey(host, port) + "','" + chunk + "');";

        cordova.getActivity().runOnUiThread(new Runnable() {

            @Override
            public void run() {
                webView.loadUrl("javascript:" + receiveHook);
            }

        });
    }

}
