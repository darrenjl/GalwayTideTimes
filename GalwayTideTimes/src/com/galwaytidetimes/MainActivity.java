package com.galwaytidetimes;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import com.google.analytics.tracking.android.EasyTracker;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Stack;

public class MainActivity extends Activity {

    private String description;
    private ArrayList<String> items;
    private TextView descriptionTextView;
    private ProgressDialog mProgress;
    private Spinner spinner;
    private Stack<Integer> previousDaysStack;
    private int currentDay;
    private boolean backSelection;
    private static String TAG="GTT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        previousDaysStack = new Stack<Integer>();
        currentDay=0;
        backSelection=false;
        setContentView(R.layout.activity_main);
        descriptionTextView = (TextView) findViewById(R.id.textView1);
        download();
        addItemsToSpinner();
        spinner.setOnItemSelectedListener(new CustomOnItemSelectedListener());
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
    public void onBackPressed() {
        Log.d(TAG, "back button pressed");
        if(previousDaysStack.size()==0)
        	super.onBackPressed();
        else{
        	Log.d(TAG, "returning to previous day");
        	backSelection=true;
        	spinner.setSelection(previousDaysStack.pop());
        }	
    }

    public void addItemsToSpinner() {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        String formattedDate = df.format(c.getTime());
        spinner = (Spinner) findViewById(R.id.spinner);
        List<String> list = new ArrayList<String>();
        list.add(formattedDate + " (Today)");
        c.add(Calendar.DAY_OF_MONTH,1);
        list.add(df.format(c.getTime()));
        c.add(Calendar.DAY_OF_MONTH,1);
        list.add(df.format(c.getTime()));
        c.add(Calendar.DAY_OF_MONTH,1);
        list.add(df.format(c.getTime()));
        c.add(Calendar.DAY_OF_MONTH,1);
        list.add(df.format(c.getTime()));
        c.add(Calendar.DAY_OF_MONTH,1);
        list.add(df.format(c.getTime()));
        c.add(Calendar.DAY_OF_MONTH,1);
        list.add(df.format(c.getTime()));
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
    }

    private InputStream getInputStream(URL url) {
        try {
            return url.openConnection().getInputStream();
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    private void download() {
        if (isNetworkConnected()) {
            // the init state of progress dialog
            mProgress = new ProgressDialog(this, ProgressDialog.THEME_HOLO_DARK);
            mProgress.setTitle("Loading");
            mProgress.setMessage("Please wait...");
            mProgress.show();
            new DownloadRss().execute("");
        } else {
            Toast.makeText(this, "No working internet connection available.",
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        int itemId = item.getItemId();
		if (itemId == R.id.action_refresh) {
			download();
			return true;
		} else if (itemId == R.id.action_info) {
			intent = new Intent(this, InfoActivity.class);
			startActivity(intent);
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni == null) {
            // There are no active networks.
            return false;
        } else
            return true;
    }

    private class DownloadRss extends AsyncTask<String, Integer, ArrayList<String>> {
        @Override
        protected ArrayList<String> doInBackground(String... urls) {
            items = new ArrayList<String>();
            String next = null;
            try {
                URL url = new URL("http://www.tidetimes.org.uk/galway-tide-times-7.rss");
                XmlPullParserFactory factory = XmlPullParserFactory
                        .newInstance();
                factory.setNamespaceAware(false);
                XmlPullParser xpp = factory.newPullParser();
                xpp.setInput(getInputStream(url), "UTF_8");
                boolean insideItem = false;

                // Returns the type of current event: START_TAG, END_TAG, etc..
                int eventType = xpp.getEventType();
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG) {

                        if (xpp.getName().equalsIgnoreCase("item")) {
                            insideItem = true;
                        } else if (xpp.getName()
                                .equalsIgnoreCase("description")) {
                            if (insideItem) {
                                next = xpp.nextText();
                                items.add(System.getProperty("line.separator") + System.getProperty("line.separator") + next.substring(204));
                                Log.d("debug", next);
                            }
                        } else if (eventType == XmlPullParser.END_TAG
                                && xpp.getName().equalsIgnoreCase("item")) {
                            insideItem = false;
                        }
                    }
                    eventType = xpp.next(); // move to next element
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e){
                e.printStackTrace();
                return null;
            }
            return items;
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(ArrayList<String> result) {
            if(result!=null && result.size()>0){
                description = result.get(0);
                descriptionTextView.setText(Html.fromHtml(description));
                descriptionTextView.setMovementMethod(LinkMovementMethod
                        .getInstance());
                Log.d("GT", description);
                if (mProgress.isShowing()) {
                    mProgress.dismiss();
                }
            } else {
                Toast.makeText(MainActivity.this, "There was a problem reading from the server.",
                        Toast.LENGTH_LONG).show();
                description = "No data could be read from the sever, please refresh to try again.";
                descriptionTextView.setText(Html.fromHtml(description));
                descriptionTextView.setMovementMethod(LinkMovementMethod
                        .getInstance());
                Log.d("GT", description);
                if (mProgress.isShowing()) {
                    mProgress.dismiss();
                }
            }

        }
    }

    private class CustomOnItemSelectedListener implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            if(items!=null&&items.size()>0){
                description = items.get(pos);
                descriptionTextView.setText(Html.fromHtml(description));
                descriptionTextView.setMovementMethod(LinkMovementMethod
                    .getInstance());
                if(!backSelection){
                	previousDaysStack.push(Integer.valueOf(currentDay));
                	currentDay=pos;
                }
                else
                	backSelection=false;
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
            // TODO Auto-generated method stub
        }

    }
}
