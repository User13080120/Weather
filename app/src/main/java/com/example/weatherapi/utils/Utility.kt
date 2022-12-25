package com.example.weatherapi.utils

import android.animation.ValueAnimator
import android.graphics.drawable.Drawable
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import androidx.appcompat.content.res.AppCompatResources
import com.example.weatherapi.R
import com.example.weatherapi.api.models.weatherconditions.WeatherCondition
import com.example.weatherapi.application.App


object Utility {
    inline fun <T> measureTimeMillis(
        loggingFunction: (Long) -> Unit,
        function: () -> T
    ): T {

        val startTime = System.currentTimeMillis()
        val result: T = function.invoke()
        loggingFunction.invoke(System.currentTimeMillis() - startTime)

        return result
    }
    fun getImageIcon(icon: String, sunriseEpoch : Long, sunsetEpoch : Long, currentEpoch : Long) : Drawable?
    {
        if(currentEpoch in sunriseEpoch until sunsetEpoch)
        {
            return when (icon){
                "snow" -> AppCompatResources.getDrawable( App.appContext, R.drawable.snow)
                "snow-showers-day","snow-showers-night" -> AppCompatResources.getDrawable( App.appContext, R.drawable.snow_showers_day)
                "thunder-rain" -> AppCompatResources.getDrawable( App.appContext, R.drawable.thunder_rain)
                "rain" -> AppCompatResources.getDrawable(App.appContext, R.drawable.rain)
                "showers-day","showers-night" -> AppCompatResources.getDrawable(App.appContext, R.drawable.showers_day)
                "thunder-showers-day","thunder-showers-night" -> AppCompatResources.getDrawable(App.appContext, R.drawable.thunder_showers_day)
                "fog" -> AppCompatResources.getDrawable(App.appContext, R.drawable.fog)
                "cloudy" -> AppCompatResources.getDrawable(App.appContext, R.drawable.cloudy)
                "partly-cloudy-day","partly-cloudy-night" -> AppCompatResources.getDrawable(App.appContext, R.drawable.partly_cloudy_day)
                "wind" -> AppCompatResources.getDrawable(App.appContext, R.drawable.wind)
                "clear-day", "night","clear-night" -> AppCompatResources.getDrawable(App.appContext, R.drawable.clear_day)
                else -> AppCompatResources.getDrawable(App.appContext, R.drawable.circle)
            }
        }
        else
        {
            return when (icon){
                "snow-showers-night","snow-showers-day","snow" -> AppCompatResources.getDrawable( App.appContext, R.drawable.snow_showers_night)
                "thunder-rain" -> AppCompatResources.getDrawable( App.appContext, R.drawable.thunder_rain)
                "wind" -> AppCompatResources.getDrawable(App.appContext, R.drawable.wind)
                "clear-day" -> AppCompatResources.getDrawable(App.appContext, R.drawable.clear_day)
                "showers-night","showers-day","rain"  -> AppCompatResources.getDrawable(App.appContext, R.drawable.showers_night)
                "night" ->  AppCompatResources.getDrawable(App.appContext, R.drawable.clear_night)
                "clear-night" ->  AppCompatResources.getDrawable(App.appContext, R.drawable.clear_night)
                "partly-cloudy-night","fog","cloudy","partly-cloudy-day" -> AppCompatResources.getDrawable(App.appContext, R.drawable.partly_cloudy_night)
                "thunder-showers-night","thunder-showers-day" -> AppCompatResources.getDrawable(App.appContext, R.drawable.thunder_showers_night)
                else -> AppCompatResources.getDrawable(App.appContext, R.drawable.circle)
            }
        }
    }

    fun getImageIcon(icon: String) : Drawable?
    {
        return when (icon){
            "snow" -> AppCompatResources.getDrawable( App.appContext, R.drawable.snow)
            "snow-showers-day" -> AppCompatResources.getDrawable( App.appContext, R.drawable.snow_showers_day)
            "snow-showers-night" -> AppCompatResources.getDrawable( App.appContext, R.drawable.snow_showers_night)
            "thunder-rain" -> AppCompatResources.getDrawable( App.appContext, R.drawable.thunder_rain)
            "rain" -> AppCompatResources.getDrawable(App.appContext, R.drawable.rain)
            "showers-day" -> AppCompatResources.getDrawable(App.appContext, R.drawable.showers_day)
            "thunder-showers-day" -> AppCompatResources.getDrawable(App.appContext, R.drawable.thunder_showers_day)
            "fog" -> AppCompatResources.getDrawable(App.appContext, R.drawable.fog)
            "cloudy" -> AppCompatResources.getDrawable(App.appContext, R.drawable.cloudy)
            "partly-cloudy-day" -> AppCompatResources.getDrawable(App.appContext, R.drawable.partly_cloudy_day)
            "wind" -> AppCompatResources.getDrawable(App.appContext, R.drawable.wind)
            "clear-day" -> AppCompatResources.getDrawable(App.appContext, R.drawable.clear_day)
            "showers-night" -> AppCompatResources.getDrawable(App.appContext, R.drawable.showers_night)
            "night" ->  AppCompatResources.getDrawable(App.appContext, R.drawable.clear_night)
            "clear-night" ->  AppCompatResources.getDrawable(App.appContext, R.drawable.clear_night)
            "partly-cloudy-night" -> AppCompatResources.getDrawable(App.appContext, R.drawable.partly_cloudy_night)
            "thunder-showers-night" -> AppCompatResources.getDrawable(App.appContext, R.drawable.thunder_showers_night)
            else -> AppCompatResources.getDrawable(App.appContext, R.drawable.circle)
        }
    }

