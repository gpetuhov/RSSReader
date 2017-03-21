package com.gpetuhov.android.rssreader;

import android.support.v4.app.Fragment;

public class FeedListActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new FeedListFragment();
    }
}
