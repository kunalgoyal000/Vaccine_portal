package com.example.covidvaccine.networking

import com.example.covidvaccine.models.response.GetDistrictsResponse
import com.example.covidvaccine.models.response.GetSessionsByPinResponse
import com.example.covidvaccine.models.response.GetStatesResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface RemoteApiService {

    //gives states list with state name and state id
    @GET("v2/admin/location/states")
    suspend fun fetchStatesList(): GetStatesResponse

    //gives districts list with district name and district id with state id to be provided
    @GET("v2/admin/location/districts/{state_id}")
    suspend fun fetchDistrictsList(@Path("state_id") stateId: Int): GetDistrictsResponse

    //gives sessions list with pincode and date to be provided
    @GET("v2/appointment/sessions/public/findByPin")
    suspend fun fetchSessionsByPin(@Query("pincode") pincode: String, @Query("date") date:String): GetSessionsByPinResponse

    //gives sessions list with district id and date to be provided
    @GET("v2/appointment/sessions/public/findByDistrict")
    suspend fun fetchSessionsByDistrict(@Query("district_id") districtId: Int, @Query("date") date:String): GetSessionsByPinResponse

    //gives centres list with pincode and date to be provided
    @GET("v2/appointment/sessions/public/calendarByPin")
    suspend fun fetchCalendarByPin(@Query("pincode") pincode: Int, @Query("date") date:String): GetSessionsByPinResponse

    //gives centres list with district and date to be provided
    @GET("v2/appointment/sessions/public/calendarByDistrict")
    suspend fun fetchCalendarByDistrict(@Query("district_id") districtId: Int, @Query("date") date:String): GetSessionsByPinResponse

}