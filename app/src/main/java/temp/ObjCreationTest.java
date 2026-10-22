package temp;

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ObjCreationTest {
    private static final String TAG = "RestDbTest";
    private static final String DB_URL = "https://projectdroiddb-ee07.restdb.io/rest/slots";
    private static final String API_KEY = "4e7f3d24d5cec54b60c3f5d61dc58f960c1af";
    public void createAllSlots() {
        // Teste de conexão com a base dados da API restdb.io
        new Thread(() -> {
            HttpURLConnection connection = null;
            try {
                // criar objeto JSON para o slot e JSONArray para guardar varios slots
                JSONArray slotsArray = new JSONArray();
                for (int row = 0; row <= 9; row++) {
                    for (char colChar = 'A'; colChar <= 'E'; colChar++) {
                        JSONObject slot = new JSONObject();
                        slot.put("row", row);
                        slot.put("column", String.valueOf(colChar));
                        slot.put("is_occupied", false);
                        slot.put("current", JSONObject.NULL);
                        slotsArray.put(slot);
                    }
                }
                // criar pedido http post
                URL url = new URL(DB_URL);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");

                // definir os headers
                connection.setRequestProperty("x-apikey", API_KEY);
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Accept", "application/json");

                // indicar que vamos enviar dados no corpo do pedido
                connection.setDoOutput(true);
                connection.setConnectTimeout(10000); // 5 segundos timeout
                connection.setReadTimeout(10000);

                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = slotsArray.toString().getBytes("utf-8");
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
                    Log.d(TAG, "100 Slots (0-9, A-J) criados com sucesso! Resposta: " + response.toString());
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

