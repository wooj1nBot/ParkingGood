package com.adventure.parkinggood;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.util.Log;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;


public class FCM_Push {
    Context context;
    String token; Map<String,String> data;
    private static final String MESSAGING_SCOPE = "https://www.googleapis.com/auth/firebase.messaging";
    private static final String[] SCOPES = { MESSAGING_SCOPE };
    private static final String PROJECT_ID = "adventuredesign-becc1";
    private static final String BASE_URL = "https://fcm.googleapis.com";
    private static final String FCM_SEND_ENDPOINT = "/v1/projects/" + PROJECT_ID + "/messages:send";

    public FCM_Push(Context context, String token, Map<String,String> data){
    this.token = token;
    this.data = data;
    this.context = context;
    }

    public void send(){
        if(token != null) {
            new UploadTask().execute();
        }
    }

    private class UploadTask extends AsyncTask<Void,Void,Void> {
        JSONObject main = new JSONObject();

        @Override
        protected void onPreExecute() {
            try {
                main = new JSONObject();
                JSONObject massage = new JSONObject();
                JSONObject datajson = new JSONObject();
                JSONObject android = new JSONObject();
                for( Map.Entry<String, String> entry : data.entrySet() ) {
                    String key = entry.getKey();
                    Object value = entry.getValue();
                    datajson.put(key, value);
                }
                JSONObject notification = new JSONObject();
                notification.put("title",data.get("title"));
                notification.put("channel_id","CH_NOTICE");

                notification.put("body", data.get("message"));
                notification.put("click_action","FCM_PARKING_ACTIVITY");

                android.put("notification",notification);
                android.put("priority","high");
                android.put("data",datajson);
                massage.put("token",token);
                massage.put("android",android);
                main.put("message",massage);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            super.onPreExecute();

        }


        @Override
        protected Void doInBackground(Void... objects) {

            try {
               sendCommonMessage();
            } catch (IOException e) {
                e.printStackTrace();
            }


            return null;
        }
        public void sendCommonMessage() throws IOException {
            sendMessage();
        }

        private void sendMessage() throws IOException {
            HttpURLConnection connection = getConnection();
            connection.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8);
            wr.write(main.toString());
            wr.flush();
            wr.close();

            Log.d("push", String.valueOf(connection.getResponseMessage()));

        }

        private HttpURLConnection getConnection() throws IOException {
            // [START use_access_token]
            URL url = new URL(BASE_URL + FCM_SEND_ENDPOINT);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("POST");
            String token = getAccessToken();
            Log.d("push", token);
            httpURLConnection.setRequestProperty("Authorization", "Bearer " + token);
            httpURLConnection.setRequestProperty("Content-Type", "application/json; UTF-8");
            return httpURLConnection;
            // [END use_access_token]
        }
        private String getAccessToken() throws IOException {
            AssetManager am = context.getAssets();
            InputStream inputStream = am.open("credentials.json");
            GoogleCredential googleCredential = GoogleCredential
                    .fromStream(inputStream)
                    .createScoped(Arrays.asList(SCOPES));
            googleCredential.refreshToken();
            return googleCredential.getAccessToken();
        }
        @Override
        protected void onPostExecute(Void v) {

        }

    }
}
