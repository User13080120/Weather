package com.example.weatherapi.extensionfunctions

import android.app.Activity
import android.location.Geocoder
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar

object ExtensionFunctions {
    fun Geocoder.getAddress(lat: Double, lng: Double): String {
        try{
            val list = this.getFromLocation(lat, lng, 1)
            return list[0].locality
        }
        catch (e : Exception)
        {
            throw e
        }
    }

    fun Activity.showSnackBar(msg: String) {
        Snackbar.make(this.findViewById(android.R.id.content), msg, Snackbar.LENGTH_SHORT)
            .show()
    }

    fun Fragment.showSnackBar(msg: String) {
        Snackbar.make(
            this.requireActivity().findViewById(android.R.id.content),
            msg,
            Snackbar.LENGTH_SHORT
        )
            .show()
    }

    fun Activity.createAlertDialog(msg : String) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(msg)

        builder.setPositiveButton(android.R.string.ok) { _, _ ->
            this.finish()
        }

        builder.setNegativeButton(android.R.string.cancel) { _, _ ->
        }


        builder.show()
    }

}