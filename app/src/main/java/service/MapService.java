package service;

import java.net.HttpURLConnection;
import java.util.List;

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

    public List<SlotDTO> fetchMapSlots(Context cxt) {
        List<SlotDTO> slotList = new ArrayList<>();
        HttpURLConnection connection = null;

        try {
            // URL para a collection dos slots
            URL url = new URL(ApiGetInfo.getApiUrl(cxt) + "slots");

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

                // O restdb devolve um JSONArray com todos os documentos
                JSONArray jsonArray = new JSONArray(response.toString());

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);

                    // Mapeamento exato com base nos teus atributos: row, column, current_vin
                    Integer row = obj.optInt("row", 0);
                    String column = obj.optString("column", "");
                    String currentVin = obj.optString("current_vin", "");

                    slotList.add(new SlotDTO(row, column, currentVin));
                }
            } else {
                Log.e(TAG, "Erro ao obter slots, código HTTP: " + responseCode);
            }

        } catch (Exception e) {
            Log.e(TAG, "Exceção ao comunicar com o restdb para obter o mapa: " + e.getMessage(), e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        return slotList;
    }
}