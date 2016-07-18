package com.greenfishlabs.greenfishlabs.greenfishvr;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Brandan on 7/16/2016.
 */
public class CollectionFetch extends AsyncTask<Void, Void, Void>
{
    private Map<String, String> parameters;
    HttpURLConnection conn;
    JSONArray j;
    @Override
    protected Void doInBackground(Void... voids)
    {
        try
        {
            URL url = new URL("http://ec2-54-84-102-152.compute-1.amazonaws.com/collectionFetch.php");
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Key", "Value");
            conn.setDoOutput(true);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        StringBuffer requestParams = new StringBuffer();

        Iterator<String> paramIterator = parameters.keySet().iterator();
        while (paramIterator.hasNext())
        {
            String key = paramIterator.next();
            String value = parameters.get(key);
            try
            {
                requestParams.append(URLEncoder.encode(key, "UTF-8"));
                requestParams.append("=").append(
                        URLEncoder.encode(value, "UTF-8"));
            }
            catch (UnsupportedEncodingException e)
            {
                e.printStackTrace();
            }
            requestParams.append("&");
        }

        OutputStreamWriter writer = null;
        try
        {
            writer = new OutputStreamWriter(
                    conn.getOutputStream());
            writer.write(requestParams.toString());
            writer.flush();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        InputStream inputStream = null;
        if (conn != null)
        {
            try
            {
                inputStream = conn.getInputStream();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        try
        {
            String response = reader.readLine();
            JSONArray r = new JSONArray();
            reader.close();
            try {
                r = new JSONArray(response);
            } catch(JSONException je) {
                je.printStackTrace();
            }
            j = r;
            Log.d("Server response:", response);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        conn.disconnect();
        return null;
    }

    public void SetParameters(Map<String, String> p)
    {
        parameters = p;
    }

    public JSONArray GetJSON()
    {
        return j;
    }
}

