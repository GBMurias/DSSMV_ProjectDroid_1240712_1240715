package temp;

import android.util.Log;
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
    public void createSingleSlotTest() {

        new Thread(() -> {
            HttpURLConnection connection = null;
            try {
                // criar objeto JSON do slot
                JSONObject slot = new JSONObject();
                slot.put("row", 1);
                slot.put("column", "A");
                slot.put("is_occupied", false);
                slot.put("current_vin", JSONObject.NULL);

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
                connection.setConnectTimeout(5000); // 5 segundos timeout
                connection.setReadTimeout(5000);

                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = slot.toString().getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                int responseCode = connection.getResponseCode();

                if (responseCode >= 200 && responseCode < 300) {
                    // sucesso
                    BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    Log.d(TAG, "Slot criado com sucesso! Resposta: " + response.toString());
                } else {
                    // erro
                    BufferedReader br = new BufferedReader(new InputStreamReader(connection.getErrorStream(), "utf-8"));
                    StringBuilder errorResponse = new StringBuilder();
                    String errorLine;
                    while ((errorLine = br.readLine()) != null) {
                        errorResponse.append(errorLine.trim());
                    }
                    Log.e(TAG, "Erro do servidor (" + responseCode + "): " + errorResponse.toString());
                }

            } catch (Exception e) {
                Log.e(TAG, "Exceção na requisição de rede: " + e.getMessage(), e);
            } finally {
                // fechar a conexao
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }).start();
    }
}
