package com.zhanglin.recordview.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Build;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.zhanglin.recordview.R;

import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 * Created by zhanglin on 2017/9/22.
 */

public class RecordManager implements SurfaceHolder.Callback, MediaRecorder.OnErrorListener,
        MediaRecorder.OnInfoListener {
    private static final String TAG = RecordManager.class.getSimpleName();
    private MediaRecorder mMediaRecorder;
    private String localVideoPath = "";
    private Camera mCamera;
    private int previewWidth = 1080;
    private int previewHeight = 1920;
    private int encodingBitRate = 2500000;
    private int recordWidth = 1280;
    private int recordHeight = 720;
    private long startTime = 0;
    private int frontCamera = 0; // 0 is back camera，1 is front camera
    private SurfaceHolder mSurfaceHolder;
    private static RecordManager instance = null;
    private int defaultVideoFrameRate = -1;
    private int maxDuration = 120000;
    private Context context;
    private boolean isRecording = false;
    private IVideoRecordCallback iVideoRecordCallback;

    public static RecordManager getInstance() {
        if (instance == null) {
            synchronized (RecordManager.class) {
                if (instance == null) {
                    instance = new RecordManager();
                }
            }
        }
        return instance;
    }

    private RecordManager() {
    }

    public void setSurfaceView(Context context, SurfaceView mSurfaceView, int maxDuration, IVideoRecordCallback iVideoRecordCallback) {
        this.iVideoRecordCallback = iVideoRecordCallback;
        this.maxDuration = maxDuration;
        this.context = context;
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.setFixedSize(previewWidth, previewHeight);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mSurfaceHolder.addCallback(this);
        localVideoPath = RecorderUtil.createFile(context, RecorderUtil.VIDEO_DIR, RecorderUtil.VIDEO_SUFFIX);
    }

    public void startRecording() {
        isRecording = true;
        startRecordThread();
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private boolean prepareRecord() {
        try {

            mMediaRecorder = new MediaRecorder();
            mCamera.unlock();
            mMediaRecorder.setCamera(mCamera);
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            mMediaRecorder.setAudioEncodingBitRate(44100);
            mMediaRecorder.setVideoSize(previewWidth, previewHeight);
            mMediaRecorder.setVideoEncodingBitRate(encodingBitRate);
            mMediaRecorder.setVideoFrameRate(defaultVideoFrameRate);
            // 实际视屏录制后的方向
            mMediaRecorder.setOrientationHint(frontCamera == 0 ? 90 : 270);
            mMediaRecorder.setMaxDuration(maxDuration);
            mMediaRecorder.setOutputFile(localVideoPath);
            mMediaRecorder.setOnErrorListener(this);
            mMediaRecorder.setOnInfoListener(this);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Exception prepareRecord: " + e.getMessage());
            onRecordError(101, e.getMessage());
            return false;
        }
        try {
            mMediaRecorder.prepare();
        } catch (Exception e) {
            Log.e(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            onRecordError(101, e.getMessage());
            return false;
        }
        return true;
    }

    public void stopRecord(boolean isSave) {
        if (isRecording) {
            isRecording = false;
            try {
                mMediaRecorder.stop();
                if (isSave) {
                    int duration = (int) ((System.currentTimeMillis() - startTime) / 1000);
                    if (iVideoRecordCallback != null) {
                        iVideoRecordCallback.onRecordStop(localVideoPath, duration, false);
                    }
                } else {
                    deleteFile();
                }
            } catch (RuntimeException r) {
                Log.e(TAG, "stopRecord Exception:" + r.getMessage());
                onRecordError(100, context.getString(R.string.record_time_too_short));
            } finally {
                releaseMediaRecorder();
            }
        }

    }

    public void stopRecordSilence() {
        if (isRecording) {
            isRecording = false;
            try {
                mMediaRecorder.stop();
                int duration = (int) ((System.currentTimeMillis() - startTime) / 1000);
                if (iVideoRecordCallback != null) {
                    iVideoRecordCallback.onRecordStop(localVideoPath, duration, true);
                }
            } catch (RuntimeException r) {
            } finally {
                releaseMediaRecorder();
            }
        }

    }

    private void startPreView(SurfaceHolder holder) {
        if (mCamera == null) {
            mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
        }
        if (mCamera != null) {
            mCamera.setDisplayOrientation(90);
            try {
                mCamera.setPreviewDisplay(holder);
                Camera.Parameters parameters = mCamera.getParameters();
                boolean hasSupportRate = false;
                List<Integer> supportedPreviewFrameRates = mCamera.getParameters()
                        .getSupportedPreviewFrameRates();
                if (supportedPreviewFrameRates != null
                        && supportedPreviewFrameRates.size() > 0) {
                    Collections.sort(supportedPreviewFrameRates);
                    for (int i = 0; i < supportedPreviewFrameRates.size(); i++) {
                        int supportRate = supportedPreviewFrameRates.get(i);
                        if (supportRate == 15) {
                            hasSupportRate = true;
                        }
                    }
                    if (hasSupportRate) {
                        defaultVideoFrameRate = 15;
                    } else {
                        defaultVideoFrameRate = supportedPreviewFrameRates.get(0);
                    }

                }
                List<Camera.Size> resolutionList = RecorderUtil.getResolutionList(mCamera);
                if (resolutionList != null && resolutionList.size() > 0) {
                    Collections.sort(resolutionList, new RecorderUtil.ResolutionComparator());
                    Camera.Size previewSize;
                    boolean hasSize = false;
                    for (int i = 0; i < resolutionList.size(); i++) {
                        Camera.Size size = resolutionList.get(i);
                        if (size != null && size.width <= recordWidth && size.height <= recordHeight) {
                            previewSize = size;
                            previewWidth = previewSize.width;
                            previewHeight = previewSize.height;
                            hasSize = true;
                            break;
                        }
                    }
                    if (!hasSize) {
                        int mediumResolution = resolutionList.size() / 2;
                        if (mediumResolution >= resolutionList.size())
                            mediumResolution = resolutionList.size() - 1;
                        previewSize = resolutionList.get(mediumResolution);
                        previewWidth = previewSize.width;
                        previewHeight = previewSize.height;

                    }
                }
                parameters.setPreviewSize(previewWidth, previewHeight);
                parameters.setPictureSize(previewWidth, previewHeight);
                CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
                profile.videoFrameWidth = previewWidth;
                profile.videoFrameHeight = previewHeight;
                profile.videoBitRate = encodingBitRate;
                List<String> focusModes = parameters.getSupportedFocusModes();
                if (focusModes != null) {
                    for (String mode : focusModes) {
                        if (mode.contains("continuous-video")) {
                            parameters.setFocusMode("continuous-video");
                        }
                    }
                }
                mCamera.setParameters(parameters);
                mCamera.startPreview();
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, e.getMessage());
                onRecordError(102, context.getString(R.string.record_init_fail));
            }
        }
    }

    private void startRecordThread() {
        if (prepareRecord()) {
            try {
                mMediaRecorder.start();
                startTime = System.currentTimeMillis();
                Log.i(TAG, "Start Record");
            } catch (RuntimeException r) {
                onRecordError(101, context.getString(R.string.record_init_fail));
                Log.e(TAG, "startRecordThread Exception" + r.getMessage());
            }
        }
    }

    private void onRecordError(int code, String msg) {
        releaseMediaRecorder();
        releaseCamera();
        deleteFile();
        if (iVideoRecordCallback != null) {
            iVideoRecordCallback.onRecordError(code, msg);
        }
    }

    private void releaseCamera() {
        if (mCamera != null) {
            // release the camera for other applications
            mCamera.release();
            mCamera = null;
            Log.i(TAG, "release Camera");
        }
    }

    private void releaseMediaRecorder() {
        if (mMediaRecorder != null) {
            // clear recorder configuration
            mMediaRecorder.setOnErrorListener(null);
            mMediaRecorder.setOnInfoListener(null);
            mMediaRecorder.reset();
            // release the recorder object
            mMediaRecorder.release();
            mMediaRecorder = null;
            Log.i(TAG, "release Recorder");
        }
    }

    public void deleteFile() {
        if (localVideoPath != null) {
            File file = new File(localVideoPath);
            if (file.exists())
                file.delete();
        }
    }

    @Override
    public void onError(MediaRecorder mr, int what, int extra) {
        if (iVideoRecordCallback != null) {
            iVideoRecordCallback.onRecordError(102, context.getString(R.string.record_init_fail));
        }
        stopRecord(false);
        onDestory();
    }

    @Override
    public void onInfo(MediaRecorder mr, int what, int extra) {
        if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
            stopRecord(true);
            Log.e(TAG, "max duration reached");
            onDestory();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mSurfaceHolder = holder;
        startPreView(holder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mCamera != null) {
            Log.i(TAG, "surfaceDestroyed: ");
            releaseCamera();
        }
        if (mMediaRecorder != null) {
            releaseMediaRecorder();
        }
    }

    public void switchCamera() {
        int cameraCount = 0;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras();//得到摄像头的个数

        for (int i = 0; i < cameraCount; i++) {
            Camera.getCameraInfo(i, cameraInfo);//得到每一个摄像头的信息
            if (frontCamera == 0) {
                //现在是后置，变更为前置
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {//代表摄像头的方位，CAMERA_FACING_FRONT前置      CAMERA_FACING_BACK后置
                    mCamera.stopPreview();//停掉原来摄像头的预览
                    mCamera.release();//释放资源
                    mCamera = null;//取消原来摄像头
                    mCamera = Camera.open(i);//打开当前选中的摄像头
                    startPreView(mSurfaceHolder);
                    frontCamera = 1;
                    break;
                }
            } else {
                //现在是前置， 变更为后置
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {//代表摄像头的方位，CAMERA_FACING_FRONT前置      CAMERA_FACING_BACK后置
                    mCamera.stopPreview();//停掉原来摄像头的预览
                    mCamera.release();//释放资源
                    mCamera = null;//取消原来摄像头
                    mCamera = Camera.open(i);//打开当前选中的摄像头
                    startPreView(mSurfaceHolder);
                    frontCamera = 0;
                    break;
                }
            }
        }
    }

    public void turnLight(boolean isOpen) {
        if (mCamera == null) return;
        RecorderUtil.turnLightOn(mCamera, isOpen);
    }

    public void setRecordWidth(int recordWidth, int height) {
        this.recordWidth = recordWidth;
        this.recordHeight = height;
    }

    public void setEncodingBitRate(int encodingBitRate) {
        this.encodingBitRate = encodingBitRate;
    }

    public boolean isRecording() {
        return isRecording;
    }

    public void onDestory() {
        releaseMediaRecorder();
        releaseCamera();
        frontCamera = 0;
    }

    public void onPause() {
        WakeLockManager.getInstance().release();
        stopRecordSilence();

    }

    public void resetCamera() {
        frontCamera = 0;
    }

    public void onResume() {
        WakeLockManager.getInstance().acquire(context);
    }

    public void setRecording(boolean recording) {
        isRecording = recording;
    }
}
