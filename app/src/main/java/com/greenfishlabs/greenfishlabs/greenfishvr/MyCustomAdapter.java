package com.greenfishlabs.greenfishlabs.greenfishvr;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

/**
 * Created by DeathStar on 6/7/16.
 */
public class MyCustomAdapter extends BaseAdapter {

    private Activity activity;
    private VrVideoInfo[] listOfVrVideoInfo;
    private Context context;

    private static LayoutInflater inflater = null;

    public MyCustomAdapter(VrVideoInfo[] vrList, Activity activity, Context context) {
        this.context = context;
        this.activity = activity;
        this.listOfVrVideoInfo = vrList;

        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    @Override
    public int getCount() {
        return listOfVrVideoInfo.length;
    }

    @Override
    public Object getItem (int pos) {
        return listOfVrVideoInfo[pos];
    }

    @Override
    public long getItemId(int pos) {
        return 0;
        // return 0 if your list items do not have an Id variable
    }

    public static class Holder {
        ImageView imageView;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final Holder holder;

        if(convertView == null) {
            Log.d("ConvertView called", "true");
            holder = new Holder();
            convertView = inflater.inflate(R.layout.custom_list_layout, parent, false);

            holder.imageView = (ImageView) convertView.findViewById(R.id.redirect_btn);

            convertView.setTag(holder);
        } else {
            holder = (Holder)convertView.getTag();
        }

        // Inject preview image into each button
        Picasso.with(context).load(listOfVrVideoInfo[position].GetImageURl()).fit().transform(new RoundedTransformation(2, 0)).into(holder.imageView);

        // Passes video info to video info activity that is being loaded
        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, VideoViewer.class);
                intent.putExtra("video_title", listOfVrVideoInfo[position].GetTitle());
                intent.putExtra("video_author", listOfVrVideoInfo[position].GetAuthor());
                intent.putExtra("videoUrl", listOfVrVideoInfo[position].GetURL());
                intent.putExtra("videoViews", listOfVrVideoInfo[position].GetCount());
                intent.putExtra("videoDescription", listOfVrVideoInfo[position].GetDescription());
                intent.putExtra("videoId", listOfVrVideoInfo[position].GetID());
                intent.putExtra("imageUrl", listOfVrVideoInfo[position].GetImageURl());
                activity.startActivity(intent);
            }
        });
        return convertView;
    }
}
