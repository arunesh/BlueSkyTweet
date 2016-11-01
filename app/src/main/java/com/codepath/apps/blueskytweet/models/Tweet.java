package com.codepath.apps.blueskytweet.models;


import android.os.Parcel;
import android.os.Parcelable;
import android.text.format.DateUtils;
import android.util.Log;

import com.codepath.apps.blueskytweet.TimelineActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;


import static com.codepath.apps.blueskytweet.TimelineActivity.TAG;


/**
 * Construct from the API response.
 * Parse the JSON and store the data.
 */
public class Tweet implements Parcelable {
    private String body;
    private long uid;  // unique ID for the tweet
    private User user;
    private String createdAt;


    public static ArrayList<Tweet> fromJsonArray(JSONArray jsonArray) {
        ArrayList<Tweet> arrayList = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i ++) {
            try {
                arrayList.add(fromJsonObject(jsonArray.getJSONObject(i)));
            } catch (JSONException e) {
                e.printStackTrace();
                continue;
            }
        }
        return arrayList;
    }

    public static Tweet fromJsonObject(JSONObject jsonObject) {
        Tweet tweet = new Tweet();
        try {
            tweet.body = jsonObject.getString("text");
            tweet.uid = jsonObject.getLong("id_str");
            tweet.user = User.fromJSONObject(jsonObject.getJSONObject("user"));
            tweet.createdAt = jsonObject.getString("created_at");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return tweet;
    }

    public String getBody() {
        return body;
    }

    public long getUid() {
        return uid;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public User getUser() {
        return user;
    }

    public static String getRelativeTimeAgo(String jsonTimestamp) {
        String twitterFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(twitterFormat, Locale.US);
        String relativeTimeSpan = "";
        try {
            Date date = sdf.parse(jsonTimestamp);
            relativeTimeSpan = DateUtils.getRelativeTimeSpanString(date.getTime(),
                    System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS).toString();
            // Log.d(TimelineActivity.TAG, "Relative time span: " + relativeTimeSpan);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return abbreviateRelativeTimespan(relativeTimeSpan);
    }

    private static String abbreviateRelativeTimespan(String relativeTimespan) {
        String[] splitArray = relativeTimespan.split(" ");
        if (splitArray != null && splitArray.length > 2) {
            String abbrev = splitArray[0] + splitArray[1].substring(0, 1);
            // Log.d(TAG, "Abbrev string:" + abbrev);
            return abbrev;

        }
        return relativeTimespan;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.body);
        dest.writeLong(this.uid);
        dest.writeParcelable(this.user, flags);
        dest.writeString(this.createdAt);
    }

    public Tweet() {
    }

    protected Tweet(Parcel in) {
        this.body = in.readString();
        this.uid = in.readLong();
        this.user = in.readParcelable(User.class.getClassLoader());
        this.createdAt = in.readString();
    }

    public static final Parcelable.Creator<Tweet> CREATOR = new Parcelable.Creator<Tweet>() {
        @Override
        public Tweet createFromParcel(Parcel source) {
            return new Tweet(source);
        }

        @Override
        public Tweet[] newArray(int size) {
            return new Tweet[size];
        }
    };
}
