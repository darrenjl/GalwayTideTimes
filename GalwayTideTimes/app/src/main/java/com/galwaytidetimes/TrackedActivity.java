package com.galwaytidetimes;

import android.app.Activity;

import com.google.analytics.tracking.android.EasyTracker;

public abstract class TrackedActivity extends Activity{

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
}
