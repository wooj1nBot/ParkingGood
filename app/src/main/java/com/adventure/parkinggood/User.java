package com.adventure.parkinggood;

import java.io.Serializable;
import java.util.List;

public class User implements Serializable {
    String name;
    String email;
    String uid;
    String profile;
    String token;
    Parking current_car;
    List<Parking> parking_record;
    List<String> friends;

    public User() {}

    public User(String name, String email, String uid, String profile, String token){
        this.name = name;
        this.email = email;
        this.uid = uid;
        this.profile = profile;
        this.token = token;
    }


    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getProfile() {
        return profile;
    }

    public String getUid() {
        return uid;
    }

    public String getToken() {
        return token;
    }

    public Parking getCurrent_car() {
        return current_car;
    }

    public List<String> getFriends() {
        return friends;
    }

    public List<Parking> getParking_record() {
        return parking_record;
    }
}
