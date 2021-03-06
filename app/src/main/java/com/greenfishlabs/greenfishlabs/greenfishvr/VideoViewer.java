package com.greenfishlabs.greenfishlabs.greenfishvr;

import com.facebook.FacebookSdk;
import com.facebook.share.model.ShareHashtag;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareButton;
import com.facebook.share.widget.ShareDialog;
import com.google.vr.sdk.widgets.video.VrVideoEventListener;
import com.google.vr.sdk.widgets.video.VrVideoView;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import jp.wasabeef.picasso.transformations.CropTransformation;
import twitter4j.*;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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

    public int getLoadVideoStatus() {
        return loadVideoStatus;
    }

    private String fileUriString;
    private String videoDesc;
    public VrVideoView videoWidgetView;
    private boolean isPaused = false, isPlaying = false;
    private SeekBar seekbar;

    private int videoViews;
    private boolean videoIsLoaded = false;
    public String videoTitle, videoAuthor;
    public String imageUrl;
    private TextView titleLabel, authorLabel, viewsLabel, descriptionLabel, playBtn;
    private ImageView previewImage;

    private ShareButton shareButton;
    private Twitter mTwitter;
    private RequestToken requestToken;

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
        authorLabel = (TextView) findViewById(R.id.videoAuthorLabel);
        viewsLabel = (TextView) findViewById(R.id.viewsCount);
        descriptionLabel = (TextView) findViewById(R.id.description);
        seekbar = (SeekBar)findViewById(R.id.seekBar);
        playBtn = (TextView) findViewById(R.id.playBtn);
        previewImage = (ImageView) findViewById(R.id.previewImage);

        Bundle b = getIntent().getExtras();
        if (b.getString("video_title") != null) {
            videoTitle = b.getString("video_title").toLowerCase();
            titleLabel.setText(b.getString("video_title"));
        } else {
            Log.d(TAG, "Video title not found");
        }

        if (b.getString("video_author") != null) {
            videoAuthor = b.getString("video_author").toLowerCase();
            authorLabel.setText(b.getString("video_author"));
        } else {
            authorLabel.setVisibility(View.GONE);
            Log.d(TAG, "Video author not found");
        }

        if (b.getString("videoUrl") != null) {
            fileUriString = b.getString("videoUrl");
        } else {
            Log.d(TAG, "Video url not found");
        }

        if (b.getInt("videoViews") != 0) {
            videoViews = b.getInt("videoViews");
            viewsLabel.setText("Views: " + Integer.toString(b.getInt("videoViews")));
        } else {
            videoViews = 0;
            viewsLabel.setText("Views: " + 0);
        }

        if (b.getString("videoDescription") != null) {
            videoDesc = b.getString("videoDescription");
            descriptionLabel.setText(b.getString("videoDescription"));
        } else {
            Log.d(TAG, "Video description not found");
        }

        previewImage.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                previewImage.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int cropSizeX = 400;
                int cropSizeY = cropSizeX*9/16;
                Bundle b = getIntent().getExtras();
                if (b.getString("imageUrl") != null) {
                    imageUrl = b.getString("imageUrl");
                    ImageView previewImage = (ImageView) findViewById(R.id.previewImage);
                    Picasso.with(getApplicationContext())
                            .load(imageUrl)
                            .transform(new CropTransformation(cropSizeX, cropSizeY, 1920-cropSizeX, 1080-cropSizeY))
                            .into(previewImage);
                } else {
                    Log.d(TAG, "Image URL Not Found");
                }
            }
        });

        imageUrl = b.getString("imageUrl");

        videoId = b.getInt("videoId");
        Log.d("Video ID: ", Integer.toString(b.getInt("videoId")));

        loadVideoStatus = LOAD_VIDEO_STATUS_UNKNOWN;

        // Initial launch of the app or an Activity recreation due to rotation.
        handleIntent(getIntent());
        videoIsLoaded = true;

        //set facebook share button and its parameters
        shareButton = (ShareButton) findViewById(R.id.shareButton);
        ShareLinkContent slc = new ShareLinkContent.Builder().setContentDescription(videoDesc)
                .setContentTitle(videoTitle).setQuote("View now on the Greenfish VR app.")
                .setContentDescription(videoDesc)
                .setContentUrl(Uri.parse(fileUriString)).setImageUrl(Uri.parse(imageUrl)).build();
        shareButton.setShareContent(slc);

        //set twitter_logo share button and its parameters
        mTwitter = new TwitterFactory().getInstance();
        mTwitter.setOAuthConsumer(getResources().getString(R.string.twitter_consumer_key), getResources().getString(R.string.twitter_secret_key));
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
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
        isPlaying = !isPlaying;

        if (!isPlaying && isPaused) {
            playBtn.setText("PLAY");
        } else if (isPlaying && !isPaused) {
            playBtn.setText("PAUSE");
            seekbar.postDelayed(onEverySecond, 1000);
        } else {
            playBtn.setText("PLAY");
        }
    }

    // Play button pressed
    public void PlayVideo(View view) {
        TextView playBtn = (TextView) findViewById(R.id.playBtn);
        if (!isPlaying && !isPaused) {
            StartVideo();
            playBtn.setText("PAUSE");
        } else {
            togglePause();
        }
    }

    void StartVideo () {
        // Grab play button object
        ImageView previewImage = (ImageView) findViewById(R.id.previewImage);

        previewImage.setVisibility(View.GONE);
        videoWidgetView.setVisibility(View.VISIBLE);

        LinearLayout seekBarContainer = (LinearLayout) findViewById(R.id.seekBarContainer);
        seekBarContainer.setVisibility(View.VISIBLE);

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
            viewsLabel.setText("Views: " + Integer.toString(videoViews + 1));

            isPlaying = true;
        } else {
            Log.i(TAG, "Video not loaded");
        }
    }

    public void GoBack(View view) {
        videoWidgetView.pauseRendering();
        videoWidgetView.shutdown();
        finish();
    }

    private class ActivityEventListener extends VrVideoEventListener {
        // Called by video widget on the UI thread when it's done loading the video.
        @Override
        public void onLoadSuccess() {
            Log.i(TAG, "Sucessfully loaded video " + videoWidgetView.getDuration());
            loadVideoStatus = LOAD_VIDEO_STATUS_SUCCESS;
            seekbar.setMax((int)videoWidgetView.getDuration());
            seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    if(b) {
                        seekbar.setProgress(i);
                        videoWidgetView.seekTo(seekBar.getProgress());
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
            seekbar.postDelayed(onEverySecond, 1000);
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
            if(!isPaused) {
                seekbar.postDelayed(onEverySecond, 1000);
            }
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

    private Runnable onEverySecond=new Runnable() {
        @Override
        public void run() {
            if(seekbar != null) {
                seekbar.setProgress((int)videoWidgetView.getCurrentPosition());
                TextView timestampText = (TextView) findViewById(R.id.timestampText);

                int currentPos = (int) videoWidgetView.getCurrentPosition();
                int videoDuration = (int)videoWidgetView.getDuration();

                String currentTime = String.format("%02d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes(currentPos),
                        TimeUnit.MILLISECONDS.toSeconds(currentPos) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(currentPos)));

                String videoLength = String.format("%02d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes(videoDuration),
                        TimeUnit.MILLISECONDS.toSeconds(videoDuration) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(videoDuration)));

                timestampText.setText(currentTime + "/" + videoLength);
            }

            if(!isPaused) {
                seekbar.postDelayed(onEverySecond, 200);
            }

        }
    };

    public void TwitterTweet (View view) {

    }

    public void TwitterLoginClick (View view) {

    }
}