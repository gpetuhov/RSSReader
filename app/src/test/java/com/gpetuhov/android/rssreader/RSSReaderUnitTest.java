package com.gpetuhov.android.rssreader;


import com.gpetuhov.android.rssreader.data.RSSFeed;
import com.gpetuhov.android.rssreader.data.RSSPost;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RSSReaderUnitTest {

    private static final String RSS_TITLE = "Feed title";
    private static final String RSS_LINK = "https://habrahabr.ru/rss/best/";

    private static final String RSS_POST_TITLE = "Post title";
    private static final String RSS_POST_DESC = "Dummy text";

    @Test
    public void rssFeedCreation_isCorrect() throws Exception {
        RSSFeed rssFeed = new RSSFeed();
        rssFeed.setTitle(RSS_TITLE);
        rssFeed.setLink(RSS_LINK);
        assertEquals(RSS_TITLE, rssFeed.getTitle());
        assertEquals(RSS_LINK, rssFeed.getLink());
    }

    @Test
    public void rssPostCreation_isCorrect() throws Exception {
        RSSPost rssPost = new RSSPost();
        rssPost.setTitle(RSS_POST_TITLE);
        rssPost.setDescription(RSS_POST_DESC);
        assertEquals(RSS_POST_TITLE, rssPost.getTitle());
        assertEquals(RSS_POST_DESC, rssPost.getDescription());
    }
}
