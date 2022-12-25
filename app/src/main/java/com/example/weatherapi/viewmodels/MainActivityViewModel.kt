package com.example.weatherapi.viewmodels
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModel

class MainActivityViewModel : ViewModel() {
    lateinit var loadingDialog : AlertDialog
    var imageViewDescription : String = ""
    var background : Drawable? = null
    var currentAddress : String = ""
    var wasUpdated = false
}