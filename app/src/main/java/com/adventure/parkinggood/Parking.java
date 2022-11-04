package com.adventure.parkinggood;

import java.util.Date;

public class Parking {
     CustomLatLng latLng;
     String address;
     Date date;
     Date unparking_date;
     String name;
     String uid;
     String profile;

     public Parking(){}

     public Parking(CustomLatLng latLng, String address, Date date, Date unparking_date,String name, String uid, String profile){
         this.latLng = latLng;
         this.date = date;
         this.unparking_date = unparking_date;
         this.address = address;
         this.name = name;
         this.uid = uid;
         this.profile = profile;
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
