package com.linnap.latencyapp;

import android.content.Context;
import android.util.Log;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class Logger {

    public static void logPingSuccess(Context context, int responseSize, long start, long delay) {
        append(context, String.format("Ping success. size=%db, delay=%dms, start=%s", responseSize, delay, App.timeString(start)));
    }

    public static void logPingFailure(Context context, IOException problem, long start) {
        append(context, String.format("Ping fail. start=%s, exception=%s", App.timeString(start), problem.toString()));
    }

    public static File getFile(Context context) {
        return new File(context.getCacheDir(), "log.txt");
    }

    private static synchronized void append(Context context, String message) {
        String name = SettingsFragment.getName(context);
        String line = String.format("%s %s: %s\n", name, App.timeString(System.currentTimeMillis()), message);
        Log.i(App.TAG, line);

        try {
            FileUtils.writeStringToFile(getFile(context), line, App.UTF8, true);
        } catch (IOException e) {
            Log.e(App.TAG, "Failed to write log", e);
        }
    }
}
