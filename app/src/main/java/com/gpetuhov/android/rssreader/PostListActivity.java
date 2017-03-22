package com.gpetuhov.android.rssreader;


import android.support.v4.app.Fragment;

// Activity for list of RSS posts in the feed
public class PostListActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new PostListFragment();
    }
}
