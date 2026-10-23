package service;

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import dto.VehicleDTO;

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

                    String make = vehicleData.optString("Make", "Unknown");
                    String model = vehicleData.optString("Model", "Unknown");
                    String yearStr = vehicleData.optString("ModelYear", "0");

                    int year = 0;
                    try {
                        year = Integer.parseInt(yearStr);
                    } catch (NumberFormatException ignored) {}

                    // Validação simples
                    if (!make.isEmpty() && !make.equals("Unknown")) {
                        // Transforma os dados num VehicleDTO e retorna-o diretamente!
                        return new VehicleDTO(vin, make, model, year);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Exceção ao consultar NHTSA: " + e.getMessage(), e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        // Retorna null se houver qualquer falha
        return null;
    }
}