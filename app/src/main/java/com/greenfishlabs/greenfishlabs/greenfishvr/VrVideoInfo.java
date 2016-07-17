package com.greenfishlabs.greenfishlabs.greenfishvr;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Brandan on 6/5/2016.
 * Holds all data that was grabbed from the JSON array.
 */
public class VrVideoInfo implements Parcelable
{
    private String videoTitle, videoAuthor, videoDescription, videoURL, videoImageURL, videoCollectionTitle;
    private int viewCount, videoId;

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(videoTitle);
        out.writeString(videoAuthor);
        out.writeString(videoDescription);
        out.writeString(videoURL);
        out.writeInt(viewCount);
        out.writeInt(videoId);
        out.writeString(videoImageURL);
        out.writeString(videoCollectionTitle);
    }

    public static final Parcelable.Creator<VrVideoInfo> CREATOR
            = new Parcelable.Creator<VrVideoInfo>() {

        // This simply calls our new constructor (typically private) and 
        // passes along the unmarshalled `Parcel`, and then returns the new object!
        @Override
        public VrVideoInfo createFromParcel(Parcel in) {
            return new VrVideoInfo(in);
        }


        @Override
        public VrVideoInfo[] newArray(int size) {
            return new VrVideoInfo[size];
        }
    };

    private VrVideoInfo(Parcel in) {
        videoTitle = in.readString();
        videoAuthor = in.readString();
        videoDescription = in.readString();
        videoURL = in.readString();
        viewCount = in.readInt();
        videoId = in.readInt();
        videoImageURL = in.readString();
        videoCollectionTitle = in.readString();
    }

    //Default constructor.
    public VrVideoInfo( String title, String author, String description, String url, int count, int vId, String vImgURL, String collectionTitle) {
        videoTitle = title;
        videoAuthor = author;
        videoDescription = description;
        videoURL = url;
        viewCount = count;
        videoId = vId;
        videoImageURL = vImgURL;
        videoCollectionTitle = collectionTitle;
    }

    // Constructor w/ no collection field
    public VrVideoInfo( String title, String author, String description, String url, int count, int vId, String vImgURL) {
        videoTitle = title;
        videoAuthor = author;
        videoDescription = description;
        videoURL = url;
        viewCount = count;
        videoId = vId;
        videoImageURL = vImgURL;
        videoCollectionTitle = "null";
    }

    /*
        Accessors
     */
    public String GetImageURl() {
        return videoImageURL;
    }

    public String GetTitle() {
        return videoTitle;
    }

    public String GetAuthor() {
        return videoAuthor;
    }

    public String GetDescription() {
        return videoDescription;
    }

    public String GetURL() {
        return videoURL;
    }

    public int GetCount()
    {
        return viewCount;
    }

    public int GetID()
    {
        return videoId;
    }

    public String GetCollectionTitle() {
        return videoCollectionTitle;
    }

    @Override
    public int describeContents()
    {
        return 0;
    }
}
