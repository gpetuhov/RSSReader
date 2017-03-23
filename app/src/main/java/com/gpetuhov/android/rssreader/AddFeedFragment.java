package com.gpetuhov.android.rssreader;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

// Fragment with add feed (add subscription) dialog
public class AddFeedFragment extends DialogFragment {

    public static final String ADD_FEED_LINK_KEY = "add_feed_link_key";

    @BindView(R.id.dialog_feed_link) EditText mFeedLinkEditText;

    // Keeps ButterKnife Unbinder object to properly unbind views in onDestroyView of the fragment
    private Unbinder mUnbinder;

    private String mFeedLink = "";

    // Called when host activity's FragmentManager displays DialogFragment on screen
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Create view with date picker
        View v = LayoutInflater.from(getActivity())
                .inflate(R.layout.fragment_add_feed, null);

        // Bind views and save reference to Unbinder object
        mUnbinder = ButterKnife.bind(this, v);

        mFeedLinkEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Save entered text
                mFeedLink = s.toString();
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Do nothing
            }
        });

        // Create new AlertDialog with this view
        AlertDialog addFeedDialog = new AlertDialog.Builder(getActivity())
                .setView(v)
                .setTitle(R.string.action_add_feed)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Send feed link to target fragment
                        sendResult(Activity.RESULT_OK);
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();

        return addFeedDialog;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // This is recommended to do here when using Butterknife in fragments
        mUnbinder.unbind();
    }

    private void sendResult(int resultCode) {
        // If there is no target fragment, do nothing and return
        if (getTargetFragment() == null) {
            return;
        }

        // Create new intent and put feed link into it
        Intent intent = new Intent();
        intent.putExtra(ADD_FEED_LINK_KEY, mFeedLink);

        // Invoke target fragment's onActivityResult() with this intent
        getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, intent);
    }
}