    fun setBackground(icon : String, imageView: ImageView, newBackground : Drawable?) : Boolean{
        if(imageView.contentDescription != icon) {
            val fadeOut: Animation = AnimationUtils.loadAnimation(App.appContext, R.anim.fade_out)
            imageView.startAnimation(fadeOut)

            fadeOut.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {}
                override fun onAnimationEnd(animation: Animation) {
                    imageView.background = newBackground

                    val fadeIn: Animation =
                        AnimationUtils.loadAnimation(App.appContext, R.anim.fade_in)
                    imageView.startAnimation(fadeIn)
                }

                override fun onAnimationRepeat(animation: Animation) {}
            })
            return true
        }
        else{
            imageView.background = newBackground
        }

        imageView.contentDescription = icon

        return false
    }

    fun getBackground(weatherCondition : WeatherCondition) : Drawable?
    {
        try {
            val icon : String = weatherCondition.currentConditions?.icon.toString()
            if(weatherCondition.currentConditions?.datetimeEpoch!! in weatherCondition.currentConditions!!.sunriseEpoch!! until weatherCondition.currentConditions!!.sunsetEpoch!!)
            {
                return when (icon){
                    "snow","snow-showers-day","snow-showers-night" -> AppCompatResources.getDrawable(App.appContext, R.drawable.snow_bg)
                    "rain","thunder-rain","thunder-showers-day","showers-day","thunder-showers-night","showers-night" -> AppCompatResources.getDrawable(App.appContext, R.drawable.rain_bg)
                    "fog" -> AppCompatResources.getDrawable(App.appContext, R.drawable.fog_bg)
                    "wind","cloudy","partly-cloudy-day","clear-day","night","clear-night","partly-cloudy-night" -> AppCompatResources.getDrawable(App.appContext, R.drawable.cloud_bg)
                    else -> AppCompatResources.getDrawable(App.appContext, R.drawable.cloud_bg)
                }
            }else
            {    return when (icon){
                    "snow","snow-showers-day","snow-showers-night","rain","thunder-rain",
                    "thunder-showers-day","showers-day","fog","wind","cloudy","partly-cloudy-day",
                    "clear-day","night","showers-night","clear-night","partly-cloudy-night",
                    "thunder-showers-night" -> AppCompatResources.getDrawable(App.appContext, R.drawable.night_bg)
                    else -> AppCompatResources.getDrawable(App.appContext, R.drawable.night_bg)
            }

            }
        }
        catch (e : Exception)
        {
            throw java.lang.Exception("getBackground failed in Utility: $e")
        }
    }


    fun expand(v: View, duration: Int, targetHeight: Int) {
        val prevHeight = v.height
        v.visibility = View.VISIBLE
        val valueAnimator = ValueAnimator.ofInt(prevHeight, targetHeight)
        valueAnimator.addUpdateListener { animation ->
            v.layoutParams.height = animation.animatedValue as Int
            v.requestLayout()
        }
        valueAnimator.interpolator = DecelerateInterpolator()
        valueAnimator.duration = duration.toLong()
        valueAnimator.start()
    }

    fun collapse(v: View, duration: Int, targetHeight: Int) {
        val prevHeight = v.height
        val valueAnimator = ValueAnimator.ofInt(prevHeight, targetHeight)
        valueAnimator.interpolator = DecelerateInterpolator()
        valueAnimator.addUpdateListener { animation ->
            v.layoutParams.height = animation.animatedValue as Int
            v.requestLayout()
        }
        valueAnimator.interpolator = DecelerateInterpolator()
        valueAnimator.duration = duration.toLong()
        valueAnimator.start()
    }

}