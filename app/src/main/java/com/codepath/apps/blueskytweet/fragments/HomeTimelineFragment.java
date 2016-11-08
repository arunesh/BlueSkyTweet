package com.codepath.apps.blueskytweet.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.codepath.apps.blueskytweet.TimelineFetcher;
import com.codepath.apps.blueskytweet.TwitterApplication;
import com.codepath.apps.blueskytweet.TwitterClient;
import com.loopj.android.http.JsonHttpResponseHandler;

/**
 * Created by arunesh on 11/4/16.
 */

public class HomeTimelineFragment extends TweetsListFragment {
    public static final String TAG = "Timeline";

    TwitterClient twitterClient;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        twitterClient = TwitterApplication.getRestClient();
        populateTimeline();
    }

    // Send a REST request to fetch the timeline. Populate the ListView.
    void populateTimeline() {
        setFetchRequest(new TimelineFetcher.FetchRequest() {
            @Override
            public boolean onFetch(long maxId, JsonHttpResponseHandler defaultHandler) {
                if (maxId != -1) {
                    Log.d(TAG, "HOME: Loading more with max_id = " + maxId);
                    twitterClient.getHomeTimelineWithMaxId(defaultHandler, maxId);
                } else {
                    Log.d(TAG, "HOME: Direct load - no max id");
                    twitterClient.getHomeTimeline(defaultHandler);
                }
                return true;
            }
        });
    }
}
