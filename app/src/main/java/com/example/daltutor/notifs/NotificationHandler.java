package com.example.daltutor.notifs;

import android.app.Application;
import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.daltutor.core.GlobalContext;
import com.example.daltutor.ui.TutorialPostingActivity;
import com.google.auth.oauth2.GoogleCredentials;

import org.json.JSONException;
import org.json.JSONObject;

public class NotificationHandler {

    private RequestQueue requestQueue;
    private Context context;

    private static final String CREDENTIALS_FILE_PATH = "daltutor-fc3e9-107662dd097a.json";
    private static final String PUSH_NOTIFICATION_ENDPOINT = "https://fcm.googleapis.com/v1/projects/daltutor-fc3e9/messages:send";

    public NotificationHandler (Context context) {
        this.context = context;
        requestQueue = Volley.newRequestQueue(context);
    }

    public void getAccessToken(AccessTokenListener listener) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            try {
                assert context.getAssets() != null;
                InputStream serviceAccountStream = context.getAssets().open(CREDENTIALS_FILE_PATH);
                GoogleCredentials googleCredentials = GoogleCredentials
                        .fromStream(serviceAccountStream)
                        .createScoped(Collections.singletonList("https://www.googleapis.com/auth/firebase.messaging"));
                googleCredentials.refreshIfExpired();
                String token = googleCredentials.getAccessToken().getTokenValue();
                listener.onAccessTokenReceived(token);
                Log.d("token", "token" + token);
            } catch (IOException e) {
                listener.onAccessTokenError(e);
            }
        });
        executorService.shutdown();
    }

    public void sendNotification(String postingID, String tutorUsername) {
        getAccessToken(new AccessTokenListener() {
            @Override
            public void onAccessTokenReceived (String token){
                sendNotificationHelper(token, postingID, tutorUsername);
            }

            @Override
            public void onAccessTokenError (Exception exception){
                exception.printStackTrace();
            }
        });
    }



    private void sendNotificationHelper(String authToken, String postingID, String tutorUsername) {
        try {
            JSONObject notificationJSONBody = new JSONObject();
            notificationJSONBody.put("title", "New Tutorial Posting");
            notificationJSONBody.put("body", "A new tutorial was posted by a preferred tutor");

            JSONObject dataJSONBody = new JSONObject();
            dataJSONBody.put("postingID", postingID);
            dataJSONBody.put("tutor", tutorUsername);

            JSONObject messageJSONBody = new JSONObject();
            messageJSONBody.put("topic", tutorUsername);
            messageJSONBody.put("notification", notificationJSONBody);
            messageJSONBody.put("data", dataJSONBody);

            JSONObject pushNotificationJSONBody = new JSONObject();
            pushNotificationJSONBody.put("message", messageJSONBody);

            Log.d("NotificationBody", "JSON Body: " + pushNotificationJSONBody.toString());

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    PUSH_NOTIFICATION_ENDPOINT,
                    pushNotificationJSONBody,
                    response -> {
                        Log.d("NotificationResponse", "Response: " + response.toString());
                    },
                    error -> {
                        Log.e("NotificationError", "Error Response: " + error.toString());
                        if (error.networkResponse != null) {
                            Log.e("NotificationError", "Status Code: " + error.networkResponse.statusCode);
                            Log.e("NotificationError", "Error Data: " + new String(error.networkResponse.data));
                        }
                        error.printStackTrace();
                    }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json; charset=UTF-8");
                    headers.put("Authorization", "Bearer " + authToken);
                    Log.d("NotificationHeaders", "Headers: " + headers.toString());
                    return headers;
                }
            };
            requestQueue.add(request);

        } catch (JSONException e) {
            Log.e("NotificationJSONException", "Error creating notification JSON: " + e.getMessage());
        }

    }
}
