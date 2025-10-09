package com.example.pyatnaski

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import java.util.Locale

object LocaleHelper {

    private const val PREFS_NAME = "LanguagePrefs"
    private const val KEY_LANGUAGE = "selected_language"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun setLocale(context: Context, languageCode: String) {
        val prefs = getPrefs(context)
        prefs.edit().putString(KEY_LANGUAGE, languageCode).apply()
        updateResources(context, languageCode)
    }

    fun loadLocale(context: Context) {
        val prefs = getPrefs(context)
        val language = prefs.getString(KEY_LANGUAGE, "ru")
        if (language != null) {
            updateResources(context, language)
        }
    }

    private fun updateResources(context: Context, code: String) {
        val locale = Locale(code)
        Locale.setDefault(locale)

        val resources = context.resources
        val config = Configuration(resources.configuration)
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }
}
