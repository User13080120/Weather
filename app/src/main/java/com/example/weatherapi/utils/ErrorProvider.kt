package com.example.weatherapi.utils

import android.os.Bundle
import android.view.View
import com.example.weatherapi.fragments.ErrorFragment
import com.example.weatherapi.fragments.ErrorFragment.Companion.BUNDLE_ERROR_MESSAGE
import com.example.weatherapi.interfaces.IReplaceFragment
import com.google.android.material.snackbar.Snackbar
import retrofit2.HttpException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class ErrorProvider(private val replaceFragmentListener: IReplaceFragment, private val view : View) {

    private val OFFLINE = "You are in offline mode!"
    private val UNABLE_TO_RESOLVE_HOST = "dnxg: UNAVAILABLE: Unable to resolve host geomobileservices-pa.googleapis.com"
    private val TIMED_OUT = "Timed out!"
    private val COULDNT_FIND_LOCATION = "Couldn't find location!"

    fun errorHandler(msg: String) {
        if(msg == OFFLINE || msg == UNABLE_TO_RESOLVE_HOST)
        {
            Snackbar.make(view, msg, Snackbar.LENGTH_SHORT).show()
        }
        else
        {
            val fragment = ErrorFragment()
            val bundle = Bundle()
            bundle.putString(BUNDLE_ERROR_MESSAGE, msg)
            fragment.arguments = bundle
            replaceFragmentListener.replaceFragment(fragment)
        }
    }

    fun errorHandler(exception: Exception) {
        if(errorResponse(exception) == OFFLINE || errorResponse(exception) == UNABLE_TO_RESOLVE_HOST)
        {
            Snackbar.make(view, errorResponse(exception), Snackbar.LENGTH_SHORT).show()
        }
        else {
            val fragment = ErrorFragment()
            val bundle = Bundle()
            bundle.putString(BUNDLE_ERROR_MESSAGE, errorResponse(exception))
            fragment.arguments = bundle
            replaceFragmentListener.replaceFragment(fragment)
        }
    }

    private fun errorResponse(exception: Exception) : String {
        val error: String
        when (exception) {
            is HttpException -> {
                error = if (exception.code() == 400) {
                    COULDNT_FIND_LOCATION
                } else {
                    exception.message.toString()
                }
            }
            is UnknownHostException -> {
                error = OFFLINE
            }
            is SocketTimeoutException -> {
                error = TIMED_OUT
            }
            else -> {
                error = exception.message.toString()
            }
        }
        return error
    }
}