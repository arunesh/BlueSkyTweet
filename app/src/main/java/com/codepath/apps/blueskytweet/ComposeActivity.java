package com.codepath.apps.blueskytweet;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.codepath.apps.blueskytweet.models.Tweet;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONObject;

import static com.codepath.apps.blueskytweet.TimelineActivity.TAG;

import cz.msebera.android.httpclient.Header;

public class ComposeActivity extends AppCompatActivity {
    EditText etTweet;
    Button btnCancel;
    Button btnTweet;
    TextView tvCharCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose);
        etTweet = (EditText)findViewById(R.id.etTweet);
        btnCancel = (Button)findViewById(R.id.btnCancel);
        btnTweet = (Button)findViewById(R.id.btnTweet);
        tvCharCount = (TextView)findViewById(R.id.tvCount);
        tvCharCount.setText("140");
        etTweet.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                int charsRemaining = 140 - etTweet.getText().length();
                if (charsRemaining < 0) {
                    btnTweet.setEnabled(false);
                } else if (!btnTweet.isEnabled()) {
                    btnTweet.setEnabled(true);
                }
                tvCharCount.setText(String.valueOf(charsRemaining));
                return false;
            }
        });
        btnTweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TwitterApplication.getRestClient().postTweet(etTweet.getText().toString(),
                        new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                Tweet tweet = Tweet.fromJsonObject(response);
                                Intent intent = new Intent();
                                intent.putExtra("tweet", tweet);
                                setResult(RESULT_OK, intent);
                                finish();
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                                Log.d(TAG, "Tweet post failed.");
                                Toast.makeText(ComposeActivity.this, "Posting new Tweet failed. Retry.", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ComposeActivity.this, "Tweet cancelled.", Toast.LENGTH_SHORT).show();
                setResult(RESULT_CANCELED, new Intent());
                finish();
            }
        });
    }
}
