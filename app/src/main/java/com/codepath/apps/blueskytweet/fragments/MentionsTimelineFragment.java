package com.codepath.apps.blueskytweet.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.codepath.apps.blueskytweet.TimelineFetcher;
import com.codepath.apps.blueskytweet.TwitterApplication;
import com.codepath.apps.blueskytweet.TwitterClient;
import com.loopj.android.http.JsonHttpResponseHandler;

/**
 * Created by arunesh on 11/4/16.
 */

public class MentionsTimelineFragment extends TweetsListFragment {
    public static final String TAG = "MentionsTimeline";

    TwitterClient twitterClient;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        twitterClient = TwitterApplication.getRestClient();
        populateTimeline();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    // Send a REST request to fetch the timeline. Populate the ListView.
    void populateTimeline() {
        Log.d(TAG, "Setting FetchRequest.");
        setFetchRequest(new TimelineFetcher.FetchRequest() {
            @Override
            public boolean onFetch(long maxId, JsonHttpResponseHandler defaultHandler) {
                if (maxId != -1) {
                    Log.d(TAG, "MENTIONS: Loading more with max_id = " + maxId);
                    twitterClient.getMentionsTimelineWithMaxId(defaultHandler, maxId);
                } else {
                    Log.d(TAG, "MENTIONS: Direct load - no max id");
                    twitterClient.getMentionsTimeline(defaultHandler);
                }
                return true;
            }
        });
    }
}
