package com.greenfishlabs.greenfishlabs.greenfishvr;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


/**
 * Created by Brandan on 6/4/2016.
 *
 * For grabbing data from the server using POST.
 */
public class RetrieveVideoInfo extends AsyncTask<String, String, Void>
{

    private String connectionURL;
    private Context context;
    private JSONArray j;
    @Override
    protected Void doInBackground(String...args)
    {



        Log.d("Fetching data", "true");
        String response = "";
        try
        {
            URL url = new URL(connectionURL);
            HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
            httpURLConnection.setReadTimeout(10000);
            httpURLConnection.setConnectTimeout(15000);




            int responseStatus = httpURLConnection.getResponseCode();
            if(responseStatus == HttpURLConnection.HTTP_OK)
            {
                String line;
                BufferedReader br=new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                while ((line=br.readLine()) != null)
                {
                    response+=line;
                }

            }
            else
            {
                response = "";
            }


        }
        catch(MalformedURLException mue)
        {
            mue.printStackTrace();
        }
        catch(IOException ioe)
        {
            ioe.printStackTrace();
        }
        JSONArray r = new JSONArray();
        try
        {
           r = new JSONArray(response);
        }
        catch(JSONException je)
        {
            je.printStackTrace();
        }

        j = r;

        return null;
    }

    public JSONArray GetJSON()
    {
        return j;
    }

    //MUST SET CONTEXT
    public void SetContext(Activity c)
    {
        context = c;
    }

    //MUST SET URL LOCATION - Where we get the list of videos from.
    public void SetUrlConnection(String connection)
    {
        connectionURL = connection;
    }


}
