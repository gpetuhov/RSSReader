package com.gpetuhov.android.rssreader.dagger;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.gpetuhov.android.rssreader.FeedFetcher;
import com.gpetuhov.android.rssreader.data.DataStorage;
import com.gpetuhov.android.rssreader.utils.UtilsPrefs;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;

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

    // Returns instance of DataStorage
    @Provides
    @Singleton
    DataStorage providesDataStorage(Application application, UtilsPrefs utilsPrefs) {
        DataStorage dataStorage = new DataStorage(application, utilsPrefs);
        return dataStorage;
    }

    // Returns instance of EventBus
    @Provides
    @Singleton
    EventBus providesEventBus() {
        return EventBus.getDefault();
    }

    // Returns instance of OkHttpClient
    @Provides
    @Singleton
    OkHttpClient provideOkHttpClient() {
        OkHttpClient client = new OkHttpClient();
        return client;
    }

    // Returns instance of FeedFetcher
    @Provides
    @Singleton
    FeedFetcher providesFeedFetcher(OkHttpClient okHttpClient, DataStorage dataStorage) {
        FeedFetcher feedFetcher = new FeedFetcher(okHttpClient, dataStorage);
        return feedFetcher;
    }
}
