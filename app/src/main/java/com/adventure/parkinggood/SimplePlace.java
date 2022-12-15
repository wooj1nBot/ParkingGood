package com.adventure.parkinggood;

import java.io.Serializable;

public class SimplePlace implements Serializable {
    String name;
    String id;
    String key;
    int floor;

    public SimplePlace(){}

    public SimplePlace(String name, String id, int floor, String key){
        this.name = name;
        this.id = id;
        this.floor = floor;
        this.key = key;
    }

    public int getFloor() {
        return floor;
    }

    public String getKey() {
        return key;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
