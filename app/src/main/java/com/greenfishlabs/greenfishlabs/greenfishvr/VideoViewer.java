package com.greenfishlabs.greenfishlabs.greenfishvr;

import com.facebook.FacebookSdk;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.google.vr.sdk.widgets.video.VrVideoEventListener;
import com.google.vr.sdk.widgets.video.VrVideoView;
import com.squareup.picasso.Picasso;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;
public class VideoViewer extends Activity {

    private static final String TAG = MenuActivity.class.getSimpleName();
    private static final String STATE_IS_PAUSED = "isPaused";
    private static final String STATE_PROGRESS_TIME = "progressTime";
    private static final String STATE_VIDEO_DURATION = "videoDuration";
    public static final int LOAD_VIDEO_STATUS_UNKNOWN = 0;
    public static final int LOAD_VIDEO_STATUS_SUCCESS = 1;
    public static final int LOAD_VIDEO_STATUS_ERROR = 2;
    private int videoId = 0;
    private int loadVideoStatus = LOAD_VIDEO_STATUS_UNKNOWN;
    public int getLoadVideoStatus() { return loadVideoStatus; }
    private String fileUriString;
    private String videoDesc;
    public VrVideoView videoWidgetView;
    private boolean isPaused = false;

    private int videoViews;
    private boolean videoIsLoaded = false;
    public String videoTitle;
    public String imageUrl;
    private TextView titleLabel, viewsLabel, descriptionLabel;

    static {
        //System.loadLibrary("gvr");
        System.loadLibrary("pano_video_renderer");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.video_view_activity);

        // Bind input and output objects for the view
        videoWidgetView = (VrVideoView) findViewById(R.id.vrViewer);
        videoWidgetView.setEventListener(new ActivityEventListener());
        titleLabel = (TextView) findViewById(R.id.videoTitleLabel);
        viewsLabel = (TextView) findViewById(R.id.viewsCount);
        descriptionLabel = (TextView) findViewById(R.id.description);

        Bundle b = getIntent().getExtras();
        if (b.getString("video_title") != null) {
            videoTitle = b.getString("video_title").toLowerCase();
            titleLabel.setText(b.getString("video_title"));
        }

        if (b.getString("videoUrl") != null) {
            fileUriString = b.getString("videoUrl");
        }

        videoViews = b.getInt("videoViews");
        viewsLabel.setText("Views: " + Integer.toString(b.getInt("videoViews")));

        if(b.getString("videoDescription") != null) {
            videoDesc = b.getString("videoDescription");
            descriptionLabel.setText(b.getString("videoDescription"));
        }

        if (b.getString("imageUrl") != null) {
            imageUrl = b.getString("imageUrl");
            ImageView previewImage = (ImageView) findViewById(R.id.previewImage);
            Picasso.with(getApplicationContext()).load(imageUrl).fit().into(previewImage);
        } else {
            Log.d(TAG, "Image URL Not Found");
        }

        videoId = b.getInt("videoId");
        Log.d("Video ID: ", Integer.toString(b.getInt("videoId")));

        loadVideoStatus = LOAD_VIDEO_STATUS_UNKNOWN;

