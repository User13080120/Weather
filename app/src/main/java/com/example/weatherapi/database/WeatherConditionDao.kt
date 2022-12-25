package com.example.weatherapi.database

import androidx.room.*
import com.example.weatherapi.api.models.weatherconditions.CurrentCondition
import com.example.weatherapi.api.models.weatherconditions.Day
import com.example.weatherapi.api.models.weatherconditions.WeatherCondition

@Dao
interface WeatherConditionDao {

    @Insert
    fun insert(weatherCondition: WeatherCondition)

    fun insertOrUpdateFavourite(weatherCondition: WeatherCondition) : Boolean {
        if (weatherCondition.isFavourite) {
            return if (!contains(weatherCondition.address)) {
                insert(weatherCondition)
                return true
            } else {
                update(weatherCondition.latitude, weatherCondition.longitude, weatherCondition.resolvedAddress,
                    weatherCondition.address, weatherCondition.days, weatherCondition.currentConditions, weatherCondition.description,
                    weatherCondition.unitGroup, weatherCondition.isFavourite, weatherCondition.isCurrentLocation) > 0
            }
        }
        return false
    }

    fun insertOrUpdate(weatherCondition: WeatherCondition) : Boolean {
        return if (!contains(weatherCondition.address)) {
            insert(weatherCondition)
            return true
        } else {
                update(weatherCondition.latitude, weatherCondition.longitude, weatherCondition.resolvedAddress,
                    weatherCondition.address, weatherCondition.days, weatherCondition.currentConditions, weatherCondition.description,
                    weatherCondition.unitGroup, weatherCondition.isFavourite, weatherCondition.isCurrentLocation) > 0
            }
    }


    @Query("update weather_condition_table SET latitude = :latitude, longitude = :longitude, resolvedAddress = :resolvedAddress, " +
            "address = :address, days = :days, currentConditions = :currentConditions, description = :description, unitGroup = :unitGroup," +
            " isFavourite = :isFavourite, isCurrentLocation = :isCurrentLocation  where address = :address")
    fun update(latitude: Double,
               longitude: Double,
               resolvedAddress: String,
               address: String,
               days: List<Day>,
               currentConditions: CurrentCondition?,
               description: String?,
               unitGroup: String?,
               isFavourite: Boolean,
               isCurrentLocation: Boolean) : Int

    @Update
    fun update(weatherCondition: WeatherCondition) : Int

    @Query("update weather_condition_table SET nextDays = :days where address = :address")
    fun updateNextDays(days : List<Day>, address: String)

    @Query("update weather_condition_table SET lastDays = :days where address = :address")
    fun updateLastDays(days : List<Day>, address: String)

    @Query("update weather_condition_table SET tomorrow = :weatherCondition where address = :address")
    fun updateTomorrow(weatherCondition: WeatherCondition?, address: String)

    @Delete
    fun delete(weatherCondition: WeatherCondition) : Int

    @Query("select * from weather_condition_table where address LIKE :address")
    fun contains(address: String) : Boolean

    @Query("select * from weather_condition_table where address LIKE :address")
    fun getWeatherCondition(address: String) : WeatherCondition?

    @Query("delete from weather_condition_table")
    fun deleteAllWeatherConditions()

    @Query("select * from weather_condition_table")
    fun getAllWeatherConditions(): List<WeatherCondition>?

    @Query("select * from weather_condition_table where isFavourite = 1")
    fun getAllFavouriteWeatherConditions(): List<WeatherCondition>?

    @Query("select * from weather_condition_table where isCurrentLocation = 1")
    fun getCurrentWeatherCondition(): WeatherCondition?
}