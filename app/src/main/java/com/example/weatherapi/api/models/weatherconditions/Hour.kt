package com.example.weatherapi.api.models.weatherconditions

import com.example.weatherapi.interfaces.IWeatherData
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Hour (
    override var datetime: String,
    override var datetimeEpoch: Long?,
    override var temp: Double?,
    override var feelslike: Double?,
    override var humidity: Double?,
    override var dew: Double?,
    override var precip: Double?,
    override  var precipprob: Double?,
    override var snow: Double?,
    var snowdepth: Double?,
    override  var preciptype: List<String>?,
    override var windgust: Double?,
    override var windspeed: Double?,
    override var winddir: Double?,
    var pressure: Double?,
    override var visibility: Double?,
    override var cloudcover: Double?,
    var solarradiation: Double?,
    var solarenergy: String?,
    var uvindex: Double?,
    override var conditions: String?,
    override var icon: String,
    override var moonphase: Double?
) : IWeatherData
