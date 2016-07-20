package com.greenfishlabs.greenfishlabs.greenfishvr;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.vr.sdk.widgets.video.VrVideoView;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MenuActivity extends Activity {
    private final Handler h = new Handler();
    JSONArray videoDataJSON = new JSONArray();
    ArrayList<VrVideoInfo> videoDataArrayList;
    RetrieveVideoInfo rvi;

    // string adapter that will handle the data of the list view
    MyCustomAdapter adapter;
    ArrayList<VrVideoInfo> videoInfo;

    private ListView lView;
    private TextView loadingError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_activity);

        videoInfo = getIntent().getParcelableArrayListExtra("rData");
        lView = (ListView) findViewById(R.id.list_container);
        loadingError = (TextView) findViewById(R.id.loading_error);

        if (videoInfo.size() == 0) { // if there was an error loading video info from the server
            Log.d("Tyler", "Issue loading data from server...");
            lView.setVisibility(View.GONE);
            loadingError.setVisibility(View.VISIBLE);
        }

        VrVideoInfo[] info = videoInfo.toArray(new VrVideoInfo[videoInfo.size()]);

        //instantiate custom adapter
        adapter = new MyCustomAdapter(info, this, this);

        //handle listview and assign adapter
        lView = (ListView) findViewById(R.id.list_container);
        //lView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        lView.setScrollingCacheEnabled(false);
        lView.setDrawingCacheEnabled(false);
        lView.setAdapter(adapter);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    public void onServerRetry (View view) { // Retry retrieving video info from server
        rvi = new RetrieveVideoInfo();
        rvi.SetContext(MenuActivity.this);
        rvi.SetUrlConnection("http://www.greenfishvr.com/fetchVideos.php");
        rvi.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        r.run();
        loadingError.setVisibility(View.GONE);
    }

    // This runnable runs to constantly check if the server responded with data.
    final Runnable r = new Runnable() {
        @Override
        public void run() {
            videoDataJSON = rvi.GetJSON();

            Log.d("Runnable", "IsRunning");
            if(videoDataJSON != null ) {
                Log.d("videoDataJSON", "Not Null");

                videoDataArrayList = new ArrayList<>();
                Log.d("jArraySize", Integer.toString(videoDataJSON.length()));
                for(int i = 0; i < videoDataJSON.length(); i++) {
                    try {
                        videoDataArrayList.add(
                            new VrVideoInfo(
                                    videoDataJSON.getJSONObject(i).getString("title"),
                                    videoDataJSON.getJSONObject(i).getString("videoAuthor"),
                                    videoDataJSON.getJSONObject(i).getString("description"),
                                    videoDataJSON.getJSONObject(i).getString("url"),
                                    videoDataJSON.getJSONObject(i).getInt("views"),
                                    videoDataJSON.getJSONObject(i).getInt("id"),
                                    videoDataJSON.getJSONObject(i).getString("imageUrl"),
                                    videoDataJSON.getJSONObject(i).getString("collectionName")
                            )
                        );
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                if (videoDataArrayList.size() > 0) { // if info was retrieved from server, populate list view with buttons
                    videoInfo = videoDataArrayList;

                    lView.setVisibility(View.VISIBLE);
                    loadingError.setVisibility(View.GONE);

                    VrVideoInfo[] info = videoInfo.toArray(new VrVideoInfo[videoInfo.size()]);
                    adapter = new MyCustomAdapter(info, MenuActivity.this, getApplicationContext());
                    lView.setAdapter(adapter);

                    rvi.cancel(true);
                } else {
                    Toast.makeText(getApplicationContext(), "Check your internet connection and try again", Toast.LENGTH_LONG).show();
                }
            } else {
                h.postDelayed(r, 1000);
            }
        }
    };
}
