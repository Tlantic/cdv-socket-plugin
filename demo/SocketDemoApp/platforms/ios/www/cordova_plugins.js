cordova.define('cordova/plugin_list', function(require, exports, module) {
module.exports = [
    {
        "file": "plugins/com.tlantic.plugins.socket/www/socket.js",
        "id": "com.tlantic.plugins.socket.Socket",
        "clobbers": [
            "window.tlantic.plugins.socket"
        ]
    }
];
module.exports.metadata = 
// TOP OF METADATA
{
    "com.tlantic.plugins.socket": "0.2.0"
}
// BOTTOM OF METADATA
});