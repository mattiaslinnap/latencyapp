package com.linnap.latencyapp;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.text.TextUtils;
import android.util.Log;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class UploadService extends IntentService {

    private static String status = "";
    public static String getStatus() {
        return status;
    }

    public UploadService() {
        super("UploadService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        PowerManager.WakeLock wakeLock = ((PowerManager)getSystemService(Context.POWER_SERVICE)).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "UploadWakeLock");
        wakeLock.setReferenceCounted(false);
        wakeLock.acquire();

        try {
            File file = Logger.getFile(this);
            if (file.exists()) {
                doUpload(file, SettingsFragment.getName(this));
                setStatus("Success.");
            } else {
                setStatus("No log file.");
            }
        } catch (IOException e) {
            Log.d(App.TAG, "Upload failed", e);
            setStatus("Failed: " + e.toString());
        } finally {
            wakeLock.release();
        }
    }

    private void doUpload(File file, String name) throws IOException {
        HttpURLConnection connection = (HttpURLConnection)getUrl(name).openConnection();
        try {
            connection.setUseCaches(false);
            connection.setRequestProperty("User-Agent", "LatencyApp");
            connection.setRequestProperty("Accept-Charset", App.UTF8);
            connection.setConnectTimeout(30 * 1000);
            connection.setReadTimeout(30 * 1000);
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setFixedLengthStreamingMode((int)file.length());
            FileUtils.copyFile(file, connection.getOutputStream());
            connection.getOutputStream().flush();

            int status = connection.getResponseCode();
            boolean error = status >= 400;
            InputStream stream = error ? connection.getErrorStream() : connection.getInputStream();
            String responseText = IOUtils.toString(stream, App.UTF8);

            if (error)
                throw new IOException("Response status " + status);
            if (TextUtils.isEmpty(responseText))
                throw new IOException("Response is empty");

            if ("OK".equals(responseText)) {
                file.delete();  // Race condition, but don't care.
            } else {
                throw new IOException("Unexpected response: " + responseText);
            }
        } finally {
            connection.disconnect();
        }
    }

    private URL getUrl(String name) throws MalformedURLException {
        return new URL(String.format("http://ping.yousense.org/upload/%s/", name));
    }

    private static void setStatus(String message) {
        status = App.timeString(System.currentTimeMillis()) + ": " + message;
    }
}
