package com.example.covidvaccine

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import com.example.covidvaccine.models.State
import com.example.covidvaccine.networking.RemoteApi
import com.example.covidvaccine.networking.buildApiService
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


private const val KEY_PREFERENCES = "vaccine_preferences"
private const val STATES = "states"
private const val DISTRICTS = "districts"
private const val RECENT_SEARCHES = "recent_searches"

class App : Application() {

    companion object {
        private lateinit var instance: App

        private val preferences by lazy {
            instance.getSharedPreferences(KEY_PREFERENCES, Context.MODE_PRIVATE)
        }

        private val prefsEditor by lazy {
           preferences.edit()
        }

        //saving list in Shared Preference
        fun setRecentSearches(list:List<String>){
            val gson = Gson()
            val json = gson.toJson(list)//converting list to Json
            prefsEditor.putString(RECENT_SEARCHES,json)
            prefsEditor.commit()
        }

        //getting the list from shared preference
        fun getRecentSearches():List<String>{
            val gson = Gson()
            val json = preferences.getString(RECENT_SEARCHES,null)
            if(!json.isNullOrBlank()) {
                val type = object : TypeToken<List<String>>() {}.type//converting the json to list
                return gson.fromJson(json, type)//returning the list
            }
            return listOf()
        }

        //saving list in Shared Preference
        fun setStates(list:List<State>){
            val gson = Gson()
            val json = gson.toJson(list)//converting list to Json
            prefsEditor.putString(STATES,json)
            prefsEditor.commit()
        }

        //getting the list from shared preference
        fun getStates():List<State>{
            val gson = Gson()
            val json = preferences.getString(STATES,null)
            if(!json.isNullOrBlank()) {
                val type = object : TypeToken<List<State>>() {}.type//converting the json to list
                return gson.fromJson(json, type)//returning the list
            }
            return listOf()
        }

        private val apiService by lazy { buildApiService() }

        val remoteApi by lazy { RemoteApi(instance,apiService) }
    }

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        instance = this
    }
}
