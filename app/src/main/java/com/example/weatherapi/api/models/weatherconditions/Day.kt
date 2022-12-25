package com.example.weatherapi.api.models.weatherconditions

import com.example.weatherapi.interfaces.IWeatherData
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Day (
    override var datetime: String,
    override var datetimeEpoch: Long?,
    var tempmax: Double?,
    var tempmin: Double?,
    override var temp: Double?,
    override var feelslike: Double?,
    override var dew: Double?,
    override var humidity: Double?,
    override var precip: Double?,
    override var precipprob: Double?,
    override var preciptype: List<String>?,
    override var snow: Double?,
    override var windgust: Double?,
    override var windspeed: Double?,
    override var winddir: Double?,
    override var cloudcover: Double?,
    override var visibility: Double?,
    var severerisk: Double?,
    var sunrise: String?,
    var sunriseEpoch: Long?,
    var sunset: String?,
    var sunsetEpoch: Long?,
    override var moonphase: Double?,
    override var conditions: String?,
    var description: String?,
    override var icon: String,
    var hours: List<Hour>?
) : IWeatherData