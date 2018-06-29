package com.zhanglin.recordview.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.media.MediaMetadataRetriever;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;

/**
 * Created by zhanglin on 2017/9/7.
 */

public class RecorderUtil {

    public static final String VIDEO_DIR = "/video_record/";
    public static final String VIDEO_THUMB_DIR = "/thumb/";
    public static final String VIDEO_SUFFIX = ".mp4";
    public static final String VIDEO_THUMB_SUFFIX = ".jpg";

    public static class ResolutionComparator implements Comparator<Camera.Size> {

        @Override
        public int compare(Camera.Size lhs, Camera.Size rhs) {
            return rhs.width - lhs.width;
        }

    }

    public static List<Camera.Size> getResolutionList(Camera camera) {
        Camera.Parameters parameters = camera.getParameters();
        return parameters.getSupportedPreviewSizes();
    }

    /**
     * 获取视频的第一针图片
     * 同时进行相应的缩放
     *
     * @param context
     * @param videoPath
     * @return
     */
    public static String getThumbPath(Context context, String videoPath) {
        Bitmap bitmap = getThumbBitmap(videoPath);
        if (bitmap == null) return null;
        String thumbnailPath = createFile(context, VIDEO_THUMB_DIR, VIDEO_THUMB_SUFFIX);
        File file = new File(thumbnailPath);
        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();
            bitmap.recycle();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return thumbnailPath;
    }

    private static Bitmap getThumbBitmap(String videoPath) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(videoPath);
        Bitmap bitmap = retriever.getFrameAtTime();
        if (bitmap == null) return null;
        Bitmap scaleBitmap = scale(bitmap);
        bitmap.recycle();
        return scaleBitmap;
    }

    public static String createFile(Context context, String subDir, String suffix) {
        File file;
        File dir = new File(context.getExternalCacheDir() + subDir);
        if (!dir.exists()) {
            dir.mkdir();
        }
        file = new File(dir, System.currentTimeMillis() + suffix);
        return file.getAbsolutePath();
    }

    /**
     * Bitmap缩小的方法
     */
    private static Bitmap scale(Bitmap bitmap) {
        Matrix matrix = new Matrix();
        matrix.postScale(0.2f, 0.2f); //长和宽放大缩小的比例
        Bitmap resizeBmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return resizeBmp;
    }

    /**
     * 切换闪光灯
     *
     * @param mCamera
     * @param isOpen
     */
    public static void turnLightOn(Camera mCamera, boolean isOpen) {
        if (mCamera == null) {
            return;
        }
        Camera.Parameters parameters = mCamera.getParameters();
        if (parameters == null) {
            return;
        }
        List<String> flashModes = parameters.getSupportedFlashModes();
        if (flashModes == null) {
            return;
        }
        String flashMode = parameters.getFlashMode();
        if (isOpen) {
            if (!Camera.Parameters.FLASH_MODE_TORCH.equals(flashMode) && flashModes.contains(Camera.Parameters.FLASH_MODE_TORCH)) {
                // Turn on the flash
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            }
        } else {
            if (!Camera.Parameters.FLASH_MODE_OFF.equals(flashMode) && flashModes.contains(Camera.Parameters.FLASH_MODE_OFF)) {
                // Turn off the flash
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            }
        }
        mCamera.setParameters(parameters);
    }

    public static boolean isFileExist(String path) {
        File file = new File(path);
        return file.exists();
    }

}
