package com.zimiao.gifcamera.app.util;

import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class MediaFileHelper {

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    private static final String APP_NAME = "GifCameraApp";

    public static Uri getOutputMediaFileUri(File file) {
        return Uri.fromFile(file);
    }

    public static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), APP_NAME);
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.e(Constants.TAG, "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        String imagePath = mediaStorageDir.getPath() + File.separator + "IMG_"+ timeStamp + ".jpg";
        String videoPath = mediaStorageDir.getPath() + File.separator + "VID_"+ timeStamp + ".mp4";
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(imagePath);
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(videoPath);
        } else {
            return null;
        }
        return mediaFile;
    }
}
