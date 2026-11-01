package service;

import android.content.Context;
import android.util.Log;

import com.example.projectdroid.R;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class ApiGetInfo {

    public static URL getApiUrl(Context cxt) {
        try {
            String urlString = cxt.getString(R.string.ApiURL);
            return new URL(urlString);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getApiKey(Context cxt) {

        return cxt.getString(R.string.ApiKEY);
    }

    public static String getDocumentId(Context cxt, String collectionName, String queryJson) {
        HttpURLConnection connection = null;
        try {
            String encodedQuery = URLEncoder.encode(queryJson, "UTF-8");
            URL url = new URL(ApiGetInfo.getApiUrl(cxt) + collectionName + "?q=" + encodedQuery);

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("x-apikey", ApiGetInfo.getApiKey(cxt));
            connection.setRequestProperty("Accept", "application/json");
            connection.setConnectTimeout(8000);
            connection.setReadTimeout(8000);

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line.trim());
                }
                br.close();


                JSONArray jsonArray = new JSONArray(response.toString());
                if (jsonArray.length() > 0) {

                    return jsonArray.getJSONObject(0).getString("_id");
                }
            } else {
                Log.e("API_GENERIC", "Erro ao procurar ID na coleção " + collectionName + ", código HTTP: " + responseCode);
            }
        } catch (Exception e) {
            Log.e("API_GENERIC", "Exception ao obter ID: " + e.getMessage(), e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return null;
    }
}

