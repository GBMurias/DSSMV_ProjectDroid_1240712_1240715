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
import dto.VehicleDTO;

public class VehicleService {
    private static final String TAG = "VehicleService";

    public boolean registerIntake(Context cxt, VehicleDTO dto) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(
                    ApiGetInfo.getApiUrl(cxt) + "slots"
            );
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("x-apikey", ApiGetInfo.getApiKey(cxt));
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);
            connection.setConnectTimeout(8000);
            connection.setReadTimeout(8000);

            JSONObject jsonBody = new JSONObject();
            jsonBody.put("vin", dto.getVin());
            jsonBody.put("make", dto.getMake());
            jsonBody.put("model", dto.getModel());
            jsonBody.put("year", dto.getYear());

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonBody.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            return (responseCode >= 200 && responseCode < 300);

        } catch (Exception e) {
            Log.e(TAG, "Erro ao guardar veículo no restdb: " + e.getMessage(), e);
            return false;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
