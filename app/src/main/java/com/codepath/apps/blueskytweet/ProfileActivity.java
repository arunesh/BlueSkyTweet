package com.codepath.apps.blueskytweet;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.codepath.apps.blueskytweet.fragments.UserTimelineFragment;
import com.codepath.apps.blueskytweet.models.User;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

import static com.codepath.apps.blueskytweet.TimelineActivity.TAG;

public class ProfileActivity extends AppCompatActivity {
    TwitterClient client;
    ImageView ivProfileImage;
    TextView tvName;
    TextView tvTagline;
    TextView tvFollowers;
    TextView tvFollowing;
    JsonHttpResponseHandler responseHandler = new JsonHttpResponseHandler(){
        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
            User user = User.fromJSONObject(response);
            Log.d(TAG, "User: " + user);
            getSupportActionBar().setTitle("@" + user.getScreenName());
            populateProfileHeader(user);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ivProfileImage = (ImageView)findViewById(R.id.ivProfileImage);
        tvName = (TextView)findViewById(R.id.tvName);
        tvTagline = (TextView)findViewById(R.id.tvTagline);
        tvFollowers = (TextView) findViewById(R.id.tvFollowers);
        tvFollowing = (TextView) findViewById(R.id.tvFollowing);


        // Get the screen name from the activity that launches this.
        String screenName = getIntent().getStringExtra("screenName");
        client = TwitterApplication.getRestClient();
        if (screenName == null) {
            client.getUserInfo(responseHandler);
        } else {
            client.getInfoForUser(screenName, responseHandler);
        }

        // Create the user timeline fragment.
        UserTimelineFragment userTimelineFragment = UserTimelineFragment.newInstance(screenName);

        if (savedInstanceState == null) {
            // Only do it the first time.

            // Display the user fragment within the activity. We use a dynamic way.
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.flContainer, userTimelineFragment);
            ft.commit();
        }
    }

    private void populateProfileHeader(User user) {
        tvName.setText(user.getName());
        tvTagline.setText(user.getTagLine());
        tvFollowing.setText(user.getFriendsCount() + " following.");
        tvFollowers.setText(user.getFollowersCount() + " followers.");
        Picasso.with(this).load(user.getProfileImageUrl()).into(ivProfileImage);
    }
}
