package com.greenfishlabs.greenfishlabs.greenfishvr;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

/**
 * Created by DeathStar on 7/16/16.
 */
public class MenuCollectionActivity extends Activity {

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
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.collection_list_view);

        videoInfo = getIntent().getParcelableArrayListExtra("rData");
        lView = (ListView) findViewById(R.id.list_container);
        loadingError = (TextView) findViewById(R.id.loading_error);

        Bundle b = getIntent().getExtras();

        if (b.getString("videoCollectionTitle") != null) {
            rvi = new RetrieveVideoInfo();
            rvi.SetContext(MenuCollectionActivity.this);
            rvi.SetUrlConnection("http://www.greenfishvr.com/fetch" + b.getString("videoCollectionTitle") + "Videos.php"); // TODO: Change to new php file url for grabbing collection data
            rvi.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            r.run();
        } else {

        }

        //handle listview and assign adapter
        lView = (ListView) findViewById(R.id.list_container);
        lView.setScrollingCacheEnabled(false);
        lView.setDrawingCacheEnabled(false);
    }

    public void onServerRetry (View view) { // Retry retrieving video info from server
        Bundle b = getIntent().getExtras();

        rvi = new RetrieveVideoInfo();
        rvi.SetContext(MenuCollectionActivity.this);
        rvi.SetUrlConnection("http://www.greenfishvr.com/fetch" + b.getString("videoCollectionTitle") + "Videos.php"); // TODO: Change to new php file url for grabbing collection data
        rvi.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        r.run();
    }

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
                                        videoDataJSON.getJSONObject(i).getString("imageUrl")
                                )
                        );
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                if (videoDataArrayList.size() > 0) { // if info was retrieved from server, populate list view with buttons
                    videoInfo = videoDataArrayList;
                    loadingError.setVisibility(View.GONE);

                    VrVideoInfo[] info = videoInfo.toArray(new VrVideoInfo[videoInfo.size()]);
                    adapter = new MyCustomAdapter(info, MenuCollectionActivity.this, getApplicationContext());
                    lView.setAdapter(adapter);

                    rvi.cancel(true);
                } else {
                    loadingError.setVisibility(View.VISIBLE);
                    Toast.makeText(getApplicationContext(), "Check your internet connection and try again", Toast.LENGTH_LONG).show();
                }
            } else {
                h.postDelayed(r, 1000);
            }
        }
    };
}
