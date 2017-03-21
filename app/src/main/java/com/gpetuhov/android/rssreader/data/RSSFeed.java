package com.gpetuhov.android.rssreader.data;


import io.realm.RealmList;
import io.realm.RealmObject;

public class RSSFeed extends RealmObject {

    private String mTitle;

    private String mLink;

    private RealmList<RSSPost> mRSSPostList;

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getLink() {
        return mLink;
    }

    public void setLink(String link) {
        mLink = link;
    }

    public RealmList<RSSPost> getRSSPostList() {
        return mRSSPostList;
    }

    public void setRSSPostList(RealmList<RSSPost> RSSPostList) {
        mRSSPostList = RSSPostList;
    }
}
