package com.gpetuhov.android.rssreader;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.gpetuhov.android.rssreader.data.DataStorage;
import com.gpetuhov.android.rssreader.data.RSSPost;
import com.gpetuhov.android.rssreader.events.FeedFetchErrorEvent;
import com.gpetuhov.android.rssreader.events.FeedFetchSuccessEvent;
import com.gpetuhov.android.rssreader.events.OpenFeedEvent;
import com.gpetuhov.android.rssreader.events.OpenPostEvent;
import com.gpetuhov.android.rssreader.utils.UtilsNet;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

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

    @Override
    public void onStart() {
        super.onStart();

        // Register to listen to EventBus events
        mEventBus.register(this);
    }

    @Override
    public void onStop() {
        super.onStop();

        // Unregister from EventBus
        mEventBus.unregister(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_post_list, container, false);

        // Bind views and save reference to Unbinder object
        mUnbinder = ButterKnife.bind(this, v);

        // Create LinearLayoutManager for our RecyclerView (we need vertical scroll list)
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mPostListRecyclerView.setLayoutManager(layoutManager);

        // Add dividers between items
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(
                mPostListRecyclerView.getContext(),
                layoutManager.getOrientation());
        mPostListRecyclerView.addItemDecoration(dividerItemDecoration);

        // Manually get event with feed link from EventBus.
        // To do this we don't have to register to listen to EventBus events in onStart().
        // Do not remove it, as it will be needed if fragment is recreated
        // (on screen rotation and returning from post fragment).
        OpenFeedEvent openFeedEvent = mEventBus.getStickyEvent(OpenFeedEvent.class);

        // Get RSS feed link from event
        mFeedLink = openFeedEvent.getFeedLink();

        updateUI();

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
        }

        return v;
    }

    private void updateUI() {
        // Get list of posts from the feed with provided link
        mPostList = mDataStorage.getPostList(mFeedLink);

        // Create new adapter with the list of posts
        mPostAdapter = new PostAdapter(mPostList);

        // Attach adapter to the RecyclerView
        mPostListRecyclerView.setAdapter(mPostAdapter);
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
        @BindView(R.id.post_title) TextView mPostTitleTextView;

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

            // Remove previously posted sticky event
            mEventBus.removeStickyEvent(OpenPostEvent.class);

            // Post STICKY event to EventBus.
            // This is needed, because at this moment post fragment is not started
            // and can't receive events.
            // Post fragment will be able
            // to get sticky event from EventBus after start.
            mEventBus.postSticky(new OpenPostEvent(mRSSPost.getTitle(), mRSSPost.getDescription()));

            // Create explicit intent to start post activity.
            // No need to add post details as intent extra,
            // because we deliver them to post fragment via EventBus.
            Intent intent = new Intent(getActivity(), PostActivity.class);

            // Start post list activity
            startActivity(intent);
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

    // === FEEDFETCHER CALLBACKS =====

    // Called when a FeedFetchSuccessEvent is posted (in the main thread to update UI)
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFeedFetchSuccess(FeedFetchSuccessEvent event) {
        mPostAdapter.notifyDataSetChanged();
    }

    // Called when a FeedFetchErrorEvent is posted (in the main thread to display Toast)
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFeedFetchError(FeedFetchErrorEvent event) {
        // Get error message from the event and display Toast
        String errorMessage = event.getErrorMessage();
        Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT).show();
    }
}