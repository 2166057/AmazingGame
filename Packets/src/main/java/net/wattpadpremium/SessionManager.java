package net.wattpadpremium;

import okhttp3.*;

import java.io.IOException;


public class SessionManager {

    private static final String BASE_URL_CLIENT = "http://127.0.0.1:8080/api/gameauth/session";
    private static final String BASE_URL_SERVERSIDE = "http://127.0.0.1:8080/api/gameauth/validate";

    public static String createUserSessionToken(String userToken, String serverIP) throws IOException, InterruptedException {
        OkHttpClient client = new OkHttpClient();

        RequestBody body = new FormBody.Builder()
                .add("userToken", userToken)
                .add("serverIP", serverIP)
                .build();

        Request request = new Request.Builder()
                .url(BASE_URL_CLIENT)
                .post(body)
                .addHeader("X-Forwarded-For", "192.168.1.1")
                .build();

        try (Response response = client.newCall(request).execute()) {
            String strResponse = response.body().string();
            System.out.println("Response: " + strResponse);
            return strResponse;
        }
    }

    public static String validateSession(String sessionToken, String serverIP) throws IOException {
        OkHttpClient client = new OkHttpClient();

        RequestBody body = new FormBody.Builder()
                .add("sessionToken", sessionToken)
                .add("serverIP", serverIP)
                .build();

        Request request = new Request.Builder()
                .url(BASE_URL_SERVERSIDE)
                .post(body)
                .addHeader("X-Forwarded-For", "192.168.1.1")
                .build();

        try (Response response = client.newCall(request).execute()) {
            String strResponse = response.body().string();
            System.out.println("Response: " + strResponse);
            return strResponse;
        }
    }


}
