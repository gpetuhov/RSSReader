package com.gpetuhov.android.rssreader;


import android.util.Log;

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

    OkHttpClient mOkHttpClient;
    Retrofit mRetrofit;

    // Keeps response from the server converted to InputStream
    // (this is needed for XMLPullParser).
    private InputStream mXMLResponse;

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

            Log.d(LOG_TAG, "Received response from the server");
            try {
                Log.d(LOG_TAG, response.body().string());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onFailure(Call<ResponseBody> call, Throwable t) {
        Log.d(LOG_TAG, "Error receiving response from the server");
    }
}
