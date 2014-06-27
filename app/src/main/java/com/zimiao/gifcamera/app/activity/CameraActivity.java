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
import com.zimiao.gifcamera.app.core.AnimatedGifEncoder;
import com.zimiao.gifcamera.app.util.Constants;
import com.zimiao.gifcamera.app.util.MediaFileHelper;
import com.zimiao.gifcamera.app.widget.CameraPreview;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class CameraActivity extends Activity implements CameraPreview.ActivityListener {

    private Camera mCamera;
    private CameraPreview mPreview;
    private FrameLayout mPreviewView;
    private AnimatedGifEncoder mEncoder = new AnimatedGifEncoder();
    private Timer mTimer = new Timer();
    private boolean mFirst = false;
    private boolean mLast = false;
    private int mDelay = 1000;
    private int mFrames = 10;
    private boolean mIsRecord = false;
    private final int mOutputWidth = 320;
    private final int mOutputHeight = 240;

    // Implementation of ActivityListener

    @Override
    public void sendBroadcast(File file) {
        // Send broadcast so Gallery will scan for the new media.
        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                MediaFileHelper.getOutputMediaFileUri(file)));
    }

    @Override
    public boolean isRecord() {
        return mIsRecord;
    }

    @Override
    public void resetRecord() {
        mIsRecord = false;
    }

    // End implementation of ActivityListener

    private class TakePictureTask extends TimerTask {
        @Override
        public void run() {
            mCamera.takePicture(null, null, mPicture);
        }
    }
    private class TakeFirstPictureTask extends TimerTask {
        @Override
        public void run() {
            mFirst = true;
            mCamera.takePicture(null, null, mPicture);
        }
    }
    private class TakeLastPictureTask extends TimerTask {
        @Override
        public void run() {
            mLast = true;
            mCamera.takePicture(null, null, mPicture);
        }
    }

    private PictureCallback mPicture = new PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            File file = MediaFileHelper.getOutputMediaFile(MediaFileHelper.MEDIA_TYPE_IMAGE);
            if (file == null) {
                Log.d(Constants.TAG, "Error creating media file, check storage permissions: ");
                return;
            }
            try {
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                bitmap = Bitmap.createScaledBitmap(bitmap, mOutputWidth, mOutputHeight, true);
                if (mFirst) {
                    FileOutputStream fos = new FileOutputStream(file);
                    mEncoder.start(fos);
                    mEncoder.setDelay(mDelay);
                    mFirst = false;
                }
                mEncoder.addFrame(bitmap);
                if (mLast) {
                    mEncoder.finish();
                    mLast = false;
                }
            } catch (FileNotFoundException e) {
                Log.d(Constants.TAG, "File not found: " + e.getMessage());
            }
            mCamera.startPreview();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_layout);

        mPreviewView = (FrameLayout) findViewById(R.id.camera_preview);

        View button = findViewById(R.id.button_capture);
        button.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mIsRecord = true;
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

        Camera.Parameters params = mCamera.getParameters();
        List<Camera.Size> supportedSizes = params.getSupportedPictureSizes();
        Camera.Size size = supportedSizes.get(supportedSizes.size() - 1);
        params.setPictureSize(size.width, size.height);
        mCamera.setParameters(params);

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera, this);

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
