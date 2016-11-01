package com.codepath.apps.blueskytweet;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.codepath.apps.blueskytweet.models.Tweet;
import com.squareup.picasso.Picasso;


import java.util.List;

import static com.codepath.apps.blueskytweet.R.id.ivProfileImage;
import static com.codepath.apps.blueskytweet.R.id.tvBody;
import static com.codepath.apps.blueskytweet.R.id.tvUsername;


// Turn Tweet objects into views that will be displayed in the list.
public class TweetsArrayAdapter extends ArrayAdapter<Tweet> {

    public TweetsArrayAdapter(Context context, List<Tweet> objects) {
        super(context, android.R.layout.simple_list_item_1, objects);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the tweet.
        Tweet tweet = getItem(position);
        // Find or inflate the template
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_tweet, parent,
                    false);
            // Find subviews to cache in the ViewHolder pattern.
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.ivProfileImage = (ImageView) convertView.findViewById(ivProfileImage);
            viewHolder.tvUsername = (TextView) convertView.findViewById(tvUsername);
            viewHolder.tvScreenname = (TextView) convertView.findViewById(R.id.tvScreenName);
            viewHolder.tvAgo = (TextView) convertView.findViewById(R.id.tvAgo);
            viewHolder.tvBody = (TextView) convertView.findViewById(tvBody);
            convertView.setTag(viewHolder);
        }

        // Fetch the cached references from the ViewHolder pattern.
        ViewHolder viewHolder = (ViewHolder) convertView.getTag();
        // Populate data into the subviews.
        viewHolder.tvUsername.setText(tweet.getUser().getName());
        viewHolder.tvScreenname.setText("@" + tweet.getUser().getScreenName());
        viewHolder.tvAgo.setText(Tweet.getRelativeTimeAgo(tweet.getCreatedAt()));
        viewHolder.tvBody.setText(tweet.getBody());
        viewHolder.ivProfileImage.setImageResource(android.R.color.transparent);  // Erase the data from the previous image.

        Picasso.with(getContext()).load(tweet.getUser().getProfileImageUrl()).into(viewHolder.ivProfileImage);

        // Return the view to be inserted.
        return convertView;
    }

    static class ViewHolder {
        ImageView ivProfileImage;
        TextView tvUsername;
        TextView tvScreenname;
        TextView tvAgo;
        TextView tvBody;
    }
}
