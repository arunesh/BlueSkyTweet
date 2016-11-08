package com.codepath.apps.blueskytweet;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.astuetz.PagerSlidingTabStrip;
import com.codepath.apps.blueskytweet.fragments.HomeTimelineFragment;
import com.codepath.apps.blueskytweet.fragments.MentionsTimelineFragment;
import com.codepath.apps.blueskytweet.models.Tweet;
import com.codepath.apps.blueskytweet.models.User;

public class TimelineActivity extends AppCompatActivity {
    public static final String TAG = "Timeline";
    private static final int REQUEST_CODE = 101;
    private HomeTimelineFragment homeTimelineFragment;
    private SwipeRefreshLayout swipeContainer;
    private ViewPager vpPager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);
        // Find the toolbar view inside the activity layout
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        // Sets the Toolbar to act as the ActionBar for this Activity window.
        // Make sure the toolbar exists in the activity and is not null

        setSupportActionBar(toolbar);
        // Get viewpager.
        vpPager = (ViewPager) findViewById(R.id.viewpager);
        // set adapter for the viewpager.
        vpPager.setAdapter(new TweetsPagerAdapter(getSupportFragmentManager()));

        // Find the pager tabs.
        PagerSlidingTabStrip tabStrip = (PagerSlidingTabStrip)findViewById(R.id.tabs);
        tabStrip.setViewPager(vpPager);
        // Attach the pager tabs to the view pager.

    }

    public void onProfileView(MenuItem item) {
        // Launch the profile view.
        Intent i = new Intent(this, ProfileActivity.class);
        startActivity(i);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.timeline_menu, menu);
        MenuItem composeItem = menu.findItem(R.id.miCompose);
        composeItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
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
                homeTimelineFragment.insertFreshTweet(tweet);
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public class TweetsPagerAdapter extends FragmentPagerAdapter {
        String tabTitles[] = {"Home", "Mentions"};


        public TweetsPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        // Order and creation of fragments within the pager.
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    homeTimelineFragment =  new HomeTimelineFragment();
                    return homeTimelineFragment;
                case 1:
                    MentionsTimelineFragment mentionsTimelineFragment = new MentionsTimelineFragment();
                    return mentionsTimelineFragment;
            }
            return null;
        }

        // How many fragments to swipe between.
        @Override
        public int getCount() {
            return tabTitles.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tabTitles[position];
        }
    }
}
