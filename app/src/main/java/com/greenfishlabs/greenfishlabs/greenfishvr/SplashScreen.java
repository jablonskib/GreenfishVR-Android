package com.greenfishlabs.greenfishlabs.greenfishvr;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
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


        /*
            Load any data that is needed here...
            Splash screen shows while loading,
        */

        // Make folder in internal storage.
       // File vrStorage = new File(getApplicationContext().getFilesDir(), "GreenfishVR/");
        /*Log.d("Ext", "vrStorage: " + vrStorage.toString());
        Log.d("Ext", "Can we write to vrStorage?: " + vrStorage.canWrite());

        if(!vrStorage.exists()) {
            Log.d("Ext", "Internal folder does not exist. Creating...");
            if (!vrStorage.mkdirs()) {
                Log.d("Ext", "Internal folder creation failed");
            } else {
                Log.d("Ext", "Internal folder creation successful");
            }
        } else {
            Log.d("Ext", "Internal folder exists at: " + vrStorage);
        }

        Log.d("Ext", "----------------------------------------------------------------");

        // Make folder in external storage.
        File vrStorageExt = new File(Environment.getExternalStorageDirectory(), "GreenfishVR/");
        Log.d("Ext", "vrStorageExt: " + vrStorageExt.getPath());

        if(!vrStorageExt.exists()) {
            Log.d("Ext", "External folder does not exist. Creating...");
            if (!vrStorageExt.mkdirs()) {
                Log.d("Ext", "External folder creation failed");
            } else {
                Log.d("Ext", "External folder creation successful");
                if (vrStorageExt.list().length == 0) {
                    //copyAssets();
                    Log.d("Copying", "Copying assets to phone.");
                } else {
                    Log.d("Ext", vrStorageExt + " folder contains " + vrStorageExt.list().length + " files in it");
                }
            }
            Uri uri = Uri.parse(Environment.getExternalStorageDirectory() +"GreenfishVR/tmp.x");
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
            vrStorageExt.setReadable(true);
        } else {
            Log.d("Ext", "External folder exists at: " + vrStorageExt);
            if (vrStorageExt.list().length == 0) {
                //copyAssets();
                Log.d("Copying", "Copying assets to phone.");
            } else {
                Log.d("Ext", vrStorageExt + " folder contains " + vrStorageExt.list().length + " files in it");
            }


        }*/



        rvi = new RetrieveVideoInfo();
        rvi.SetContext(SplashScreen.this);
        rvi.SetUrlConnection("http://www.greenfishvr.com/fetchVideos.php");
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
                        videoDataArrayList.add(new VrVideoInfo(videoDataJSON.getJSONObject(i).getString("title"), videoDataJSON.getJSONObject(i).getString("description"), videoDataJSON.getJSONObject(i).getString("url"),
                                videoDataJSON.getJSONObject(i).getInt("views"), videoDataJSON.getJSONObject(i).getInt("id"), videoDataJSON.getJSONObject(i).getString("imageUrl")));
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
