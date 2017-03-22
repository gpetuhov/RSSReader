package com.gpetuhov.android.rssreader;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Xml;

import com.gpetuhov.android.rssreader.data.DataStorage;
import com.gpetuhov.android.rssreader.data.RSSFeed;
import com.gpetuhov.android.rssreader.data.RSSPost;
import com.gpetuhov.android.rssreader.events.OpenFeedEvent;
import com.gpetuhov.android.rssreader.utils.UtilsPrefs;

import org.greenrobot.eventbus.EventBus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import okhttp3.OkHttpClient;

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

    private boolean mFirstRunFlagOldValue;

    // Realm configuration for test Realm file
    RealmConfiguration mTestRealmConfiguration;

    // Realm instance for test Realm file
    private Realm mTestRealm;

    private XmlPullParser mXmlPullParser;

    @Before
    public void initTest() {
        // Get context of the app under test
        mContext = InstrumentationRegistry.getTargetContext();

        // Get SharedPreferences and instantiate UtilsPrefs with it
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        mUtilsPrefs = new UtilsPrefs(mSharedPreferences);

        saveFirstRunFlagInitialValue();

        createTestRealm();
    }

    private void saveFirstRunFlagInitialValue() {
        mFirstRunFlagOldValue = mUtilsPrefs.isFirstRun();
    }

    private void restoreFirstRunFlagInitialValue() {
        mUtilsPrefs.setFirstRunFlagValue(mFirstRunFlagOldValue);
    }

    // Create Realm test file
    private void createTestRealm() {
        // Realm initialization must be done once.
        Realm.init(mContext);

        // Create Realm configuration for the test realm file
        mTestRealmConfiguration = new RealmConfiguration.Builder()
                .name("testRealm.realm")
                .build();

        // Get Realm instance
        mTestRealm = Realm.getInstance(mTestRealmConfiguration);
    }

    @Test
    public void checkAppContext() throws Exception {
        assertEquals("com.gpetuhov.android.rssreader", mContext.getPackageName());
    }

    @Test
    public void firstRunFlag_isCorrect() {
        // Set flag to false
        mUtilsPrefs.setNotFirstRun();

        // Check if works properly
        assertEquals(false, mUtilsPrefs.isFirstRun());
    }


    @Test
    public void checkDataStorageReadWrite() {

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
    }

    @Test
    public void checkDataStorageDefaultListCreation() {

        // Imitate app's first run
        mUtilsPrefs.setFirstRunFlagValue(true);

        // Create DataStorage instance and set Realm for it
        DataStorage dataStorage = new DataStorage(mContext, mUtilsPrefs, mTestRealm);
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
                    mTestRealm.where(RSSFeed.class).equalTo("mTitle", defaultFeedTitles[i]).findAll();

            // Check if result exists and there is only 1 result
            assertNotNull(rssFeeds);
            assertTrue(rssFeeds.size() == 1);

            // Check if link of i-th feed equals to value in initial array
            assertEquals(defaultFeedLinks[i], rssFeeds.get(0).getLink());
        }
    }

    @Test
    public void checkOpenFeedEventCreation() {
        // Get instance of EventBus
        EventBus eventBus = EventBus.getDefault();

        // Create new OpenFeedEvent and post it to EventBus
        eventBus.postSticky(new OpenFeedEvent(FEED_LINK));

        // Manually get event from EventBus and remove it.
        OpenFeedEvent openFeedEvent = eventBus.removeStickyEvent(OpenFeedEvent.class);

        // Event must be not null, as we have just posted it.
        assertNotNull(openFeedEvent);

        // We must get event, that we have just posted.
        assertEquals(FEED_LINK, openFeedEvent.getFeedLink());
    }

    @Test
    public void checkGetPostListFromStorage() {

        // Write test data to Realm
        mTestRealm.beginTransaction();
        // Create post
        RSSPost rssPost = mTestRealm.createObject(RSSPost.class);
        rssPost.setTitle(POST_TITLE);
        rssPost.setDescription(POST_DESCRIPTION);
        // Create feed
        RSSFeed rssFeed = mTestRealm.createObject(RSSFeed.class);
        rssFeed.setTitle(FEED_TITLE);
        rssFeed.setLink(FEED_LINK);
        // Add post to feed
        rssFeed.getRSSPostList().add(rssPost);
        mTestRealm.commitTransaction();

        // Create DataStorage instance and set Realm for it
        DataStorage dataStorage = new DataStorage(mContext, mUtilsPrefs, mTestRealm);

        // Get list of posts for just created feed
        List<RSSPost> rssPostList = dataStorage.getPostList(rssFeed.getLink());

        // Check if result exists and there is only one post in the list
        assertNotNull(rssPostList);
        assertTrue(rssPostList.size() == 1);

        // Check if the post in result is the one we have just written
        assertEquals(POST_TITLE, rssPostList.get(0).getTitle());
        assertEquals(POST_DESCRIPTION, rssPostList.get(0).getDescription());
    }

    @Test
    public void checkExtractFeedTitleFromXML() {

        String expectedFeedTitle = "Awesome feed title";

        // Sample XML response
        String xmlString =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<rss version=\"2.0\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\"  >\n" +
                "<channel>\n" +
                "\t<title>" + expectedFeedTitle + "</title>\n" +
                "\t<link>https://server.com/</link>\n" +
                "</channel>\n" +
                "</rss>";

        // Create InputStream from String
        InputStream inputStream = new ByteArrayInputStream(xmlString.getBytes());

        try {
            createParser(inputStream);

            // Extract feed title from sample XML
            String feedTitle =
                    new FeedFetcher(new OkHttpClient()).extractFeedTitle(mXmlPullParser);

            // Check if extracted title is the same as in sample XML
            assertEquals(expectedFeedTitle, feedTitle);

        } catch (XmlPullParserException e) {
        } catch (IOException e) {
        }
    }

    // Create XML parser for provided input stream
    private void createParser(InputStream input) throws XmlPullParserException {
        // Create new XML parser (ExpatPullParser is used)
        mXmlPullParser = Xml.newPullParser();

        // Do not process namespaces
        mXmlPullParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);

        // Set input for the parser
        mXmlPullParser.setInput(input, null);
    }

    @After
    public void afterTest() {
        deleteTestRealm();
        restoreFirstRunFlagInitialValue();
    }

    private void deleteTestRealm() {
        // Test Realm instance must be closed
        mTestRealm.close();

        // Delete test Realm file
        Realm.deleteRealm(mTestRealmConfiguration);
    }
}
