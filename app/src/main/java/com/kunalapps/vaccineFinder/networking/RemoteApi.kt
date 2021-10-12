package com.kunalapps.vaccineFinder.networking

import android.content.Context
import com.kunalapps.vaccineFinder.R
import com.kunalapps.vaccineFinder.models.*

const val BASE_URL = "https://cdn-api.co-vin.in/api/"
const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.93 Safari/537.36"

class RemoteApi(private val context: Context, private val apiService: RemoteApiService) {

    suspend fun getStates(): Result<List<State>> = try {
        val data = apiService.fetchStatesList()
        if(data.states.isEmpty()){
            FailureString(context.getString(R.string.no_slots_available))
        }else {
            Success(data.states)
        }
    } catch (error: Throwable) {
        Failure(error)
    }

    suspend fun getDistricts(stateId: Int): Result<List<District>> = try {
        val data = apiService.fetchDistrictsList(stateId)
        if(data.districts.isEmpty()){
            FailureString(context.getString(R.string.no_slots_available))
        }else {
            Success(data.districts)
        }
    } catch (error: Throwable) {
        Failure(error)
    }

    suspend fun getSessionsByPin(pincode:String,date:String): Result<List<Session>> = try {

        val data = apiService.fetchSessionsByPin(pincode,date)
        if(data.sessions.isEmpty()){
            FailureString(context.getString(R.string.no_slots_available))
        }else {
            Success(data.sessions)
        }
    } catch (error: Throwable) {
        Failure(error)
        }

    suspend fun getSessionsByDistricts(districtId:Int,date:String): Result<List<Session>> = try {
        val data = apiService.fetchSessionsByDistrict(districtId,date)
        if(data.sessions.isEmpty()){
            FailureString(context.getString(R.string.no_slots_available))
        }else {
            Success(data.sessions)
        }
    } catch (error: Throwable) {
        Failure(error)
    }
}