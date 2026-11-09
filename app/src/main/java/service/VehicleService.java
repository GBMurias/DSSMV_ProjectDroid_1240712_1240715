package service;

import static service.ApiGetInfo.getDocumentId;

import android.util.Log;
import android.content.Context;
import com.example.projectdroid.MainActivity;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import static handler.JsonHandler.serializeVehicle;
import static handler.JsonHandler.deserializeVehicle;
import static handler.JsonHandler.serializeParts;
import dto.PartsDTO;
import model.Parts;
import service.ApiGetInfo;
import dto.VehicleDTO;

public class VehicleService {
    private static final String TAG = "VehicleService";

    public boolean registerIntake(Context ctx, VehicleDTO dto, String infoType) {
        HttpURLConnection connection = null;

        try {
            JSONObject jsonBody = serializeVehicle(dto, infoType);
            URL url = new URL(
                    ApiGetInfo.getApiUrl(ctx) + infoType
            );

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("x-apikey", ApiGetInfo.getApiKey(ctx));
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);
            connection.setConnectTimeout(8000);
            connection.setReadTimeout(8000);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonBody.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            return (responseCode >= 200 && responseCode < 300);

        } catch (Exception e) {
            Log.e(TAG, "Error while storing vehicle in restdb: " + e.getMessage(), e);
            return false;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public VehicleDTO fetchVehicle(Context cxt, String vin) {
        HttpURLConnection connection = null;
        try {
            JSONObject queryObj = new JSONObject();
            queryObj.put("VIN", vin);
            String query = queryObj.toString();

            String docId = getDocumentId(cxt, "intakes", query);
            Log.d(TAG, "Query enviada: " + query + " | DocId encontrado: " + docId);

            if (docId == null) {
                return null;
            }

            String encodedQuery = URLEncoder.encode(query, "UTF-8");
            URL url = new URL(ApiGetInfo.getApiUrl(cxt) + "intakes?q=" + encodedQuery);

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
                    JSONObject vehicleJson = jsonArray.getJSONObject(0);
                    return deserializeVehicle(vehicleJson);
                }
            } else {
                Log.e(TAG, "Erro ao procurar veículo pelo VIN, código HTTP: " + responseCode);
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception ao procurar veículo: " + e.getMessage(), e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return null;
    }

    public boolean updateVehicleParts(Context cxt, String vin, List<PartsDTO> partsDtoList) {
        HttpURLConnection connection = null;
        try {
            String query = "{\"VIN\":\"" + vin + "\"}";
            String docId = getDocumentId(cxt, "intakes", query);
            Log.d(TAG, "Query sent: " + query + " | Document ID found: " + docId);

            if (docId == null) {
                Log.e(TAG, "Vehicle not found (for update).");
                return false;
            }

            JSONArray partsArray = serializeParts(partsDtoList);
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("parts", partsArray);

            URL url = new URL(ApiGetInfo.getApiUrl(cxt) + "intakes/" + docId);

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("PATCH");
            Log.d("DEBUG_JSON", "Payload sent (parts): " + jsonBody.toString());
            connection.setRequestProperty("x-apikey", ApiGetInfo.getApiKey(cxt));
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            connection.setDoOutput(true);
            connection.setConnectTimeout(8000);
            connection.setReadTimeout(8000);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonBody.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }
            int responseCode = connection.getResponseCode();
            Log.d(TAG, "Response Code do PATCH (parts): " + responseCode);
            return (responseCode >= 200 && responseCode < 300);

        } catch (Exception e) {
            Log.e(TAG, "Exception while communicating with restdb to update vehicle parts: " + e.getMessage(), e);
            return false;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public boolean deleteVehicle(Context cxt, String vin) {
        HttpURLConnection connection = null;
        try {
            String query = "{\"VIN\":\"" + vin + "\"}";
            String docId = getDocumentId(cxt, "intakes", query);
            Log.d(TAG, "Query sent: " + query + " | Document ID found: " + docId);

            if (docId == null) {
                Log.e(TAG, "Vehicle not found (for delete).");
                return false;
            }

            URL url = new URL(ApiGetInfo.getApiUrl(cxt) + "intakes/" + docId);

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("DELETE");
            connection.setRequestProperty("x-apikey", ApiGetInfo.getApiKey(cxt));
            connection.setRequestProperty("Accept", "application/json");
            connection.setConnectTimeout(8000);
            connection.setReadTimeout(8000);

            int responseCode = connection.getResponseCode();
            Log.d(TAG, "DELETE Response Code: " + responseCode);
            return (responseCode >= 200 && responseCode < 300);

        } catch (Exception e) {
            Log.e(TAG, "Exception while communicating with restdb to delete vehicle: " + e.getMessage(), e);
            return false;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
