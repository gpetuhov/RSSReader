package com.gpetuhov.android.rssreader;

import android.support.v4.app.Fragment;

// Activity for post details
public class PostActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        return new PostFragment();
    }
}
