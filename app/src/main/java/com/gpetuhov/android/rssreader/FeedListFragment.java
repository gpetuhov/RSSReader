package com.gpetuhov.android.rssreader;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.gpetuhov.android.rssreader.data.DataStorage;
import com.gpetuhov.android.rssreader.data.RSSFeed;
import com.gpetuhov.android.rssreader.events.FeedFetchErrorEvent;
import com.gpetuhov.android.rssreader.events.FeedFetchSuccessEvent;
import com.gpetuhov.android.rssreader.events.OpenFeedEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


// Fragment with list of RSS feeds
public class FeedListFragment extends Fragment {

    // Needed to show add feed dialog and get results from it
    public static final int ADD_FEED_REQUEST_CODE = 0;
    public static final String ADD_FEED_DIALOG_TAG = "AddFeedDialogTag";

    // Dependencies injected by Dagger
    @Inject DataStorage mDataStorage;
    @Inject EventBus mEventBus;
    @Inject FeedFetcher mFeedFetcher;

    // RecyclerView for RSS feed list
    @BindView(R.id.feed_list_recycler_view) RecyclerView mFeedListRecyclerView;

    // Keeps Unbinder object to properly unbind views in onDestroyView of the fragment
    private Unbinder mUnbinder;

    // Adapter for the RecyclerView
    private FeedAdapter mFeedAdapter;

    private String mAddedFeedLink;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // This fragment has menu
        setHasOptionsMenu(true);

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
        View v = inflater.inflate(R.layout.fragment_feed_list, container, false);

        // Bind views and save reference to Unbinder object
        mUnbinder = ButterKnife.bind(this, v);

        // Create LinearLayoutManager for our RecyclerView (we need vertical scroll list)
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mFeedListRecyclerView.setLayoutManager(layoutManager);

        // Add dividers between items
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(
                mFeedListRecyclerView.getContext(),
                layoutManager.getOrientation());
        mFeedListRecyclerView.addItemDecoration(dividerItemDecoration);

        // Get list of RSS feeds from the storage
        // and create new adapter for the RecyclerView with it.
        mFeedAdapter = new FeedAdapter(mDataStorage.getFeedList());

        // Attach adapter to the RecyclerView
        mFeedListRecyclerView.setAdapter(mFeedAdapter);

        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // This is recommended to do here when using Butterknife in fragments
        mUnbinder.unbind();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate menu
        inflater.inflate(R.menu.menu_fragment_feed_list, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Get selected item ID
        int id = item.getItemId();

        // If user selected Add Subscription item
        if (R.id.action_add_feed == id) {
            showAddFeedDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showAddFeedDialog() {
        // Get fragment manager of the host of this fragment
        // (in our case - host activity's fragment manager).
        FragmentManager manager = getFragmentManager();

        // Create new fragment with add feed dialog
        AddFeedFragment addFeedFragment = new AddFeedFragment();

        // Set this FeedListFragment as target fragment for AddFeedFragment
        // (this is needed to return results from AddFeedFragment to FeedListFragment).
        // This connection is managed by FragmentManager.
        addFeedFragment.setTargetFragment(FeedListFragment.this, ADD_FEED_REQUEST_CODE);

        // Add fragment into FragmentManager and show fragment on screen
        addFeedFragment.show(manager, ADD_FEED_DIALOG_TAG);
    }

    // Called by add feed fragment to return results
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {

        // If result is not OK, do nothing and return
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        // Check if the result comes from add feed fragment
        if (requestCode == ADD_FEED_REQUEST_CODE) {
            // Get feed link from the intent
            mAddedFeedLink = intent.getStringExtra(AddFeedFragment.ADD_FEED_LINK_KEY);

            if (null == mAddedFeedLink || "" == mAddedFeedLink) {
                Toast.makeText(getActivity(), "Not a valid URL", Toast.LENGTH_SHORT).show();
                return;
            }

            // Start fetching feed from the provided link
            mFeedFetcher.fetchFeed(mAddedFeedLink);

            // TODO: Show progress bar here
        }
    }

    // === RECYCLERVIEW VIEWHOLDER AND ADAPTER =====

    class FeedHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        // Keeps RSS feed list item
        private RSSFeed mRSSFeed;

        // TextView for RSS Feed title
        @BindView(R.id.rss_feed_title) TextView mRSSFeedTitleTextView;

        public FeedHolder(View itemView) {
            super(itemView);

            itemView.setOnClickListener(this);

            // Get access to TextView in itemView
            ButterKnife.bind(this, itemView);
        }

        public void bindFeed(RSSFeed rssFeed) {
            mRSSFeed = rssFeed;
            mRSSFeedTitleTextView.setText(rssFeed.getTitle());
        }

        @Override
        public void onClick(View v) {

            // Remove previously posted sticky event
            mEventBus.removeStickyEvent(OpenFeedEvent.class);

            // Post STICKY event to EventBus.
            // This is needed, because at this moment post list fragment is not started
            // and can't receive events.
            // Post list fragment will be able
            // to get sticky event from EventBus after start.
            mEventBus.postSticky(new OpenFeedEvent(mRSSFeed.getLink()));

            // Create explicit intent to start post list activity.
            // No need to add feed link as intent extra,
            // because we deliver it to post list fragment via EventBus.
            Intent intent = new Intent(getActivity(), PostListActivity.class);

            // Start post list activity
            startActivity(intent);
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

    // === FEEDFETCHER CALLBACKS =====

    // Called when a FeedFetchSuccessEvent is posted (in the main thread to update UI)
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFeedFetchSuccess(FeedFetchSuccessEvent event) {
        mFeedAdapter.notifyDataSetChanged();
    }

    // Called when a FeedFetchErrorEvent is posted (in the main thread to display Toast)
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFeedFetchError(FeedFetchErrorEvent event) {
        // Get error message from the event and display Toast
        String errorMessage = event.getErrorMessage();
        Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT).show();
    }
}
