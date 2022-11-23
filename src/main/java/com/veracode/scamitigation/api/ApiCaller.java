package com.veracode.scamitigation.api;

import com.veracode.scamitigation.Logger;
import com.veracode.scamitigation.selenium.ExecutionParameters;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

public class ApiCaller {
    private static final String URL_BASE = "https://api.veracode.com";

    public static Optional<ApiResults> runApi(String apiUrl, String requestType, String jsonParameters) {
        HttpsURLConnection connection;
        String fullUrl = URL_BASE + apiUrl;
        try {
            URL applicationsApiUrl = new URL(fullUrl);
            String authorizationHeader = HmacRequestSigner.getVeracodeAuthorizationHeader(
                    ApiCredentials.fromProfile(ExecutionParameters.getInstance().getProfileToUse()),
                    applicationsApiUrl, requestType);
            connection = (HttpsURLConnection)applicationsApiUrl.openConnection();
            connection.setRequestMethod(requestType);
            connection.setRequestProperty("Authorization", authorizationHeader);
            if (jsonParameters != null) {
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);
                try (OutputStream outputStream = connection.getOutputStream()) {
                    byte[] input = jsonParameters.getBytes(StandardCharsets.UTF_8);
                    outputStream.write(input, 0, input.length);
                }
            }

            try (InputStream responseInputStream = connection.getInputStream()) {
                return Optional.of(new ApiResults(readResponse(responseInputStream)));
            }
        } catch (NoSuchAlgorithmException | IllegalStateException | IOException | JSONException |
                 InvalidKeyException e) {
            Logger.log("Unable to run API at: " + fullUrl + "\n\tWith parameters: " + jsonParameters);
            return Optional.empty();
        }
    }

    private static JSONObject readResponse(InputStream responseInputStream) throws IOException, JSONException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] responseBytes = new byte[16384];

        int readResult;
        while((readResult = responseInputStream.read(responseBytes, 0, responseBytes.length)) != -1) {
            outputStream.write(responseBytes, 0, readResult);
        }

        outputStream.flush();
        return outputStream.size() == 0 ? null : new JSONObject(outputStream.toString());
    }
}
