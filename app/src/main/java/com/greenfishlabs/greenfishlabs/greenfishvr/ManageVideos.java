/*package com.greenfishlabs.greenfishlabs.videoviewer;

import android.os.Bundle;
import android.app.Activity;
import android.widget.ListView;
import android.widget.TabHost;

import java.util.ArrayList;

public class ManageVideos extends Activity {

    ArrayList<VrVideoInfo> availableVideos, downloadedVideos;

    ArrayList<String> videoTitleList;
    MyCustomAdapter adapter;

    int[] videoTileImageList = { R.drawable.back_button,
            R.drawable.back_button,
            R.drawable.back_button,
            R.drawable.back_button,
            R.drawable.back_button,
            R.drawable.back_button };



    TabHost th;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_videos);
        ListView lv = (ListView)findViewById(R.id.listViewTabs);

        th = (TabHost)findViewById(R.id.tabHost);

        th.setCurrentTab(0);

        //Figure out where things will be loaded in from...


        th.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId)
            {
                if(tabId.equals("Available"))
                {

                }

                if(tabId.equals("Downloaded"))
                {

                }

            }
        });

        th.setup();
    }

}*/
