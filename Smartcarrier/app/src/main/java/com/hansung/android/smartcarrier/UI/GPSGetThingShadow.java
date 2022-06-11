package com.hansung.android.smartlocker.UI;

import android.app.Activity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import com.hansung.android.smartlocker.HomeActivity;
import com.hansung.android.smartlocker.httpconnection.GetRequest;

public class GPSGetThingShadow extends GetRequest {
    final static String TAG = "AndroidAPITest";
    String urlStr;
    static String lat;
    static String lon;
    public GPSGetThingShadow(Activity activity, String urlStr) {
        super(activity);
        this.urlStr = urlStr;

    }

    @Override
    protected void onPreExecute() {
        try {
            Log.e(TAG, urlStr);
            url = new URL(urlStr);

        } catch (MalformedURLException e) {
            Toast.makeText(activity,"URL is invalid:"+urlStr, Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            activity.finish();
        }
    }

    @Override
    protected void onPostExecute(String jsonString) {
        if (jsonString == null)
            return;
        Map<String, String> state = getStateFromJSONString(jsonString);

       ((HomeActivity)HomeActivity.mContext).LAT = state.get("latstate");
       ((HomeActivity)HomeActivity.mContext).LON = state.get("lonstate");



    }

    protected Map<String, String> getStateFromJSONString(String jsonString) {
        Map<String, String> output = new HashMap<>();
        try {
            // 처음 double-quote와 마지막 double-quote 제거
            jsonString = jsonString.substring(1,jsonString.length()-1);
            // \\\" 를 \"로 치환
            jsonString = jsonString.replace("\\\"","\"");
            Log.i(TAG, "jsonString="+jsonString);
            JSONObject root = new JSONObject(jsonString);
            JSONObject state = root.getJSONObject("state");
            JSONObject reported = state.getJSONObject("reported");
            String weightvalue = reported.getString("WEIGHT");
            String servovalue = reported.getString("SERVO");
            String latvalue = reported.getString("LAT");
            String lonvalue = reported.getString("LON");
            output.put("weghitstate", weightvalue);
            output.put("servostate", servovalue);
            output.put("latstate", latvalue);
            output.put("lonstate", lonvalue);

//            output.put(lat, latvalue);
//            output.put(lon, lonvalue);
//            output.put(((HomeActivity)HomeActivity.mContext).LAT, latvalue);
//            output.put(((HomeActivity)HomeActivity.mContext).LON, lonvalue);

            JSONObject desired = state.getJSONObject("desired");
            String desired_servo = desired.getString("servostate");
            output.put("desired_servo", desired_servo);

        } catch (JSONException e) {
            Log.e(TAG, "Exception in processing JSONString.", e);
            e.printStackTrace();
        }
        return output;
    }
}