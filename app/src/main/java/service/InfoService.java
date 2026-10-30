package service;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class InfoService {

    private static final String TAG = "ArrayService";

    public ArrayList<String> fetchSimpleInfo(Context cxt, String infoType){
        ArrayList<String> infoList = new ArrayList<>();
        HttpURLConnection connection = null;
        try {
            URL url = new URL(ApiGetInfo.getApiUrl(cxt) + infoType);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("x-apikey", ApiGetInfo.getApiKey(cxt));
            connection.setRequestProperty("Accept", "application/json");
            connection.setConnectTimeout(8000);
            connection.setReadTimeout(8000);

            int responseCode = connection.getResponseCode();
            if (responseCode == 200){
                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line.trim());
                }

                JSONArray jsonArray = new JSONArray(response.toString());

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    String partName = obj.getString("name");
                    infoList.add(partName);
                }
            } else {
                Log.e(TAG, "Error while fetching info, HTTP code: " + responseCode);
            }

        } catch (Exception e) {
            Log.e(TAG, "Exception while communicating with restdb to obtain the map: " + e.getMessage(), e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        return infoList;
    }
}
