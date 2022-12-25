package com.example.weatherapi.database.converter

import androidx.room.TypeConverter
import com.example.weatherapi.api.models.weatherconditions.CurrentCondition
import com.example.weatherapi.api.models.weatherconditions.Day
import com.example.weatherapi.api.models.weatherconditions.WeatherCondition
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.Serializable
import java.lang.reflect.Type


class DataConverter : Serializable {
    @TypeConverter
    fun fromDayList(optionValues: List<Day?>?): String? {
        if (optionValues == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken<List<Day?>?>() {}.type
        return gson.toJson(optionValues, type)
    }

    @TypeConverter
    fun toDayList(optionValuesString: String?): List<Day>? {
        if (optionValuesString == null) {
            return null
        }
        val gson = Gson()
        val type: Type = object : TypeToken<List<Day?>?>() {}.type
        return gson.fromJson<List<Day>>(optionValuesString, type)
    }

    @TypeConverter
    fun fromCurrentConditionToJSON(currentCondition: CurrentCondition): String {
        return Gson().toJson(currentCondition)
    }
    @TypeConverter
    fun fromJSONToCurrentCondition(json: String): CurrentCondition {
        return Gson().fromJson(json, CurrentCondition::class.java)
    }

    @TypeConverter
    fun fromDayToJSON(day: Day?): String {
        return Gson().toJson(day)
    }
    @TypeConverter
    fun fromJSONToDay(json: String): Day? {
        return Gson().fromJson(json, Day::class.java)
    }

    @TypeConverter
    fun fromWeatherConditionToJSON(weatherCondition: WeatherCondition?): String {
        return Gson().toJson(weatherCondition)
    }
    @TypeConverter
    fun fromJSONToWeatherCondition(json: String): WeatherCondition? {
        return Gson().fromJson(json, WeatherCondition::class.java)
    }
}