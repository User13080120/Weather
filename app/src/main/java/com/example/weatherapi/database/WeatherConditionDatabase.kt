package com.example.weatherapi.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.weatherapi.api.models.weatherconditions.WeatherCondition
import com.example.weatherapi.database.converter.DataConverter


@Database(entities = [WeatherCondition::class], version = 9, exportSchema = false)
@TypeConverters(DataConverter::class)

abstract class WeatherConditionDatabase : RoomDatabase(){

    abstract fun weatherConditionDao(): WeatherConditionDao

    companion object {
        private var instance: WeatherConditionDatabase? = null

        @Synchronized
        fun getInstance(ctx: Context): WeatherConditionDatabase {
            if (instance == null)
                instance = Room.databaseBuilder(
                    ctx.applicationContext, WeatherConditionDatabase::class.java,
                    "weather_condition_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(roomCallback)
                    .build()

            return instance!!
        }

        private val roomCallback = object : Callback() {
        }
    }
}