package com.galwaytidetimes;

import android.app.Activity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

/**
 * Created by Darren on 16/05/13.
 */
@EActivity(R.layout.activity_info)
public class InfoActivity extends Activity {
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