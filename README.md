cdv-socket-plugin
=================

Cordova TCP Socket Plugin


Follows the [Cordova Plugin spec](https://github.com/apache/cordova-plugman/blob/master/plugin_spec.md), so that it works with [Plugman](https://github.com/apache/cordova-plugman)

## Cordova/Phonegap Support ##

This plugin was tested and qualified using Cordova 3.4. The demo app contains an Android 4.1.1 app, used for plugin testing.

## Adding the plugin ##

To add the plugin, just run the following command through cordova CLI:

```
cordova plugin add https://github.com/Tlantic/cdv-socket-plugin
```

## Using the plugin ##

The plugin creates a "Socket" object exposed on window.tlantic.plugins.socket. The following methods can be accessed:

* connect: opens a socket connection;
* disconnect: closes a socket connection;
* disconnectAll: closes ALL opened connections;
* send: send data using a given connection;
* receive: callback used by plugin's native code. Can be override by a custom implementation.

### connect (successCallback, errorCallback, host, port)

Example:

```
window.tlantic.plugins.socket.connect(
  function () {
    console.log('worked!');  
  },
  
  function () {
    console.log('failed!');
  },
  '192.168.2..5',
  18002
);
```

### disconnect (successCallback, errorCallback, host, port)

Disconnects any connection opened for a given host/port.

Example:

```
window.tlantic.plugins.socket.disconnect(
  function () {
    console.log('worked!');  
  },
  
  function () {
    console.log('failed!');
  },
  '192.168.2..5',
  18002
);
```

### disconnectAll (successCallback, errorCallback)

Example:

```
window.tlantic.plugins.socket.connect(
  function () {
    console.log('worked!');  
  },
  
  function () {
    console.log('failed!');
  }
);
```

### send (successCallback, errorCallback, host, port, data)

Sends information and calls success callback if information was send and does not wait for any response. To check how to receive data, please see the item below.

Example:

```
window.tlantic.plugins.socket.send(
  function () {
    console.log('worked!');  
  },
  
  function () {
    console.log('failed!');
  },
  '192.168.2..5',
  18002,
  'This is the data i want to send!'
);
```

### receive (host, port, data)

This method is a callback invoked by native code through webview capabilities. You can replace this method by your own implementation. Even this way, the default implementation dispatches a JS event which can be catched listening that event. Here it goes a proper implementation regarding default method behavior:

```
document.addEventListener(window.tlantic.plugins.socket.receiveHookName, function (ev) {
  console.log(ev.metadata.host);    // host who sent the data
  console.log(ev.metadata.port);    // sender port
  console.log(ev.metadata.data);    // received data
});
```
