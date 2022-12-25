package com.example.weatherapi.utils

object Constants {
    const val BASE_URL =
        "https://weather.visualcrossing.com/VisualCrossingWebServices/rest/services/timeline/"

    const val TIMELINE_WEATHER_API_URL = "{location}/{date}"
    const val QUERY_KEY = "key"
    const val QUERY_INCLUDE = "include"
    const val QUERY_ELEMENTS = "elements"
    const val QUERY_UNITGROUP = "unitGroup"
    const val PATH_LOCATION = "location"
    const val PATH_DATE = "date"
    const val TODAY = "today"
    const val TOMORROW = "tomorrow"
    const val NEXT_SIXTEEN_DAYS_AND_CURRENT = "next17days"
    const val LAST_SIXTEEN_DAYS = "last16days"
    const val THIRTY_MINUTES = 1800000L
    const val US_UNIT_GROUP = "us"
    const val METRIC_UNIT_GROUP = "metric"
    const val DETAILED_WEATHER_INCLUDES = "fcst,current,days,events,obs,remote,statsfcst,hours"
    const val GENERAL_WEATHER_INCLUDES = "fcst,current,days,obs,remote,statsfcst"
    const val DETAILED_WEATHER_ELEMENTS =
        "datetime,datetimeEpoch,tempmax,tempmin,temp,feelslike,dew,humidity,precip,precipprob," +
                "preciptype,snow,windgust,windspeed,winddir,cloudcover,visibility,severerisk,sunrise,sunriseEpoch,sunset,sunsetEpoch," +
                "moonphase,conditions,description,icon"
    const val GENERAL_WEATHER_ELEMENTS = "temp,conditions,icon,datetime,preciptype"
}