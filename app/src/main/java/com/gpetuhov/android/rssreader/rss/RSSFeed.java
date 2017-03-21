package com.gpetuhov.android.rssreader.rss;


public class RSSFeed {

    private String mTitle;

    private String mLink;

    public RSSFeed(String link) {
        mTitle = link;  // TODO: Get Title from RSS XML
        mLink = link;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getLink() {
        return mLink;
    }
}
