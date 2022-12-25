package com.example.weatherapi.manager

import android.content.Context

class SharedPreferencesManager(context: Context) {

    val TAG = "SharedPreferencesMan"

    private val sharedPreferences =
        context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)

    private val editor = sharedPreferences.edit()

    companion object {
        const val SHARED_PREFERENCES_NAME = "WeatherSharedPreferences"
        const val LOCATIONS_FRAGMENT_UPDATE_TIMESTAMP = "LocationsFragmentUpdateTimestamp"
    }

    operator fun set(key: String?, value: String?) {
        editor.putString(key, value)
        editor.commit()
    }

    operator fun get(key: String?) : String? {
        return sharedPreferences.getString(key, null)
    }

    fun clear() {
        editor.clear().commit()
        sharedPreferences.edit().clear().apply()
    }
}