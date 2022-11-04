package com.adventure.parkinggood;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ReverseRetrofit {
    // @GET( EndPoint-자원위치(URI) )
    @GET("maps/api/geocode/json") //HTTP 메서드 및 URL
    //Requests 타입의 DTO 데이터와 API 키를 요청

    Call<ReverseGeoResponse> getPosts(@Query("latlng") String latlng, @Query("key") String key, @Query("language") String language);
}