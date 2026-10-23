package service;

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class NHTSAService {
    private static final String TAG = "NHTSAService";
    public interface vehicleDetailsCallback {
        void onSuccess(String make, String model, int year);
        void onError(String errorMessage);
    }
    public void fetchVehicleDetails(String vin, vehicleDetailsCallback callback) {
        // Executa a chamada à API numa thread secundária
        new Thread(() -> {
            HttpURLConnection connection = null;
            try {
                // Monta o URL oficial da NHTSA com o VIN fornecido
                String urlString = "https://vpic.nhtsa.dot.gov/api/vehicles/decodevinvalues/" + vin + "?format=json";
                URL url = new URL(urlString);

                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Accept", "application/json");
                connection.setConnectTimeout(8000);
                connection.setReadTimeout(8000);

                int responseCode = connection.getResponseCode();
                if (responseCode == 200) {
                    // ler resposta JSON
                    BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line.trim());
                    }

                    // NHTSA devolve um objeto raiz com um array "Results"
                    JSONObject jsonResponse = new JSONObject(response.toString());
                    JSONArray resultsArray = jsonResponse.getJSONArray("Results");

                    if (resultsArray.length() > 0) {
                        JSONObject vehicleData = resultsArray.getJSONObject(0);

                        // extrair os atributos pretendidos (se vierem vazios colocar como "Desconhecido")
                        String make = vehicleData.optString("Make", "Unknown");
                        String model = vehicleData.optString("Model", "Unknown");
                        String yearStr = vehicleData.optString("ModelYear", "0");

                        int year = 0;
                        try {
                            year = Integer.parseInt(yearStr);
                        } catch (NumberFormatException e) {
                            year = 0;
                        }

                        // Validação básica para ver se o VIN era real (a NHTSA devolve Make vazio se o VIN for inválido)
                        if (make.isEmpty() || make.equals("Unknown")) {
                            callback.onError("VIN inválido ou não reconhecido pela NHTSA.");
                        } else {
                            // Sucesso! Devolve os dados
                            callback.onSuccess(make, model, year);
                        }
                    } else {
                        callback.onError("Nenhum resultado encontrado para este VIN.");
                    }

                } else {
                    callback.onError("Erro na ligação à API NHTSA. Código: " + responseCode);
                }

            } catch (Exception e) {
                Log.e(TAG, "Exceção ao consultar NHTSA: " + e.getMessage(), e);
                callback.onError("Erro de rede: " + e.getMessage());
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }).start();
    }
}
