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

	exports.disposeConnection = function(socket) {
		'use strict';

		var result = false;

		try {
			socket.close();
			result = true;

		} catch(e) {
			console.log('Unable to close connection!');
			result= false;

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
				fail('Connection ' + key + 'not found!');
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
	};

	exports.send = function (win, fail, data) {
		'use strict';
	};

	exports.sendMessage = function (host, port, connectionId, data) {
		'use strict';
	};

	require('cordova/windows8/commandProxy').add('Socket', exports);