package com.galwaytidetimes;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;

import com.galwaytidetimes.databinding.ActivityInfoBinding;

public class InfoActivity extends Activity {

    private ActivityInfoBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        updateDescription();
    }

    void updateDescription() {
        binding.infoTextView.setText(Html.fromHtml(getString(R.string.info_string)));
        binding.infoTextView.setMovementMethod(LinkMovementMethod.getInstance());
    }
}