        // Initial launch of the app or an Activity recreation due to rotation.
        handleIntent(getIntent());
        videoIsLoaded = true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.i(TAG, this.hashCode() + ".onNewIntent()");
        // Save the intent. This allows the getIntent() call in onCreate() to use this new Intent during
        // future invocations.
        setIntent(intent);
        // Load the new image.
        handleIntent(intent);
    }

     // Load custom videos based on the Intent or load the default video. See the Javadoc for this
     // class for information on generating a custom intent via adb.
    private void handleIntent(Intent intent) {
        // Determine if the Intent contains a file to load.
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            Log.i(TAG, "ACTION_VIEW Intent received");

            if (fileUriString == null) {
                Log.w(TAG, "No data uri specified. Use \"-d /path/filename\".");
            } else {
                Log.i(TAG, "Using file " + fileUriString);
            }
        } else {
            Log.i(TAG, "Intent is not ACTION_VIEW. Using the default video.");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putLong(STATE_PROGRESS_TIME, videoWidgetView.getCurrentPosition());
        savedInstanceState.putLong(STATE_VIDEO_DURATION, videoWidgetView.getDuration());
        savedInstanceState.putBoolean(STATE_IS_PAUSED, isPaused);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        long progressTime = savedInstanceState.getLong(STATE_PROGRESS_TIME);
        videoWidgetView.seekTo(progressTime);

        isPaused = savedInstanceState.getBoolean(STATE_IS_PAUSED);
        if (isPaused) {
            videoWidgetView.pauseVideo();
        }
    }

    // Pause video
    @Override
    protected void onPause() {
        super.onPause();
        videoWidgetView.pauseRendering();
        isPaused = true;
    }

    // Resume video
    @Override
    protected void onResume() {
        super.onResume();
        videoWidgetView.resumeRendering();
    }

    // Video view destroyed
    @Override
    protected void onDestroy() {
        videoWidgetView.shutdown();
        super.onDestroy();
    }

    private void togglePause() {
        if (isPaused) {
            videoWidgetView.playVideo();
        } else {
            videoWidgetView.pauseVideo();
        }
        isPaused = !isPaused;
    }

    // Play button pressed
    public void PlayVideo(View view) {
        // Grab play button object
        ImageButton playBtn = (ImageButton) findViewById(R.id.playIcon);
        ImageView previewImage = (ImageView) findViewById(R.id.previewImage);

        previewImage.setVisibility(View.GONE);
        videoWidgetView.setVisibility(View.VISIBLE);

        try { // try to load video from video url
            videoWidgetView.loadVideo(Uri.parse(fileUriString), null);
        } catch (Exception e) {
            // An error here is normally due to being unable to locate the file.
            loadVideoStatus = LOAD_VIDEO_STATUS_ERROR;
            Log.e(TAG, "Could not open video: " + e);
            e.printStackTrace();
        }

        if (videoIsLoaded) {
            Map<String, String> m = new HashMap<>();
            m.put("id", Integer.toString(videoId));
            UpdateViews uv = new UpdateViews();
            uv.SetParameters(m);
            uv.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            viewsLabel.setText("Views: " + Integer.toString(videoViews +1));
            playBtn.setVisibility(View.GONE);
        } else {
            Log.i(TAG, "Video not loaded");
        }
    }

    public void GoBack (View view) {
        videoWidgetView.pauseVideo();
        finish();
    }

    private class ActivityEventListener extends VrVideoEventListener  {
         // Called by video widget on the UI thread when it's done loading the video.
        @Override
        public void onLoadSuccess() {
            Log.i(TAG, "Sucessfully loaded video " + videoWidgetView.getDuration());
            loadVideoStatus = LOAD_VIDEO_STATUS_SUCCESS;
        }

         // Called by video widget on the UI thread on any asynchronous error.
        @Override
        public void onLoadError(String errorMessage) {
            // An error here is normally due to being unable to decode the video format.
            loadVideoStatus = LOAD_VIDEO_STATUS_ERROR;
            Log.e(TAG, "Error loading video: " + errorMessage);
        }

        @Override
        public void onClick() {
            togglePause();
        }

        // Update the UI every frame.
        @Override
        public void onNewFrame() {

        }

        // Make the video play in a loop. This method could also be used to move to the next video in
        // a playlist.
        @Override
        public void onCompletion() {
            //videoWidgetView.seekTo(0);
        }
    }

    public void ShareToFacebook(View view) {
        Log.d("ShareButton", "Pressed");
        ShareDialog sd = new ShareDialog(this);

        ShareLinkContent slc = new ShareLinkContent.Builder().setContentDescription(videoDesc)
                .setContentTitle(videoTitle).setQuote("View now on the Greenfish VR app.")
                .setContentUrl(Uri.parse(fileUriString)).setImageUrl(Uri.parse(imageUrl)).build();
        sd.show(slc );
    }
}
