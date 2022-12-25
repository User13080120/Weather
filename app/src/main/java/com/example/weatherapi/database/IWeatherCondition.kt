package com.example.weatherapi.database

import com.example.weatherapi.api.models.weatherconditions.CurrentCondition
import com.example.weatherapi.api.models.weatherconditions.Day

interface IWeatherCondition {
    var latitude: Double
    var longitude: Double
    var resolvedAddress: String
    var address: String
    var days: List<Day>
    var currentConditions: CurrentCondition?
    var description: String?
    var unitGroup: String?
    var isFavourite: Boolean
    var isCurrentLocation: Boolean
}