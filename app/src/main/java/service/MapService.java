package service;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.List;
import static service.ApiGetInfo.getDocumentId;
import static handler.JsonHandler.deserializeMap;
import static handler.JsonHandler.serializeSlotCurrentVinField;
import dto.SlotDTO;
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
public class MapService {

    private static final String TAG = "MapService";

    public List<SlotDTO> fetchMapSlots(Context ctx) {
        List<SlotDTO> slotList = new ArrayList<>();
        HttpURLConnection connection = null;

        try {
            URL url = new URL(ApiGetInfo.getApiUrl(ctx) + "slots");

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
                slotList = deserializeMap(response.toString());

            } else {
                Log.e(TAG, "Error while getting slots, HTTP code: " + responseCode);
            }

        } catch (Exception e) {
            Log.e(TAG, "Exception while communicating with restdb to obtain the map: " + e.getMessage(), e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        return slotList;
    }

    public boolean registerVIN2Slot(Context cxt, SlotDTO tempdto){
        HttpURLConnection connection = null;
        try {
            JSONObject jsonBody = serializeSlotCurrentVinField(tempdto);

            String query = "{\"row\":" + tempdto.getRow() + ",\"column\":\"" + tempdto.getColumn() + "\"}";
            String docId = getDocumentId(cxt, "slots", query);
            Log.d("DEBUG_RESTDB", "Query enviada: " + query + " | DocId encontrado: " + docId);

            if (docId == null) {
                Log.e(TAG, "Slot não encontrado para atualizar.");
                return false;
            }
            URL url = new URL(ApiGetInfo.getApiUrl(cxt) + "slots/" + docId);

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("PATCH");
            Log.d("DEBUG_JSON", "Payload enviado: " + jsonBody.toString());
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
            Log.d("DEBUG_RESTDB", "Response Code do PATCH: " + responseCode);
            return (responseCode >= 200 && responseCode < 300);
        }

        catch (Exception e) {
            Log.e(TAG, "Exception while communicating with restdb to obtain the map: " + e.getMessage(), e);
            return false;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}