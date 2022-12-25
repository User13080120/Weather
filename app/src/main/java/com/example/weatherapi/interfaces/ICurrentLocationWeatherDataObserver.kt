package com.example.weatherapi.interfaces

import com.example.weatherapi.api.models.weatherconditions.WeatherCondition

interface ICurrentLocationWeatherDataObserver {
    fun currentWeatherCondition(weatherCondition: WeatherCondition)
}