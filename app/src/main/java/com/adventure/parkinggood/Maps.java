package com.adventure.parkinggood;

import java.util.List;

public class Maps {
    List<Parking> parkings;

    public Maps(){}

    public Maps(List<Parking> parkings){
        this.parkings = parkings;
    }

    public List<Parking> getParkings() {
        return parkings;
    }
}
