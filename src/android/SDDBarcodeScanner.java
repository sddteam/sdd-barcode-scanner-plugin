package sdd.cordova.barcodescanner;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.content.pm.PackageManager;
import android.content.Intent;

import org.apache.cordova.CordovaPlugin;

import javax.security.auth.callback.Callback;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.apache.cordova.PermissionHelper;

// import com.google.zxing.client.android.CaptureActivity;
// import com.journeyapps.barcodescanner.CaptureActivity;
// import sdd.cordova.barcodescanner.CameraPreview.BarcodeScanInterface;
// import com.google.zxing.client.android.encode.EncodeActivity;
// import com.google.zxing.client.android.Intents;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.widget.FrameLayout;

import sdd.cordova.barcodescanner.ScannerFragment;
// import com.google.zxing.BarcodeFormat;
// import com.google.zxing.qrcode.QRCodeWriter;
// import com.google.zxing.common.BitMatrix;
// import com.journeyapps.barcodescanner.BarcodeEncoder;
// import com.google.zxing.WriterException;
import android.view.View;
import android.view.ViewParent;
import android.view.ViewGroup;
import androidx.annotation.ColorInt;

/**
 * This class echoes a string called from JavaScript.
 */
public class SDDBarcodeScanner extends CordovaPlugin implements ScannerFragment.ScannerResultListener {
    private static final String STARTCAMERASCAN = "startCameraScan";
    private static final String ISCAMERAREADY = "isCameraReady";
    private static final String CAMERAUNBIND = "cameraUnbind";

    private static final String LOG_TAG = "BarcodeScanner";

    private String [] permissions = { Manifest.permission.CAMERA };

    private JSONArray requestArgs;
    private CallbackContext callbackContext;

    private ScannerFragment cameraPreview;

    private CallbackContext startCameraCallback;
    private CallbackContext cameraReadyCallback;
    private CallbackContext cameraUnbindCallback;

    private int containerViewId = 20;
    private ViewParent webViewParent;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if(action.equals(STARTCAMERASCAN)) {
            this.startCameraCallback = callbackContext;
            if(!hasPermisssion()) {
                requestPermissions(0);
            } else {
                startCameraScan();
            }
        } else if (action.equals(ISCAMERAREADY)) {
            isCameraReady(callbackContext);
        } else if (action.equals(CAMERAUNBIND)) {
            cameraUnbind(callbackContext);
        } else {
            return false;
        }

        return true;
    }
    
    public void startCameraScan() {
        final float opacity = Float.parseFloat("1");
        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                FrameLayout containerView = (FrameLayout) cordova.getActivity().findViewById(containerViewId);
        
                if(containerView == null){
                    containerView = new FrameLayout(cordova.getActivity().getApplicationContext());
                    containerView.setId(containerViewId);
        
                    FrameLayout.LayoutParams containerLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
                    cordova.getActivity().addContentView(containerView, containerLayoutParams);
                }
                boolean toBack = true;
                if(toBack){
                    View view = webView.getView();
                    ViewParent rootParent = containerView.getParent();
                    ViewParent currentParent = view.getParent();

                    @ColorInt int color = 0x00000000;

                    view.setBackgroundColor(color);

                    if(currentParent.getParent() != rootParent){
                        while(currentParent != null && currentParent.getParent() != rootParent){
                            currentParent = currentParent.getParent();
                        }

                        if(currentParent != null){
                            ((ViewGroup)currentParent).setBackgroundColor(color);
                            ((ViewGroup)currentParent).bringToFront();
                        } else {
                            currentParent = view.getParent();
                            webViewParent = currentParent;
                            ((ViewGroup)view).bringToFront();
                        }
                    } else {
                        webViewParent = currentParent;
                        ((ViewGroup)currentParent).bringToFront();
                    }
                } else {
                    containerView.setAlpha(opacity);
                    containerView.bringToFront();
                }
        
                cameraPreview = new ScannerFragment();
                cameraPreview.setEventListener(SDDBarcodeScanner.this);
                FragmentManager fragmentManager = cordova.getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.add(containerView.getId(), cameraPreview);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });
    }

    @Override
    public void onQRCodeScanned(String qrCode) {
        if(!qrCode.isEmpty()) {
          Log.i("Received QR Code: ", qrCode);
          this.startCameraCallback.success(qrCode);
        } else {
          this.startCameraCallback.error("Error reading qr.");
        }
    }

    public void isCameraReady(CallbackContext callback) {
        this.cameraReadyCallback = callback;
    }

    @Override
    public void onCameraReady(boolean ready) {
        if(ready){
            Log.i("Camera Ready: ", "" + ready);
            this.cameraReadyCallback.success("" + ready);
        } else {
            this.cameraReadyCallback.error("" + false);
        }
    }

    public void cameraUnbind(CallbackContext callback) {
        this.cameraUnbindCallback = callback;
        
        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(cameraPreview != null){
                cameraPreview.callUnbindCamera();
                }
            }
        });
    }


    @Override
    public void onCameraUnbind(boolean unbind) {
      if(unbind){
        Log.i("Camera Unbind: ", "" + unbind);
        this.cameraUnbindCallback.success("" + unbind);
      } else {
        this.cameraUnbindCallback.error("" + unbind);
      }
    }

    public boolean hasPermisssion() {
        for(String p : permissions)
        {
            if(!PermissionHelper.hasPermission(this, p))
            {
                return false;
            }
        }
        return true;
    }

    public void requestPermissions(int requestCode) {
        PermissionHelper.requestPermissions(this, requestCode, permissions);
    }

    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) throws JSONException {
         PluginResult result;
         for (int r : grantResults) {
             if (r == PackageManager.PERMISSION_DENIED) {
                Log.d(LOG_TAG, "Permission Denied!");
                result = new PluginResult(PluginResult.Status.ILLEGAL_ACCESS_EXCEPTION);
                this.startCameraCallback.sendPluginResult(result);
                return;
             }
         }
  
         switch(requestCode)
         {
             case 0: 
                startCameraScan();
                break;
         }
     }
}
