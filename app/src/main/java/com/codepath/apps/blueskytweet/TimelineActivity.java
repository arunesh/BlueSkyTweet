package com.codepath.apps.blueskytweet;

import android.content.Context;
import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.format.Time;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.codepath.apps.blueskytweet.models.Tweet;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class TimelineActivity extends AppCompatActivity {
    public static final String TAG = "Timeline";
    private static final int REQUEST_CODE = 101;

    TwitterClient twitterClient;
    TweetsArrayAdapter tweetsArrayAdapter;
    LinkedList<Tweet> tweetArrayList;
    ListView lvTweets;
    TimelineFetcher timelineFetcher;
    private SwipeRefreshLayout swipeContainer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);
        // Find the toolbar view inside the activity layout
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        // Sets the Toolbar to act as the ActionBar for this Activity window.
        // Make sure the toolbar exists in the activity and is not null
        setSupportActionBar(toolbar);
        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);


        lvTweets = (ListView)findViewById(R.id.lvTweets);
        tweetArrayList = new LinkedList<>();
        tweetsArrayAdapter = new TweetsArrayAdapter(this, tweetArrayList);
        lvTweets.setAdapter(tweetsArrayAdapter);
        twitterClient = TwitterApplication.getRestClient();
        timelineFetcher = new TimelineFetcher(this, twitterClient, tweetArrayList,
                tweetsArrayAdapter);

        lvTweets.setOnScrollListener(timelineFetcher);
        // timelineFetcher.initialFetch();
        //populateTimeline();

        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                timelineFetcher.refresh(swipeContainer);
            }
        });

    }


    // Send a REST request to fetch the timeline. Populate the ListView.
    void populateTimeline() {
        twitterClient.getHomeTimeline(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                Log.d(TAG, response.toString());
                tweetArrayList.addAll(Tweet.fromJsonArray(response));
                tweetsArrayAdapter.notifyDataSetChanged();
                // De-serialize json, create model objects, load models into listview.
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable,
                                  JSONObject errorResponse) {
                Log.e(TAG, "Failure:" + errorResponse.toString());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.timeline_menu, menu);
        MenuItem composeItem = menu.findItem(R.id.miCompose);
        composeItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // Toast.makeText(TimelineActivity.this, "Compose clicked.", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(TimelineActivity.this, ComposeActivity.class);
                startActivityForResult(intent, REQUEST_CODE);
                return false;
            }
        });
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Tweet tweet = data.getParcelableExtra("tweet");
                timelineFetcher.insertFreshTweet(tweet);
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
