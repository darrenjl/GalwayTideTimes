package com.galwaytidetimes.service;

import android.os.Handler;
import android.os.Looper;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class TidesService {

    // Executor for background tasks
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    // Handler to post results back to the main thread
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    // Callback interface to communicate with the UI
    public interface TidesServiceCallback {
        void onDownloadComplete(ArrayList<String> items);
    }

    private InputStream getInputStream(URL url) {
        try {
            return url.openConnection().getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Public method to start the download
    public void downloadTideTimes(TidesServiceCallback callback) {
        executor.execute(() -> {
            ArrayList<String> itemList = doInBackground();
            mainThreadHandler.post(() -> callback.onDownloadComplete(itemList));
        });
    }

    // The actual background logic, now in a private method
    private ArrayList<String> doInBackground() {
        ArrayList<String> itemList = new ArrayList<>();
        String next;
        try {
            URL url = new URL("https://www.tidetimes.org.uk/galway-tide-times-7.rss");
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(false);
            XmlPullParser xpp = factory.newPullParser();

            InputStream inputStream = getInputStream(url);
            if (inputStream == null) {
                return itemList; // Return empty list if stream is null
            }
            xpp.setInput(inputStream, "UTF_8");

            boolean insideItem = false;
            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    if (xpp.getName().equalsIgnoreCase("item")) {
                        insideItem = true;
                    } else if (xpp.getName().equalsIgnoreCase("description")) {
                        if (insideItem) {
                            next = xpp.nextText();
                            Pattern ptrn = Pattern.compile("(\\d{2}:\\d{2}\\s-\\s)(Low|High)(\\sTide\\s\\(\\d.\\d{1,2}m\\))");
                            Matcher mtchr = ptrn.matcher(next);
                            StringBuilder timesStringBuilder = new StringBuilder();
                            while (mtchr.find()) {
                                String match = mtchr.group();
                                if (timesStringBuilder.length() != 0) {
                                    timesStringBuilder.append("<br>");
                                }
                                timesStringBuilder.append(match);
                            }
                            String item = timesStringBuilder.toString();
                            if (!item.isEmpty()) {
                                itemList.add(item);
                            }
                        }
                    }
                } else if (eventType == XmlPullParser.END_TAG && xpp.getName().equalsIgnoreCase("item")) {
                    insideItem = false;
                }
                eventType = xpp.next(); // move to next element
            }
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
            // In case of error, return the list which might be partially filled or empty
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return itemList; // Return empty list on error
        }
        return itemList;
    }
}
