package com.greenfishlabs.greenfishlabs.greenfishvr;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class SplashScreen extends Activity {
    private final Handler h = new Handler();
    JSONArray videoDataJSON = new JSONArray();
    ArrayList<VrVideoInfo> videoDataArrayList;
    RetrieveVideoInfo rvi;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splashscreen);

        //When calling RetrieveVideoInfo, you MUST set the context and url connection.
        rvi = new RetrieveVideoInfo();
        rvi.SetContext(SplashScreen.this);
        rvi.SetUrlConnection("http://ec2-54-84-102-152.compute-1.amazonaws.com/fetchVideos.php");
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
                        videoDataArrayList.add(new VrVideoInfo(videoDataJSON.getJSONObject(i).getString("title"),
                                //videoDataJSON.getJSONObject(i).getString("videoAuthor"),
                                videoDataJSON.getJSONObject(i).getString("description"),
                                videoDataJSON.getJSONObject(i).getString("url"),
                                videoDataJSON.getJSONObject(i).getInt("views"),
                                videoDataJSON.getJSONObject(i).getInt("id"),
                                videoDataJSON.getJSONObject(i).getString("imageUrl")));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                //When we get the data, change views.
                Intent i = new Intent(getApplicationContext(), MenuActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                i.putParcelableArrayListExtra("rData", videoDataArrayList);

                rvi.cancel(true);
                finish();
                startActivity(i);
            } else {
                h.postDelayed(r, 5000);
            }
        }
    };
}
