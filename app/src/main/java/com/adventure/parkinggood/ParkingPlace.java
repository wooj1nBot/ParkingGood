package com.adventure.parkinggood;

import com.google.android.libraries.places.api.model.Place;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ParkingPlace implements Serializable {

    String name;
    String id;
    String phone;
    String address;
    CustomLatLng latLng;
    int row;
    int column;
    int floor;
    ArrayList<Parking> parkings;

    public ParkingPlace(Place place){
        name = place.getName();
        id = place.getId();
        phone = place.getPhoneNumber();
        address = place.getAddress();
        latLng = new CustomLatLng(place.getLatLng());
    }


    public ArrayList<Parking> getParkings() {
        return parkings;
    }


    public Parking isHasParking(String uid){
        if(parkings == null){
            return null;
        }
        for(Parking p : parkings){
            if(p.uid.equals(uid)) return p;
        }
        return null;
    }

    public boolean isHasParking(int floor, String key){
        if(parkings == null){
            return false;
        }
        for(Parking p : parkings){
           SimplePlace place = p.getPlace();
           if(place.floor == floor && place.key.equals(key)) return true;
        }
        return false;
    }

    public ParkingPlace(){}

    public void setAddress(String address) {
        this.address = address;
    }

    public void setFloor(int floor) {
        this.floor = floor;
    }

    public int getFloor() {
        return floor;
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
