package service;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.List;
import android.content.Context;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import dto.SlotDTO;
import dto.VehicleDTO;

public class    HistoryService {

    private static final String TAG = "HistoryService";

    public List<VehicleDTO> fetchScrappedVehicles(Context ctx) {
        List<VehicleDTO> scrappedList = new ArrayList<>();
        HttpURLConnection connection = null;

        try {
            URL url = new URL(ApiGetInfo.getApiUrl(ctx) + "history");

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("x-apikey", ApiGetInfo.getApiKey(ctx));
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
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

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);

                    String vin = obj.optString("VIN", "");
                    String make = obj.optString("Make", "");
                    String model = obj.optString("Model", "");
                    Integer year = obj.optInt("Year", 0);

                    scrappedList.add(new VehicleDTO(vin, make, model, year));
                }
            } else {
                Log.e(TAG, "Error while getting vehicles, HTTP code: " + responseCode);
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception while communicating with restdb to obtain the vehicles: " + e.getMessage(), e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        return scrappedList;
    }
}
