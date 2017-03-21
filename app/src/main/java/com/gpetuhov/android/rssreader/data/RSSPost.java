package com.gpetuhov.android.rssreader.data;


import io.realm.RealmObject;

public class RSSPost extends RealmObject {

    private String mTitle;
    private String mDescription;

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        mDescription = description;
    }
}
