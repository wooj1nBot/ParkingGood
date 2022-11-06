package com.adventure.parkinggood;

import static android.app.Notification.DEFAULT_SOUND;
import static android.app.Notification.DEFAULT_VIBRATE;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.Request;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SizeReadyCallback;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.Random;


public class MyFCMService extends FirebaseMessagingService {
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    public MyFCMService() {
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {

        super.onMessageReceived(remoteMessage);

            db = FirebaseFirestore.getInstance();
            mAuth = FirebaseAuth.getInstance();

            Map<String, String> data = remoteMessage.getData();

            if (db != null && mAuth != null && remoteMessage.getData().size() > 0) {

                SharedPreferences preferences = getSharedPreferences("ChatNotifi", MODE_PRIVATE);
                boolean mainisnotifi_off = preferences.getBoolean("isnotifi_off", false);
                if (!mainisnotifi_off) {
                    sendFriendNotification(data);
                }

            }

    }

    private void sendFriendNotification(Map<String,String> data) {

        int noticationID = 998;
        int randomID = new Random().nextInt();
        String groupkey = "MESSAGE";


        //메인
        Intent intent = new Intent(this, MapActivity.class);
        intent.setAction("FCM_PARKING_ACTIVITY"); intent.addCategory(Intent.CATEGORY_LAUNCHER); intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        String channelId = "CH_NOTICE";
        String title = data.get("title");
        String message = data.get("message");
        String profile = data.get("profile");

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.map_48px)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setAutoCancel(true)
                        .setWhen(System.currentTimeMillis())
                        .setSound(defaultSoundUri)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                        .setDefaults(DEFAULT_SOUND | DEFAULT_VIBRATE)
                        .setGroup(groupkey)
                        .setContentIntent(pendingIntent);

        Notification summaryNotification =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.map_48px)
                        .setContentText("주차 알림")
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setDefaults(DEFAULT_SOUND | DEFAULT_VIBRATE)
                        .setGroup(groupkey)
                        .setGroupSummary(true)
                        .setContentIntent(pendingIntent).build();

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.diskCacheStrategy(DiskCacheStrategy.NONE);
        requestOptions.skipMemoryCache(true);
        requestOptions.format(DecodeFormat.PREFER_RGB_565);
        if(profile != null) {
            Glide.with(this).asBitmap().load(profile)
                    .apply(requestOptions)
                    .fitCenter()
                    .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                    .into(new Target<Bitmap>() {
                        @Override
                        public void onLoadStarted(@Nullable Drawable placeholder) {

                        }

                        @Override
                        public void onLoadFailed(@Nullable Drawable errorDrawable) {

                        }

                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            notificationBuilder.setLargeIcon(resource);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                String description = "Receive new notifications";
                                int importance = NotificationManager.IMPORTANCE_HIGH;
                                NotificationChannel channel = new NotificationChannel("CH_NOTICE", "New message notification", importance);
                                channel.setDescription(description);
                                channel.setShowBadge(true);
                                channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
                                // Register the channel with the system; you can't change the importance
                                // or other notification behaviors after this
                                notificationManager.createNotificationChannel(channel);
                            }

                            notificationManager.notify(randomID /* ID of notification */, notificationBuilder.build());
                            notificationManager.notify(noticationID /* ID of notification */, summaryNotification);

                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {

                        }

                        @Override
                        public void getSize(@NonNull SizeReadyCallback cb) {

                        }

                        @Override
                        public void removeCallback(@NonNull SizeReadyCallback cb) {

                        }

                        @Override
                        public void setRequest(@Nullable Request request) {

                        }

                        @Nullable
                        @Override
                        public Request getRequest() {
                            return null;
                        }

                        @Override
                        public void onStart() {

                        }

                        @Override
                        public void onStop() {

                        }

                        @Override
                        public void onDestroy() {

                        }
                    });
        }else {
            Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.profile);
            notificationBuilder.setLargeIcon(icon);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                String description = "Receive new notifications";
                int importance = NotificationManager.IMPORTANCE_HIGH;
                NotificationChannel channel = new NotificationChannel("CH_NOTICE", "New message notification", importance);
                channel.setDescription(description);
                channel.setShowBadge(true);
                channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
                // Register the channel with the system; you can't change the importance
                // or other notification behaviors after this
                notificationManager.createNotificationChannel(channel);
            }

            notificationManager.notify(randomID /* ID of notification */, notificationBuilder.build());
            notificationManager.notify(noticationID /* ID of notification */, summaryNotification);
        }


    }

}