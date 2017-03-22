package com.gpetuhov.android.rssreader.events;

// Signals of error during feed fetching
public class FeedFetchErrorEvent {

    String errorMessage;

    public FeedFetchErrorEvent(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
