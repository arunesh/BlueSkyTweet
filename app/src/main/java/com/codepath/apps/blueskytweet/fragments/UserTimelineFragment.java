package com.codepath.apps.blueskytweet.fragments;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.codepath.apps.blueskytweet.TimelineFetcher;
import com.codepath.apps.blueskytweet.TwitterApplication;
import com.codepath.apps.blueskytweet.TwitterClient;
import com.loopj.android.http.JsonHttpResponseHandler;

public class UserTimelineFragment extends  TweetsListFragment {

    public static final String TAG = "Timeline";

    TwitterClient twitterClient;
    private String screenName;

    public static UserTimelineFragment newInstance(String screenName) {
        UserTimelineFragment userTimelineFragment = new UserTimelineFragment();
        Bundle args = new Bundle();
        args.putString("screenName", screenName);
        userTimelineFragment.setArguments(args);
        return userTimelineFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        twitterClient = TwitterApplication.getRestClient();
        screenName = getArguments().getString("screenName");
        populateTimeline();
    }

    // Send a REST request to fetch the timeline. Populate the ListView.
    void populateTimeline() {
        setFetchRequest(new TimelineFetcher.FetchRequest() {
            @Override
            public boolean onFetch(long maxId, JsonHttpResponseHandler defaultHandler) {
                if (maxId != -1) {
                    Log.d(TAG, "USER: Loading more with max_id = " + maxId);
                    twitterClient.getUserTimelineWithMaxId(screenName, defaultHandler, maxId);
                } else {
                    Log.d(TAG, "USER: Direct load - no max id");
                    twitterClient.getUserTimeline(screenName, defaultHandler);
                }
                return true;
            }
        });

    }
}
