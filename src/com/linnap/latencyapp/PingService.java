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

    private static String status = "";
    public static String getStatus() {
        return status;
    }

    public static void startPing(Context context) {
        context.startService(new Intent(context, PingService.class));
    }

    public PingService() {
        super("PingService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(App.TAG, "PingService woke up.");
        long start = System.currentTimeMillis();

        try {
            try {
                doPing();
                long delay = System.currentTimeMillis() - start;
                Logger.logPingSuccess(this, RESPONSE_SIZE, start, delay);
                setStatus("Success.");
            } catch (IOException e) {
                e.printStackTrace();
                Logger.logPingFailure(this, e, start);
                setStatus("Failed: " + e.toString());
            }
        } finally {
            StaticWakeLock.getInstance(this).release();
            Log.d(App.TAG, "PingService done.");
        }
    }

    private void doPing() throws IOException {
        System.setProperty("http.keepAlive", "false");
        HttpURLConnection connection = (HttpURLConnection)getUrl(RESPONSE_SIZE).openConnection();
        try {
            connection.setUseCaches(false);
            connection.setRequestProperty("User-Agent", "LatencyApp");
            connection.setRequestProperty("Accept-Charset", App.UTF8);
            connection.setRequestProperty("Accept-Encoding", "identity");
            connection.setRequestProperty("Connection", "close");
            connection.setConnectTimeout(30 * 1000);
            connection.setReadTimeout(30 * 1000);
            connection.setRequestMethod("GET");

            int status = connection.getResponseCode();
            boolean error = status >= 400;
            InputStream stream = error ? connection.getErrorStream() : connection.getInputStream();
            String responseText = IOUtils.toString(stream, App.UTF8);

            if (error)
                throw new IOException("Response status " + status);
            if (responseText == null)
                throw new IOException(String.format("Response is null (status %d)", status));
            if (TextUtils.isEmpty(responseText))
                throw new IOException(String.format("Response is empty (status %d)", status));
            if (responseText.length() != RESPONSE_SIZE)
                throw new IOException(String.format("Response size is %d, expected %d", responseText.length(), RESPONSE_SIZE));
        } finally {
            connection.disconnect();
        }
    }

    private URL getUrl(int responseSize) throws MalformedURLException {
        return new URL(String.format("http://ping.yousense.org/ping/%d/", responseSize));
    }

    private static void setStatus(String message) {
        status = App.timeString(System.currentTimeMillis()) + ": " + message;
    }
}
