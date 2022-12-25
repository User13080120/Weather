package com.example.weatherapi.api.models.weatherconditions

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.weatherapi.database.IWeatherCondition
import com.squareup.moshi.JsonClass

@Entity(tableName = "weather_condition_table")
@JsonClass(generateAdapter = true)
data class WeatherCondition(
    override var latitude: Double,
    override var longitude: Double,
    override var resolvedAddress: String,
    @PrimaryKey
    override var address: String,
    override var days: List<Day>,
    var nextDays : List<Day>?,
    var lastDays : List<Day>?,
    var tomorrow : WeatherCondition?,
    override var currentConditions: CurrentCondition? = null,
    override var description: String?,
    override var unitGroup: String?,
    override var isFavourite: Boolean = false,
    override var isCurrentLocation: Boolean = false
) : IWeatherCondition
{
    override fun equals(other: Any?): Boolean {
        if (other is WeatherCondition) {

            return (other.latitude == latitude && other.longitude == longitude) ||
                    other.resolvedAddress == resolvedAddress ||
                    other.address == address
        }
        return super.equals(other)
    }

    override fun hashCode(): Int {
        var result = latitude.hashCode()
        result = 31 * result + longitude.hashCode()
        result = 31 * result + resolvedAddress.hashCode()
        result = 31 * result + address.hashCode()
        result = 31 * result + unitGroup.hashCode()
        result = 31 * result + days.hashCode()
        result = 31 * result + currentConditions.hashCode()
        return result
    }
}