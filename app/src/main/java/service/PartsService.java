package service;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

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
                if (infoType.equals("parts")) {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject obj = jsonArray.getJSONObject(i);
                        String partName = obj.getString("name");
                        partsList.add(new PartsDTO(partName));
                    }
                } else if (infoType.equals("inventory")) {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject obj = jsonArray.getJSONObject(i);
                        String partName = obj.getString("name");
                        String associatedVIN = obj.getString("associatedVIN");
                        String associatedModel = obj.getString("associatedModel");
                        String associatedMake = obj.getString("associatedMake");
                        Integer associatedYear = obj.getInt("associatedYear");
                        partsList.add(new PartsDTO(partName, associatedVIN, associatedModel, associatedMake, associatedYear));
                    }
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

        return partsList;
    }

    public boolean registerParts2Inventory(Context ctx, List<PartsDTO> dtoList){
        HttpURLConnection connection = null;
        try {
            JSONArray partsArray = new JSONArray();
            JSONObject part = new JSONObject();
            for(PartsDTO dto : dtoList) {
                part.put("name", dto.getName());
                part.put("associatedVIN", dto.getAssociatedVIN());
                part.put("asscoiatedModel", dto.getAssociatedModel());
                part.put("associatedMake", dto.getAssociatedMake());
                part.put("associatedYear", dto.getAssociatedYear());
                partsArray.put(part);
            }

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
