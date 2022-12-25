package com.example.weatherapi.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.weatherapi.application.App
import com.example.weatherapi.databinding.FragmentLocationSearchBinding
import com.example.weatherapi.fragments.LocationSearchFragment.LocationSearchFragmentConstants.BUNDLE_IS_FAVOURITE
import com.example.weatherapi.fragments.LocationSearchFragment.LocationSearchFragmentConstants.BUNDLE_LOCATION
import com.example.weatherapi.fragments.LocationSearchFragment.LocationSearchFragmentConstants.BUNDLE_UNITGROUP
import com.example.weatherapi.fragments.LocationSearchFragment.LocationSearchFragmentConstants.CHECK_WEATHER_FOR
import com.example.weatherapi.fragments.LocationSearchFragment.LocationSearchFragmentConstants.SELECTED_LOCATION
import com.example.weatherapi.interfaces.IHideLocationSearchButton
import com.example.weatherapi.interfaces.IHideSearchView
import com.example.weatherapi.interfaces.IHideTopNavigationLayout
import com.example.weatherapi.utils.Constants.METRIC_UNIT_GROUP
import com.example.weatherapi.utils.Constants.US_UNIT_GROUP


class LocationSearchFragment : BaseFragment(), IHideLocationSearchButton, IHideTopNavigationLayout,
    IHideSearchView {

    object LocationSearchFragmentConstants {
        const val SELECTED_LOCATION = "SelectedLocation"
        const val BUNDLE_LOCATION = "location"
        const val BUNDLE_UNITGROUP = "unitGroup"
        const val BUNDLE_IS_FAVOURITE = "isFavourite"
        const val CHECK_WEATHER_FOR = "check weather for"
    }
    private val TAG = "LocationSearchFragment"

    private lateinit var binding: FragmentLocationSearchBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLocationSearchBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewWeatherBtn.setOnClickListener {
            viewWeatherButtonClicked()
        }

        addTextChangedListener()
        configureToolbarForCreate()
    }

    override fun onDestroy() {
        super.onDestroy()
        configureToolbarForDestroy()
    }

    private fun addTextChangedListener()
    {
        binding.locationEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                binding.locationEt.error = null
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun viewWeatherButtonClicked() {
        accessActivityUI.showLoadingDialog()
        binding.viewWeatherBtn.isEnabled = false
        if(App.isOnline)
        {
            val location = binding.locationEt.text.toString()

            try{
                val locationName = tryToGetLocationName(location)
                if(locationName.isNotEmpty())
                {
                    val unitGroup = if(binding.degreeSw.isChecked) US_UNIT_GROUP else METRIC_UNIT_GROUP
                    val isFavourite = binding.favouriteSw.isChecked
                    val detailsFragment = LocationDetailsFragment(SELECTED_LOCATION)

                    val bundle = Bundle()
                    bundle.putString(BUNDLE_LOCATION, locationName)
                    bundle.putString(BUNDLE_UNITGROUP, unitGroup)
                    bundle.putBoolean(BUNDLE_IS_FAVOURITE, isFavourite)
                    detailsFragment.arguments = bundle


                    binding.viewWeatherBtn.isEnabled = true

                    replaceFragmentListener.replaceFragment(detailsFragment)
                }
            }
            catch (e : Exception){
                binding.locationEt.error = "Location was not found!"
                accessActivityUI.hideLoadingDialog()
                binding.viewWeatherBtn.isEnabled = true
            }
        }
        else
        {
            binding.locationEt.error = "You are offline!"
            accessActivityUI.hideLoadingDialog()
            binding.viewWeatherBtn.isEnabled = true
        }

    }

    private fun tryToGetLocationName(location : String) : String
    {
        val result = App.geocoder.getFromLocationName(location, 1)
        val locationName: String

        if(location.isNotEmpty())
        {
            if(result[0].locality != null)
            {
                locationName = result[0].locality
            }
            else if(result[0].countryName != null)
            {
                locationName = result[0].countryName
            }
            else if(result[0].getAddressLine(0) != null)
            {
                locationName = result[0].countryName
            }
            else
            {
                binding.locationEt.error = "Location was not found!"
                accessActivityUI.hideLoadingDialog()
                binding.viewWeatherBtn.isEnabled = true
                return ""
            }
        }
        else
        {
            binding.locationEt.error = "Location was not found!"
            accessActivityUI.hideLoadingDialog()
            binding.viewWeatherBtn.isEnabled = true
            return ""
        }

        return locationName
    }



    private fun configureToolbarForCreate() {
        accessActivityUI.getToolbar().title = CHECK_WEATHER_FOR
        accessActivityUI.getToolbar().menu.getItem(0).isVisible = false
        accessActivityUI.getToolbar().menu.getItem(1).isVisible = false
    }

    private fun configureToolbarForDestroy() {
        accessActivityUI.getToolbar().menu.getItem(0).isVisible = true
        accessActivityUI.getToolbar().menu.getItem(1).isVisible = true
    }

}