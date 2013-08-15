package com.linnap.latencyapp;

import android.content.Context;
import android.os.PowerManager;

public class StaticWakeLock {
    private static StaticWakeLock instance;

    public static synchronized StaticWakeLock getInstance(Context context) {
        if (instance == null)
            instance = new StaticWakeLock(context);
        return instance;
    }

    private PowerManager.WakeLock wakeLock;

    private StaticWakeLock(Context context) {
        PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "StaticWakeLock");
        wakeLock.setReferenceCounted(true);
    }

    public void acquire() {
        wakeLock.acquire();
    }

    public void release() {
        wakeLock.release();
    }
}
