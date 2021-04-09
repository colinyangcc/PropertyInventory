package com.example.agc_inventory.fragment;

import android.support.v4.app.Fragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2015-03-10.
 */
public class KeyDwonFragment extends Fragment {

    public void myOnKeyDwon() {

    }

    public static String httpConnectionPost(String apiUrl, Map<String, String> params) {
        HttpURLConnection conn = null;
        StringBuilder response = new StringBuilder();

        try {
            URL url = new URL(apiUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            conn.setDoInput(true); //允許輸入流，即允許下載
            conn.setDoOutput(true); //允許輸出流，即允許上傳
            conn.setUseCaches(false); //設置是否使用緩存

            OutputStream os = conn.getOutputStream();
            DataOutputStream writer = new DataOutputStream(os);
            String jsonString = getJSONString(params);
            writer.writeBytes(jsonString);
            writer.flush();
            writer.close();
            os.close();
            //Get Response
            InputStream is = conn.getInputStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        return response.toString();
    }

    public static String httpConnectionPost(String apiUrl, ArrayList<HashMap<String, String>> params) {
        HttpURLConnection conn = null;
        StringBuilder response = new StringBuilder();

        try {
            URL url = new URL(apiUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            conn.setDoInput(true); //允許輸入流，即允許下載
            conn.setDoOutput(true); //允許輸出流，即允許上傳
            conn.setUseCaches(false); //設置是否使用緩存

            OutputStream os = conn.getOutputStream();
            DataOutputStream writer = new DataOutputStream(os);
            String jsonString = getJSONString(params);
            writer.writeBytes(jsonString);
            writer.flush();
            writer.close();
            os.close();
            //Get Response
            InputStream is = conn.getInputStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        return response.toString();
    }

    public static String getJSONString(Map<String, String> params) {
        JSONObject json = new JSONObject();
        for (String key : params.keySet()) {
            try {
                json.put(key, params.get(key));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return json.toString();
    }

    public static String getJSONString(ArrayList<HashMap<String, String>> list) {
        List<JSONObject> jsonObj = new ArrayList<JSONObject>();
        for(HashMap<String, String> data : list) {
            JSONObject obj = new JSONObject(data);
            jsonObj.add(obj);
        }
        JSONArray test = new JSONArray(jsonObj);
        return test.toString();
    }

    public static ArrayList<Map<String, String>> jsonStringToArray(String jsonString) throws JSONException {

        ArrayList<Map<String, String>> stringArray = new ArrayList<Map<String, String>>();

        JSONArray jsonArray = new JSONArray(jsonString);

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject object = jsonArray.getJSONObject(i);
            stringArray.add(jsonObjecttoMap(object));
        }

        return stringArray;
    }
    public static Map<String, String> jsonObjecttoMap(JSONObject object) throws JSONException {
        Map<String, String> map = new HashMap<String, String>();

        Iterator<String> keysItr = object.keys();
        while (keysItr.hasNext()) {
            String key = keysItr.next();
            String value = object.getString(key);
            map.put(key, value);
        }
        return map;
    }

    public static Map<String,String> jsonStringtoMap(String jsonString) throws JSONException {
        Map<String, String> map = new HashMap<String, String>();
        JSONObject object = new JSONObject(jsonString);

        Iterator<String> keysItr = object.keys();
        while (keysItr.hasNext()) {
            String key = keysItr.next();
            String value = object.getString(key);
            map.put(key, value);
        }
        return map;
    }
}
