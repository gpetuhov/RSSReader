package com.gpetuhov.android.rssreader;


import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;

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

    private static final String LOG_TAG = FeedFetcher.class.getName();

    private OkHttpClient mOkHttpClient;
    private Retrofit mRetrofit;

    // Keeps response from the server converted to InputStream
    // (this is needed for XMLPullParser).
    private InputStream mXMLResponse;

    private String mFeedTitle;

    // API interface to be used in Retrofit
    private interface FeedFetchService {
        @GET()
        Call<ResponseBody> getFeed(@Url String url);
        // As different RSS feeds have different URLs, we must provide full URL here,
        // not base URL, @GET(url) and query parameters as usual.
    }

    public FeedFetcher(OkHttpClient okHttpClient) {
        mOkHttpClient = okHttpClient;
    }

    public void fetchFeed(String feedLink) {

        // Build Retrofit instance for the provided link
        mRetrofit = new Retrofit.Builder()
                .client(mOkHttpClient)
                .baseUrl(feedLink)
                .build();

        // Create instance of the API interface implementation
        FeedFetchService service = mRetrofit.create(FeedFetchService.class);

        // Create call
        Call<ResponseBody> call = service.getFeed(feedLink);

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

        // TODO: Report success
    }

    @Override
    public void onFailure(Call<ResponseBody> call, Throwable t) {
        // TODO: Report error
    }

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

                // Move to first tag (start the parsing process)
                parser.nextTag();

                mFeedTitle = extractFeedTitle(parser);

                // TODO: Write feed title to storage

                // TODO: Extract posts here

            } catch (XmlPullParserException e) {
                // TODO: Report error
            } catch (IOException e) {
                // TODO: Report error
            }
        } else {
            // TODO: Report error
        }
    }

    // Extract feed title and posts from XML response
    public String extractFeedTitle(XmlPullParser parser) throws XmlPullParserException, IOException {

        // Get type of current parser event
        int event = parser.getEventType();

        // Look through entire XML response
        // (until we find feed title or reach end of document).
        while (event != XmlPullParser.END_DOCUMENT) {

            // If current event is START_TAG
            if (event == XmlPullParser.START_TAG) {
                // Get name of the tag
                String name = parser.getName();

                // If name of the tag is "title"
                if (name.equals("title")) {

                    // Move to TEXT event
                    parser.next();

                    // Get text of the current event (this is feed title)
                    String feedTitle = parser.getText();

                    // Stop searching for feed title
                    return feedTitle;
                }
            }

            // Move to next event
            event = parser.next();
        }

        // If we got here, there were no title tags in XML response (this is error)
        // TODO: Report error

        return "";
    }
}
