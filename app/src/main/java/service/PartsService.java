package service;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;
import static handler.JsonHandler.deserializeParts;
import static handler.JsonHandler.serializeParts;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import dto.PartsDTO;

public class PartsService {

    private static final String TAG = "PartsService";

    public List<PartsDTO> fetchPartsInfo(Context cxt, String infoType) {
        List<PartsDTO> partsList = new ArrayList<>();
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
            if (responseCode == 200) {
                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line.trim());
                }

                JSONArray jsonArray = new JSONArray(response.toString());
                partsList = deserializeParts(jsonArray, infoType);

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

        return partsList;
    }

    public boolean registerParts2Inventory(Context ctx, List<PartsDTO> dtoList){
        HttpURLConnection connection = null;
        try {
            JSONArray partsArray = serializeParts(dtoList);
            URL url = new URL(ApiGetInfo.getApiUrl(ctx) + "inventory");

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("x-apikey", ApiGetInfo.getApiKey(ctx));
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);
            connection.setConnectTimeout(8000);
            connection.setReadTimeout(8000);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = partsArray.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            return (responseCode >= 200 && responseCode < 300);

        } catch (Exception e) {
            Log.e(TAG, "Error while storing vehicle in restdb: " + e.getMessage(), e);
            return false;
        } finally {
            if (connection != null) { connection.disconnect(); }
        }
    }
}
