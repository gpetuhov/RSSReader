package com.gpetuhov.android.rssreader;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.gpetuhov.android.rssreader.data.DataStorage;
import com.gpetuhov.android.rssreader.data.RSSFeed;
import com.gpetuhov.android.rssreader.data.RSSPost;
import com.gpetuhov.android.rssreader.utils.UtilsPrefs;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class RSSReaderInstrumentedTest {

    public static final String POST_TITLE = "Post title";
    public static final String POST_DESCRIPTION = "Post description";
    public static final String FEED_TITLE = "Feed title";
    public static final String FEED_LINK = "Feed link";

    private Context mContext;
    private SharedPreferences mSharedPreferences;
    private UtilsPrefs mUtilsPrefs;

    @Before
    public void getReferences() {
        // Get context of the app under test
        mContext = InstrumentationRegistry.getTargetContext();

        // Get SharedPreferences and instantiate UtilsPrefs with it
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        mUtilsPrefs = new UtilsPrefs(mSharedPreferences);
    }

    @Test
    public void checkAppContext() throws Exception {
        assertEquals("com.gpetuhov.android.rssreader", mContext.getPackageName());
    }

    @Test
    public void firstRunFlag_isCorrect() {

        // Save flag initial value
        boolean firstRunFlagOldValue = mUtilsPrefs.isFirstRun();

        // Set flag to false
        mUtilsPrefs.setNotFirstRun();

        // Check if works properly
        assertEquals(false, mUtilsPrefs.isFirstRun());

        // Restore initial flag value
        mUtilsPrefs.setFirstRunFlagValue(firstRunFlagOldValue);
    }

    @Test
    public void checkDataStorageReadWrite() {

        // Save flag initial value
        boolean firstRunFlagOldValue = mUtilsPrefs.isFirstRun();

        DataStorage dataStorage = new DataStorage(mContext, mUtilsPrefs);
        Realm realm = dataStorage.getRealm();

        // Check if DataStorage set first run flag to false
        assertEquals(false, mUtilsPrefs.isFirstRun());

        // Write test data to storage
        realm.beginTransaction();
        // Create post
        RSSPost rssPost = realm.createObject(RSSPost.class);
        rssPost.setTitle(POST_TITLE);
        rssPost.setDescription(POST_DESCRIPTION);
        // Create feed and add created post to it
        RSSFeed rssFeed = realm.createObject(RSSFeed.class);
        rssFeed.setTitle(FEED_TITLE);
        rssFeed.setLink(FEED_LINK);
        rssFeed.getRSSPostList().add(rssPost);
        realm.commitTransaction();

        // Query Realm for just written data
        final RealmResults<RSSFeed> rssFeedsResult =
                realm.where(RSSFeed.class).equalTo("mTitle", FEED_TITLE).findAll();

        // Check if there is result
        assertNotNull(rssFeedsResult);
        assertTrue(rssFeedsResult.size() > 0);

        // Check if returned correct RSSFeed
        assertEquals(FEED_TITLE, rssFeedsResult.get(0).getTitle());
        assertEquals(FEED_LINK, rssFeedsResult.get(0).getLink());

        // Get posts list from result feed
        RSSPost rssPostResult = rssFeedsResult.get(0).getRSSPostList().get(0);

        // Cehck if returned correct RSSPost
        assertEquals(POST_TITLE, rssPostResult.getTitle());
        assertEquals(POST_DESCRIPTION, rssPostResult.getDescription());

        // Delete written data from Realm
        realm.beginTransaction();
        rssFeedsResult.deleteAllFromRealm(); // This will delete ONLY feed, NOT post
        realm.commitTransaction();

        // Check if feed deleted successfully
        assertTrue(rssFeedsResult.size() == 0);

        // Query for just written post
        final RealmResults<RSSPost> rssPostsResult =
                realm.where(RSSPost.class).equalTo("mTitle", POST_TITLE).findAll();

        // Check if post still remains in Realm
        assertTrue(rssPostsResult.size() > 0);

        // Delete post from Realm
        realm.beginTransaction();
        rssPostsResult.deleteAllFromRealm();
        realm.commitTransaction();

        // Check if post deleted successfully
        assertTrue(rssPostsResult.size() == 0);

        // Restore initial first run flag value
        mUtilsPrefs.setFirstRunFlagValue(firstRunFlagOldValue);
    }

    @Test
    public void checkDataStorageDefaultListCreation() {

        // Realm initialization must be done once.
        Realm.init(mContext);

        // Create Realm configuration for the test realm file
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder()
                .name("testRealm.realm")
                .build();

        // Get Realm instance
        Realm realm = Realm.getInstance(realmConfiguration);

        // Save first run flag value
        boolean firstRunFlagOldValue = mUtilsPrefs.isFirstRun();

        // Imitate app's first run
        mUtilsPrefs.setFirstRunFlagValue(true);

        // Create DataStorage instance and set Realm for it
        DataStorage dataStorage = new DataStorage(mContext, mUtilsPrefs, realm);
        // After this default feed list must be written to storage

        // Get default feed titles and links from resources
        String[] defaultFeedTitles =
                mContext.getResources().getStringArray(R.array.default_rss_feed_titles);
        String[] defaultFeedLinks =
                mContext.getResources().getStringArray(R.array.default_rss_feed_links);

        // For all titles
        for (int i = 0; i < defaultFeedTitles.length; i++) {
            // Find feed in Realm with i-th title
            RealmResults<RSSFeed> rssFeeds =
                    realm.where(RSSFeed.class).equalTo("mTitle", defaultFeedTitles[i]).findAll();

            // Check if result exists and there is only 1 result
            assertNotNull(rssFeeds);
            assertTrue(rssFeeds.size() == 1);

            // Check if link of i-th feed equals to value in initial array
            assertEquals(defaultFeedLinks[i], rssFeeds.get(0).getLink());
        }

        // Restore first run flag value
        mUtilsPrefs.setFirstRunFlagValue(firstRunFlagOldValue);

        // Realm instance must be closed
        realm.close();

        // Delete test Realm file
        Realm.deleteRealm(realmConfiguration);
    }
}
