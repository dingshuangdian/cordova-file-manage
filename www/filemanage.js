
var exec = require('cordova/exec');
var filemanageOpenJs = {
	
    filemanageOpen: function(successCallback, errorCallback) {
        exec(successCallback, errorCallback, 'filemanage', 'filemanageOpen', []);
    }
};
module.exports = filemanageOpenJs
