package service;

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
import service.ApiGetInfo;

public class VehicleService {
    private static final String TAG = "VehicleService";
    public void registerIntake(Context cxt){
        new Thread(() -> {
            HttpURLConnection connection = null;
            try {
                // criar objeto JSON para o slot e JSONArray para guardar varios slots
                JSONObject intake = new JSONObject();
                intake.put("VIN", "A");
                intake.put("model", "A");
                intake.put("make", "A");
                intake.put("year", 2009);
                intake.put("is_salvageable", false);
                intake.put("slot_id", JSONObject.NULL);
                intake.put("entry_date", JSONObject.NULL);

                // criar pedido http post
                URL url = new URL(
                        ApiGetInfo.getApiUrl(cxt) + "intakes"
                );
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");

                // definir os headers
                connection.setRequestProperty("x-apikey", ApiGetInfo.getApiKey(cxt));
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Accept", "application/json");

                // indicar que vamos enviar dados no corpo do pedido
                connection.setDoOutput(true);
                connection.setConnectTimeout(10000); // 5 segundos timeout
                connection.setReadTimeout(10000);

                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = intake.toString().getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                int responseCode = connection.getResponseCode();

                if (responseCode >= 200 && responseCode < 300) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line.trim());
                    }
                    Log.d(TAG, "50 Slots (0-9, A-F) criados com sucesso! Resposta: " + response.toString());
                } else {
                    BufferedReader br = new BufferedReader(new InputStreamReader(connection.getErrorStream(), "utf-8"));
                    StringBuilder errorResponse = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        errorResponse.append(line.trim());
                    }
                    Log.e(TAG, "Erro ao criar slots (" + responseCode + "): " + errorResponse.toString());
                }

            } catch (Exception e) {
                Log.e(TAG, "Exceção: " + e.getMessage(), e);
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }).start();
    }
}
