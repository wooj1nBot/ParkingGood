package com.adventure.parkinggood;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
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
import com.google.maps.android.ui.IconGenerator;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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
    private Parking myCar;
    private User user;
    private  Date time;
    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    private FirebaseFirestore db;

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
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                new LatLng(location.getLatitude(),
                                        location.getLongitude()), DEFAULT_ZOOM));
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
            }
        });
    }

    public void getMapData(){
        db.collection("map").document("map").get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                 Maps maps = documentSnapshot.toObject(Maps.class);
                 if(maps != null) {
                     if(maps.parkings == null) return;
                     drawParkingMarker(maps.parkings);
                 }
            }
        });
    }

    public void removeCar(Parking parking){
        db.collection("map").document("map").update("parkings", FieldValue.arrayRemove(parking)).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                 getMapData();
            }
        });
    }

    public void removeMyCar(Parking parking){
        db.collection("users").document(currentUser.getUid()).update("current_car", null);
        db.collection("map").document("map").update("parkings", FieldValue.arrayRemove(parking)).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                getMapData();
            }
        });
        if(marker_car != null) marker_car.remove();
    }

    public void setMyCarLoc(Parking parking){
        db.collection("users").document(currentUser.getUid()).update("current_car", parking);
        db.collection("map").document("map").update("parkings", FieldValue.arrayUnion(parking));
        db.collection("users").document(currentUser.getUid()).update("parking_record", FieldValue.arrayUnion(parking));
        setMyCarMarker(parking);
        removeCar(myCar);
        myCar = parking;
        Toast.makeText(MapActivity.this, "주차 표시가 완료되었습니다.", Toast.LENGTH_LONG).show();
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
        for(int i = 0; i < parkings.size(); i++){
            Parking parking = (Parking) parkings.get(i);
            setParkingMarker(parking);
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
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 MM월 dd일 hh:mm a");
            tv_unparking_date.setText(sdf.format(parking.unparking_date));
        }else {
            tv_unparking_date.setText("주차 예정 시간 없음");
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 MM월 dd일 hh:mm a");
        String getDate = sdf.format(parking.date);
        tv_date.setText(getDate);

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
                Parking parking = new Parking(new CustomLatLng(latLng), address, date, time, user.name, user.uid, user.profile);
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
                        cal.set(Calendar.HOUR_OF_DAY, i);
                        cal.set(Calendar.MINUTE, i1);
                        time = cal.getTime();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 MM월 dd일 hh:mm a");
                        String getDate = sdf.format(time);
                        tv_unparking_date.setText(getDate);
                    }
                }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false);
                timePickerDialog.show();
            }
        });

        iv_close.setVisibility(View.INVISIBLE);



        alertDialog.show();
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
        updateLocationUI();
    }

    private void updateLocationUI() {
        if (map == null) {
            return;
        }
        LatLngBounds USBounds = new LatLngBounds(
                new LatLng(24, 125), // SW bounds
                new LatLng(48, 67)  // NE bounds
        );
        map.moveCamera(CameraUpdateFactory.newLatLngBounds(USBounds, 0));
        try {
            if (locationPermissionGranted) {
                map.setMyLocationEnabled(true);
                map.getUiSettings().setMyLocationButtonEnabled(false);
            } else {
                getLocationPermission();
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
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
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
                        showPlaceDialog(places);
                    }
                }else {

                }
            }
        });
    }

    public void showPlaceDialog(Place place){
        AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
        View view = LayoutInflater.from(MapActivity.this).inflate(R.layout.place_dialog, (RelativeLayout) findViewById(R.id.dialog));
        TextView tv_place = view.findViewById(R.id.tv_place);
        TextView tv_address = view.findViewById(R.id.tv_address);
        TextView tv_space = view.findViewById(R.id.tv_space);
        TextView tv_chaos = view.findViewById(R.id.tv_chaos);
        TextView tv_num = view.findViewById(R.id.tv_num);
        FloatingActionButton fb_call = view.findViewById(R.id.floatingActionButton);
        ImageView iv_close = view.findViewById(R.id.iv_close2);
        Button btn_book = view.findViewById(R.id.btn_booking);
        Button btn_setting = view.findViewById(R.id.btn_setting);
        TableLayout tableLayout = view.findViewById(R.id.table);

        tv_place.setText(place.getName());
        tv_address.setText(place.getAddress());

        btn_book.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                Intent intent = new Intent(MapActivity.this, SettingActivity.class);
                ParkingPlace parkingPlace = new ParkingPlace(place);
                intent.putExtra("place", parkingPlace);
                startActivity(intent);
            }
        });


        builder.setView(view);
        builder.setCancelable(true);
        AlertDialog alertDialog = builder.create();
        iv_close.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                alertDialog.dismiss();
            }
        });
        alertDialog.show();
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
            check.setTag(location);
            check.showInfoWindow();
            search_markers.add(check);
        }
    }
    @Override
    public void onBackPressed() {
        if (layout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
            layout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        } else {
            if (pressedTime == 0) {
                Toast.makeText(MapActivity.this, "Press once more to exit.", Toast.LENGTH_LONG).show();
                pressedTime = System.currentTimeMillis();
            } else {
                int seconds = (int) (System.currentTimeMillis() - pressedTime);

                if (seconds > 2000) {
                    Toast.makeText(MapActivity.this, "Press once more to exit.", Toast.LENGTH_LONG).show();
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
        updateLocationUI();
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
                        showParkingInfo(parking);
                    }
                }else {
                    Place location = (Place) marker.getTag();
                    showPlaceDialog(location);
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