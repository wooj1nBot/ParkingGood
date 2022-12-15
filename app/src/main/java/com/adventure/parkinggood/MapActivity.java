package com.adventure.parkinggood;

import static com.adventure.parkinggood.SettingActivity.PARKING_SIZE;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.TimePickerDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PointOfInterest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.maps.android.ui.IconGenerator;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class MapActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback {

    private Marker marker_car;
    private List<Marker> markers = new ArrayList<>();
    private List<Marker> place_markers = new ArrayList<>();
    private List<Marker> search_markers = new ArrayList<>();
    private SlidingUpPanelLayout layout;
    private TextView tv_result;
    private RecyclerView rc;
    private long pressedTime;
    private PlacesClient placesClient;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean locationPermissionGranted;
    private GoogleMap map;
    private SupportMapFragment mapFragment;
    private static final int DEFAULT_ZOOM = 17;
    private int searchComplete = 0;
    private int searchResult = 0;
    private List<Place> searchLocations = new ArrayList<>();
    private FloatingActionButton fb_loc;
    private FloatingActionButton fb_mycar;
    private List<ParkingPlace> places;
    private Parking myCar;
    private User user;
    private  Date time;
    private Maps mapData;
    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    private FirebaseFirestore db;
    private String key;
    private Parking parking;
    private CardView messageView;
    private TextView tv_message;


    private static final double DEFAULT_TOLERANCE = 40;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        Places.initialize(getApplicationContext(), getString(R.string.maps_api_key));
        placesClient = Places.createClient(this);

        // Construct a FusedLocationProviderClient.
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        tv_result = findViewById(R.id.tv_result);
        ImageView iv_del = findViewById(R.id.delete);
        rc = findViewById(R.id.rc);
        rc.setLayoutManager(new LinearLayoutManager(this));
        rc.addItemDecoration(new DividerItemDecoration(MapActivity.this, 1));
        layout = findViewById(R.id.main_panel);
        fb_loc = findViewById(R.id.fb_loc);
        fb_mycar = findViewById(R.id.fb_mycar);

        layout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
        CardView bar = findViewById(R.id.bar);
        CardView search_bar = findViewById(R.id.search);
        RelativeLayout search_view = findViewById(R.id.search_view);
        search_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bar.setVisibility(View.GONE);
                search_bar.setVisibility(View.VISIBLE);
            }
        });
        ImageView iv_home = findViewById(R.id.iv_home);
        iv_home.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                finish();
            }
        });

        ImageView iv_back = findViewById(R.id.back);

        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bar.setVisibility(View.VISIBLE);
                search_bar.setVisibility(View.GONE);
                for(Marker marker : search_markers){
                    marker.remove();
                }
                search_markers.clear();
                layout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
            }
        });
        messageView = findViewById(R.id.messageview);
        tv_message = findViewById(R.id.tv_message);


        EditText editText = findViewById(R.id.ed_search);
        editText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    //키패드 내리기
                    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);

                    //처리
                    String s = editText.getText().toString();
                    if(s.length() > 0){
                        SearchLocation(s);
                    }
                    return true;
                }
                return false;
            }
        });

        iv_del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editText.setText(null);
                for(Marker marker : search_markers){
                    marker.remove();
                }
                search_markers.clear();
                layout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
            }
        });

        fb_loc.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                if(map == null) return;
                fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<android.location.Location>() {
                    @Override
                    public void onSuccess(android.location.Location location) {
                        LatLng latLng =  new LatLng(location.getLatitude(),
                                location.getLongitude());
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                latLng, DEFAULT_ZOOM));
                        showMessage(findMyNearByParkingCount(latLng), 50);
                    }
                });
            }
        });
        fb_mycar.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                if(map == null || myCar == null) return;
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        myCar.latLng.gLatLng(), DEFAULT_ZOOM));
            }
        });

        Intent intent = getIntent();

        parking = (Parking) intent.getSerializableExtra("parking");

    }
    public void showMessage(int count, int radius){
        if(count == 0){
            tv_message.setText(String.format("반경 %dm 이내에 등록된 주차장이 없습니다.", radius, count));
        }else {
            tv_message.setText(String.format("반경 %dm 이내에 등록된 주차장이 %d군데 있습니다.", radius, count));
        }
        messageView.setVisibility(View.VISIBLE);
        ObjectAnimator animator = ObjectAnimator.ofFloat(messageView, "alpha", 0f, 1f);
        animator.setDuration(1000);
        animator.start();
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                ObjectAnimator animator = ObjectAnimator.ofFloat(messageView, "alpha", 1f, 1f);
                animator.setDuration(1500);
                animator.start();
                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        ObjectAnimator animator = ObjectAnimator.ofFloat(messageView, "alpha", 1f, 0f);
                        animator.setDuration(1000);
                        animator.start();
                        animator.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                messageView.setVisibility(View.GONE);
                            }
                        });
                    }
                });
            }
        });



    }

    public void ReverseGeo(LatLng latLng) throws UnsupportedEncodingException {
        LoadingView loadingView = new LoadingView(MapActivity.this);
        loadingView.show("loading...");
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
                loadingView.stop();
                if(response.body() != null && response.body().status.equals("OK")){
                    ReverseGeoResult result = response.body().results.get(0);
                    CustomLatLng lng = new CustomLatLng();
                    lng.sLatLng(latLng);
                    setParkingDialog(latLng, result.formatted_address);
                }else {
                    setParkingDialog(latLng, null);
                }
            }

            @Override
            public void onFailure(Call<ReverseGeoResponse> call, Throwable t) {
                loadingView.stop();
                setParkingDialog(latLng, null);
            }
        });

    }

    public void init(){
        getMapData();
        getMyInfo();
        getPlaceData();
    }

    public void getMyInfo(){
        db.collection("users").document(currentUser.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                user = documentSnapshot.toObject(User.class);
                myCar = user.getCurrent_car();
                if(myCar != null){
                    setMyCarMarker(myCar);
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                            myCar.latLng.gLatLng(), DEFAULT_ZOOM));
                }
                if(parking != null){
                    setParkingDialog(parking.latLng.gLatLng(), parking.address);
                }
            }
        });
    }

    public void getMapData(){
        db.collection("map").document("map").get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                 mapData = documentSnapshot.toObject(Maps.class);
                 if(mapData != null) {
                     if(mapData.parkings == null) return;
                     drawParkingMarker(mapData.parkings);
                 }
            }
        });
    }

    public void getPlaceData(){
        db.collection("place").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                 if(!queryDocumentSnapshots.isEmpty()){
                     places = queryDocumentSnapshots.toObjects(ParkingPlace.class);
                     drawParkingPlaceMarker(places);
                 }
            }
        });
    }

    public void removeCar(Parking parking){
        db.collection("map").document("map").update("parkings", FieldValue.arrayRemove(parking)).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                 init();
            }
        });
    }

    public void removeMyCar(Parking parking){
        db.collection("users").document(currentUser.getUid()).update("current_car", null).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(MapActivity.this, "주차 해제 완료되었습니다.", Toast.LENGTH_LONG).show();
                init();
            }
        });
        if(parking.place != null){
            db.collection("place").document(parking.place.id).update("parkings", FieldValue.arrayRemove(parking));
        }
        db.collection("map").document("map").update("parkings", FieldValue.arrayRemove(parking));
        if(marker_car != null) marker_car.remove();
    }

    @Override
    protected void onResume() {
        super.onResume();
        init();
    }

    public void setMyCarLoc(Parking parking){

        db.collection("map").document("map").update("parkings", FieldValue.arrayUnion(parking)).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(MapActivity.this, "주차 표시가 완료되었습니다.", Toast.LENGTH_LONG).show();
                }else {
                    Toast.makeText(MapActivity.this, "주차 표시에 실패했습니다.", Toast.LENGTH_LONG).show();
                }
            }
        });
        db.collection("users").document(currentUser.getUid()).update("parking_record", FieldValue.arrayUnion(parking));
        db.collection("users").document(currentUser.getUid()).update("current_car", parking);
        setMyCarMarker(parking);
        removeCar(myCar);
        myCar = parking;

    }

    public void setMyCarMarker(Parking parking){
        if(marker_car != null) marker_car.remove();
        View view = LayoutInflater.from(MapActivity.this).inflate(R.layout.icon, (RelativeLayout) findViewById(R.id.rv));
        IconGenerator iconFactory = new IconGenerator(this);
        iconFactory.setContentView(view);
        iconFactory.setBackground(null);
        Marker de = map.addMarker(new MarkerOptions()
                .position(parking.latLng.gLatLng()).title("내 차").icon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon()))
        );
        de.setTag(parking);
        marker_car = de;
    }

    public void setSearchMarker(List<Place> locations){
        View view = LayoutInflater.from(MapActivity.this).inflate(R.layout.icon, (RelativeLayout) findViewById(R.id.rv));
        ImageView iv_marker = view.findViewById(R.id.imageView);
        ImageView iv_icon = view.findViewById(R.id.iv_icon);
        View fill = view.findViewById(R.id.view);
        TextView textView = view.findViewById(R.id.textView);
        IconGenerator iconFactory = new IconGenerator(this);
        iconFactory.setContentView(view);
        iconFactory.setBackground(null);

        for(Marker marker : search_markers){
            marker.remove();
        }
        search_markers.clear();
        iv_icon.setImageResource(R.drawable.search_48px);
        iv_marker.setImageTintList(ColorStateList.valueOf(Color.parseColor("#00ACC1")));
        fill.setBackgroundColor(Color.parseColor("#00ACC1"));
        for(int i = 0; i < locations.size(); i++){
            Place location = locations.get(i);
            textView.setText(location.getName());
            Marker check = map.addMarker(new MarkerOptions()
                    .position(locations.get(i).getLatLng()).title(location.getName()).icon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon()))
            );
            check.setTag(location.getId());
            check.showInfoWindow();
            search_markers.add(check);
        }
    }

    public void setPlaceMarker(ParkingPlace parking){
        float max = parking.row * parking.column * parking.floor;
        float per = (max - parking.parkings.size()) / max;

        View view = LayoutInflater.from(MapActivity.this).inflate(R.layout.icon, (RelativeLayout) findViewById(R.id.rv));
        ImageView iv_marker = view.findViewById(R.id.imageView);
        ImageView iv_icon = view.findViewById(R.id.iv_icon);
        View fill = view.findViewById(R.id.view);
        TextView textView = view.findViewById(R.id.textView);
        IconGenerator iconFactory = new IconGenerator(this);
        iconFactory.setContentView(view);
        iconFactory.setBackground(null);
        iv_icon.setImageResource(R.drawable.local_parking_48px);

        if(per == 0){
            iv_marker.setImageTintList(ColorStateList.valueOf(Color.parseColor("#F04C22")));
            fill.setBackgroundColor(Color.parseColor("#F04C22"));
        }else {
            if(per >= 0.75f){
                iv_marker.setImageTintList(ColorStateList.valueOf(Color.parseColor("#43A047")));
                fill.setBackgroundColor(Color.parseColor("#43A047"));
            }else if(per >= 0.5f){
                iv_marker.setImageTintList(ColorStateList.valueOf(Color.parseColor("#BFCB30")));
                fill.setBackgroundColor(Color.parseColor("#BFCB30"));
            }else if(per >= 0.25f){
                iv_marker.setImageTintList(ColorStateList.valueOf(Color.parseColor("#FFAF19")));
                fill.setBackgroundColor(Color.parseColor("#FFAF19"));
            }else{
                iv_marker.setImageTintList(ColorStateList.valueOf(Color.parseColor("#F68121")));
                fill.setBackgroundColor(Color.parseColor("#F68121"));
            }
        }

        textView.setText("주차장");

        Marker de = map.addMarker(new MarkerOptions()
                .position(parking.latLng.gLatLng()).title(parking.name).icon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon()))
        );
        de.setTag(parking.getId());
        place_markers.add(de);
    }

    public void setParkingMarker(Parking parking){
        View view = LayoutInflater.from(MapActivity.this).inflate(R.layout.custom_marker, (RelativeLayout) findViewById(R.id.rv));
        IconGenerator iconFactory = new IconGenerator(this);
        iconFactory.setContentView(view);
        iconFactory.setBackground(null);
        Marker park = map.addMarker(new MarkerOptions()
                .position(parking.latLng.gLatLng()).title("주차됨").icon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon()))
        );
        park.setTag(parking);
        markers.add(park);
    }

    public void drawParkingMarker(List parkings){
        for(Marker marker : markers){
            marker.remove();
        }
        markers.clear();
        for(int i = 0; i < parkings.size(); i++){
            Parking parking = (Parking) parkings.get(i);
            setParkingMarker(parking);
        }
    }

    public void drawParkingPlaceMarker(List<ParkingPlace> places){
        for(Marker marker : place_markers){
            marker.remove();
        }
        place_markers.clear();
        for(int i = 0; i < places.size(); i++){
            ParkingPlace parking = places.get(i);
            if(parking.isHasParking(currentUser.getUid()) == null){
                setPlaceMarker(parking);
            }
        }
    }


    public void showParkingInfo(Parking parking){
        AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
        View view = LayoutInflater.from(MapActivity.this).inflate(R.layout.location_dialog, (RelativeLayout) findViewById(R.id.dialog));
        CircleImageView profile = view.findViewById(R.id.profile);
        TextView tv_name = view.findViewById(R.id.tv_name);
        TextView tv_address = view.findViewById(R.id.tv_address);
        TextView tv_date = view.findViewById(R.id.tv_date);
        Button btn_unparking = view.findViewById(R.id.btn_mode);
        TextView tv_unparking_date = view.findViewById(R.id.tv_unparking_date);
        ImageView iv_close = view.findViewById(R.id.iv_close);
        RelativeLayout userview = view.findViewById(R.id.btn_user);

        if(parking.profile != null){
            Glide.with(MapActivity.this).load(Uri.parse(parking.profile)).into(profile);
        }
        tv_name.setText(parking.name);

        if(parking.address != null){
            tv_address.setText(parking.address);
        }else {
            tv_address.setText("주소 정보 없음");
        }

        if(parking.unparking_date != null){
            tv_unparking_date.setText(getTimeDiff(parking.unparking_date.getTime()));
        }else {
            tv_unparking_date.setText("주차 예정 시간 없음");
        }
        userview.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                User user = new User(parking.name, null, parking.uid, parking.profile, parking.phone, parking.token);
                showDriverDialog(user);
            }
        });

        tv_date.setText(getTimeDiff(parking.date.getTime()));

        builder.setView(view);
        builder.setCancelable(true);
        AlertDialog alertDialog = builder.create();

        if(parking.uid.equals(currentUser.getUid())){
            btn_unparking.setVisibility(View.VISIBLE);
            btn_unparking.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                     removeMyCar(parking);
                     alertDialog.dismiss();
                }
            });
        }else {
            btn_unparking.setVisibility(View.GONE);
        }

        iv_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });

        alertDialog.show();

    }


    public void setParkingDialog(LatLng latLng , String address){
        AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
        View view = LayoutInflater.from(MapActivity.this).inflate(R.layout.location_dialog, (RelativeLayout) findViewById(R.id.dialog));
        CircleImageView profile = view.findViewById(R.id.profile);
        TextView tv_name = view.findViewById(R.id.tv_name);
        TextView tv_title = view.findViewById(R.id.textView3);
        TextView tv_address = view.findViewById(R.id.tv_address);
        TextView tv_date = view.findViewById(R.id.tv_date);
        TextView tv_unparking_date = view.findViewById(R.id.tv_unparking_date);
        Button btn_unparking = view.findViewById(R.id.btn_mode);
        ImageView iv_close = view.findViewById(R.id.iv_close);
        tv_title.setText("주차하기");
        btn_unparking.setText("시간 설정");

        if(user.profile != null){
            Glide.with(MapActivity.this).load(Uri.parse(user.profile)).into(profile);
        }
        tv_name.setText(user.name);

        if(address != null){
            tv_address.setText(address);
        }else {
            tv_address.setText("주소 정보 없음");
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 MM월 dd일 hh:mm a");
        Calendar c = Calendar.getInstance();
        Date date = c.getTime();
        String getDate = sdf.format(date);
        tv_date.setText(getDate);
        time = null;

        builder.setView(view);
        builder.setCancelable(false);

        builder.setPositiveButton("추가", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Parking parking = new Parking(new CustomLatLng(latLng), address, date, time, user.name, user.uid, user.profile, user.phone, user.token);
                setMyCarLoc(parking);

            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });


        AlertDialog alertDialog = builder.create();

        btn_unparking.setVisibility(View.VISIBLE);
        btn_unparking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(MapActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int i, int i1) {
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(date);
                        if (cal.get(Calendar.HOUR_OF_DAY) > i) {
                            cal.add(Calendar.DATE, 1);
                        }
                        cal.set(Calendar.HOUR_OF_DAY, i);
                        cal.set(Calendar.MINUTE, i1);
                        time = cal.getTime();
                        tv_unparking_date.setText(getTimeDiff(time.getTime()));
                    }
                }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false);
                timePickerDialog.show();
            }
        });

        iv_close.setVisibility(View.INVISIBLE);



        alertDialog.show();
    }

    public String getTimeDiff(Long longtime){
        Long curret = System.currentTimeMillis();
        Long diff = Math.abs(curret - longtime);
        String af = curret - longtime >= 0 ? "전" : "후";

        Long sec = diff / 1000;
        Long min = sec/60;
        Long hour = min/60;
        Long day = hour/24;
        Long week = day/7;
        Long month = week/4;
        Long year = month/12;
        if(sec >= 60){
            if(min >= 60){
                if(hour >= 24){
                    if(day >= 30){
                        if(week >= 4){
                            if(month >= 12){
                                int y = year.intValue();
                                return  y+"년 " + af;
                            }else {
                                int mon = month.intValue();
                                return mon+"개월 " + af;
                            }
                        }else {
                            int w = week.intValue();
                            return w+"주 "+ af;
                        }
                    }else {
                        int d = day.intValue();
                        return d+"일 "+ af;
                    }
                }else {
                    int h = hour.intValue();
                    return h+"시간 "+ af;
                }
            }else {
                int m = min.intValue();
                return m+"분 "+ af;
            }
        }else {
            int s = sec.intValue();
            return s+"초 "+ af;
        }
    }

    public void makeReservation(ParkingPlace parkingPlace, int floor, String key, AlertDialog dialog){
        LoadingView loadingView = new LoadingView(MapActivity.this);
        loadingView.show("예약 중...");

        Parking parking = new Parking(parkingPlace.latLng, parkingPlace.address, new Date(System.currentTimeMillis()), null, user.name, user.uid, user.profile, user.phone, user.token);
        parking.setPlace(new SimplePlace(parkingPlace.name, parkingPlace.id, floor, key));

        db.collection("place").document(parkingPlace.id).update("parkings", FieldValue.arrayUnion(parking)).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    db.collection("users").document(currentUser.getUid()).update("parking_record", FieldValue.arrayUnion(parking));
                    db.collection("users").document(currentUser.getUid()).update("current_car", parking).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            init();
                        }
                    });
                    setMyCarMarker(parking);
                    removeCar(myCar);
                    myCar = parking;
                    Toast.makeText(MapActivity.this, "주차장 예약에 성공했습니다.", Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                }else {
                    Toast.makeText(MapActivity.this, "주차장 예약에 실패했습니다.", Toast.LENGTH_LONG).show();
                }
                loadingView.stop();

            }
        });
    }


    public void showDialog(ParkingPlace parkingPlace, int floor, String key, AlertDialog dialog){
        AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
        builder.setTitle("주차장 예약");
        builder.setMessage((floor+1) + "층 " + key + "자리 예약하시겠습니까?");
        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                makeReservation(parkingPlace, floor, key, dialog);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.create().show();
    }

    public void requestRemoveParking(User user, User my){
        Map<String,String> map = new HashMap<>();
        map.put("profile",user.getProfile());
        map.put("title", "주차 해제 요청");
        map.put("message", my.name + "님이 주차 해제를 요청했습니다.");
        FCM_Push fcm_push = new FCM_Push(MapActivity.this, user.token, map);
        fcm_push.send();
        Toast.makeText(MapActivity.this, user.name + "님에게 주차 해제를 요청했습니다.", Toast.LENGTH_LONG).show();
    }

    public void reportParking(){
        String reportApp = "kr.go.safepeople";
        if(getPackageList(reportApp)){
            Toast.makeText(MapActivity.this, "안전신문고 앱 > 불법주정차 신고 항목에서 불법 주정차 신고해주세요.", Toast.LENGTH_LONG).show();
            Intent intent = getPackageManager().getLaunchIntentForPackage(reportApp);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }else {
            Toast.makeText(MapActivity.this, "신고를 위해 안전신문고 앱 설치페이지로 이동합니다.", Toast.LENGTH_LONG).show();
            String url = "market://details?id=" + reportApp;
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(i);
        }
    }

    public boolean getPackageList(String pack) {
        boolean isExist = false;

        PackageManager pkgMgr = getPackageManager();
        List<ResolveInfo> mApps;
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        mApps = pkgMgr.queryIntentActivities(mainIntent, 0);

        try {
            for (int i = 0; i < mApps.size(); i++) {
                if(mApps.get(i).activityInfo.packageName.startsWith(pack)){
                    isExist = true;
                    break;
                }
            }
        }
        catch (Exception e) {
            isExist = false;
        }
        return isExist;
    }

    public void showDriverDialog(User user){
        AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this, android.R.style.Theme_Holo_Light_Dialog_NoActionBar);
        View view = LayoutInflater.from(MapActivity.this).inflate(R.layout.driver_dialog, (RelativeLayout) findViewById(R.id.dialog));
        Button call = view.findViewById(R.id.call);
        Button message = view.findViewById(R.id.message);
        Button request = view.findViewById(R.id.request);
        Button report = view.findViewById(R.id.report);
        ImageView close = view.findViewById(R.id.iv_close3);

        report.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                reportParking();
            }
        });

        request.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                 if(user.token != null){
                     requestRemoveParking(user, MapActivity.this.user);
                 }else {
                     Toast.makeText(MapActivity.this, "등록하지 않은 사용자입니다.", Toast.LENGTH_LONG).show();
                 }
            }
        });

        call.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                if(user.phone != null) {
                    Intent mIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("tel:" + user.phone));
                    startActivity(mIntent);
                }else {
                    Toast.makeText(MapActivity.this, "휴대폰 번호를 등록하지 않은 사용자입니다.", Toast.LENGTH_LONG).show();
                }

            }
        });
        message.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                if(user.phone != null) {
                    Intent intent = new Intent( Intent.ACTION_SENDTO );
                    intent.putExtra("sms_body", "차를 빼주시면 감사하겠습니다 ^^");
                    intent.setData( Uri.parse( "smsto:"+user.phone ) );
                    startActivity(intent);
                }else {
                    Toast.makeText(MapActivity.this, "휴대폰 번호를 등록하지 않은 사용자입니다.", Toast.LENGTH_LONG).show();
                }
            }
        });



        builder.setView(view);
        builder.setCancelable(true);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        close.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                alertDialog.dismiss();
            }
        });

    }

    public void showRemoveDialog(Parking parking, AlertDialog dialog){
        AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
        builder.setTitle("주차 해제");
        builder.setMessage((parking.place.floor+1) + "층 " + parking.place.key + "자리 주차 해제하시겠습니까?");
        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                removeMyCar(parking);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.create().show();
    }

    public void showPlaceDialog(Place place, ParkingPlace parkingPlace){
        key = null;
        AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
        View view = LayoutInflater.from(MapActivity.this).inflate(R.layout.place_dialog, (RelativeLayout) findViewById(R.id.dialog));
        TextView tv_place = view.findViewById(R.id.tv_place);
        TextView tv_address = view.findViewById(R.id.tv_address);
        TextView tv_space = view.findViewById(R.id.tv_space);
        TextView tv_chaos = view.findViewById(R.id.tv_chaos);
        TextView tv_num = view.findViewById(R.id.tv_num);
        Spinner spinner = view.findViewById(R.id.spinner);
        FloatingActionButton fb_call = view.findViewById(R.id.floatingActionButton);
        ImageView iv_close = view.findViewById(R.id.iv_close2);
        Button btn_book = view.findViewById(R.id.btn_booking);
        Button btn_setting = view.findViewById(R.id.btn_setting);
        TableLayout tableLayout = view.findViewById(R.id.table);

        int num = findNearByParkingCount(place);
        tv_num.setText(num == 0 ? "없음" : num + "대");

        fb_call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(place.getPhoneNumber() != null){
                    Intent mIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("tel:" + place.getPhoneNumber()));
                    startActivity(mIntent);
                }
            }
        });

        tv_place.setText(place.getName());
        tv_address.setText(place.getAddress());

        builder.setView(view);
        builder.setCancelable(true);
        AlertDialog alertDialog = builder.create();


        if(parkingPlace != null) {
            btn_book.setVisibility(View.VISIBLE);
            btn_setting.setVisibility(View.GONE);

            List<String> sp = new ArrayList<>();
            for(int i = 0 ; i < parkingPlace.floor; i++){
                sp.add((i+1) + "층");
            }
            int cnt = parkingPlace.row * parkingPlace.column * parkingPlace.floor - parkingPlace.parkings.size();
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(MapActivity.this, android.R.layout.simple_spinner_item, sp);
            spinner.setAdapter(adapter);

            boolean ist = false;

            if(parkingPlace.isHasParking(currentUser.getUid()) != null){
                btn_book.setText("주차 해제");
                ist = true;
                spinner.setSelection(user.current_car.place.floor);
            }else {
                btn_book.setText("주차 신청");
            }
            tv_space.setText(cnt + "자리");

            boolean finalIst = ist;
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    setTable(parkingPlace, tableLayout, i, finalIst);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });

        }else {
            btn_book.setVisibility(View.GONE);
            btn_setting.setVisibility(View.VISIBLE);
            tv_space.setText("주차장 미등록");
            btn_setting.setOnClickListener(new OnSingleClickListener() {
                @Override
                public void onSingleClick(View v) {
                    alertDialog.dismiss();
                    Intent intent = new Intent(MapActivity.this, SettingActivity.class);
                    ParkingPlace parkingPlace = new ParkingPlace(place);
                    intent.putExtra("place", parkingPlace);
                    startActivity(intent);

                }
            });
        }


        btn_book.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                Parking p = parkingPlace.isHasParking(currentUser.getUid());
                if(p != null){
                    showRemoveDialog(p, alertDialog);
                }else {
                    if(key != null){
                        showDialog(parkingPlace, spinner.getSelectedItemPosition(), key, alertDialog);
                    }else {
                        Toast.makeText(MapActivity.this, "자리를 선택해주세요.", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
        iv_close.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                alertDialog.dismiss();
            }
        });
        alertDialog.show();
    }

    public void setTable(ParkingPlace place, TableLayout layout, int floor, boolean ischecked){
        int r = place.row;
        int c = place.column;
        layout.removeAllViews();
        TableRow.LayoutParams rowLayout = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        TableRow[] row = new TableRow[r];
        TextView[][] text = new TextView[r][c];

        for (int tr = 0; tr < r; tr++) {                  // for문을 이용한 줄수 (TR)

            row[tr] = new TableRow(this);

            char rc = (char) (65 + tr);

            int margin = 10;

            for (int td = 0; td < c; td++) {              // for문을 이용한 칸수 (TD)

                text[tr][td] = new TextView(this);
                TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(150,150);
                if(td == 0){
                    layoutParams.setMargins(0, margin, margin, margin);
                }else if(td == c-1){
                    layoutParams.setMargins(margin, margin, 0, margin);
                }else {
                    layoutParams.setMargins(margin, margin, margin, margin);
                }
                text[tr][td].setLayoutParams(layoutParams);
                String key = rc +""+ (td+1);
                text[tr][td].setText(key);// 데이터삽입
                text[tr][td].setClickable(true);
                TypedValue outValue = new TypedValue();
                getTheme().resolveAttribute(
                        android.R.attr.selectableItemBackground, outValue, true);
                text[tr][td].setForeground(getDrawable(outValue.resourceId));
                text[tr][td].setTag(key);
                if(place.isHasParking(floor, key)){
                    Parking p = place.isHasParking(currentUser.getUid());
                    if(p != null && p.place.floor == floor && p.place.key.equals(key)){
                        text[tr][td].setBackgroundResource(R.drawable.stroke_select);
                        text[tr][td].setTextColor(Color.WHITE);     // 폰트컬러
                    }else {
                        text[tr][td].setBackgroundResource(R.drawable.stroke_inactive);
                        text[tr][td].setTextColor(Color.parseColor("#444444"));     // 폰트컬러
                    }

                }else {
                    text[tr][td].setBackgroundResource(R.drawable.stroke_activie);
                    text[tr][td].setTextColor(Color.WHITE);     // 폰트컬러
                }

                text[tr][td].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String key= (String) view.getTag();
                        if(ischecked){
                            Toast.makeText(MapActivity.this, "이미 예약된 주차장입니다.", Toast.LENGTH_LONG).show();
                        }else{
                            if(place.isHasParking(floor, key)){
                                Toast.makeText(MapActivity.this, "이미 예약된 자리입니다.", Toast.LENGTH_LONG).show();
                            }else {
                                cleanPlace(text, place, floor);
                                view.setBackgroundResource(R.drawable.stroke_select);
                                ((TextView)(view)).setTextColor(Color.WHITE);     // 폰트컬러
                                MapActivity.this.key = key;
                            }
                        }
                    }
                });

                text[tr][td].setTextSize(16);                     // 폰트사이즈

                text[tr][td].setGravity(Gravity.CENTER);    // 폰트정렬

                row[tr].addView(text[tr][td]);

            } // td for end

            layout.addView(row[tr], rowLayout);

        } // tr for end
    }

    public void cleanPlace(TextView[][] text , ParkingPlace place, int floor){
        int r = place.row;
        int c = place.column;

        for (int tr = 0; tr < r; tr++) {
            char rc = (char) (65 + tr);

            for (int td = 0; td < c; td++) {
                String key = rc +""+ (td+1);
                if(place.isHasParking(floor, key)){
                    text[tr][td].setBackgroundResource(R.drawable.stroke_inactive);
                    text[tr][td].setTextColor(Color.parseColor("#444444"));     // 폰트컬러
                }else {
                    text[tr][td].setBackgroundResource(R.drawable.stroke_activie);
                    text[tr][td].setTextColor(Color.WHITE);     // 폰트컬러
                }
            }
        }

    }

    public int findNearByParkingCount(Place place){
        if(mapData != null){
            int cnt = 0;
            List<Parking> parkings = mapData.parkings;
            for(Parking p : parkings){
                if(isNearBy(place.getLatLng(), p.latLng.gLatLng(), 50)) cnt++;
            }
            return cnt;
        }
        return 0;
    }
    public int findMyNearByParkingCount(LatLng myloc){
        if(mapData != null){
            int cnt = 0;
            for(ParkingPlace p : places){
                if(isNearBy(myloc, p.latLng.gLatLng(), 50)) cnt++;
            }
            return cnt;
        }
        return 0;
    }

    public boolean isNearBy(LatLng place, LatLng parking, int DEFAULT_TOLERANCE){
        return calculateLocationDifference(place, parking) <= DEFAULT_TOLERANCE;
    }

    private float calculateLocationDifference(LatLng lastLocation, LatLng firstLocation) {
        float[] dist = new float[1];
        Location.distanceBetween(lastLocation.latitude, lastLocation.longitude, firstLocation.latitude, firstLocation.longitude, dist);
        return dist[0];
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        locationPermissionGranted = false;
        if (requestCode
                == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationPermissionGranted = true;
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

    }

    private void updateLocationUI() {
        if (map == null) {
            return;
        }
        try {
            if (locationPermissionGranted) {
                map.setMyLocationEnabled(true);
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
            updateLocationUI();
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MapActivity.this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                AlertDialog.Builder localBuilder = new AlertDialog.Builder(this);
                localBuilder.setTitle("위치 권한 설정")
                        .setMessage("위치 권한 거절로 인해 자동 주차 감지 기능 및 내 위치 찾기 기능이 제한됩니다.")
                        .setPositiveButton("권한 설정하러 가기", new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt){
                                try {
                                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                            .setData(Uri.parse("package:" + getPackageName()));
                                    startActivity(intent);
                                } catch (ActivityNotFoundException e) {
                                    e.printStackTrace();
                                    Intent intent = new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
                                    startActivity(intent);
                                }
                            }})
                        .setNegativeButton("취소하기", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt) {

                            }})
                        .create()
                        .show();


            }else {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            }

        }
    }

    public void SearchLocation(String query) {
        searchResult = 0;
        searchComplete = 0;
        searchLocations.clear();

        AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();
        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                // Call either setLocationBias() OR setLocationRestriction().
                //.setLocationRestriction(bounds)
                .setQuery(query)
                .setSessionToken(token)
                .build();

        placesClient.findAutocompletePredictions(request).addOnSuccessListener((response) -> {
            List<AutocompletePrediction> list = response.getAutocompletePredictions();
            if(list.size() == 0){
                Toast.makeText(MapActivity.this, "No results found", Toast.LENGTH_LONG).show();
            }else {
                for (AutocompletePrediction prediction : list) {
                    SearchPlaceId(prediction.getPlaceId(), list.size());
                }
            }
        }).addOnFailureListener((exception) -> {
            Toast.makeText(MapActivity.this, "No results found", Toast.LENGTH_LONG).show();
        });

    }

    public void SearchPlaceId(String placeId, int size){

        final List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG, Place.Field.PHONE_NUMBER, Place.Field.PHOTO_METADATAS);

        final FetchPlaceRequest request = FetchPlaceRequest.newInstance(placeId, placeFields);

        placesClient.fetchPlace(request).addOnCompleteListener(new OnCompleteListener<FetchPlaceResponse>() {
            @Override
            public void onComplete(@NonNull Task<FetchPlaceResponse> task) {
                searchComplete++;
                if(task.isSuccessful()){
                    searchResult++;
                    FetchPlaceResponse response = task.getResult();
                    Place places = response.getPlace();
                    if(places.getLatLng() != null) {
                        searchLocations.add(places);
                    }
                }

                if(searchComplete == size){
                    tv_result.setText(searchResult + " search result");
                    setSearchMarker(searchLocations);
                    MoveCamera(searchLocations);
                    SearchListAdopter adopter = new SearchListAdopter(searchLocations, search_markers, MapActivity.this);
                    rc.setAdapter(adopter);
                    new Handler().postDelayed(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            layout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
                            //딜레이 후 시작할 코드 작성
                        }
                    }, 100);
                }
            }
        });
    }

    public void getPlace(String placeId){
        LoadingView loadingView = new LoadingView(MapActivity.this);
        loadingView.show("loading...");
        final List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG, Place.Field.PHONE_NUMBER, Place.Field.PHOTO_METADATAS);

        final FetchPlaceRequest request = FetchPlaceRequest.newInstance(placeId, placeFields);

        placesClient.fetchPlace(request).addOnCompleteListener(new OnCompleteListener<FetchPlaceResponse>() {
            @Override
            public void onComplete(@NonNull Task<FetchPlaceResponse> task) {
                loadingView.stop();
                if(task.isSuccessful()){
                    FetchPlaceResponse response = task.getResult();
                    Place places = response.getPlace();
                    if(places.getLatLng() != null) {
                        db.collection("place").document(places.getId()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                System.out.println(task.isSuccessful());
                                if(task.isSuccessful()){
                                    ParkingPlace parkingPlace = task.getResult().toObject(ParkingPlace.class);
                                    showPlaceDialog(places, parkingPlace);
                                }else {
                                    showPlaceDialog(places, null);
                                }
                            }
                        });
                    }
                }else {
                    Toast.makeText(MapActivity.this, "등록된 장소가 아닙니다.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }





    public void MoveCamera(List<Place> locations){
        double top = 0;
        double bottom = Integer.MAX_VALUE;
        double left = Integer.MAX_VALUE;
        double right = 0;
        for(Place location : locations){
            top = Math.max(top, location.getLatLng().longitude); //고위도
            bottom = Math.min(bottom, location.getLatLng().longitude); //저위도
            left = Math.min(left, location.getLatLng().latitude);
            right = Math.max(right, location.getLatLng().latitude);
        }
        LatLngBounds latLngBounds = new LatLngBounds(new LatLng(left, bottom), new LatLng(right, top));
        System.out.println(latLngBounds);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(latLngBounds, 100);
        map.moveCamera(cameraUpdate);

    }


    @Override
    public void onBackPressed() {
        if (layout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
            layout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        } else {
            if (pressedTime == 0) {
                Toast.makeText(MapActivity.this, "한번 더 누르면 종료됩니다." , Toast.LENGTH_SHORT).show();
                pressedTime = System.currentTimeMillis();
            } else {
                int seconds = (int) (System.currentTimeMillis() - pressedTime);

                if (seconds > 2000) {
                    Toast.makeText(MapActivity.this, "한번 더 누르면 종료됩니다." , Toast.LENGTH_SHORT).show();
                    pressedTime = 0;
                } else {
                    super.onBackPressed();

                    finish(); // app 종료 시키기
                }
            }
        }
    }

    @SuppressLint("PotentialBehaviorOverride")
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.map = googleMap;
        getLocationPermission();
        map.getUiSettings().setMyLocationButtonEnabled(false);
        init();
        if(locationPermissionGranted){
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<android.location.Location>() {
                @Override
                public void onSuccess(android.location.Location location) {
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(location.getLatitude(),
                                    location.getLongitude()), DEFAULT_ZOOM));
                }
            });
        }
        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng latLng) {
                if(layout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED){
                    layout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                }else {
                    try {
                        ReverseGeo(latLng);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {
                if(marker.getTag() instanceof Parking){
                    Parking parking = (Parking) marker.getTag();
                    if (parking != null) {
                        if(parking.place != null){
                            getPlace(parking.place.id);
                        }else {
                            showParkingInfo(parking);
                        }
                    }
                }else {
                    String id = (String) marker.getTag();
                    getPlace(id);
                }

                return false;
            }
        });

        map.setOnPoiClickListener(new GoogleMap.OnPoiClickListener() {
            @Override
            public void onPoiClick(@NonNull PointOfInterest poi) {
               getPlace(poi.placeId);
            }
        });
    }

}