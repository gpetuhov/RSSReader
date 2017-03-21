package com.gpetuhov.android.rssreader.dagger;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.gpetuhov.android.rssreader.utils.UtilsPrefs;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

// Dagger module tells, what instances will be instantiated
@Module
public class AppModule {

    Application mApplication;

    public AppModule(Application application) {
        mApplication = application;
    }

    // Returns instance of Application class
    @Provides
    @Singleton
    Application providesApplication() {
        return mApplication;
    }

    // Returns instance of default SharedPreferences.
    // This instance will be instantiated only once and will exist during entire application lifecycle.
    @Provides
    @Singleton
    SharedPreferences providesDefaultSharedPreferences(Application application) {
        return PreferenceManager.getDefaultSharedPreferences(application);
    }

    // Returns instance of UtilsPrefs
    @Provides
    @Singleton
    UtilsPrefs providesUtilsPrefs(SharedPreferences sharedPreferences) {
        UtilsPrefs utilsPrefs = new UtilsPrefs(sharedPreferences);
        return utilsPrefs;
    }
}