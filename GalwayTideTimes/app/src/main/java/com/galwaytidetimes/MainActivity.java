package com.galwaytidetimes;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.galwaytidetimes.service.TidesService;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Stack;

@EActivity(R.layout.activity_main)
@OptionsMenu(R.menu.main)
public class MainActivity extends TrackedActivity {

    private String description;
    private ArrayList<String> items;

    @Bean
    TidesService tidesService;

    @ViewById(R.id.textView1)
    TextView descriptionTextView;

    private ProgressDialog mProgress;
    private Spinner spinner;
    private Stack<Integer> previousDaysStack;
    private int currentDay;
    private boolean newlyCreated;
    private boolean backSelection;
    private SharedPreferences sharedPref;
    private static String DOWNLOAD_TIME_PREF = "com.galwaytidetimes.downloadTime";
    private static String DOWNLOAD_STRING_PREF = "com.galwaytidetimes.downloadString";
    private static String CURRENT_DAY_PREF = "com.galwaytidetimes.currentDay";

    @AfterViews
    public void initialise(){
        previousDaysStack = new Stack<Integer>();
        backSelection = false;
        newlyCreated = true;
        sharedPref = getPreferences(MODE_PRIVATE);
        currentDay = 0;
        download();
        AppLaunchChecker.checkFirstOrRateLaunch(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong(CURRENT_DAY_PREF, new Date().getTime());
    }

    @Override
    public void onBackPressed() {
        if (previousDaysStack.size() == 0)
            super.onBackPressed();
        else {
            backSelection = true;
            spinner.setSelection(previousDaysStack.pop());
        }
    }

    public void addItemsToSpinner() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        SimpleDateFormat df2 = new SimpleDateFormat("dd-MMM-yyyy (EEE)");
        String formattedDate = df.format(c.getTime());
        spinner = (Spinner) findViewById(R.id.spinner);
        List<String> list = new ArrayList<String>();
        if (items.size() > 0) {
            list.add(formattedDate + " (Today)");
            for (int i = 1; i < items.size(); i++) {
                c.add(Calendar.DAY_OF_MONTH, 1);
                list.add(df2.format(c.getTime()));
            }
        }
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, list);
        dataAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
        spinner.setOnItemSelectedListener(new CustomOnItemSelectedListener());
    }

    private void download() {
        Long downloadTime = sharedPref.getLong(DOWNLOAD_TIME_PREF, 0);
        Date downloadDate = new Date(downloadTime);
        SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
        if (downloadTime != 0
                && fmt.format(downloadDate).equals(fmt.format(new Date()))) {
            description = sharedPref.getString(DOWNLOAD_STRING_PREF,
                    "No data available.");
            items = new ArrayList<String>();
            int size = sharedPref.getInt(DOWNLOAD_STRING_PREF + "size", 0);
            for (int i = 0; i < size; i++) {
                items.add(sharedPref.getString(DOWNLOAD_STRING_PREF + i,
                        "Sorry not available"));
            }
            descriptionTextView.setText(Html.fromHtml(items.get(0)));
            descriptionTextView.setMovementMethod(LinkMovementMethod
                    .getInstance());
            addItemsToSpinner();
            if (items.size() < 7)
                Toast.makeText(MainActivity.this,
                        "No more information available, please try again later.",
                        Toast.LENGTH_LONG).show();

            return;
        }
        if (isNetworkConnected()) {
            // the init state of progress dialog
            mProgress = new ProgressDialog(this, ProgressDialog.THEME_HOLO_DARK);
            mProgress.setTitle("Loading");
            mProgress.setMessage("Please wait...");
            mProgress.show();
            tidesService.downloadTideTimes();
        } else {
            Toast.makeText(this, "No working internet connection available.",
                    Toast.LENGTH_LONG).show();
        }
    }

    @OptionsItem
    void action_infoSelected() {
        Intent intent = new Intent(this, InfoActivity_.class);
        startActivity(intent);
    }

    @OptionsItem
    void action_refreshSelected() {
        download();
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni == null) {
            return false;
        } else
            return true;
    }

    public void handleDownloadResults(ArrayList<String> result) {
        items = result;

        if (result != null && result.size() > 0) {
            addItemsToSpinner();
            if (result.size() < 7)
                Toast.makeText(MainActivity.this,
                        "No more information available, please try again later.",
                        Toast.LENGTH_LONG).show();
            description = result.get(0);
            descriptionTextView.setText(Html.fromHtml(description));
            descriptionTextView.setMovementMethod(LinkMovementMethod
                    .getInstance());
            if (mProgress.isShowing()) {
                mProgress.dismiss();
            }
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putLong(DOWNLOAD_TIME_PREF, new Date().getTime());
            editor.putString(DOWNLOAD_STRING_PREF, description);
            editor.putInt(DOWNLOAD_STRING_PREF + "size", result.size());
            for (int i = 0; i < result.size(); i++) {

                editor.remove(DOWNLOAD_STRING_PREF + i);
                editor.putString(DOWNLOAD_STRING_PREF + i, result.get(i));
            }
            editor.commit();
            spinner.setSelection(0);
        } else {
            Toast.makeText(MainActivity.this,
                    "There was a problem reading from the server.",
                    Toast.LENGTH_LONG).show();
            description = "No data could be read from the sever, please refresh to try again.";
            descriptionTextView.setText(Html.fromHtml(description));
            descriptionTextView.setMovementMethod(LinkMovementMethod
                    .getInstance());
            if (mProgress.isShowing()) {
                mProgress.dismiss();
            }
        }

    }

    private class CustomOnItemSelectedListener implements
            AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent, View view, int pos,
                                   long id) {
            if (items != null && items.size() > 0) {
                description = items.get(pos);
                descriptionTextView.setText(Html.fromHtml(description));
                descriptionTextView.setMovementMethod(LinkMovementMethod
                        .getInstance());
                if (newlyCreated) {
                    newlyCreated = false;
                    return;
                }
                if (!backSelection) {
                    previousDaysStack.push(Integer.valueOf(currentDay));
                    currentDay = pos;
                } else
                    backSelection = false;
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
        }

    }
}
