package com.example.weatherapi.api

import com.example.weatherapi.api.models.weatherconditions.WeatherCondition
import com.example.weatherapi.utils.Constants
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface WeatherApi {

    @GET(Constants.TIMELINE_WEATHER_API_URL)
    suspend fun getCurrentWeather(
        @Path(Constants.PATH_LOCATION) location: String,
        @Path(Constants.PATH_DATE) date: String,
        @Query(Constants.QUERY_KEY) key: String,
        @Query(Constants.QUERY_INCLUDE) include: String,
        @Query(Constants.QUERY_ELEMENTS) elements: String,
        @Query(Constants.QUERY_UNITGROUP) unitGroup: String,
    ): WeatherCondition

}