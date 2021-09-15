package com.kunalapps.vaccinePortal.locale

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import com.kunalapps.vaccinePortal.App
import com.kunalapps.vaccinePortal.PREF_APP_SELECTED_LANGUAGE
import com.kunalapps.vaccinePortal.PREF_APP_SELECTED_LANGUAGE_COUNTRY
import java.util.*
import android.preference.PreferenceManager
import java.util.prefs.Preferences


object LocaleHelper {
    private const val SELECTED_LANGUAGE = "Locale.Helper.Selected.Language"
    fun onAttach(context: Context): Context {
        //  String lang = getPersistedData(context, Locale.getDefault().getLanguage());
        return setLocale(context, language)
    }

    fun onAttach(context: Context, defaultLanguage: String?): Context {
        //  String lang = getPersistedData(context, defaultLanguage);
        return setLocale(context, defaultLanguage)
    }

    val language: String
        get() = persistedData

    //    public static Context setLocale(Context context, String language) {
    //        persist(context, language);
    //
    //        Resources rs = context.getResources();
    //        Configuration config = rs.getConfiguration();
    //
    //        Locale locale = new Locale(language);
    //        Locale.setDefault(locale);
    //
    ////        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
    ////            config.setLocale(locale);
    ////        } else {
    ////            config.locale = locale;
    ////        }
    ////
    ////        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
    ////            context = context.createConfigurationContext(config);
    ////        } else {
    ////            context.getResources().updateConfiguration(config, rs.getDisplayMetrics());
    ////        }
    //
    //        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
    //            LocaleList localeList = new LocaleList(locale); // 2
    //            LocaleList.setDefault(localeList); // 3
    //            config.setLocales(localeList); // 4
    //        } else {
    //            config.locale = locale; // 5
    //        }
    //        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
    //            context = context.createConfigurationContext(config); // 6
    //        } else {
    //            rs.updateConfiguration(config, rs.getDisplayMetrics()); // 7
    //        }
    //
    //        return context;
    //    }
    fun setLocale(context: Context, language: String?): Context {
//        persist(context, language);
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            updateResources(context, language)
        } else updateResourcesLegacy(context, language)
    }

    @TargetApi(Build.VERSION_CODES.N)
    private fun updateResources(context: Context, language: String?): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val configuration = context.resources.configuration
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)
        return context.createConfigurationContext(configuration)
    }

    private fun updateResourcesLegacy(context: Context, language: String?): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val resources = context.resources
        val configuration = resources.configuration
        configuration.locale = locale
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration.setLayoutDirection(locale)
        }
        resources.updateConfiguration(configuration, resources.displayMetrics)
        return context
    }

    //        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
//        return preferences.getString(SELECTED_LANGUAGE, defaultLanguage);
    //    private static void persist(Context context, String language) {
    private val persistedData: String
        private get() {
            var language = ""
            var phoneLanguage = ""
            phoneLanguage = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Resources.getSystem().configuration.locales[0].language
            } else {
                Resources.getSystem().configuration.locale.language
            }
            if (App.isAppLanguageSet()) {
                if (App.getAppSelectedLanguage().equals("en")) {
                    language = "en"
                } else if (App.getAppSelectedLanguage().equals("hi")) {
                    language = "hi"
                }
            } else {
                language = if (phoneLanguage == "en") {
                    "en"
                } else if (phoneLanguage == "hi") {
                    "hi"
                } else {
                    "en"
                }
            }
            return language
        }
}