package com.example.weatherapi.application.workers

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.weatherapi.api.models.weatherconditions.WeatherCondition
import com.example.weatherapi.application.App
import com.example.weatherapi.repository.Repository
import com.example.weatherapi.utils.Constants
import com.example.weatherapi.utils.Constants.LAST_SIXTEEN_DAYS
import com.example.weatherapi.utils.Constants.NEXT_SIXTEEN_DAYS_AND_CURRENT
import com.example.weatherapi.utils.Constants.TODAY
import com.example.weatherapi.utils.Constants.TOMORROW
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class UpdateDatabaseWorker(appContext: Context, workerParams: WorkerParameters): Worker(appContext, workerParams) {

    private val TAG = "UpdateDatabaseWorker"

    override fun doWork(): Result {
        CoroutineScope(Dispatchers.IO).launch {
            val repository = Repository()
            val includeDetailed = Constants.DETAILED_WEATHER_INCLUDES
            val elementsDetailed = Constants.DETAILED_WEATHER_ELEMENTS
            val includeGeneral = Constants.GENERAL_WEATHER_INCLUDES
            val elementsGeneral = Constants.GENERAL_WEATHER_ELEMENTS

            try {
                val favouriteWeatherConditions =
                    App.weatherConditionDao.getAllFavouriteWeatherConditions()
                if (favouriteWeatherConditions != null) {
                    for (favouriteWeatherCondition in favouriteWeatherConditions) {
                        val weatherResponseToday = updateToday(favouriteWeatherCondition, repository, includeDetailed, elementsDetailed)
                        updateTomorrow(favouriteWeatherCondition, weatherResponseToday, repository, includeDetailed, elementsDetailed)
                        updateNextDays(favouriteWeatherCondition, weatherResponseToday, repository, includeGeneral, elementsGeneral)
                        updateLastDays(favouriteWeatherCondition, weatherResponseToday, repository, includeGeneral, elementsGeneral)
                    }

                }

            } catch (e: Exception) {
                Log.d(TAG, "doWork failed with: $e")
            }
        }


        return Result.success()
    }

    private suspend fun updateToday(favouriteWeatherCondition : WeatherCondition,
                                    repository: Repository, includeDetailed : String,
                                    elementsDetailed : String) : WeatherCondition?
    {
        val weatherResponseToday =
            favouriteWeatherCondition.unitGroup?.let { unitGroup ->
                repository.getWeather(
                    favouriteWeatherCondition.address,
                    TODAY,
                    App.KEY,
                    includeDetailed,
                    elementsDetailed,
                    unitGroup
                )
            }
        if (weatherResponseToday != null) {
            weatherResponseToday.unitGroup = favouriteWeatherCondition.unitGroup
            weatherResponseToday.isFavourite = favouriteWeatherCondition.isFavourite
            weatherResponseToday.isCurrentLocation = favouriteWeatherCondition.isCurrentLocation
            App.weatherConditionDao.insertOrUpdateFavourite(weatherResponseToday)
            Log.d(TAG, "doWork: weatherResponseToday was assigned")

            return weatherResponseToday
        }
        else{
            Log.d(TAG, "doWork: weatherResponseToday was null")
        }
        return null
    }

    private suspend fun updateTomorrow(favouriteWeatherCondition : WeatherCondition,
                                       weatherResponseToday : WeatherCondition?,
                                       repository: Repository, includeDetailed : String,
                                       elementsDetailed : String)
    {
        val weatherResponseTomorrow =
            favouriteWeatherCondition.unitGroup?.let { unitGroup ->
                repository.getWeather(
                    favouriteWeatherCondition.address,
                    TOMORROW,
                    App.KEY,
                    includeDetailed,
                    elementsDetailed,
                    unitGroup
                )
            }

        if (weatherResponseTomorrow != null) {
            weatherResponseTomorrow.unitGroup = favouriteWeatherCondition.unitGroup
            weatherResponseTomorrow.isFavourite =
                favouriteWeatherCondition.isFavourite
            if (weatherResponseToday != null) {
                App.weatherConditionDao.updateTomorrow(
                    weatherResponseTomorrow,
                    weatherResponseToday.address
                )
                Log.d(TAG, "doWork: weatherResponseTomorrow was assigned")
            }
        } else {
            Log.d(TAG, "doWork: weatherResponseTomorrow was null")
        }
    }

    private suspend fun updateNextDays(favouriteWeatherCondition : WeatherCondition,
                                       weatherResponseToday : WeatherCondition?,
                                       repository: Repository, includeGeneral : String,
                                       elementsGeneral : String)
    {
        if (weatherResponseToday != null) {
            if (weatherResponseToday.nextDays != null) {
                val weatherResponseNextDays =
                    favouriteWeatherCondition.unitGroup?.let { unitGroup ->
                        repository.getWeather(
                            favouriteWeatherCondition.address,
                            NEXT_SIXTEEN_DAYS_AND_CURRENT,
                            App.KEY,
                            includeGeneral,
                            elementsGeneral,
                            unitGroup
                        )
                    }

                if (weatherResponseNextDays != null) {
                    weatherResponseNextDays.unitGroup =
                        favouriteWeatherCondition.unitGroup
                    weatherResponseNextDays.isFavourite =
                        favouriteWeatherCondition.isFavourite
                    App.weatherConditionDao.updateNextDays(
                        weatherResponseNextDays.days,
                        favouriteWeatherCondition.address
                    )
                    Log.d(TAG, "doWork: weatherResponseNextDays was assigned")
                } else {
                    Log.d(TAG, "doWork: weatherResponseNextDays was null")
                }
            } else {
                Log.d(TAG, "doWork: weatherResponseToday.nextDays was null")
            }
        } else {
            Log.d(
                TAG,
                "doWork: weatherResponseToday was null can't continue with nextDays"
            )
        }
    }

    private suspend fun updateLastDays(favouriteWeatherCondition : WeatherCondition,
                                       weatherResponseToday : WeatherCondition?,
                                       repository: Repository, includeGeneral : String,
                                       elementsGeneral : String)
    {
        if (weatherResponseToday != null) {
            if (weatherResponseToday.lastDays != null) {
                val weatherResponseLastDays =
                    favouriteWeatherCondition.unitGroup?.let { unitGroup ->
                        repository.getWeather(
                            favouriteWeatherCondition.address,
                            LAST_SIXTEEN_DAYS,
                            App.KEY,
                            includeGeneral,
                            elementsGeneral,
                            unitGroup
                        )
                    }

                if (weatherResponseLastDays != null) {
                    weatherResponseLastDays.unitGroup =
                        favouriteWeatherCondition.unitGroup
                    weatherResponseLastDays.isFavourite =
                        favouriteWeatherCondition.isFavourite
                    App.weatherConditionDao.updateLastDays(
                        weatherResponseLastDays.days,
                        favouriteWeatherCondition.address
                    )
                    Log.d(TAG, "doWork: weatherResponseLastDays was assigned")
                } else {
                    Log.d(TAG, "doWork: weatherResponseLastDays was null")
                }
            } else {
                Log.d(TAG, "doWork: weatherResponseToday.lastDays was null")
            }
        } else {
            Log.d(
                TAG,
                "doWork: weatherResponseToday was null can't continue with lastDays"
            )
        }
    }
}