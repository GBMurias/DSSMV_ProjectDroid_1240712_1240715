package service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ServiceTest {

    public String fazerPedido() {
        HttpURLConnection connection = null;
        StringBuilder resposta = new StringBuilder();

        try {
            String apiURL = "https://projectdroiddb-ee07.restdb.io/rest/park";

            URL url = new URL(apiURL);
            connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("x-apikey", "4e7f3d24d5cec54b60c3f5d61dc58f960c1af");

            int codigo = connection.getResponseCode();

            InputStream stream;
            if (codigo >= 200 && codigo < 300) {
                stream = connection.getInputStream();
            } else {
                stream = connection.getErrorStream();
            }

            if (stream != null) {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(stream, "UTF-8")
                );

                String linha;
                while ((linha = reader.readLine()) != null) {
                    resposta.append(linha);
                }
                reader.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "Erro ao conectar: " + e.getMessage();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        return resposta.toString();
    }
}