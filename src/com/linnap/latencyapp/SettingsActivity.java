package com.linnap.latencyapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class SettingsActivity extends PreferenceActivity {

    private EditTextPreference name;
    private Preference startPing;
    private Preference stopPing;
    private Preference upload;
    private Handler handler = new Handler();

    public static String getName(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString("prefs_name", "unknown");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
        setHandlers();
    }

    @Override
    public void onResume() {
        super.onResume();
        handler.post(updater);
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(updater);
    }

    private void setHandlers() {
        name = (EditTextPreference)findPreference("prefs_name");
        startPing = findPreference("prefs_start_ping");
        stopPing = findPreference("prefs_stop_ping");
        upload = findPreference("prefs_upload");
        startPing.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                WakeupReceiver.start(getApplicationContext());
                return true;
            }
        });
        stopPing.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                WakeupReceiver.stop(getApplicationContext());
                return true;
            }
        });
        upload.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startService(new Intent(SettingsActivity.this, UploadService.class));
                return true;
            }
        });
    }

    private void updateCurrentState() {
        name.setSummary(getName(this));
        startPing.setSummary(PingService.getStatus());
        upload.setSummary(UploadService.getStatus());
    }

    Runnable updater = new Runnable() {
        @Override
        public void run() {
            updateCurrentState();
            handler.postDelayed(this, 1000);
        }
    };
}

