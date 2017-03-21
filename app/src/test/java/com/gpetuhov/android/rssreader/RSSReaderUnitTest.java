package com.gpetuhov.android.rssreader;

import com.gpetuhov.android.rssreader.data.RSSFeed;

import org.junit.Test;

import static org.junit.Assert.*;

public class RSSReaderUnitTest {

    private static final String RSS_LINK = "https://habrahabr.ru/rss/best/";

    @Test
    public void rssFeedCreation_isCorrect() throws Exception {
        RSSFeed rssFeed = new RSSFeed(RSS_LINK);
        assertEquals(RSS_LINK, rssFeed.getLink());
        assertNotNull(rssFeed.getTitle());
    }
}