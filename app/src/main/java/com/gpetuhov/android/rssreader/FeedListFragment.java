package com.gpetuhov.android.rssreader;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gpetuhov.android.rssreader.data.RSSFeed;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


// Fragment with list of RSS feeds
public class FeedListFragment extends Fragment {

    // RecyclerView for RSS feed list
    @BindView(R.id.feed_list_recycler_view) RecyclerView mFeedListRecyclerView;

    // Keeps Unbinder object to properly unbind views in onDestroyView of the fragment
    private Unbinder mUnbinder;

    // Adapter for the RecyclerView
    private FeedAdapter mFeedAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_feed_list, container, false);

        // Bind views and save reference to Unbinder object
        mUnbinder = ButterKnife.bind(this, v);

        // Set LinearLayoutManager for our RecyclerView (we need vertical scroll list)
        mFeedListRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        // Create new adapter for the RecyclerView
        mFeedAdapter = new FeedAdapter(createDummyFeedList());

        // Attach adapter to the RecyclerView
        mFeedListRecyclerView.setAdapter(mFeedAdapter);

        return v;
    }

    private List<RSSFeed> createDummyFeedList() {
        String[] feedLinks = {
                "https://habrahabr.ru/rss/best/",
                "https://habrahabr.ru/rss/best/weekly/",
                "https://habrahabr.ru/rss/best/monthly/",
                "https://habrahabr.ru/rss/best/alltime/"
        };

        List<RSSFeed> rssFeeds = new ArrayList<>();

        for (String feedLink : feedLinks) {
            RSSFeed rssFeed = new RSSFeed(feedLink);
            rssFeeds.add(rssFeed);
        }

        return rssFeeds;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // This is recommended to do here when using Butterknife in fragments
        mUnbinder.unbind();
    }


    // === RECYCLERVIEW VIEWHOLDER AND ADAPTER =====

    class FeedHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        // Keeps RSS feed list item
        private RSSFeed mRSSFeed;

        // TextView for RSS Feed title
        @BindView(R.id.rss_feed_title) public TextView mRSSFeedTitleTextView;

        public FeedHolder(View itemView) {
            super(itemView);

            itemView.setOnClickListener(this);

            // Get access to TextViews in itemView
            ButterKnife.bind(this, itemView);
        }

        public void bindFeed(RSSFeed rssFeed) {
            mRSSFeed = rssFeed;
            mRSSFeedTitleTextView.setText(rssFeed.getTitle());
        }

        @Override
        public void onClick(View v) {
            // TODO: Implement item clicks here
        }
    }

    private class FeedAdapter extends RecyclerView.Adapter<FeedHolder> {

        private List<RSSFeed> mRSSFeeds;

        public FeedAdapter(List<RSSFeed> RSSFeeds) {
            mRSSFeeds = RSSFeeds;
        }

        @Override
        public FeedHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            // Get LayoutInflater from parent activity
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());

            // Create view for one list item from item layout
            View view = layoutInflater.inflate(R.layout.list_item_feed, parent, false);

            // Create ViewHolder with inflated view for one list item
            return new FeedHolder(view);
        }

        @Override
        public void onBindViewHolder(FeedHolder holder, int position) {
            RSSFeed rssFeed = mRSSFeeds.get(position);
            holder.bindFeed(rssFeed);
        }

        @Override
        public int getItemCount() {
            if (mRSSFeeds != null) {
                return mRSSFeeds.size();
            } else {
                return 0;
            }
        }
    }
}