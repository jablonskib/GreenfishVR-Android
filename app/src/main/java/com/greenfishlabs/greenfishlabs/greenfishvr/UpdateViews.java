package com.greenfishlabs.greenfishlabs.greenfishvr;

import android.os.AsyncTask;
import android.util.Log;

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
 * Created by Brandan on 6/20/2016.
 */
public class UpdateViews extends AsyncTask<Void, Void, Void> {

    private Map<String, String> parameters;
    HttpURLConnection conn;
    @Override
    protected Void doInBackground(Void... voids) {
        try {
            URL url = new URL("http://www.greenfishvr.com/updateViews.php");
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Key", "Value");
            conn.setDoOutput(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        StringBuffer requestParams = new StringBuffer();

        Iterator<String> paramIterator = parameters.keySet().iterator();
        //Format to POST.
        while (paramIterator.hasNext()) {
            String key = paramIterator.next();
            String value = parameters.get(key);
            try {
                requestParams.append(URLEncoder.encode(key, "UTF-8"));
                requestParams.append("=").append(
                        URLEncoder.encode(value, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            requestParams.append("&");
        }

        OutputStreamWriter writer = null;
        try {
            writer = new OutputStreamWriter(
                    conn.getOutputStream());
            writer.write(requestParams.toString());
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        InputStream inputStream = null;
        if (conn != null) {
            try {
                inputStream = conn.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        try {
            String response = reader.readLine();
            reader.close();
            Log.d("Server response:", response);
        } catch (IOException e) {
            e.printStackTrace();
        }
        conn.disconnect();
        return null;
    }

    //Set the parameters to be passed in in key-pair format.
    public void SetParameters(Map<String, String> p)
    {
        parameters = p;
    }
}
