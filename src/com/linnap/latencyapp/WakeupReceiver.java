package com.linnap.latencyapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class WakeupReceiver extends BroadcastReceiver {
    public static final long WAKEUP_INTERVAL = 60 * 1000;

    public static void start(Context context) {
        Log.d(App.TAG, "Starting regular wakeups.");
        AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), WAKEUP_INTERVAL, getPendingIntent(context));
    }

    public static void stop(Context context) {
        Log.d(App.TAG, "Stopping regular wakeups.");
        AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(getPendingIntent(context));
        getPendingIntent(context).cancel();
    }

    private static PendingIntent getPendingIntent(Context context) {
        Intent intent = new Intent(context, WakeupReceiver.class);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(App.TAG, "WakeupReceiver woke up.");
        StaticWakeLock.getInstance(context).acquire();
        PingService.startPing(context);
    }
}
