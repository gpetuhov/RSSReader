package com.gpetuhov.android.rssreader.data;


import android.content.Context;

import com.gpetuhov.android.rssreader.R;
import com.gpetuhov.android.rssreader.utils.UtilsPrefs;

import java.util.List;

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
        final RSSFeed rssFeed = getFeed(feedLink);

        // Check if result exists
        if (rssFeed != null) {
            // If feed exists, return list of posts from it
            return rssFeed.getRSSPostList();
        } else {
            // Otherwise return empty list
            return new RealmList<>();
        }
    }

    public RSSFeed getFeed(String feedLink) {
        return mRealm.where(RSSFeed.class).equalTo("mLink", feedLink).findFirst();
    }

    public void addFeed(String link) {
        mRealm.beginTransaction();
        RSSFeed rssFeed = mRealm.createObject(RSSFeed.class);
        rssFeed.setLink(link);
        mRealm.commitTransaction();
    }

    // Set title of the feed with the given link.
    // Return true on success.
    public boolean setFeedTitle(String feedLink, String newTitle) {

        // Query Realm for RSS feed with provided link
        final RSSFeed rssFeed = getFeed(feedLink);

        // Check if result exists
        if (rssFeed != null) {
            // If feed exists, update its title
            mRealm.beginTransaction();
            rssFeed.setTitle(newTitle);
            mRealm.commitTransaction();
            return true;
        } else {
            // Otherwise return false
            return false;
        }
    }

    // Update feed with new title and posts.
    // If the feed does not exist, create it.
    public void updateFeed(String feedLink, String newTitle, List<RSSPost> newPosts) {

        // Get feed with provided link
        RSSFeed rssFeed = getFeed(feedLink);

        mRealm.beginTransaction();

        // If no such feed in storage
        if (null == rssFeed) {
            // Create it
            rssFeed = mRealm.createObject(RSSFeed.class);
            rssFeed.setTitle(newTitle);
            rssFeed.setLink(feedLink);
        } else {
            // Otherwise update its title
            rssFeed.setTitle(newTitle);
        }

        // Get list of old posts
        RealmList<RSSPost> rssPosts = rssFeed.getRSSPostList();

        // Delete old posts
        rssPosts.deleteAllFromRealm();

        // Add new posts
        for (RSSPost newPost : newPosts) {
            RSSPost rssPost = mRealm.createObject(RSSPost.class);
            rssPost.setTitle(newPost.getTitle());
            rssPost.setDescription(newPost.getDescription());
            rssPosts.add(rssPost);
        }

        mRealm.commitTransaction();
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
