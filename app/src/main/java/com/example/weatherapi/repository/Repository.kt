package com.example.weatherapi.repository

import com.example.weatherapi.api.RetrofitInstance
import com.example.weatherapi.api.models.weatherconditions.WeatherCondition

class Repository {

    suspend fun getWeather(
        location: String,
        date: String,
        key: String,
        include: String = "",
        elements: String = "",
        unitGroup: String = "metric"
    ): WeatherCondition {
        return RetrofitInstance.api.getCurrentWeather(
            location,
            date,
            key,
            include,
            elements,
            unitGroup
        )
    }
}