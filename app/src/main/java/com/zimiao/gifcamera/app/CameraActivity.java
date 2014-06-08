package com.zimiao.gifcamera.app;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;

public class CameraActivity extends Activity {

    private Camera mCamera;
    private CameraPreview mPreview;
    private FrameLayout mPreviewView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_layout);
        mPreviewView = (FrameLayout) findViewById(R.id.camera_preview);
    }
    @Override
    public void onPause() {
        super.onPause();
        if (mCamera != null) {
            mPreviewView.removeView(mPreview);
            mCamera.release();
        }
    }
    @Override
    public void onResume() {
        super.onResume();

        // Create an instance of Camera
        mCamera = getCameraInstance();

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera);
        mPreviewView.addView(mPreview);
    }

    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
            Log.e(Constants.TAG, "exception when Camera.open()");
        }
        return c; // returns null if camera is unavailable
    }
}

