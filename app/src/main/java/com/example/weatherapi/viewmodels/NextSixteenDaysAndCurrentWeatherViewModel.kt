package com.example.weatherapi.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.weatherapi.api.models.weatherconditions.WeatherCondition
import com.example.weatherapi.application.App
import com.example.weatherapi.repository.Repository
import com.example.weatherapi.utils.Constants.GENERAL_WEATHER_ELEMENTS
import com.example.weatherapi.utils.Constants.GENERAL_WEATHER_INCLUDES
import com.example.weatherapi.utils.Constants.NEXT_SIXTEEN_DAYS_AND_CURRENT
import com.example.weatherapi.utils.Utility


class NextSixteenDaysAndCurrentWeatherViewModel(private val repository: Repository) : ViewModel() {

    private val TAG = "NextSixteenDaysWVM"

    val nextSixteenDaysAndCurrentWeatherCondition: MutableLiveData<WeatherCondition> =
        MutableLiveData()
    var nextSixteenDaysAndCurrentWeatherConditionError: MutableLiveData<Exception> =
        MutableLiveData()

    suspend fun getLocationWeather(
        location: String,
        unitGroup: String,
        isFavourite: Boolean = false,
    ) {

        val include = GENERAL_WEATHER_INCLUDES
        val elements = GENERAL_WEATHER_ELEMENTS
        val date = NEXT_SIXTEEN_DAYS_AND_CURRENT

        try {

            val getWeatherResponse = Utility.measureTimeMillis({ time ->
                Log.d(
                    TAG,
                    "Get weather took $time ms"
                )
            }) {
                repository.getWeather(
                    location,
                    date,
                    App.KEY,
                    include,
                    elements,
                    unitGroup
                )
            }
            getWeatherResponse.unitGroup = unitGroup
            getWeatherResponse.isFavourite = isFavourite

            nextSixteenDaysAndCurrentWeatherCondition.postValue(getWeatherResponse)

        } catch (e: Exception) {
            exceptionHandler(e)
        }

    }

    private fun exceptionHandler(e: Exception) {
        Log.d(TAG, "exception: $e")
        nextSixteenDaysAndCurrentWeatherConditionError.postValue(e)
    }
}
