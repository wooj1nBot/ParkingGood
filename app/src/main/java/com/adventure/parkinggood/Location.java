package com.adventure.parkinggood;


import java.io.Serializable;

public class Location implements Serializable {
    String name;
    String address;
    int type;
    CustomLatLng latLng;
    boolean isSearch;
    boolean isPassed;

    public Location(){}

    public Location(String name, String address, int type, CustomLatLng latLng, boolean isSearch, boolean isPassed){
        this.name = name;
        this.address = address;
        this.type = type;
        this.latLng = latLng;
        this.isSearch = isSearch;
        this.isPassed = isPassed;
    }

    public String getName() {
        return name;
    }

    public int getType() {
        return type;
    }

    public CustomLatLng getLatLng() {
        return latLng;
    }

    public String getAddress() {
        return address;
    }

    public boolean getIsPassed(){
        return isPassed;
    }

}
