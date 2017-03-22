package com.gpetuhov.android.rssreader;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gpetuhov.android.rssreader.events.OpenPostEvent;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

// Fragment for post details
public class PostFragment extends Fragment {

    // Dependencies injected by Dagger
    @Inject EventBus mEventBus;

    // TextViews for post title and description
    @BindView(R.id.post_title) TextView mPostTitleTextView;
    @BindView(R.id.post_description) TextView mPostDescriptionTextView;

    // Keeps Unbinder object to properly unbind views in onDestroyView of the fragment
    private Unbinder mUnbinder;

    private String mPostTitle;
    private String mPostDescription;

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
        View v = inflater.inflate(R.layout.fragment_post, container, false);

        // Bind views and save reference to Unbinder object
        mUnbinder = ButterKnife.bind(this, v);

        // Manually get event with post details from EventBus.
        // To do this we don't have to register to listen to EventBus events in onStart().
        // Do not remove it, as it will be needed if fragment is recreated.
        OpenPostEvent openPostEvent = mEventBus.getStickyEvent(OpenPostEvent.class);

        // Get post details from the event
        mPostTitle = openPostEvent.getPostTitle();
        mPostDescription = openPostEvent.getPostDescription();

        mPostTitleTextView.setText(mPostTitle);
        mPostDescriptionTextView.setText(mPostDescription);

        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // This is recommended to do here when using Butterknife in fragments
        mUnbinder.unbind();
    }
}
