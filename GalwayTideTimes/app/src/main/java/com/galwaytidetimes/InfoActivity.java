package com.galwaytidetimes;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;
import com.google.analytics.tracking.android.EasyTracker;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

/**
 * Created by Darren on 16/05/13.
 */
@EActivity(R.layout.activity_info)
public class InfoActivity extends TrackedActivity {
    @ViewById(R.id.info_text_view)
    TextView descriptionTextView;

    @AfterViews
    void updateDescription() {
        descriptionTextView.setText(Html.fromHtml(getString(R.string.info_string)));
        descriptionTextView.setMovementMethod(LinkMovementMethod
                .getInstance());
    }
}