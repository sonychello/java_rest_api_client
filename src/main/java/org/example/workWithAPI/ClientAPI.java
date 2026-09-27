package org.example.workWithAPI;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;

public interface ClientAPI {
    ResultAPI getRequest() throws APIConnectException;


    default ResultAPI getInformation(String urlString, String nameAPI) throws APIConnectException {
        try {
            ResultAPI result = new ResultAPI();
            URL url = URI.create(urlString).toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(2000);
            connection.setReadTimeout(5000);
            int responseCode = connection.getResponseCode();
            OffsetDateTime now = OffsetDateTime.now();
            if (responseCode != 200 & responseCode != 201) {
                throw new ArithmeticException("Request execution error: " + responseCode);
            }

            result.time = now.toString();
            result.nameAPI = nameAPI;
            result.textResult = new StringBuffer(
                    new String(connection
                            .getInputStream()
                            .readAllBytes(), StandardCharsets.UTF_8));
            return result;

        } catch (MalformedURLException e) {
            throw new APIConnectException("Error in make URL: " + e.getMessage());
        } catch (IOException e) {
            throw new APIConnectException("Error in connection: " + e.getMessage());
        } catch (IllegalArgumentException e) {  // <-- ДОБАВИТЬ ЭТОТ БЛОК
            throw new APIConnectException("Error in make URL: " + e.getMessage());
        }
    }


    String getName();
}
