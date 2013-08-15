package com.linnap.latencyapp;

import java.text.SimpleDateFormat;
import java.util.Date;

public class App {
    public static final String TAG = "latencyapp";
    public static final String UTF8 = "UTF-8";
    private static SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");

    public static synchronized String timeString(long millis) {
        if (millis == 0)
            return "never";
        else
            return TIME_FORMAT.format(new Date(millis));
    }

}
