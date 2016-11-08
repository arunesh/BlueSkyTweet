package com.codepath.apps.blueskytweet;

import org.scribe.builder.api.Api;
import org.scribe.builder.api.TwitterApi;

import android.content.Context;

import com.codepath.oauth.OAuthBaseClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import static android.R.attr.x;

/*
 * 
 * This is the object responsible for communicating with a REST API. 
 * Specify the constants below to change the API being communicated with.
 * See a full list of supported API classes: 
 *   https://github.com/fernandezpablo85/scribe-java/tree/master/src/main/java/org/scribe/builder/api
 * Key and Secret are provided by the developer site for the given API i.e dev.twitter.com
 * Add methods for each relevant endpoint in the API.
 * 
 * NOTE: You may want to rename this object based on the service i.e TwitterClient or FlickrClient
 * 
 */
public class TwitterClient extends OAuthBaseClient {
    public static final int COUNT = 10;
	public static final Class<? extends Api> REST_API_CLASS = TwitterApi.class; // Change this
	public static final String REST_URL = "https://api.twitter.com/1.1/"; // Change this, base API URL
	public static final String REST_CONSUMER_KEY = "ud6tAZGbtFmoiXFFy5ULyIljU";       // Change this
	public static final String REST_CONSUMER_SECRET = "GKa1GBDj4zXWtHDT7vjMZxeDHiKjG4zggy1tISdf73TsreUVAa"; // Change this
	public static final String REST_CALLBACK_URL = "oauth://cpblueskytweets"; // Change this (here and in manifest)

	public TwitterClient(Context context) {
		super(context, REST_API_CLASS, REST_URL, REST_CONSUMER_KEY, REST_CONSUMER_SECRET, REST_CALLBACK_URL);
	}

	public synchronized  void getHomeTimeline(AsyncHttpResponseHandler handler) {
		getHomeTimeline(handler, 1);
	}

	public synchronized void getHomeTimeline(AsyncHttpResponseHandler handler, long since_id) {
		String apiUrl = getApiUrl("statuses/home_timeline.json");
		RequestParams params = new RequestParams();
		params.put("count", COUNT);
		params.put("since_id", since_id);  // Show tweets since the very first tweet.
		getClient().get(apiUrl, params, handler);
	}

	public synchronized void getMentionsTimeline(AsyncHttpResponseHandler handler) {
		String apiUrl = getApiUrl("statuses/mentions_timeline.json");
		RequestParams params = new RequestParams();
		params.put("count", COUNT);
		getClient().get(apiUrl, params, handler);
	}

	public synchronized void getMentionsTimelineWithMaxId(AsyncHttpResponseHandler handler, long max_id) {
		String apiUrl = getApiUrl("statuses/mentions_timeline.json");
		RequestParams params = new RequestParams();
		params.put("count", COUNT);
		params.put("max_id", max_id);
		getClient().get(apiUrl, params, handler);
	}

	public synchronized void getHomeTimelineWithMaxId(AsyncHttpResponseHandler handler, long max_id) {
		String apiUrl = getApiUrl("statuses/home_timeline.json");
		RequestParams params = new RequestParams();
		params.put("count", COUNT);
		params.put("max_id", max_id);  // Show tweets since the very first tweet.
		getClient().get(apiUrl, params, handler);
	}

    // Separate method for composing a tweet.
    public synchronized void postTweet(String body, AsyncHttpResponseHandler handler) {
        String apiUrl = getApiUrl("statuses/update.json");
        RequestParams params = new RequestParams();
        params.put("status", body);
        getClient().post(apiUrl, params, handler);
    }


	public synchronized void getUserTimelineWithMaxId(String screenName, AsyncHttpResponseHandler handler, long max_id) {
		String apiUrl = getApiUrl("statuses/user_timeline.json");
		RequestParams params = new RequestParams();
		params.put("count", COUNT);
		params.put("screen_name", screenName);
		params.put("max_id", max_id);
		getClient().get(apiUrl, params, handler);
	}

	public synchronized void getUserTimeline(String screenName, AsyncHttpResponseHandler handler) {
		String apiUrl = getApiUrl("statuses/user_timeline.json");
		RequestParams params = new RequestParams();
		params.put("count", COUNT);
		params.put("screen_name", screenName);
		getClient().get(apiUrl, params, handler);
	}

	public synchronized void getUserInfo(AsyncHttpResponseHandler handler) {
		String apiUrl = getApiUrl("account/verify_credentials.json");
		getClient().get(apiUrl, null, handler);
	}

    public synchronized void getInfoForUser(String screenName, AsyncHttpResponseHandler handler) {
        String apiUrl = getApiUrl("users/show.json");
        RequestParams params = new RequestParams();
        params.put("screen_name", screenName);
        getClient().get(apiUrl, params, handler);
    }
}