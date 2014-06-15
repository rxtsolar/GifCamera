package com.zimiao.gifcamera.app.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import com.zimiao.gifcamera.app.R;
import com.zimiao.gifcamera.app.util.Constants;
import com.zimiao.gifcamera.app.util.MediaFileHelper;
import com.zimiao.gifcamera.app.widget.CameraPreview;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class CameraActivity extends Activity {

    private Camera mCamera;
    private CameraPreview mPreview;
    private FrameLayout mPreviewView;
    private View mButton;


    private PictureCallback mPicture = new PictureCallback() {
        final int outputWidth = 640;
        final int outputHeight = 480;
        final int quality = 90;

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            File file = MediaFileHelper.getOutputMediaFile(MediaFileHelper.MEDIA_TYPE_IMAGE);
            if (file == null) {
                Log.d(Constants.TAG, "Error creating media file, check storage permissions: ");
                return;
            }
            try {
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                FileOutputStream fos = new FileOutputStream(file);
                bitmap = Bitmap.createScaledBitmap(bitmap, outputWidth, outputHeight, true);
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, fos);
                // Send broadcast so Gallery will scan for the new media.
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                        MediaFileHelper.getOutputMediaFileUri(file)));
            } catch (FileNotFoundException e) {
                Log.d(Constants.TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(Constants.TAG, "Error accessing file: " + e.getMessage());
            }
            mCamera.startPreview();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_layout);

        mPreviewView = (FrameLayout) findViewById(R.id.camera_preview);
        mButton = findViewById(R.id.button_capture);
        mButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mCamera.takePicture(null, null, mPicture);
                    }
                }
        );
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

    private static Camera getCameraInstance() {
        Camera c = null;
        try {
            // attempt to get a Camera instance
            c = Camera.open();
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
            Log.e(Constants.TAG, "exception when Camera.open()");
        }
        // returns null if camera is unavailable
        return c;
    }
}

