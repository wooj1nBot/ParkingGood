package com.adventure.parkinggood;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ReverseGeoResponse {


    @Expose
    @SerializedName("results")
    List<ReverseGeoResult> results;

    @Expose
    @SerializedName("status")
    String status;
}
