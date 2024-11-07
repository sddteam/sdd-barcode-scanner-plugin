var exec = require("cordova/exec");

var scanInProgress = false;

var SDDBarcodeScanner = function () {};

SDDBarcodeScanner.startCameraScan = function (successCallback, errorCallback) {
  if (errorCallback == null) {
    errorCallback = function () {};
  }

  if (typeof errorCallback != "function") {
    console.log(
      "SDDBarcodeScanner.startCameraScan failure: failure parameter not a function"
    );
    return;
  }

  if (typeof successCallback != "function") {
    console.log(
      "SDDBarcodeScanner.startCameraScan failure: success callback parameter must be a function"
    );
    return;
  }

  if (scanInProgress) {
    errorCallback("Scan is already in progress");
    return;
  }

  scanInProgress = true;

  exec(
    function (result) {
      scanInProgress = false;
      successCallback(result);
    },
    function (error) {
      scanInProgress = false;
      errorCallback(error);
    },
    "SDDBarcodeScanner",
    "startCameraScan",
    []
  );
};

SDDBarcodeScanner.isCameraReady = function (successCallback, errorCallback) {
  if (errorCallback == null) {
    errorCallback = function () {};
  }

  if (typeof errorCallback != "function") {
    console.log(
      "SDDBarcodeScanner.isCameraReady failure: failure parameter not a function"
    );
    return;
  }

  if (typeof successCallback != "function") {
    console.log(
      "SDDBarcodeScanner.isCameraReady failure: success callback parameter must be a function"
    );
    return;
  }

  exec(
    function (result) {
      successCallback(result);
    },
    function (error) {
      errorCallback(error);
    },
    "SDDBarcodeScanner",
    "isCameraReady",
    []
  );
};

SDDBarcodeScanner.cameraUnbind = function (successCallback, errorCallback) {
  if (errorCallback == null) {
    errorCallback = function () {};
  }

  if (typeof errorCallback != "function") {
    console.log(
      "SDDBarcodeScanner.cameraUnbind failure: failure parameter not a function"
    );
    return;
  }

  if (typeof successCallback != "function") {
    console.log(
      "SDDBarcodeScanner.cameraUnbind failure: success callback parameter must be a function"
    );
    return;
  }

  exec(
    function (result) {
      scanInProgress = false;
      successCallback(result);
    },
    function (error) {
      scanInProgress = true;
      errorCallback(error);
    },
    "SDDBarcodeScanner",
    "cameraUnbind",
    []
  );
};

module.exports = SDDBarcodeScanner;
