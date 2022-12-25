package com.example.weatherapi.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import com.example.weatherapi.interfaces.IGpsStatusChange


class GpsReceiver(private val gpsStatusChangeListener : IGpsStatusChange) : BroadcastReceiver() {
    private var firstConnect = true

    override fun onReceive(context: Context, intent: Intent) {

        if (intent.action == LocationManager.PROVIDERS_CHANGED_ACTION) {
            val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

            if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                if(firstConnect){
                    gpsStatusChangeListener.gpsStatusChanged(true)
                    firstConnect=false
                }
            }else{
                if(!firstConnect){
                    gpsStatusChangeListener.gpsStatusChanged(false)
                    firstConnect=true
                }
            }

        }
    }



}