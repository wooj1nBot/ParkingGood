package com.adventure.parkinggood;

import com.google.android.libraries.places.api.model.Place;

import java.io.Serializable;

public class ParkingPlace implements Serializable {

    String name;
    String id;
    String phone;
    String address;
    CustomLatLng latLng;
    int row;
    int column;

    public ParkingPlace(Place place){
        name = place.getName();
        id = place.getId();
        phone = place.getPhoneNumber();
        address = place.getAddress();
        latLng = new CustomLatLng(place.getLatLng());
    }


    public ParkingPlace(){}

    public void setAddress(String address) {
        this.address = address;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getColumn() {
        return column;
    }

    public int getRow() {
        return row;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setLatLng(CustomLatLng latLng) {
        this.latLng = latLng;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public CustomLatLng getLatLng() {
        return latLng;
    }

    public String getId() {
        return id;
    }

    public String getPhone() {
        return phone;
    }

}
