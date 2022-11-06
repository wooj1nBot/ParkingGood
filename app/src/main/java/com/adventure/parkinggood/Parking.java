package com.adventure.parkinggood;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Parking implements Serializable {
     CustomLatLng latLng;
     String address;
     Date date;
     Date unparking_date;
     String name;
     String uid;
     String profile;
     String phone;
     String token;
     SimplePlace place;

     public Parking(){}

     public Parking(CustomLatLng latLng, String address, Date date, Date unparking_date, String name, String uid, String profile, String phone, String token){
         this.latLng = latLng;
         this.date = date;
         this.unparking_date = unparking_date;
         this.address = address;
         this.name = name;
         this.uid = uid;
         this.profile = profile;
         this.phone = phone;
         this.token = token;
     }

    public String getPhone() {
        return phone;
    }

    public String getToken() {
        return token;
    }

    public void setPlace(SimplePlace place) {
        this.place = place;
    }

    public SimplePlace getPlace() {
        return place;
    }

    public CustomLatLng getLatLng() {
        return latLng;
    }

    public String getAddress() {
        return address;
    }

    public Date getDate() {
        return date;
    }

    public String getName() {
        return name;
    }

    public String getUid() {
        return uid;
    }

    public String getProfile() {
        return profile;
    }

    public Date getUnparking_date() {
        return unparking_date;
    }
}
