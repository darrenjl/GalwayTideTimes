package com.galwaytidetimes;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.galwaytidetimes.service.ColourService;
import com.google.analytics.tracking.android.EasyTracker;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

/**
 * Created by Darren on 16/05/13.
 */
@EActivity(R.layout.activity_info)
public class InfoActivity extends TrackedActivity {
    @ViewById(R.id.info_text_view)
    TextView descriptionTextView;

    @ViewById(R.id.info_layout)
    RelativeLayout infoLayout;

    @AfterViews
    void updateDescription() {
        descriptionTextView.setText(Html.fromHtml(getString(R.string.info_string)));
        descriptionTextView.setMovementMethod(LinkMovementMethod
                .getInstance());
    }
}