package com.galwaytidetimes;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.galwaytidetimes.databinding.ActivityMainBinding;
import com.galwaytidetimes.service.TidesService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Stack;

public class MainActivity extends Activity implements TidesService.TidesServiceCallback {

    private ActivityMainBinding binding;

    private String description;
    private ArrayList<String> items;

    TidesService tidesService;

    private ProgressDialog mProgress;
    private Stack<Integer> previousDaysStack;
    private int currentDay;
    private boolean newlyCreated;
    private boolean backSelection;
    private SharedPreferences sharedPref;
    private static final String DOWNLOAD_TIME_PREF = "com.galwaytidetimes.downloadTime";
    private static final String DOWNLOAD_STRING_PREF = "com.galwaytidetimes.downloadString";
    private static final String CURRENT_DAY_PREF = "com.galwaytidetimes.currentDay";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        tidesService = new TidesService();
        initialise();
    }

    public void initialise() {
        previousDaysStack = new Stack<>();
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
        editor.apply();
    }

    @Override
    public void onBackPressed() {
        if (previousDaysStack.size() == 0)
            super.onBackPressed();
        else {
            backSelection = true;
            binding.spinner.setSelection(previousDaysStack.pop());
        }
    }

    public void addItemsToSpinner() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        SimpleDateFormat df2 = new SimpleDateFormat("dd-MMM-yyyy (EEE)");
        String formattedDate = df.format(c.getTime());
        List<String> list = new ArrayList<>();
        if (items.size() > 0) {
            list.add(formattedDate + " (Today)");
            for (int i = 1; i < items.size(); i++) {
                c.add(Calendar.DAY_OF_MONTH, 1);
                list.add(df2.format(c.getTime()));
            }
        }
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, list);
        dataAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinner.setAdapter(dataAdapter);
        binding.spinner.setOnItemSelectedListener(new CustomOnItemSelectedListener());
    }

    private void download() {
        Long downloadTime = sharedPref.getLong(DOWNLOAD_TIME_PREF, 0);
        Date downloadDate = new Date(downloadTime);
        SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
        if (downloadTime != 0
                && fmt.format(downloadDate).equals(fmt.format(new Date()))) {
            description = sharedPref.getString(DOWNLOAD_STRING_PREF,
                    "No data available.");
            items = new ArrayList<>();
            int size = sharedPref.getInt(DOWNLOAD_STRING_PREF + "size", 0);
            for (int i = 0; i < size; i++) {
                items.add(sharedPref.getString(DOWNLOAD_STRING_PREF + i,
                        "Sorry not available"));
            }
            binding.textView1.setText(Html.fromHtml(items.get(0)));
            binding.textView1.setMovementMethod(LinkMovementMethod
                    .getInstance());
            addItemsToSpinner();
            if (items.size() < 7)
                Toast.makeText(MainActivity.this,
                        "No more information available, please try again later.",
                        Toast.LENGTH_LONG).show();

            return;
        }
        if (isNetworkConnected()) {
            mProgress = new ProgressDialog(this, ProgressDialog.THEME_HOLO_DARK);
            mProgress.setTitle("Loading");
            mProgress.setMessage("Please wait...");
            mProgress.show();
            tidesService.downloadTideTimes(this);
        } else {
            Toast.makeText(this, "No working internet connection available.",
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_info) {
            action_infoSelected();
            return true;
        } else if (itemId == R.id.action_refresh) {
            action_refreshSelected();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void action_infoSelected() {
        Intent intent = new Intent(this, InfoActivity.class);
        intent.setPackage(this.getPackageName());
        startActivity(intent);
    }

    void action_refreshSelected() {
        download();
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni != null && ni.isConnected();
    }

    @Override
    public void onDownloadComplete(ArrayList<String> result) {
        items = result;

        if (result != null && result.size() > 0) {
            addItemsToSpinner();
            if (result.size() < 7)
                Toast.makeText(MainActivity.this,
                        "No more information available, please try again later.",
                        Toast.LENGTH_LONG).show();
            description = result.get(0);
            binding.textView1.setText(Html.fromHtml(description));
            binding.textView1.setMovementMethod(LinkMovementMethod
                    .getInstance());
            if (mProgress != null && mProgress.isShowing()) {
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
            editor.apply();
            binding.spinner.setSelection(0);
        } else {
            Toast.makeText(MainActivity.this,
                    "There was a problem reading from the server.",
                    Toast.LENGTH_LONG).show();
            description = "No data could be read from the sever, please refresh to try again.";
            binding.textView1.setText(Html.fromHtml(description));
            binding.textView1.setMovementMethod(LinkMovementMethod
                    .getInstance());
            if (mProgress != null && mProgress.isShowing()) {
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
                binding.textView1.setText(Html.fromHtml(description));
                binding.textView1.setMovementMethod(LinkMovementMethod
                        .getInstance());
                if (newlyCreated) {
                    newlyCreated = false;
                    return;
                }
                if (!backSelection) {
                    previousDaysStack.push(currentDay);
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
