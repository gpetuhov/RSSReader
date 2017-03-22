package com.gpetuhov.android.rssreader;


import android.util.Xml;

import com.gpetuhov.android.rssreader.data.DataStorage;
import com.gpetuhov.android.rssreader.data.RSSPost;
import com.gpetuhov.android.rssreader.events.FeedFetchErrorEvent;
import com.gpetuhov.android.rssreader.events.FeedFetchSuccessEvent;

import org.greenrobot.eventbus.EventBus;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.GET;
import retrofit2.http.Url;

// Fetches XML for the RSS feed
public class FeedFetcher implements Callback<ResponseBody> {

    private OkHttpClient mOkHttpClient;
    private DataStorage mDataStorage;
    private EventBus mEventBus;

    private Retrofit mRetrofit;

    // Keeps response from the server converted to InputStream
    // (this is needed for XMLPullParser).
    private InputStream mXMLResponse;

    // Feed title, link and posts
    private String mFeedTitle;
    private String mFeedLink;
    private List<RSSPost> mRSSPosts;

    // API interface to be used in Retrofit
    private interface FeedFetchService {
        @GET()
        Call<ResponseBody> getFeed(@Url String url);
        // As different RSS feeds have different URLs, we must provide full URL here,
        // not base URL, @GET(url) and query parameters as usual.
    }

    public FeedFetcher(OkHttpClient okHttpClient, DataStorage dataStorage, EventBus eventBus) {
        mOkHttpClient = okHttpClient;
        mDataStorage = dataStorage;
        mEventBus = eventBus;
    }

    public void fetchFeed(String feedLink) {

        mFeedLink = feedLink;

        // Build Retrofit instance for the provided link
        mRetrofit = new Retrofit.Builder()
                .client(mOkHttpClient)
                .baseUrl(mFeedLink)
                .build();

        // Create instance of the API interface implementation
        FeedFetchService service = mRetrofit.create(FeedFetchService.class);

        // Create call
        Call<ResponseBody> call = service.getFeed(mFeedLink);

        // Execute call asynchronously
        // (retrofit performs and handles the method execution in a separate thread).
        // If no converter is specified, Retrofit returns OkHttp ResponseBody.
        call.enqueue(this);
    }

    // === RETROFIT CALLBACKS =====

    @Override
    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

        if (response != null) {
            // Get OkHttp ResponseBody from Retrofit Response and convert it to InputStream
            mXMLResponse = response.body().byteStream();

            // Parse received response
            parseXMLResponse();
        }
    }

    @Override
    public void onFailure(Call<ResponseBody> call, Throwable t) {
        reportError("Error fetching feed from network");
    }

    // === XML PARSING =====

    private void parseXMLResponse() {

        // Check if there is response
        if (mXMLResponse != null) {
            try {
                // Create new XML parser (ExpatPullParser is used)
                XmlPullParser parser = Xml.newPullParser();

                // Do not process namespaces
                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);

                // Set received response as input for the parser
                parser.setInput(mXMLResponse, null);

                // Extract feed title
                mFeedTitle = extractFeedTitle(parser);

                // If feed title is empty, report error
                if (mFeedTitle.equals("")) {
                    reportError("Error extracting feed title");
                    return;
                }

                // Extract feed posts
                mRSSPosts = extractFeedPosts(parser);

                if (null == mRSSPosts) {
                    reportErrorExtractingPosts();
                    return;
                } else if (mRSSPosts.size() == 0) {
                    reportErrorExtractingPosts();
                    return;
                }

                // Write extracted feed to storage
                mDataStorage.updateFeed(mFeedLink, mFeedTitle, mRSSPosts);

                reportSuccess();

            } catch (XmlPullParserException | IOException e) {
                reportErrorParsingXML();
            }
        } else {
            reportError("Received no XML response");
        }
    }

    // Extract feed title from XML response
    public String extractFeedTitle(XmlPullParser parser) throws XmlPullParserException, IOException {
        return extractTagText("title", parser);
    }

    private String extractTagText(String tagName, XmlPullParser parser) throws XmlPullParserException, IOException {

        boolean isFound = moveToTag(tagName, parser);

        if (isFound) {
            // Tag found
            // Move to TEXT event
            parser.next();
            // Get text of the current event
            return parser.getText();
        } else {
            // Tag not found, return empty string;
            return "";
        }
    }

    // Move to tag with provided name.
    // Return true if tag is found or false otherwise.
    private boolean moveToTag(String tagName, XmlPullParser parser) throws XmlPullParserException, IOException {

        // Get type of current parser event
        int event = parser.getEventType();

        // Look through entire XML response
        while (event != XmlPullParser.END_DOCUMENT) {

            // If current event is START_TAG
            if (event == XmlPullParser.START_TAG) {
                // Get name of the tag
                String name = parser.getName();

                if (name.equals(tagName)) {
                    // Tag with provided name found.
                    // Stop searching and return true.
                    return true;
                }
            }

            // Move to next event
            event = parser.next();
        }

        // If we got here, no tags with provided name found in XML.
        // Return false.
        return false;
    }

    // Extract feed posts from XML response
    public List<RSSPost> extractFeedPosts(XmlPullParser parser) throws XmlPullParserException, IOException {

        List<RSSPost> rssPosts = new ArrayList<>();

        boolean isFound = moveToTag("item", parser);

        while (isFound) {
            RSSPost rssPost = new RSSPost();

            String postTitle = extractTagText("title", parser);
            if (postTitle.equals("")) {
                return rssPosts;
            }
            rssPost.setTitle(postTitle);

            String postDescription = extractTagText("description", parser);
            if (postDescription.equals("")) {
                return rssPosts;
            }
            rssPost.setDescription(postDescription);

            rssPosts.add(rssPost);

            isFound = moveToTag("item", parser);
        }

        return rssPosts;
    }

    // === REPORT SUCCESS OR ERROR =====

    private void reportSuccess() {
        mEventBus.post(new FeedFetchSuccessEvent());
    }

    private void reportError(String errorMessage) {
        mEventBus.post(new FeedFetchErrorEvent(errorMessage));
    }

    private void reportErrorExtractingPosts() {
        reportError("Error extracting feed posts");
    }

    private void reportErrorParsingXML() {
        reportError("Error parsing XML");
    }
}
