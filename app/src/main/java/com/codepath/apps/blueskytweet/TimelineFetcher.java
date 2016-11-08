package com.codepath.apps.blueskytweet;

import android.content.Context;
import android.os.Handler;
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
import java.util.List;

import cz.msebera.android.httpclient.Header;

import static com.codepath.apps.blueskytweet.TimelineActivity.TAG;

public class TimelineFetcher implements AbsListView.OnScrollListener {
    // How much to fetch in the first request.
    private static final int COUNT_PER_PAGE = 25;
    private static final int VISIBLE_THRESHOLD = 3;
    private static final int RETRY_COUNT = 3;
    private static final long RETRY_DELAY = 5000L; // two seconds retry delay.

    Context context;

    LinkedList<Tweet> adapterList;
    ArrayAdapter<Tweet> timelineArrayAdapter;
    boolean loading;
    SwipeRefreshLayout swipeRefreshLayout;

    FetchRequest fetchRequest;
    ProgressListener progressListener;
    int numTotalEntries = -1;  // total number of entries.

    int fetchRequestRetryCount = -1; // Becomes zero for the first retry.
    long fetchRequestMaxIdParam = -1;  // Value for the max_id param for a fetch.

    Handler handler;

    boolean isOffline;

    JsonHttpResponseHandler defaultHandler = new JsonHttpResponseHandler() {
        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
            Log.d(TAG, response.toString());
            ArrayList<Tweet> tweetList = Tweet.fromJsonArray(response);
            Log.d(TAG, "Fetched " + tweetList.size() + "entries");
            addAll(tweetList);
            if (tweetList.size() == 0) {
                numTotalEntries = adapterList.size();
            }
            if (progressListener != null) {
                progressListener.onFetchRequestComplete(200L);
            }
            fetchRequestRetryCount = -1;  // Reset retry count.
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
            Log.e(TAG, "Failure:" + errorResponse.toString());
            if (canRetry()) {
                Toast.makeText(context, "Timeline fetch failed. Retry attempt " + (fetchRequestRetryCount + 1),
                        Toast.LENGTH_SHORT).show();
                makeRetryAttempt();
                return;
            }
            Toast.makeText(context, "Timeline fetch failed.", Toast.LENGTH_SHORT).show();
            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setRefreshing(false);
                swipeRefreshLayout = null;
            }
            if (progressListener != null) {
                progressListener.onFetchRequestComplete(200L);
            }
            // Since we are done retrying, just give up.
            loading = false;
            numTotalEntries = adapterList.size(); // Stop further load attempts.
        }
    };

    // Listener to get updates on API requests.
    public interface ProgressListener {

        void onFetchRequestStarted();
        void onFetchRequestComplete(long delay);

    }

    // Interface for callers to pass in a custom API fetch request.
    public interface FetchRequest {
        boolean onFetch(long maxId, JsonHttpResponseHandler defaultHandler);
    }

    // The maxId parameter will be passed as "since_id" to fetch updates on the timeline.
    // The minId will be passed as the "max_id" parameter for scrolling into older tweets.
    long maxId; // Max ID for any tweet we have downloaded so far.
    long minId;  // The lowest ID of any tweet we have seen so far.

    public TimelineFetcher(Context context, LinkedList<Tweet> adapterList, ArrayAdapter<Tweet> timelineArrayAdapter) {
        this.context = context;
        this.adapterList = adapterList;
        this.timelineArrayAdapter = timelineArrayAdapter;
        loading = false;
        maxId = -1;
        minId = -1;
        swipeRefreshLayout = null;
        handler = new Handler(context.getMainLooper());
        isOffline = false;
    }

    public void setFetchRequest(FetchRequest fetchRequest) {
        this.fetchRequest = fetchRequest;
    }

    public void setOffline() {
        isOffline = true;
    }

    public void setOnlineWithRetry() {
        isOffline = false;
        makeFetchRequest();
    }

    public void setProgressListener(ProgressListener progressListener) {
        this.progressListener = progressListener;
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
        if (fetchRequest != null) {
            fetchRequest.onFetch(maxId, defaultHandler);
        }
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

        if (isOffline) {
            return;
        }

        if (loading) {
            // If we are currently loading, ignore this scroll callback.
            Log.d(TAG, "Currently loading.");
            return;
        }

        if (numTotalEntries != -1) {
            Log.d(TAG, "Not loading more, all entires loaded.");
            return;
        }

        // If it isn't currently loading, we check to see if we have breached
        // the visibleThreshold and need to reload more data.
        // If we do need to reload some more data, we execute onLoadMore to fetch the data.
        if ((firstVisibleItem + visibleItemCount + VISIBLE_THRESHOLD) > totalItemCount ) {
            if (fetchRequest != null) {
                makeFetchRequest();
            } else {
                Log.d(TAG, "Null FetchRequest, unable to scroll.");
            }
        }
    }

    private void makeFetchRequest() {
        if (progressListener != null) {
            progressListener.onFetchRequestStarted();
        }
        long finalMaxId = (minId > 0 ) ? minId - 1 : minId;
        fetchRequestRetryCount ++;
        fetchRequestMaxIdParam = finalMaxId;

        loading = fetchRequest.onFetch(finalMaxId, defaultHandler);
    }

    private void makeRetryAttempt() {
        if (!canRetry()) return;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                makeFetchRequest();
            }
        }, RETRY_DELAY);
    }

    public void addAll(List<Tweet> tweetList) {
        updateIdState(tweetList);
        adapterList.addAll(tweetList);
        loading = false;
        timelineArrayAdapter.notifyDataSetChanged();
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
            swipeRefreshLayout = null;
        }
    }

    private void updateIdState(List<Tweet> tweetList) {
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

    private boolean canRetry() {
        return fetchRequestRetryCount < RETRY_COUNT;
    }
}
