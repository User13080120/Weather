package com.example.weatherapi.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.weatherapi.api.models.weatherconditions.WeatherCondition
import com.example.weatherapi.application.App
import com.example.weatherapi.repository.Repository
import com.example.weatherapi.utils.Constants.DETAILED_WEATHER_ELEMENTS
import com.example.weatherapi.utils.Constants.DETAILED_WEATHER_INCLUDES
import com.example.weatherapi.utils.Constants.TODAY
import com.example.weatherapi.utils.Constants.TOMORROW
import com.example.weatherapi.utils.Utility

class OneDayWeatherViewModel(private val repository: Repository) : ViewModel() {

    private val TAG = "OneDayWeatherVM"

    val oneDayWeatherCondition: MutableLiveData<WeatherCondition> = MutableLiveData()
    var oneDayWeatherConditionError: MutableLiveData<Exception> = MutableLiveData()

    suspend fun getLocationWeather(
        location: String,
        date: String,
        unitGroup: String,
        isFavourite: Boolean = false,
    ) {

        val include = DETAILED_WEATHER_INCLUDES
        val elements = DETAILED_WEATHER_ELEMENTS

        if (date != TODAY && date != TOMORROW) {
                throw java.lang.IllegalArgumentException("Date must be : today or tomorrow")
        }

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

            oneDayWeatherCondition.postValue(getWeatherResponse)

        } catch (e: Exception) {
            exceptionHandler(e)
        }

    }


    private fun exceptionHandler(e: Exception) {
        Log.d(TAG, "exception: $e")
        oneDayWeatherConditionError.postValue(e)
    }
}
