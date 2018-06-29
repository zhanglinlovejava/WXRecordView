package com.zhanglin.recordview.utils;

/**
 * Created by zhanglin on 2017/9/7.
 */

public interface IVideoRecordCallback {
    void onRecordStop(String videoPath, int duration, boolean isSilence);

    void onRecordError(int code, String msg);
}
