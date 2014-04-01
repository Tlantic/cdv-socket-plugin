cordova.define("com.tlantic.plugins.socket.Socket", function(require, exports, module) { /* global module, require, document */
var exec = require('cordova/exec');

//
function Socket(){
    'use strict';

    this.receiveHookName = 'SOCKET_RECEIVE_DATA_HOOK';      // *** Event name to act as "hook" for data receiving
    this.pluginRef = 'Socket';                              // *** Plugin reference for Cordova.exec calls
}

//
Socket.prototype.connect = function (successCallback, errorCallback, host, port) {
    'use strict';
    exec(successCallback, errorCallback, this.pluginRef, 'connect', [host, port]);
};

//
Socket.prototype.disconnect = function (successCallback, errorCallback, host, port) {
    'use strict';
    exec(successCallback, errorCallback, this.pluginRef, 'disconnect', [host, port]);
};

//
Socket.prototype.disconnectAll = function (successCallback, errorCallback) {
    'use strict';
    exec(successCallback, errorCallback, this.pluginRef, 'disconnectAll', []);
};

//
Socket.prototype.send = function (successCallback, errorCallback, host, port, data) {
    'use strict';
    exec(successCallback, errorCallback, this.pluginRef, 'send', [host, port, typeof data == 'string' ? data : JSON.stringify(data)]);
};

//
Socket.prototype.receive = function (host, port, chunk) {
    'use strict';

    var evReceive = document.createEvent('Events');
    
    evReceive.initEvent(this.receiveHookName);
    evReceive.metadata = {
        host: host,
        port: port,
        data: typeof chunk == 'object' ? chunk : JSON.parse(chunk)
    };

    document.dispatchEvent(evReceive);
};

module.exports = new Socket();
});
