package com.gpetuhov.android.rssreader.events;


// Delivers RSS feed link from FeedListFragment to PostListFragment
public class OpenFeedEvent {

    private String mFeedLink;

    public OpenFeedEvent(String feedLink) {
        mFeedLink = feedLink;
    }

    public String getFeedLink() {
        return mFeedLink;
    }
}
