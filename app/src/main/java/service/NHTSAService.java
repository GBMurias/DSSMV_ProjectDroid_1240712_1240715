package service;

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import dto.VehicleDTO;
import static handler.JsonHandler.deserializeNHTSA;

public class NHTSAService {
    private static final String TAG = "NHTSAService";
    public VehicleDTO fetchVehicleDetails(String vin) {
        HttpURLConnection connection = null;
        try {
            String urlString = "https://vpic.nhtsa.dot.gov/api/vehicles/decodevinvalues/" + vin + "?format=json";
            URL url = new URL(urlString);

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
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

                JSONObject jsonResponse = new JSONObject(response.toString());
                JSONArray resultsArray = jsonResponse.getJSONArray("Results");

                if (resultsArray.length() > 0) {
                    JSONObject vehicleData = resultsArray.getJSONObject(0);
                    VehicleDTO dto = deserializeNHTSA(vehicleData, vin);
                    if (!dto.getMake().isEmpty() && !dto.getMake().equals("Unknown")) {
                        return dto;
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception while consulting NHTSA: " + e.getMessage(), e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        return null;
    }
}