    /* global exports, console, Windows, require */

    


    //
    exports.pool = [];

    //
    exports.buildKey = function (host, port) {
        'use strict';
        return host.toLowerCase() + ':' + port;
    };

    //
    exports.connect = function (win, fail, args) {
        'use strict';
        var host, port, key, socket, hostname, service;

        // validating parameters
        if (args.length !== 2) {
            fail('Missing arguments for "connect" action.');
            return;

        } else {

            // building connection key
            host = args[0];
            port = args[1];
            key = exports.buildKey(host, port);

            // trying to recover an existing connection
            if (exports.pool[key]) {
                console.log('Recovered connection with ', key);
                win(key);
                return;

            } else {

                // creating new connection;
                hostname = new Windows.Networking.HostName(host);
                service = port.toString();
                socket = new Windows.Networking.Sockets.StreamSocket();

                // opening stream
                socket.connectAsync(hostname, service).done(function () {

                    // adding to pool
                    console.log('Connection with ', key, ' opened successfully!');
                    exports.pool[key] = socket;

                    // returning key as success sign
                    win(key);
                }, fail);
            }
        }

    };

    exports.disposeConnection = function (socket) {
        'use strict';

        var result = false;

        try {
            socket.close();
            result = true;

        } catch (e) {
            console.log('Unable to close connection!');
            result = false;

        } finally {
            return result;
        }
    };

    exports.disconnect = function (win, fail, args) {
        'use strict';

        var key, socket;

        // validating parameters
        if (args.length !== 1) {
            fail('Missing arguments for "disconnect" action.');
            return;
        } else {

            // retrieving existing connection
            key = args[0];
            socket = exports.pool[key];
            if (!socket) {
                fail('Connection ' + key + ' not found!');
                return;
            } else {

                // removing from pool and closing socket
                exports.pool[key] = undefined;

                if (exports.disposeConnection(socket)) {
                    win();
                } else {
                    fail('Unable to close connection with ' + key);
                }

                return;
            }
        }

    };

    exports.disconnectAll = function (win, fail) {
        'use strict';
        var socket, partial = false;

        console.log('Preparing to disconnect all connections:');

        // checking all connections from pool
        for (var key in exports.pool) {

            // retrieving existing socket
            console.log('- Closing ', key, ' ...');
            socket = exports.pool[key];

            // disposing socket
            if (!exports.disposeConnection(socket)) {
                partial = true;
                console.log('- Unable to close ', key);
            }

            // removing from pool
            exports.pool[key] = undefined;
        }


        // returning based on all results
        if (partial) {
            fail('Some connections could not be closed!');
        } else {
            win();
        }
    };

    exports.send = function (win, fail, args) {
        'use strict';

        var key, data, socket, writer, bufSize;

        // validating parameters
        if (args.length !== 2) {
            fail('Missing arguments for "disconnect" action.');
            return;
        } else {

            // retrieving existing connection
            key = args[0];
            data = args[1];

            socket = exports.pool[key];
            if (!socket) {
                fail('Connection ' + key + ' not found!');
                return;
            } else {

                // preparing for sending
                writer = new Windows.Storage.Streams.DataWriter(socket.outputStream);
                bufSize = writer.measureString(data); // Gets the UTF-8 string length.
                writer.writeInt32(bufSize);
                writer.writeString(data);

                console.log('Sending ', data);

                // flushing information
                writer.storeAsync().done(function () {

                    // detaching outputStream and with success
                    writer.detachStream();
                    win();

                }, fail);

            }
        }

    };

    exports.sendMessage = function (host, port, connectionId, data) {
        'use strict';
    };

    require('cordova/windows8/commandProxy').add('Socket', exports);