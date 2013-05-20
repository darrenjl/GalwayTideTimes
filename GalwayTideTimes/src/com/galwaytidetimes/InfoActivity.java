package com.galwaytidetimes;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.widget.TextView;
import com.google.analytics.tracking.android.EasyTracker;

/**
 * Created by Darren on 16/05/13.
 */
public class InfoActivity extends Activity {
	private static String TAG = "GTT";
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_info);
        TextView descriptionTextView = (TextView) findViewById(R.id.info_text_view);
        descriptionTextView.setText(Html.fromHtml(getString(R.string.info_string)));
        descriptionTextView.setMovementMethod(LinkMovementMethod
                .getInstance());
    }

    @Override
    public void onStart() {
        super.onStart();
        EasyTracker.getInstance().activityStart(this); // Add this method.
    }

    @Override
    public void onStop() {
        super.onStop();
        EasyTracker.getInstance().activityStop(this); // Add this method.
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setContentView(R.layout.activity_info);
    }
}