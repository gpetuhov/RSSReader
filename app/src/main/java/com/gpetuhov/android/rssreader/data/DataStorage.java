package com.gpetuhov.android.rssreader.data;


import android.content.Context;

import com.gpetuhov.android.rssreader.R;
import com.gpetuhov.android.rssreader.utils.UtilsPrefs;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;

// Controls data storage for offline use
public class DataStorage {

    private Context mContext;
    private UtilsPrefs mUtilsPrefs;
    private Realm mRealm;

    // Constructor for use in app
    public DataStorage(Context context, UtilsPrefs utilsPrefs) {

        mContext = context;
        mUtilsPrefs = utilsPrefs;

        // Realm initialization must be done once.
        Realm.init(context);

        // Get Realm instance
        mRealm = Realm.getDefaultInstance();

        initStorage();
    }

    // Constructor for testing.
    // Realm must be provided by user.
    public DataStorage(Context context, UtilsPrefs utilsPrefs, Realm realm) {
        mContext = context;
        mUtilsPrefs = utilsPrefs;
        mRealm = realm;

        initStorage();
    }

    private void initStorage() {
        // Check if this is the first time the app runs on the device
        if (mUtilsPrefs.isFirstRun()) {
            // Create default RSS feed list
            createDefaultFeeds();
            // Reset first run flag
            mUtilsPrefs.setNotFirstRun();
        }
    }

    // Write initial list of RSS feeds to storage
    private void createDefaultFeeds() {
        // Get default RSS feed titles and links from resources
        String[] defaultRSSFeedTitles =
                mContext.getResources().getStringArray(R.array.default_rss_feed_titles);
        String[] defaultRSSFeedLinks =
                mContext.getResources().getStringArray(R.array.default_rss_feed_links);

        // Write default RSS feeds to Realm
        mRealm.beginTransaction();
        for (int i = 0; i < defaultRSSFeedTitles.length; i++) {
            RSSFeed rssFeed = mRealm.createObject(RSSFeed.class);
            rssFeed.setTitle(defaultRSSFeedTitles[i]);
            rssFeed.setLink(defaultRSSFeedLinks[i]);
        }
        mRealm.commitTransaction();
    }

    public Realm getRealm() {
        return mRealm;
    }

    // Get list of all RSS feeds in the storage
    public RealmResults<RSSFeed> getFeedList() {
        return mRealm.where(RSSFeed.class).findAll();
    }

    // Get list of posts in the feed with the given link
    public RealmList<RSSPost> getPostList(String feedLink) {

        // Query Realm for RSS feed with provided link
        final RSSFeed rssFeed =
                mRealm.where(RSSFeed.class).equalTo("mLink", feedLink).findFirst();

        // Check if result exists
        if (rssFeed != null) {
            // If feed exists, return list of posts from it
            return rssFeed.getRSSPostList();
        } else {
            // Otherwise return empty list
            return new RealmList<>();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();

        if (mRealm != null) {
            // All Realm instances must be closed.
            // We have 1 instance, which is opened in constructor, and we close it here.
            mRealm.close();
        }
    }
}
