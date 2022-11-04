package com.adventure.parkinggood;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;


public class CustomLatLng implements Serializable {

    public double latitude;
    public double longitude;

    public CustomLatLng(){}

    public CustomLatLng(LatLng latLng){
        this.latitude = latLng.latitude;
        this.longitude = latLng.longitude;
    }

    public CustomLatLng(double latitude, double longitude) {
         this.latitude = latitude;
         this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    LatLng gLatLng(){
        return new LatLng(latitude, longitude);
    }

    void sLatLng(LatLng latLng){
        this.latitude = latLng.latitude;
        this.longitude = latLng.longitude;
    }
}
