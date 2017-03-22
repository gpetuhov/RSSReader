package com.gpetuhov.android.rssreader.dagger;

import com.gpetuhov.android.rssreader.FeedListFragment;
import com.gpetuhov.android.rssreader.PostListFragment;

import javax.inject.Singleton;

import dagger.Component;

// Dagger component tells, into which classes instances instantiated by Module will be injected.
@Singleton
@Component(modules = AppModule.class)
public interface AppComponent {
    void inject(FeedListFragment feedListFragment);
    void inject(PostListFragment postListFragment);
}
