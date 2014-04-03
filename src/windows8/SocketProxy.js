/* global require, exports */

//var Connection = require('com.tlantic.plugins.socket.Connection');

exports.connect = function (win, fail, host, port) {
};

exports.disconnect = function (win, fail, connectionId) {
};

exports.disconnectAll = function (win, fail) {
};

exports.send = function (win, fail, data) {

};

exports.sendMessage = function(host, port, connectionId, data) {
};

require('cordova/windows8/commandProxy').add('Socket', exports);