package jp.ac.oit.elc.mail.ibeaconlocationsystem;

import android.graphics.Point;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by yuuki on 11/4/15.
 */
public class LocationSender {
    private static final String TAG = LocationSender.class.getSimpleName();
    private URL mServerUrl;

    public LocationSender(String serverUrl) {
        try {
            mServerUrl = new URL(serverUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public void send(Point position){
        if(mServerUrl == null){
            Log.e(TAG, "URL is nothing");
            return;
        }
        final String json = convertToJson(position);
        new AsyncTask<Void, Void, Void>() {
            HttpURLConnection connection = null;
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    connection = (HttpURLConnection)mServerUrl.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setDoOutput(true);
                    connection.setFixedLengthStreamingMode(json.getBytes().length);
                    connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    connection.connect();
                    BufferedOutputStream stream = new BufferedOutputStream(connection.getOutputStream());
                    stream.write(json.getBytes());
                    stream.flush();
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }finally{
                    if (connection != null){
                        connection.disconnect();
                    }
                }
                return null;
            }
        }.execute();

    }
    private String convertToJson(Point position){
        JSONObject json = new JSONObject();
        try {
            json.put("x", position.x);
            json.put("y", position.y);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }
}
