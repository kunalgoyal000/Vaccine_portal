package com.kunalapps.vaccineFinder

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kunalapps.vaccineFinder.models.State
import com.kunalapps.vaccineFinder.networking.RemoteApi
import com.kunalapps.vaccineFinder.networking.buildApiService


private const val KEY_PREFERENCES = "vaccine_preferences"
private const val STATES = "states"
private const val DISTRICTS = "districts"
private const val RECENT_SEARCHES = "recent_searches"
public const val PREF_IS_APP_LANGUAGE_SET = "PREF_IS_APP_LANGUAGE_SET"
public const val PREF_APP_SELECTED_LANGUAGE = "PREF_APP_SELECTED_LANGUAGE"
public const val PREF_APP_SELECTED_LANGUAGE_COUNTRY = "PREF_APP_SELECTED_LANGUAGE_COUNTRY"

class App : Application() {

    companion object {
        private lateinit var instance: App

        val preferences by lazy {
            instance.getSharedPreferences(KEY_PREFERENCES, Context.MODE_PRIVATE)
        }

        private val prefsEditor by lazy {
            preferences.edit()
        }

        //saving list in Shared Preference
        fun setRecentSearches(list: List<String>) {
            val gson = Gson()
            val json = gson.toJson(list)//converting list to Json
            prefsEditor.putString(RECENT_SEARCHES, json)
            prefsEditor.commit()
        }

        //getting the list from shared preference
        fun getRecentSearches(): List<String> {
            val gson = Gson()
            val json = preferences.getString(RECENT_SEARCHES, null)
            if (!json.isNullOrBlank()) {
                val type = object : TypeToken<List<String>>() {}.type//converting the json to list
                return gson.fromJson(json, type)//returning the list
            }
            return listOf()
        }

        //saving list in Shared Preference
        fun setStates(list: List<State>) {
            val gson = Gson()
            val json = gson.toJson(list)//converting list to Json
            prefsEditor.putString(STATES, json)
            prefsEditor.commit()
        }

        //getting the list from shared preference
        fun getStates(): List<State> {
            val gson = Gson()
            val json = preferences.getString(STATES, null)
            if (!json.isNullOrBlank()) {
                val type = object : TypeToken<List<State>>() {}.type//converting the json to list
                return gson.fromJson(json, type)//returning the list
            }
            return listOf()
        }

        fun isAppLanguageSet(): Boolean {
            return preferences.getBoolean(PREF_IS_APP_LANGUAGE_SET, false)
        }

        fun setAppLanguage(isAppLanguageSet: Boolean) {
            prefsEditor.putBoolean(PREF_IS_APP_LANGUAGE_SET, isAppLanguageSet)
            prefsEditor.commit()
        }

        fun getAppSelectedLanguage(): String? {
            return preferences.getString(PREF_APP_SELECTED_LANGUAGE, "en")
        }

        fun setAppSelectedLanguage(appSelectedLanguage: String?) {
            prefsEditor.putString(PREF_APP_SELECTED_LANGUAGE, appSelectedLanguage)
            prefsEditor.commit()
        }

        private val apiService by lazy { buildApiService() }

        val remoteApi by lazy { RemoteApi(instance, apiService) }
    }

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        instance = this
    }
}
