package com.gpetuhov.android.rssreader.events;


// Delivers post title and description to PostFragment
public class OpenPostEvent {

    private String mPostTitle;
    private String mPostDescription;

    public OpenPostEvent(String postTitle, String postDescription) {
        mPostTitle = postTitle;
        mPostDescription = postDescription;
    }

    public String getPostTitle() {
        return mPostTitle;
    }

    public String getPostDescription() {
        return mPostDescription;
    }
}
