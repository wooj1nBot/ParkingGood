package com.adventure.parkinggood;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {
    private long pressedTime;
    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    View headerView;
    private FirebaseFirestore db;
    TextView tv_name;
    TextView tv_email;
    CircleImageView iv_profile;
    RecyclerView rc;
    private final int REQUEST_CODE_LOCATION_PERMISSION = 21;
    private final int REQUEST_CODE_BACKGROUND_LOCATION_PERMISSION = 21;
    private boolean locationPermissionGranted;
    CardView mother;
    TextView tv_address;
    TextView tv_location;
    TextView tv_date;
    TextView tv_dateTime ;
    TextView tv_undateTime ;
    TextView tv_parking_name;
    CircleImageView profile ;
    Button btn_unparking;
    LinearLayout locate_view;



    Switch sw;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(false)
                .build();
        db.setFirestoreSettings(settings);
        ImageView iv_friend = findViewById(R.id.iv_friend);

        if(currentUser != null && currentUser.isEmailVerified()) {
            FloatingActionButton fb_add = findViewById(R.id.fb_add);
            fb_add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(MainActivity.this, MapActivity.class);
                    startActivity(intent);
                }
            });
            drawerLayout = (DrawerLayout) findViewById(R.id.dl_main_drawer_root);
            navigationView = (NavigationView) findViewById(R.id.nv_main_navigation_root);
            headerView = navigationView.getHeaderView(0);
            tv_name = headerView.findViewById(R.id.tv_name);
            tv_email = headerView.findViewById(R.id.tv_email);
            iv_profile = headerView.findViewById(R.id.profile);
            rc = headerView.findViewById(R.id.rc);
            ImageView iv_logout = headerView.findViewById(R.id.iv_logout);
            sw = headerView.findViewById(R.id.switch1);
            mother = findViewById(R.id.mother);
            tv_address = findViewById(R.id.tv_origin);
            tv_location = findViewById(R.id.tv_dest);
            tv_date = findViewById(R.id.tv_depdate);
            tv_dateTime = findViewById(R.id.tv_dep);
            tv_undateTime = findViewById(R.id.tv_arr);
            tv_parking_name = findViewById(R.id.tv_name);
            profile = findViewById(R.id.profile);
            btn_unparking = findViewById(R.id.btn_unparking);
            locate_view = findViewById(R.id.dest_view);

            sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if(b){
                        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_LOCATION_PERMISSION);
                        } else {
                            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                showPermissionDialog();
                            }else {
                                startLocationService();
                            }
                        }
                    }else {
                        stopLocationService();
                    }
                }
            });

            rc.setLayoutManager(new LinearLayoutManager(this));

            iv_logout.setOnClickListener(new OnSingleClickListener() {
                @Override
                public void onSingleClick(View v) {
                    logout();
                }
            });

            iv_friend.setOnClickListener(new OnSingleClickListener() {
                @Override
                public void onSingleClick(View v) {
                    if(drawerLayout.isDrawerOpen(Gravity.RIGHT)){
                        drawerLayout.closeDrawer(Gravity.RIGHT);
                    }else {
                        drawerLayout.openDrawer(Gravity.RIGHT);
                    }
                }
            });

            drawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
                @Override
                public void onDrawerOpened(View drawerView) {
                    super.onDrawerOpened(drawerView);
                    getUserData();
                    if (isLocationServiceRunning()) {
                        sw.setText("On");
                        sw.setChecked(true);
                    }else {
                        sw.setText("Off");
                        sw.setChecked(false);
                    }
                }
            });

        }else {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }



    }

    public void setMyParking(Parking myParking){
        mother.setVisibility(View.VISIBLE);
        mother.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                Intent intent = new Intent(MainActivity.this, MapActivity.class);
                startActivity(intent);
            }
        });
        tv_address.setText(myParking.address);
        if(myParking.place != null){
            locate_view.setVisibility(View.VISIBLE);
            tv_location.setText(String.format("%s, %d층 %s자리", myParking.place.name, myParking.place.floor+1, myParking.place.key));
        }else {
            locate_view.setVisibility(View.GONE);
        }
        tv_parking_name.setText(myParking.name);
        if(myParking.profile != null){
            Glide.with(MainActivity.this).load(Uri.parse(myParking.profile)).into(profile);
        }else {
            profile.setImageResource(R.drawable.profile);
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 MM월 dd일");
        String getDate = sdf.format(myParking.date);
        tv_date.setText(getDate);
        SimpleDateFormat sdf2 = new SimpleDateFormat("hh:mm a");
        tv_dateTime.setText(sdf2.format(myParking.date) + " 주차");
        if(myParking.unparking_date != null){
            tv_undateTime.setText(sdf2.format(myParking.unparking_date));
        }else {
            tv_undateTime.setText("예정 없음");
        }
        btn_unparking.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                 removeMyCar(myParking);
            }
        });

    }
    public void removeMyCar(Parking parking){
        LoadingView loadingView = new LoadingView(MainActivity.this);
        loadingView.show("주차 해제 중...");
        db.collection("users").document(currentUser.getUid()).update("current_car", null).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                loadingView.stop();
                Toast.makeText(MainActivity.this, "주차 해제 완료되었습니다.", Toast.LENGTH_LONG).show();
                getUserData();
            }
        });
        if(parking.place != null){
            db.collection("place").document(parking.place.id).update("parkings", FieldValue.arrayRemove(parking));
        }
        db.collection("map").document("map").update("parkings", FieldValue.arrayRemove(parking));
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_LOCATION_PERMISSION && grantResults.length > 0) {
            locationPermissionGranted = true;
            showPermissionDialog();
        }
        if (requestCode == REQUEST_CODE_BACKGROUND_LOCATION_PERMISSION && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if(locationPermissionGranted) {
                    startLocationService();
                }else{
                    Toast.makeText(this, "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "퍼미션이 거부되었습니다.", Toast.LENGTH_SHORT).show();
            }
        }

    }

    private void showPermissionDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("권한 필요");
        builder.setMessage("백그라운드 위치 권한을 위해 항상 허용으로 설정해주세요.");
        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                backgroundPermission();
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.create().show();
    }



    private void backgroundPermission(){
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, REQUEST_CODE_BACKGROUND_LOCATION_PERMISSION);
    }

    private boolean isLocationServiceRunning() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager != null) {
            for (ActivityManager.RunningServiceInfo service : activityManager.getRunningServices(Integer.MAX_VALUE)) {
                if (ParkingService.class.getName().equals(service.service.getClassName())) {
                    if (service.foreground) {
                        return true;
                    }
                }
            }
            return false;
        }
        return false;
    }
    private void startLocationService() {
        if (!isLocationServiceRunning()) {
            sw.setText("On");
            Intent intent = new Intent(getApplicationContext(), ParkingService.class);
            intent.setAction(Constants.ACTION_START_LOCATION_SERVICE);
            startService(intent);
            Toast.makeText(this, "주차 자동 감지 기능이 실행되었습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopLocationService() {
        if (isLocationServiceRunning()) {
            sw.setText("Off");
            Intent intent = new Intent(getApplicationContext(), ParkingService.class);
            intent.setAction(Constants.ACTION_STOP_LOCATION_SERVICE);
            startService(intent);
            Toast.makeText(this, "주차 자동 감지 기능이 중지되었습니다.", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        getUserData();
    }

    public void logout(){
        String uid = currentUser.getUid();
        if(!uid.equals("") && db != null) {
            db.collection("users").document(uid).update("status", 0);
        }
        FirebaseMessaging.getInstance().deleteToken();
        FirebaseAuth.getInstance().signOut();
        Intent intents = new Intent(MainActivity.this,LoginActivity.class);
        startActivity(intents);
        finish();
    }

    public void getUserData(){

        db.collection("users").document(currentUser.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    User user = task.getResult().toObject(User.class);
                    if(user != null){
                        String name = (String) user.name;
                        String email = (String) user.email;
                        tv_name.setText(name);
                        tv_email.setText(email);
                        if(user.profile != null){
                            Glide.with(MainActivity.this).load(Uri.parse(user.profile)).into(iv_profile);
                        }
                        if(user.getCurrent_car() != null){
                            setMyParking(user.getCurrent_car());
                        }else {
                            mother.setVisibility(View.GONE);
                        }

                    }
                }
            }
        });
    }



    @Override
    public void onBackPressed() {
        if ( pressedTime == 0 ) {
            Toast.makeText(MainActivity.this, "한번 더 누르면 종료됩니다." , Toast.LENGTH_SHORT).show();
            pressedTime = System.currentTimeMillis();
        }
        else {
            int seconds = (int) (System.currentTimeMillis() - pressedTime);

            if ( seconds > 2000 ) {
                Toast.makeText(MainActivity.this, "한번 더 누르면 종료됩니다." , Toast.LENGTH_SHORT).show();
                pressedTime = 0 ;
            }
            else {
                super.onBackPressed();

                finish(); // app 종료 시키기
            }
        }
    }

}