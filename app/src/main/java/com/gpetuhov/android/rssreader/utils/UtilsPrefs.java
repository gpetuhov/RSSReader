package com.gpetuhov.android.rssreader.utils;

import android.content.SharedPreferences;

// Utilities for SharedPreferences
public class UtilsPrefs {

    // Key for first run flag in SharedPreferences
    private static final String PREF_KEY_FIRST_RUN = "first_run_flag";

    private SharedPreferences mSharedPreferences;

    public UtilsPrefs(SharedPreferences sharedPreferences) {
        mSharedPreferences = sharedPreferences;
    }

    // Return true if app runs on device for the first time
    public boolean isFirstRun() {
        // Get flag from SharedPrefs. If flag not exists (first run), return true
        boolean firstRun = getBooleanFromSharedPreferences(PREF_KEY_FIRST_RUN, true);
        return firstRun;
    }

    // Set first run flag to false (not first run)
    public void setNotFirstRun() {
        putBooleanToSharedPreferences(PREF_KEY_FIRST_RUN, false);
    }

    public void setFirstRunFlagValue(boolean value) {
        putBooleanToSharedPreferences(PREF_KEY_FIRST_RUN, value);
    }

    private boolean getBooleanFromSharedPreferences(String key, boolean defValue) {
        return mSharedPreferences.getBoolean(key, defValue);
    }

    private void putBooleanToSharedPreferences(String key, boolean value) {
        mSharedPreferences
                .edit()
                .putBoolean(key, value)
                .apply();
    }
}
