package com.codepath.apps.blueskytweet.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.codepath.apps.blueskytweet.ProfileActivity;
import com.codepath.apps.blueskytweet.R;
import com.codepath.apps.blueskytweet.TimelineFetcher;
import com.codepath.apps.blueskytweet.TweetsArrayAdapter;
import com.codepath.apps.blueskytweet.models.Tweet;
import com.codepath.apps.blueskytweet.models.User;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import static android.R.attr.delay;

public class TweetsListFragment extends Fragment implements TweetsArrayAdapter.UserProfileCallback, TimelineFetcher.ProgressListener{

    TweetsArrayAdapter tweetsArrayAdapter;
    LinkedList<Tweet> tweetArrayList;
    ListView lvTweets;
    TimelineFetcher timelineFetcher;
    ProgressBar progressBarFooter;
    Handler handler;
    boolean isOffline = false;
    // inflation logic

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        handler = new Handler(getActivity().getMainLooper());
        Log.d("TweetsListFragment", "OnCreateView called.");
        View view = inflater.inflate(R.layout.fragment_tweets_list, container, false);
        lvTweets = (ListView) view.findViewById(R.id.lvTweets);
        setupListWithFooter(savedInstanceState);

        // Set the adapter AFTER setting the footer in the call above.
        lvTweets.setAdapter(tweetsArrayAdapter);
        lvTweets.setOnScrollListener(timelineFetcher);
        timelineFetcher.setProgressListener(this);
        tweetsArrayAdapter.setUserProfileCallback(this);
        showProgressBar();
        return view;
    }

    protected void setFetchRequest(TimelineFetcher.FetchRequest fetchRequest) {
        retryIfOffline(false);
        timelineFetcher.setFetchRequest(fetchRequest);
    }

    private void retryIfOffline(boolean shouldRetry) {
        if (isOffline()) {
            timelineFetcher.setOffline();
            isOffline = false;
            Toast.makeText(getContext(), "You are offline, will retry in 10 seconds.", Toast.LENGTH_LONG).show();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    retryIfOffline(true);
                }
            }, 10000L);
        } else if (shouldRetry) {
            timelineFetcher.setOnlineWithRetry();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d("TweetsListFragment", "OnCreate called.");
        super.onCreate(savedInstanceState);
        tweetArrayList = new LinkedList<>();
        tweetsArrayAdapter = new TweetsArrayAdapter(getContext(), tweetArrayList);
        timelineFetcher = new TimelineFetcher(getContext(), tweetArrayList, tweetsArrayAdapter);
    }

    public void insertFreshTweet(Tweet freshTweet) {
        tweetArrayList.addFirst(freshTweet);
        tweetsArrayAdapter.notifyDataSetChanged();
    }

    @Override
    public void userProfileClicked(User user) {
        // Display the given user's  profile.
        Intent i = new Intent(getContext(), ProfileActivity.class);
        i.putExtra("screenName", user.getScreenName());
        startActivity(i);
    }

    private void setupListWithFooter(Bundle savedInstanceState) {
        View footer = getLayoutInflater(savedInstanceState).inflate(R.layout.footer_progress, null);
        progressBarFooter = (ProgressBar)footer.findViewById(R.id.pbFooterLoading);
        lvTweets.addFooterView(footer);
    }

    // Show progress
    public void showProgressBar() {
        if (isUIThread()) {
            progressBarFooter.setVisibility(View.VISIBLE);
        } else {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressBarFooter.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    // Hide progress
    public void hideProgressBar(long delay) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                progressBarFooter.setVisibility(View.GONE);
            }
        }, delay);
    }

    @Override
    public void onFetchRequestStarted() {
        showProgressBar();
    }

    @Override
    public void onFetchRequestComplete(long delay) {
        hideProgressBar(delay);
    }

    private boolean isUIThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

    public boolean isOnline() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int     exitValue = ipProcess.waitFor();
            return (exitValue == 0);
        } catch (IOException e)          { e.printStackTrace(); }
        catch (InterruptedException e) { e.printStackTrace(); }
        return false;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    private boolean isOffline() {
        if (!isNetworkAvailable()) return true;
        return !isOnline();
    }
}
