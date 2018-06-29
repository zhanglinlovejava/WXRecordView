package com.zhanglin.recordview.utils;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

/**
 * Created by zhanglin on 2018/6/28.
 */
public class VideoPlayer implements MediaPlayer.OnCompletionListener, SurfaceHolder.Callback {
    private static final String TAG = "VideoPlayer";
    private MediaPlayer mMediaPlayer;
    private int currentPosition;
    private SurfaceView mPlayView;
    private SurfaceHolder mHolder;

    private static class VideoPlayerHolder {
        private static VideoPlayer instance = new VideoPlayer();
    }

    public void init(SurfaceView playView) {
        this.mPlayView = playView;
    }

    public static VideoPlayer getInstance() {
        return VideoPlayerHolder.instance;
    }

    public void playVideo(String videoPath) {
        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setOnCompletionListener(this);
        } else {
            mMediaPlayer.reset();
        }
        try {
            mHolder = mPlayView.getHolder();
            mHolder.addCallback(this);
            mMediaPlayer.setDataSource(videoPath);
            mMediaPlayer.prepareAsync();
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

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.e(TAG, "surfaceDestroyed");
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mMediaPlayer.seekTo(currentPosition);
        mMediaPlayer.start();
    }

    public void stopPlay() {
        if (mMediaPlayer != null) {
            mHolder.removeCallback(this);
            mMediaPlayer.stop();
            currentPosition = 0;
        }
    }

    public void onDestroy() {
        if (mMediaPlayer != null) {
            stopPlay();
            mMediaPlayer.setOnPreparedListener(null);
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }
}
