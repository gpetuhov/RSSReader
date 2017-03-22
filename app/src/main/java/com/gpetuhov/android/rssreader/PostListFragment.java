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

import com.gpetuhov.android.rssreader.data.DataStorage;
import com.gpetuhov.android.rssreader.data.RSSPost;
import com.gpetuhov.android.rssreader.events.OpenFeedEvent;
import com.gpetuhov.android.rssreader.utils.UtilsNet;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

// Fragment for list of RSS posts in the feed
public class PostListFragment extends Fragment {

    // Dependencies injected by Dagger
    @Inject DataStorage mDataStorage;
    @Inject EventBus mEventBus;
    @Inject FeedFetcher mFeedFetcher;

    // RecyclerView for posts list
    @BindView(R.id.post_list_recycler_view) RecyclerView mPostListRecyclerView;

    // TextView to display when no data available
    @BindView(R.id.empty_view) TextView mEmptyTextView;

    // Keeps Unbinder object to properly unbind views in onDestroyView of the fragment
    private Unbinder mUnbinder;

    // Adapter for the RecyclerView
    private PostAdapter mPostAdapter;

    // Keeps RSS feed link
    private String mFeedLink;

    // Keeps list of posts from the feed with provided link
    private List<RSSPost> mPostList;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inject dependencies
        RSSReaderApp.getAppComponent().inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_post_list, container, false);

        // Bind views and save reference to Unbinder object
        mUnbinder = ButterKnife.bind(this, v);

        // Set LinearLayoutManager for our RecyclerView (we need vertical scroll list)
        mPostListRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        // Manually get event with feed link from EventBus and remove it.
        // To do this we don't have to register to listen to EventBus events in onStart().
        OpenFeedEvent openFeedEvent = mEventBus.removeStickyEvent(OpenFeedEvent.class);

        // Check if an event was actually posted before
        if(openFeedEvent != null) {
            // Get RSS feed link from event
            mFeedLink = openFeedEvent.getFeedLink();

            // Get list of posts from the feed with provided link
            mPostList = mDataStorage.getPostList(mFeedLink);

            // Create new adapter with the list of posts
            mPostAdapter = new PostAdapter(mPostList);

            // Attach adapter to the RecyclerView
            mPostListRecyclerView.setAdapter(mPostAdapter);

            // Check network connection
            if (!UtilsNet.isNetworkAvailableAndConnected(getActivity())) {
                // No network connection
                // Check if there is cached data
                if (mPostList.size() == 0) {
                    // No cached data. Display error

                    // Hide RecyclerView
                    mPostListRecyclerView.setVisibility(View.GONE);

                    // Display empty view
                    mEmptyTextView.setVisibility(View.VISIBLE);
                }
            } else {
                // Network connection available
                // Display RecyclerView with cached data
                mPostListRecyclerView.setVisibility(View.VISIBLE);

                // Hide empty view
                mEmptyTextView.setVisibility(View.GONE);

                // Start fetching post list from the feed link
                mFeedFetcher.fetchFeed(mFeedLink);

                // TODO: Implement callbacks from the fetcher
            }
        }

        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // This is recommended to do here when using Butterknife in fragments
        mUnbinder.unbind();
    }

    // === RECYCLERVIEW VIEWHOLDER AND ADAPTER =====

    class PostHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        // Keeps post list item
        private RSSPost mRSSPost;

        // TextView for post title
        @BindView(R.id.post_title)
        public TextView mPostTitleTextView;

        public PostHolder(View itemView) {
            super(itemView);

            itemView.setOnClickListener(this);

            // Get access to TextView in itemView
            ButterKnife.bind(this, itemView);
        }

        public void bindPost(RSSPost rssPost) {
            mRSSPost = rssPost;
            mPostTitleTextView.setText(rssPost.getTitle());
        }

        @Override
        public void onClick(View v) {
            // TODO: Handle post clicks here
        }
    }

    private class PostAdapter extends RecyclerView.Adapter<PostHolder> {

        private List<RSSPost> mRSSPosts;

        public PostAdapter(List<RSSPost> RSSPosts) {
            mRSSPosts = RSSPosts;
        }

        @Override
        public PostHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            // Get LayoutInflater from parent activity
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());

            // Create view for one list item from item layout
            View view = layoutInflater.inflate(R.layout.list_item_post, parent, false);

            // Create ViewHolder with inflated view for one list item
            return new PostHolder(view);
        }

        @Override
        public void onBindViewHolder(PostHolder holder, int position) {
            RSSPost rssPost = mRSSPosts.get(position);
            holder.bindPost(rssPost);
        }

        @Override
        public int getItemCount() {
            if (mRSSPosts != null) {
                return mRSSPosts.size();
            } else {
                return 0;
            }
        }
    }
}