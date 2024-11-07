package sdd.cordova.barcodescanner;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.CameraState;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;

import java.util.concurrent.ExecutionException;

public class ScannerFragment extends Fragment {
    private PreviewView previewView;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private BarcodeScanner scanner;
    private boolean isScanning = true;
    private String appResourcePackage;
    private boolean fromCordova = true;

    public interface ScannerResultListener {
        void onQRCodeScanned(String qrCode);
        void onCameraReady(boolean ready);
        void onCameraUnbind(boolean unbind);
    }

    private ScannerResultListener scannerResultListener;
    public void setEventListener(ScannerResultListener listener){
      try {
        scannerResultListener = (ScannerResultListener) listener;
      } catch (ClassCastException e) {
        throw new ClassCastException(listener.toString()
          + " must implement ScannerResultListener");
      }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        appResourcePackage = getActivity().getPackageName();
        int cameraLayout = getResources().getIdentifier("activity_camera", "layout", appResourcePackage);
        View rootView = inflater.inflate(cameraLayout, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        int previewID = getResources().getIdentifier("previewView", "id", appResourcePackage);
        previewView = view.findViewById(previewID);
        cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                if (isScanning) {
                    bindPreview(cameraProvider);
                }
            } catch (ExecutionException | InterruptedException e) {
                // Handle any errors (including cancellation)
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(requireContext()));

        // Set up the barcode scanner
        BarcodeScannerOptions options =
                new BarcodeScannerOptions.Builder()
                        .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                        .build();
        scanner = BarcodeScanning.getClient(options);
    }

    private void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder().build();
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview);
        observeCameraState(camera);

        // Set up the analysis use case to scan barcodes
        androidx.camera.core.ImageAnalysis imageAnalysis =
                new androidx.camera.core.ImageAnalysis.Builder()
                        .build();

        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(requireContext()), imageProxy -> {
            @OptIn(markerClass = ExperimentalGetImage.class)
            InputImage image = InputImage.fromMediaImage(imageProxy.getImage(), imageProxy.getImageInfo().getRotationDegrees());
            scanner.process(image)
                    .addOnSuccessListener(barcodes -> {
                        for (Barcode barcode : barcodes) {
                            String rawValue = barcode.getRawValue();
                            if(!rawValue.isEmpty()){
                              System.out.println("Scanned QR Code: " + rawValue);
                              scannerResultListener.onQRCodeScanned(rawValue);
                              isScanning = false;
                              fromCordova = false;
                              unbindCamera();
                            }
                        }
                    })
                    .addOnCompleteListener(task -> imageProxy.close());
        });

        cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, imageAnalysis);
    }

    public void callUnbindCamera(){
      unbindCamera();
    }

    private void unbindCamera() {
        if (cameraProviderFuture.isDone()) {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                cameraProvider.unbindAll(); // Unbind all use cases and stop the camera
                if(fromCordova){
                    scannerResultListener.onCameraUnbind(true);
                }
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
                if(fromCordova){
                    scannerResultListener.onCameraUnbind(false);
                }
            }
        }
    }

    private void observeCameraState(Camera camera) {
        camera.getCameraInfo().getCameraState().observe((LifecycleOwner) this, new Observer<CameraState>() {
            @Override
            public void onChanged(@NonNull CameraState cameraState) {
                CameraState.Type type = cameraState.getType();
                switch (type) {
                    case PENDING_OPEN:
                        // Camera is pending open
                        break;
                    case OPENING:
                        // Camera is opening
                        break;
                    case OPEN:
                        // Camera is open
                        scannerResultListener.onCameraReady(true);
                        break;
                    case CLOSING:
                        // Camera is closing
                        break;
                    case CLOSED:
                        // Camera is closed
                        break;
                }
            }
        });
    }
}
