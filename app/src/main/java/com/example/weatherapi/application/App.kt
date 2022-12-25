package com.example.weatherapi.application

import android.app.Application
import android.content.Context
import android.location.Geocoder
import android.util.Log
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.example.weatherapi.application.workers.UpdateDatabaseWorker
import com.example.weatherapi.database.WeatherConditionDao
import com.example.weatherapi.database.WeatherConditionDatabase
import com.example.weatherapi.interfaces.IUpdateDataListener
import com.example.weatherapi.manager.SharedPreferencesManager
import com.example.weatherapi.utils.Constants.THIRTY_MINUTES
import com.example.weatherapi.utils.Timer
import java.util.concurrent.TimeUnit

class App : Application() {

    private lateinit var timer: Timer


    private lateinit var weatherConditionDatabase: WeatherConditionDatabase

    companion object {
        const val KEY = "U9K4325CKZVCK8TX6M4JZL3QU"
        private const val TAG = "WeatherAPI"

        lateinit var appContext: Context
        lateinit var geocoder: Geocoder
        lateinit var sharedPreferencesManager: SharedPreferencesManager
        lateinit var weatherConditionDao: WeatherConditionDao
        private lateinit var updateDataListeners: MutableList<IUpdateDataListener>
        var isOnline = false
        var isGPSEnabled = false

        fun addToUpdateListeners(context: Context)
        {
            try {
                updateDataListeners.add(context as IUpdateDataListener)
            } catch (castException: ClassCastException) {
                Log.e(TAG, castException.message.toString())
            }
        }

        fun removeFromUpdateListeners(context: Context)
        {
            for(i in updateDataListeners.indices)
            {
                if(updateDataListeners[i] == context)
                {
                    updateDataListeners.removeAt(i)
                    break
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        geocoder = Geocoder(this)
        appContext = this
        weatherConditionDatabase = WeatherConditionDatabase.getInstance(this)
        weatherConditionDao = weatherConditionDatabase.weatherConditionDao()
        updateDataListeners = mutableListOf()
        sharedPreferencesManager = SharedPreferencesManager(applicationContext)

        timer = Timer ({ updateData() }, THIRTY_MINUTES)
        timer.startTimer()

        startUpdateDatabaseWorker(12L, TimeUnit.HOURS)
    }

    private fun startUpdateDatabaseWorker(repeatInterval : Long, timeUnit: TimeUnit)
    {
        val periodicWork = PeriodicWorkRequest.Builder(UpdateDatabaseWorker::class.java, repeatInterval, timeUnit)
            .build()
        val completed = WorkManager.getInstance(this).enqueue(periodicWork)
        Log.d(TAG, "periodicWork:  ${completed.result}")
    }

    override fun onTerminate() {
        super.onTerminate()
        timer.cancelTimer()
    }

    private fun updateData()
    {
        for(updateListener in updateDataListeners)
        {
            updateListener.updateData()
        }
    }


}