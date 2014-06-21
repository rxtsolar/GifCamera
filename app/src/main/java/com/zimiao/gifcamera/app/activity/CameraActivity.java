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

public class CameraActivity extends Activity {

    private Camera mCamera;
    private CameraPreview mPreview;
    private FrameLayout mPreviewView;
    private AnimatedGifEncoder mEncoder = new AnimatedGifEncoder();
    private Timer mTimer = new Timer();
    private boolean first = false;
    private boolean last = false;
    private int delay = 1000;
    private int frames = 10;

    private class TakePictureTask extends TimerTask {
        @Override
        public void run() {
            mCamera.takePicture(null, null, mPicture);
        }
    }
    private class TakeFirstPictureTask extends TimerTask {
        @Override
        public void run() {
            first = true;
            mCamera.takePicture(null, null, mPicture);
        }
    }
    private class TakeLastPictureTask extends TimerTask {
        @Override
        public void run() {
            last = true;
            mCamera.takePicture(null, null, mPicture);
        }
    }

    private PictureCallback mPicture = new PictureCallback() {
        private final int outputWidth = 320;
        private final int outputHeight = 240;

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            File file = MediaFileHelper.getOutputMediaFile(MediaFileHelper.MEDIA_TYPE_IMAGE);
            if (file == null) {
                Log.d(Constants.TAG, "Error creating media file, check storage permissions: ");
                return;
            }
            try {
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                bitmap = Bitmap.createScaledBitmap(bitmap, outputWidth, outputHeight, true);
                if (first) {
                    FileOutputStream fos = new FileOutputStream(file);
                    mEncoder.start(fos);
                    mEncoder.setDelay(delay);
                    first = false;
                }
                mEncoder.addFrame(bitmap);
                if (last) {
                    mEncoder.finish();
                    last = false;
                }
                // Send broadcast so Gallery will scan for the new media.
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                        MediaFileHelper.getOutputMediaFileUri(file)));
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
                        mTimer.schedule(new TakeFirstPictureTask(), 0);
                        for (int i = 1; i < frames - 1; i++) {
                            mTimer.schedule(new TakePictureTask(), i * delay);
                        }
                        mTimer.schedule(new TakeLastPictureTask(), (frames - 1) * delay);
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
