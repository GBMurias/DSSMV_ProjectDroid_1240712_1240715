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

import dto.PartsDTO;
import model.Parts;
import service.ApiGetInfo;
import dto.VehicleDTO;

public class VehicleService {
    private static final String TAG = "VehicleService";

    public boolean registerIntake(Context ctx, VehicleDTO dto, String infoType) {
        HttpURLConnection connection = null;


        try {
            JSONObject jsonBody = new JSONObject();
            if(infoType.equals("intakes")) {
                jsonBody.put("VIN", dto.getVin());
                jsonBody.put("Make", dto.getMake());
                jsonBody.put("Model", dto.getModel());
                jsonBody.put("Year", dto.getYear());
                jsonBody.put("is_salvageable", dto.isSalvageable());

                if (dto.getPartsList() != null && !dto.getPartsList().isEmpty()) {
                    JSONArray partsArray = new JSONArray();
                    for (PartsDTO part : dto.getPartsList()) {
                        JSONObject partObj = new JSONObject();
                        partObj.put("name", part.getName());
                        partObj.put("associatedVIN", part.getAssociatedVIN());
                        partObj.put("associatedModel", part.getAssociatedModel());
                        partObj.put("associatedMake", part.getAssociatedMake());
                        partObj.put("associatedYear", part.getAssociatedYear());
                        partsArray.put(partObj);
                    }
                    jsonBody.put("parts", partsArray);
                }
            } else if(infoType.equals("history")){
                jsonBody.put("VIN", dto.getVin());
                jsonBody.put("Make", dto.getMake());
                jsonBody.put("Model", dto.getModel());
                jsonBody.put("Year", dto.getYear());
            }

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
            Log.d("DEBUG_RESTDB", "Query enviada: " + query + " | DocId encontrado: " + docId);

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

                    VehicleDTO dto = new VehicleDTO();
                    if (vehicleJson.has("VIN")) dto.setVin(vehicleJson.getString("VIN"));
                    if (vehicleJson.has("Make")) dto.setMake(vehicleJson.getString("Make"));
                    if (vehicleJson.has("Model")) dto.setModel(vehicleJson.getString("Model"));
                    if (vehicleJson.has("Year")) dto.setYear(vehicleJson.getInt("Year"));
                    if (vehicleJson.has("is_salvageable")) dto.setSalvageable(vehicleJson.getBoolean("is_salvageable"));

                    if (vehicleJson.has("parts")) {
                        JSONArray partsArray = vehicleJson.getJSONArray("parts");
                        List<PartsDTO> partsList = new java.util.ArrayList<>();
                        for (int i = 0; i < partsArray.length(); i++) {
                            JSONObject partObj = partsArray.getJSONObject(i);
                            PartsDTO part = new PartsDTO();
                            if (partObj.has("name")) part.setName(partObj.getString("name"));
                            if (partObj.has("associatedVIN")) part.setAssociatedVIN(partObj.getString("associatedVIN"));
                            if (partObj.has("associatedModel")) part.setAssociatedModel(partObj.getString("associatedModel"));
                            if (partObj.has("associatedMake")) part.setAssociatedMake(partObj.getString("associatedMake"));
                            if (partObj.has("associatedYear")) part.setAssociatedYear(partObj.getInt("associatedYear"));
                            partsList.add(part);
                        }
                        dto.setPartsList(partsList);
                    }

                    return dto;
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
            String docId = getDocumentId(cxt, "vehicles", query);
            Log.d("DEBUG_RESTDB", "Query enviada: " + query + " | DocId encontrado para atualizar parts: " + docId);

            if (docId == null) {
                Log.e(TAG, "Veículo não encontrado para atualizar as peças.");
                return false;
            }

            JSONArray partsArray = new JSONArray();
            if (partsDtoList != null) {
                for (PartsDTO partDto : partsDtoList) {
                    JSONObject partObj = new JSONObject();
                    partObj.put("name", partDto.getName());
                    partObj.put("associatedVIN", partDto.getAssociatedVIN());
                    partObj.put("associatedModel", partDto.getAssociatedModel());
                    partObj.put("associatedMake", partDto.getAssociatedMake());
                    partObj.put("associatedYear", partDto.getAssociatedYear());

                    partsArray.put(partObj);
                }
            }

            JSONObject jsonBody = new JSONObject();
            jsonBody.put("parts", partsArray);

            URL url = new URL(ApiGetInfo.getApiUrl(cxt) + "vehicles/" + docId);

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("PATCH");
            Log.d("DEBUG_JSON", "Payload enviado (parts): " + jsonBody.toString());
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
            Log.d("DEBUG_RESTDB", "Response Code do PATCH (parts): " + responseCode);
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
}
