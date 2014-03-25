/* jslint node:true */
var net = require('net'),
	clients = [],
	serverInstance,
	onListen,
	onConnect;

onListen = function () {
	'use strict';
	console.log('Server listening on port ', this.address().port);
};

onConnect = function (socket) {
	'use strict';

	console.log('- Connection from ', socket.remoteAddress, ' on port ', socket.localPort, ' established.');
};


serverInstance = function (socket) {
	'use strict';

	// client identification
	socket.name = socket.remoteAddress + ':' + socket.remotePort;

	// add into client list
	clients.push(socket);

	// handle incoming messages
	socket.on('data', function (data) {
		console.log('- Received information on port ', socket.localPort, ': ', data);
	});

	// Remove client from list
	socket.on('end', function () {
		clients.splice(clients.indexOf(socket), 1);
	});

};

net.createServer(serverInstance).listen(18002, onListen).on('connection', onConnect);
net.createServer(serverInstance).listen(18004, onListen).on('connection', onConnect);