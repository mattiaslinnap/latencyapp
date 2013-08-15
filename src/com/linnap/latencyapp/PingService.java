package com.linnap.latencyapp;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class PingService extends IntentService {

    private static final int RESPONSE_SIZE = 5000;
    private static long lastStart;

    public static void startPing(Context context) {
        context.startService(new Intent(context, PingService.class));
    }

    public static long getLastStart() {
        return lastStart;
    }

    public PingService() {
        super("PingService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(App.TAG, "PingService woke up.");
        long start = System.currentTimeMillis();
        lastStart = start;

        try {
            try {
                doPing();
                long delay = System.currentTimeMillis() - start;
                Logger.logPingSuccess(this, RESPONSE_SIZE, start, delay);
            } catch (IOException e) {
                e.printStackTrace();
                Logger.logPingFailure(this, e, start);
            }
        } finally {
            StaticWakeLock.getInstance(this).release();
            Log.d(App.TAG, "PingService done.");
        }
    }

    private void doPing() throws IOException {
        HttpURLConnection connection = (HttpURLConnection)getUrl(RESPONSE_SIZE).openConnection();
        try {
            connection.setUseCaches(false);
            connection.setRequestProperty("User-Agent", "LatencyApp");
            connection.setRequestProperty("Accept-Charset", App.UTF8);
            connection.setConnectTimeout(30 * 1000);
            connection.setReadTimeout(30 * 1000);
            connection.setRequestMethod("GET");

            int status = connection.getResponseCode();
            boolean error = status >= 400;
            InputStream stream = error ? connection.getErrorStream() : connection.getInputStream();
            String responseText = IOUtils.toString(stream, App.UTF8);

            if (error)
                throw new IOException("Response status " + status);
            if (TextUtils.isEmpty(responseText))
                throw new IOException("Response is empty");
            if (responseText.length() != RESPONSE_SIZE)
                throw new IOException(String.format("Response size is %d, expected %d", responseText.length(), RESPONSE_SIZE));
        } finally {
            connection.disconnect();
        }
    }

    private URL getUrl(int responseSize) throws MalformedURLException {
        return new URL(String.format("http://ping.yousense.org/ping/%d/", responseSize));
    }

}
