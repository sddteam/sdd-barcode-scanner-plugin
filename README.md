# Barcode Scanner Plugin

A cordova plugin for Ionic with a fragment of barcode scanner camera activity using google mlkit package. It is only available for android as of now, use [phonegap-plugin-barcodescanner](https://github.com/phonegap/phonegap-plugin-barcodescanner) for iOS.

## Installation

Add barcode scanner plugin in your project

```
ionic cordova plugin add https://github.com/Cha-er-si/barcode-scanner-plugin.git --link
```

Install the wrapper for the barcode scanner plugin

```
npm install https://github.com/Cha-er-si/awesome-cordova-plugins-chaersi-barcodescanner.git --save-dev
```

If you want other version of the wrapper

```
npm install https://github.com/Cha-er-si/awesome-cordova-plugins-chaersi-barcodescanner.git#<Version Number> --save-dev
```

Add this to your config.xml

```
<preference name="android-targetSdkVersion" value="34" />
<preference name="android-compileSdkVersion" value="34" />
```

When you add platform to your project for android it must be the latest

```
cordova platform add android@latest
```

for Ionic projects

```
ionic cordova platform add android@latest
```

## Usage/Examples

Import the barcode scanner.

```javascript
import { ChaersiBarcodeScanner } from "@awesome-cordova-plugins/chaersi-barcode-scanner/ngx";
```

Add a constructor for the barcode scanner.

```javascript
 constructor(private customBarcodeScanner: ChaersiBarcodeScanner) {}
```

### startCameraScan()

Add it on ngOnInit.

```javascript
  ngOnInit() {
    this.platform.ready().then(() => {
      this.customBarcodeScanner
        .startCameraScan()
        .then((result) => {
          console.log({ result });
        })
        .catch((error) => {
          console.error({ error });
        });
    });
  }
```

### isCameraReady()

isCameraReady() returns boolean as string ("true"/"false")

```
this.customBarcodeScanner
  .isCameraReady()
  .then((result) => {
    // Handle Result
  })
  .catch((error) => {
    // Handle Error
  });
```

You are free to do what you need from the result.

### cameraUnbind()

cameraUnbind() returns boolean as string ("true"/"false")

```
this.customBarcodeScanner
  .cameraUnbind()
  .then((result) => {
    // Handle Result
  })
  .catch((error) => {
    // Handle Error
  });
```

You are free to do what you need from the result.

### Important Note

Take note that your ionic should disable the dark mode styles and that the ion-content should have a transparent background.
