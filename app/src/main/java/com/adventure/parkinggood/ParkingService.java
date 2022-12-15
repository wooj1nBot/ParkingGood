package com.adventure.parkinggood;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.io.UnsupportedEncodingException;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class ParkingService extends Service{

    private final int MIN_CAR_SPEED = 20;
    private final int PARKING_SPEED = 5;
    private boolean isStart = false;
    private boolean isShow = false;
    private long startTime = -1;
    private long startOffTime = -1;

    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    private User user;
    private FirebaseFirestore db;
    private boolean ist = false;
    public static final int NOTIFICATION_PARKING = 127;

    public ParkingService() {
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        db.collection("users").document(currentUser.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()){
                    user = documentSnapshot.toObject(User.class);
                }
            }
        });
    }

    private final LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            super.onLocationResult(locationResult);

            if (locationResult.getLastLocation() != null) {
                Location location = locationResult.getLastLocation();

                double latitude = location.getLatitude();
                double longitude = location.getLongitude();

                if(location.getSpeed() * 3.6f >= MIN_CAR_SPEED){
                    LatLng latLng = new LatLng(latitude, longitude);
                    checkParking(latLng, location);
                }else if(location.getSpeed() * 3.6f <= PARKING_SPEED){
                    isShow = false;
                    LatLng latLng = new LatLng(latitude, longitude);
                    checkParking(latLng, location);

                    if(isStart){
                        if(startTime == -1){
                            startTime = System.currentTimeMillis();
                        }else {
                            if(System.currentTimeMillis() - startTime > 1000 * 60){ // 1분으로 변경
                                isStart = false;
                                try {
                                    ReverseGeo(latLng);
                                } catch (UnsupportedEncodingException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }else {
                    LatLng latLng = new LatLng(latitude, longitude);
                    checkParking(latLng, location);
                    startTime = -1;
                }

            }
        }
    };

    private void checkParking(LatLng latLng, Location location){

        db.collection("users").document(currentUser.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()){
                    user = documentSnapshot.toObject(User.class);
                    if (user != null && user.current_car != null) {
                        if(isNearBy(latLng, user.getCurrent_car().latLng.gLatLng(), 100) && location.getSpeed() * 3.6f >= MIN_CAR_SPEED){
                            if(!isShow) {
                                isShow = true;
                                showParkingOffNotification(user.current_car);
                            }
                        }
                    }else {
                        isStart = true;
                        startTime = -1;
                        showStartNotification();
                    }
                }
            }
        });

    }

    public boolean isNearBy(LatLng place, LatLng parking, int DEFAULT_TOLERANCE){
        return calculateLocationDifference(place, parking) > DEFAULT_TOLERANCE;
    }

    private float calculateLocationDifference(LatLng lastLocation, LatLng firstLocation) {
        float[] dist = new float[1];
        Location.distanceBetween(lastLocation.latitude, lastLocation.longitude, firstLocation.latitude, firstLocation.longitude, dist);
        return dist[0];
    }




    private void startLocationService() {
        startTime = -1;
        String channelId = "location_notification_channel";
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Intent intent = new Intent(this, MainActivity.class);
        intent.setAction(Intent.ACTION_MAIN); intent.addCategory(Intent.CATEGORY_LAUNCHER); intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_MUTABLE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channelId);
        builder.setSmallIcon(R.drawable.map_48px);
        builder.setContentTitle("자동 주차 감지");
        builder.setDefaults(NotificationCompat.DEFAULT_ALL);
        builder.setContentText("실행 중입니다...");
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(false);
        builder.setPriority(NotificationCompat.PRIORITY_MAX);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager != null && notificationManager.getNotificationChannel(channelId) == null) {
                NotificationChannel notificationChannel = new NotificationChannel(channelId, "Location Service", NotificationManager.IMPORTANCE_HIGH);
                notificationChannel.setDescription("This channel is used by location service");
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(500);
        locationRequest.setFastestInterval(500);
        locationRequest.setPriority(Priority.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(locationRequest, mLocationCallback, Looper.getMainLooper());
        startForeground(Constants.LOCATION_SERVICE_ID, builder.build());
    }

    private void stopLocationService() {
        LocationServices.getFusedLocationProviderClient(this).removeLocationUpdates(mLocationCallback);
        stopForeground(true);
        stopSelf();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {
                if (action.equals(Constants.ACTION_START_LOCATION_SERVICE)) {
                    startLocationService();
                } else if (action.equals(Constants.ACTION_STOP_LOCATION_SERVICE)) {
                    stopLocationService();
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    public void ReverseGeo(LatLng latLng) throws UnsupportedEncodingException {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://maps.googleapis.com")
                .addConverterFactory(GsonConverterFactory.create())
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        ReverseRetrofit retrofitService = retrofit.create(ReverseRetrofit.class);
        Call<ReverseGeoResponse> call = retrofitService.getPosts(latLng.latitude+","+latLng.longitude, getString(R.string.maps_api_key), "ko");
        call.enqueue(new Callback<ReverseGeoResponse>() {
            @Override
            public void onResponse(Call<ReverseGeoResponse> call, Response<ReverseGeoResponse> response) {
                if(response.body() != null && response.body().status.equals("OK")){
                    ReverseGeoResult result = response.body().results.get(0);
                    showParkingNotification(latLng, result.formatted_address);
                }else {
                    showParkingNotification(latLng, null);
                }
            }

            @Override
            public void onFailure(Call<ReverseGeoResponse> call, Throwable t) {
                 showParkingNotification(latLng, null);
            }
        });



    }

    public void showStartNotification(){
        String channelId = "parking_notification_channel";
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channelId);
        builder.setSmallIcon(R.drawable.map_48px);
        builder.setContentTitle("출발 감지함");
        builder.setDefaults(NotificationCompat.DEFAULT_ALL);
        builder.setContentText("시속 "+MIN_CAR_SPEED +"km/h를 초과하여 출발을 감지했습니다.");
        builder.setAutoCancel(true);
        builder.setPriority(NotificationCompat.PRIORITY_MAX);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager != null && notificationManager.getNotificationChannel(channelId) == null) {
                NotificationChannel notificationChannel = new NotificationChannel(channelId, "Background Parking Service", NotificationManager.IMPORTANCE_HIGH);
                notificationChannel.setDescription("This channel is used by location service");
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }

        notificationManager.notify(123 /* ID of notification */, builder.build());
    }


    public void showParkingNotification(LatLng latLng, String address){
        Parking parking = new Parking(new CustomLatLng(latLng), address, new Date(System.currentTimeMillis()), null, user.name, currentUser.getUid(), user.profile,  user.phone, user.token);

        String channelId = "parking_notification_channel";
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Intent intent = new Intent(this, MapActivity.class);
        intent.putExtra("parking", parking);
        intent.setAction(Intent.ACTION_MAIN); intent.addCategory(Intent.CATEGORY_LAUNCHER); intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        Intent snoozeIntent = new Intent(this, ParkingReceiver.class);
        snoozeIntent.setAction("PARKING_ACTION");
        snoozeIntent.putExtra("parking", parking);
        PendingIntent snoozePendingIntent =
                PendingIntent.getBroadcast(this, 0, snoozeIntent, PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channelId);
        builder.setSmallIcon(R.drawable.map_48px);
        builder.setContentTitle("주차 감지함");
        builder.setDefaults(NotificationCompat.DEFAULT_ALL);
        builder.addAction(R.drawable.done_48px,"예", snoozePendingIntent);

        if(address != null){
            builder.setContentText(address + "에 주차하셨습니까?");
        }else {
            builder.setContentText("주차를 감지했습니다. 주차하셨습니까?");
        }
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(true);
        builder.setPriority(NotificationCompat.PRIORITY_MAX);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager != null && notificationManager.getNotificationChannel(channelId) == null) {
                NotificationChannel notificationChannel = new NotificationChannel(channelId, "Background Parking Service", NotificationManager.IMPORTANCE_HIGH);
                notificationChannel.setDescription("This channel is used by location service");
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }

        notificationManager.notify(NOTIFICATION_PARKING /* ID of notification */, builder.build());
    }

    public void showParkingOffNotification(Parking parking){

        String channelId = "parking_notification_channel";
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Intent intent = new Intent(this, MapActivity.class);
        intent.setAction(Intent.ACTION_MAIN); intent.addCategory(Intent.CATEGORY_LAUNCHER); intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        Intent snoozeIntent = new Intent(this, ParkingReceiver.class);
        snoozeIntent.setAction("PARKING_ACTION");
        snoozeIntent.putExtra("parkingOff", parking);
        PendingIntent snoozePendingIntent =
                PendingIntent.getBroadcast(this, 0, snoozeIntent, PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channelId);
        builder.setSmallIcon(R.drawable.map_48px);
        builder.setContentTitle("주차 해제 감지");
        builder.setDefaults(NotificationCompat.DEFAULT_ALL);
        builder.addAction(R.drawable.done_48px,"예", snoozePendingIntent);

        builder.setContentText("주차 해제를 감지했습니다. 주차 공간을 벗어났나요?");
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(true);
        builder.setPriority(NotificationCompat.PRIORITY_MAX);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager != null && notificationManager.getNotificationChannel(channelId) == null) {
                NotificationChannel notificationChannel = new NotificationChannel(channelId, "Background Parking Service", NotificationManager.IMPORTANCE_HIGH);
                notificationChannel.setDescription("This channel is used by location service");
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }

        notificationManager.notify(NOTIFICATION_PARKING /* ID of notification */, builder.build());
    }






    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}