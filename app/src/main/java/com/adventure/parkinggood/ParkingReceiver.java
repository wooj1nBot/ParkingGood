package com.adventure.parkinggood;

import static android.content.Context.NOTIFICATION_SERVICE;

import static com.adventure.parkinggood.ParkingService.NOTIFICATION_PARKING;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.UnsupportedEncodingException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class ParkingReceiver extends BroadcastReceiver {
    private FirebaseFirestore db;
    FirebaseAuth mAuth;
    FirebaseUser currentUser;

    public ParkingReceiver(){
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_PARKING);
        Parking parking = (Parking) intent.getSerializableExtra("parking");
        if(parking != null){
            setMyCarLoc(parking);
        }else {
            parking = (Parking) intent.getSerializableExtra("parkingOff");
            removeMyCar(parking, context);
        }

    }

    public void removeMyCar(Parking parking, Context context){
        db.collection("users").document(currentUser.getUid()).update("current_car", null).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(context, "주차 해제 완료되었습니다.", Toast.LENGTH_LONG).show();
            }
        });
        if(parking.place != null){
            db.collection("place").document(parking.place.id).update("parkings", FieldValue.arrayRemove(parking));
        }
        db.collection("map").document("map").update("parkings", FieldValue.arrayRemove(parking));
    }



    public void setMyCarLoc(Parking parking){
        db.collection("map").document("map").update("parkings", FieldValue.arrayUnion(parking));
        db.collection("users").document(currentUser.getUid()).update("parking_record", FieldValue.arrayUnion(parking));
        db.collection("users").document(currentUser.getUid()).update("current_car", parking);
    }


}