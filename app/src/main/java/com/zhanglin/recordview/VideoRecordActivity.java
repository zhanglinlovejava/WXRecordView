package com.zhanglin.recordview;

import android.Manifest;
import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.zhanglin.recordview.utils.DialogHelper;
import com.zhanglin.recordview.utils.IVideoRecordCallback;
import com.zhanglin.recordview.utils.RecordManager;
import com.zhanglin.recordview.utils.ScreenUtils;

import java.io.IOException;
import java.util.List;

/**
 * Created by zhanglin on 2018/6/28.
 */
public class VideoRecordActivity extends BaseActivity implements RecordButton.IRecordViewAction, IVideoRecordCallback
        , MediaPlayer.OnCompletionListener, SurfaceHolder.Callback {
    private static final String TAG = "VideoRecordActivity";
    private MediaPlayer mMediaPlayer;
    private String videoPath;
    private int currentPosition;
    private RecordButton mRecordButton;
    private View ivClose, ivCancel, ivSwitch, ivConfirm;
    private SurfaceView mRecordView, mPlayView;
    private FrameLayout mRootView;
    private boolean isPlaying = false;
    private int maxDuration = 15000;//ms
    String[] permissions = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
    };

    public static void actionLaunch(Context context) {
        context.startActivity(new Intent(context, VideoRecordActivity.class));
    }

    @Override
    public int setLayoutId() {
        return R.layout.act_record;
    }

    @Override
    public void initUIAndData() {
        mRecordButton = findViewById(R.id.recordButton);
        mRootView = findViewById(R.id.videoRoot);
        mRecordView = new SurfaceView(mContext);
        mPlayView = new SurfaceView(mContext);
        mRecordButton.setMaxDuration(maxDuration);
        mRecordButton.setiRecordViewAction(this);
        ivClose = addClickListener(R.id.ivClose);
        ivConfirm = addClickListener(R.id.iv_confirm);
        ivCancel = addClickListener(R.id.iv_cancel);
        ivSwitch = addClickListener(R.id.iv_switch);
        checkPermission();

    }

    private void checkPermission() {
        AndPermission.with(this)
                .runtime()
                .permission(permissions)
                .onDenied(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> data) {
                        for (String permission : data) {
                            if (permission.equals(permissions[0])) {
                                showPermissionDialog(R.string.camera_permission_refuse);
                            } else if (permission.equals(permissions[1])) {
                                showPermissionDialog(R.string.audio_permission_refuse);
                            }
                        }
                    }
                }).onGranted(new Action<List<String>>() {
            @Override
            public void onAction(List<String> data) {
                if (data.size() == permissions.length) {
                    initRecord();
                }

            }
        }).start();
    }

    private void initRecord() {
        mRootView.addView(mRecordView);
        RecordManager.getInstance().setSurfaceView(mContext, mRecordView, maxDuration, this);
    }

    @Override
    public void onSingleClick() {
    }

    @Override
    public void onRecordStart() {
        ivSwitch.setVisibility(View.GONE);
        ivClose.setVisibility(View.GONE);
        RecordManager.getInstance().startRecording();
    }

    @Override
    public void onRecordFinish() {
        RecordManager.getInstance().stopRecord(true);
    }

    @Override
    public void onRecordStop(String videoPath, int duration, boolean isSilence) {
        if (duration < 1) {
            Toast.makeText(mContext, R.string.record_time_too_short, Toast.LENGTH_SHORT).show();
        } else {
            this.videoPath = videoPath;
            Log.e("test", videoPath + "--- " + duration);
            playVideo();
        }
    }


    @Override
    public void onRecordError(int code, String msg) {
        Log.e(TAG, "出错了--" + code + "--" + msg);
        if (code == 100) {
            Toast.makeText(mContext, R.string.record_time_too_short, Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_cancel:
                reRecord();
                break;
            case R.id.iv_confirm:
                finish();
                break;
            case R.id.ivClose:
                finish();
                break;
            case R.id.iv_switch:
                RecordManager.getInstance().switchCamera();
                break;
        }
    }

    private void reRecord() {
        new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mRootView.removeView(mPlayView);
            }
        }, 600);
        RecordManager.getInstance().resetCamera();
        mRootView.addView(mRecordView);
        ivClose.setVisibility(View.VISIBLE);
        ivSwitch.setVisibility(View.VISIBLE);
        releaseMediaPlayer();
        toggleButton(false);
        isPlaying = false;
    }

    private void showPermissionDialog(int strId) {
        DialogHelper.showOneButtonDialog(mContext, String.format(getString(strId), getString(R.string.app_name)), "知道了", false, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
    }

    public void playVideo() {
        if (!isPlaying)
            mRootView.addView(mPlayView);
        SurfaceHolder mHolder = mPlayView.getHolder();
        mHolder.addCallback(this);
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnCompletionListener(this);
        try {
            mMediaPlayer.setDataSource(videoPath);
            mMediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "playVideo:" + e.getMessage());
        }

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.e(TAG, "surfaceCreated");
        if (mMediaPlayer != null) {
            mMediaPlayer.setDisplay(holder);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setLooping(true);
            mMediaPlayer.start();
        }

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.e(TAG, "surfaceChanged");
        toggleButton(true);
        isPlaying = true;
        mRootView.removeView(mRecordView);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.e(TAG, "surfaceDestroyed");
        if (isPlaying) {
            release();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RecordManager.getInstance().onDestory();
        releaseMediaPlayer();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mMediaPlayer.seekTo(currentPosition);
        mMediaPlayer.start();
    }


    public void release() {
        if (mMediaPlayer != null) {
            currentPosition = mMediaPlayer.getCurrentPosition();
            mMediaPlayer.stop();
            mMediaPlayer.setOnCompletionListener(null);
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    public void releaseMediaPlayer() {
        if (mMediaPlayer != null) {
            release();
            currentPosition = 0;
            isPlaying = false;
            videoPath = null;
        }
    }

    private void toggleButton(boolean isShow) {
        if (isShow) mRecordButton.setVisibility(View.GONE);
        translationView(ivConfirm, isShow ? ScreenUtils.dip2px(mContext, 110) : 0);
        translationView(ivCancel, isShow ? -ScreenUtils.dip2px(mContext, 110) : 0);
    }

    private void translationView(View view, final float delta) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationX", delta);
        animator.setDuration(300);
        animator.start();
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (delta == 0) {
                    mRecordButton.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        RecordManager.getInstance().deleteFile();
    }

    @Override
    public void finish() {
        super.finish();
        this.overridePendingTransition(0, R.anim.act_close);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isPlaying) {
            playVideo();
        }
    }
}
