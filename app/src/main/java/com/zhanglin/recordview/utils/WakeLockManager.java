package com.zhanglin.recordview.utils;
import android.content.Context;
import android.os.PowerManager;

/**
 * Created by zhanglin on 2017/8/10.
 */
public class WakeLockManager {
    private static WakeLockManager instance = null;
    private PowerManager powerManager;
    private PowerManager.WakeLock wakeLock;
    public static WakeLockManager getInstance() {
        if (instance == null) {
            synchronized (WakeLockManager.class) {
                if (instance == null) {
                    instance = new WakeLockManager();
                }
            }
        }
        return instance;
    }
    /**
     * 对于newWakeLock的第一个参数,有以下选择:
     * <p>
     * PARTIAL_WAKE_LOCK:保持CPU 运转，屏幕和键盘灯有可能是关闭的。
     * SCREEN_DIM_WAKE_LOCK：保持CPU 运转，允许保持屏幕显示但有可能是灰的，允许关闭键盘灯
     * SCREEN_BRIGHT_WAKE_LOCK：保持CPU 运转，允许保持屏幕高亮显示，允许关闭键盘灯
     * FULL_WAKE_LOCK：保持CPU 运转，保持屏幕高亮显示，键盘灯也保持亮度
     *
     * @param context
     */
    public void acquire(Context context) {
        if (context==null)return;
        if (powerManager == null) {
            powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "screenon");
        }
        wakeLock.acquire();
    }
    public void release() {
        if (powerManager!=null&&wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
            wakeLock = null;
            powerManager = null;
        }
    }
}