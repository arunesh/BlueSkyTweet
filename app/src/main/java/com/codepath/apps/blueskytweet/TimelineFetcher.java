package com.codepath.apps.blueskytweet;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.codepath.apps.blueskytweet.models.Tweet;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;

import cz.msebera.android.httpclient.Header;

import static com.codepath.apps.blueskytweet.TimelineActivity.TAG;

public class TimelineFetcher implements AbsListView.OnScrollListener {
    // How much to fetch in the first request.
    private static final int COUNT_PER_PAGE = 25;
    private static final int VISIBLE_THRESHOLD = 5;

    Context context;

    TwitterClient twitterClient;
    LinkedList<Tweet> adapterList;
    ArrayAdapter<Tweet> timelineArrayAdapter;
    boolean loading;
    SwipeRefreshLayout swipeRefreshLayout;

    JsonHttpResponseHandler responseHandler = new JsonHttpResponseHandler() {
        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
            // Log.d(TAG, response.toString());
            ArrayList<Tweet> tweetList = Tweet.fromJsonArray(response);
            updateIdState(tweetList);
            adapterList.addAll(tweetList);
            loading = false;
            timelineArrayAdapter.notifyDataSetChanged();
            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setRefreshing(false);
                swipeRefreshLayout = null;
            }
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
            Log.e(TAG, "Failure:" + errorResponse.toString());
            Toast.makeText(context, "Initial timeline fetch failed.", Toast.LENGTH_SHORT).show();
            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setRefreshing(false);
                swipeRefreshLayout = null;
            }
        }
    };

    // The maxId parameter will be passed as "since_id" to fetch updates on the timeline.
    // The minId will be passed as the "max_id" parameter for scrolling into older tweets.
    long maxId; // Max ID for any tweet we have downloaded so far.
    long minId;  // The lowest ID of any tweet we have seen so far.

    TimelineFetcher(Context context, TwitterClient twitterClient, LinkedList<Tweet> adapterList,
                    ArrayAdapter<Tweet> timelineArrayAdapter) {
        this.context = context;
        this.twitterClient = twitterClient;
        this.adapterList = adapterList;
        this.timelineArrayAdapter = timelineArrayAdapter;
        loading = false;
        maxId = -1;
        minId = -1;
        swipeRefreshLayout = null;
    }

    public void insertFreshTweet(Tweet tweet) {
        adapterList.addFirst(tweet);
        timelineArrayAdapter.notifyDataSetChanged();
    }

    public void refresh(SwipeRefreshLayout swipeRefreshLayout) {
        loading = true;
        maxId = -1;
        minId = -1;
        adapterList.clear();
        this.swipeRefreshLayout = swipeRefreshLayout;
        loadMoreAtBottom();
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        // Ignore this.
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

        Log.d(TAG, "onScroll: firstVisibleItem = " + firstVisibleItem
                + " visibleItemCount = " + visibleItemCount + " totalItemCount = "
                + totalItemCount);
        // If the total item count is zero and the previous isn't, assume the
        // list is invalidated and should be reset back to initial state
        if (totalItemCount < adapterList.size()) {
            Log.e(TAG, "Array list got INVALIDATED. ");
            // this.currentPage = this.startingPageIndex;
            // this.previousTotalItemCount = totalItemCount;
            if (totalItemCount == 0) {
                this.loading = true;
            }
        }

        if (loading) {
            // If we are currently loading, ignore this scroll callback.
            Log.d(TAG, "Currently loading.");
            return;
        }

        // If it isn't currently loading, we check to see if we have breached
        // the visibleThreshold and need to reload more data.
        // If we do need to reload some more data, we execute onLoadMore to fetch the data.
        if ((firstVisibleItem + visibleItemCount + VISIBLE_THRESHOLD) >= totalItemCount ) {
            loading = loadMoreAtBottom();
        }
        //else if (firstVisibleItem - VISIBLE_THRESHOLD < 0) {
        // loading = tryLoadMoreAtTop();
        //}

    }

    private boolean loadMoreAtBottom() {
        Log.d(TAG, "loadMoreAtBottom called.");
        if (maxId != -1) {
            Log.d(TAG, "Loading more with max_id = " + maxId);
            twitterClient.getHomeTimelineWithMaxId(responseHandler, maxId);
        } else {
            Log.d(TAG, "Direct load - no max id");
            twitterClient.getHomeTimeline(responseHandler);
        }
        return true;
    }

    private void updateIdState(ArrayList<Tweet> tweetList) {
        for (Tweet tweet : tweetList) {
            if (minId == -1 || tweet.getUid() < minId) {
                minId = tweet.getUid();
            }
            if (maxId == -1 || tweet.getUid() > maxId) {
                maxId = tweet.getUid();
            }
        }
        Log.d(TAG, "minId = " + minId + "maxId = " + maxId);
    }
}
