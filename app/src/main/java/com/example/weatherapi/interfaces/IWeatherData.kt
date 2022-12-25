package com.example.weatherapi.interfaces

interface IWeatherData {
    var datetime: String
    var datetimeEpoch: Long?
    var temp: Double?
    var feelslike: Double?
    var dew: Double?
    var humidity: Double?
    var precip: Double?
    var precipprob: Double?
    var preciptype: List<String>?
    var snow: Double?
    var windgust: Double?
    var windspeed: Double?
    var winddir: Double?
    var cloudcover: Double?
    var visibility: Double?
    var moonphase: Double?
    var conditions: String?
    var icon: String
}