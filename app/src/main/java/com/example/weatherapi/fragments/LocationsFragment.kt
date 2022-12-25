package com.example.weatherapi.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherapi.adapter.LocationAdapter
import com.example.weatherapi.api.models.weatherconditions.WeatherCondition
import com.example.weatherapi.application.App
import com.example.weatherapi.databinding.FragmentLocationsBinding
import com.example.weatherapi.fragments.LocationsFragment.LocationsFragmentConstants.SELECTED_LOCATION
import com.example.weatherapi.interfaces.IFragmentUpdateData
import com.example.weatherapi.interfaces.IHideToolbar
import com.example.weatherapi.interfaces.IUpdateDataListener
import com.example.weatherapi.manager.SharedPreferencesManager.Companion.LOCATIONS_FRAGMENT_UPDATE_TIMESTAMP
import com.example.weatherapi.utils.Constants.THIRTY_MINUTES
import com.example.weatherapi.utils.Constants.TODAY
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LocationsFragment : BaseFragment(), IHideToolbar, IUpdateDataListener, IFragmentUpdateData, LocationAdapter.ItemClickListener {

    object LocationsFragmentConstants {
        const val SELECTED_LOCATION = "SelectedLocation"
    }
    private val TAG = "LocationsFragment"
    private lateinit var binding: FragmentLocationsBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var locationAdapter: LocationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        observeOneDayWeatherViewModel()
        if(savedInstanceState == null)
        {
            updateData()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLocationsBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeRecyclerViewWithWeatherDataSavedInTheDatabase()
        searchViewHandler()
    }

    override fun onDetach() {
        super.onDetach()
        App.removeFromUpdateListeners(requireContext())
    }

    override fun onLocationAdapterItemClick(position: Int) {
        accessActivityUI.showLoadingDialog()

        Log.d(TAG, locationAdapter.weatherConditionFilterList[position].toString())

        val detailsFragment = LocationDetailsFragment(SELECTED_LOCATION)
        val bundle = Bundle()
        val location = locationAdapter.weatherConditionFilterList[position].address

        bundle.putString("location", location)
        bundle.putString("unitGroup", locationAdapter.weatherConditionFilterList[position].unitGroup )
        bundle.putBoolean("isFavourite", locationAdapter.weatherConditionFilterList[position].isFavourite)
        detailsFragment.arguments = bundle

        replaceFragmentListener.replaceFragment(detailsFragment)
    }

    private fun initializeRecyclerViewWithWeatherDataSavedInTheDatabase(){
        lifecycleScope.launch(Dispatchers.IO) {
            val weatherConditions =
                App.weatherConditionDao.getAllFavouriteWeatherConditions()
                    ?.toMutableList()
            withContext(Dispatchers.Main)
            {
                if(weatherConditions != null)
                {
                    recycleViewAndAdapterHandler(weatherConditions)
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun recycleViewAndAdapterHandler(locations: MutableList<WeatherCondition>) {
        recyclerView = binding.recyclerViewLocations

        locationAdapter =
            LocationAdapter(this, locations)

        val layoutManager: RecyclerView.LayoutManager =
            LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
        recyclerView.layoutManager = layoutManager
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.adapter = locationAdapter

        recyclerView.addItemDecoration(
            DividerItemDecoration(
                requireActivity(),
                DividerItemDecoration.VERTICAL
            )
        )

        locationAdapter.notifyDataSetChanged()
    }


    private fun searchViewHandler() {
        accessActivityUI.getSearchView()
            .setOnQueryTextListener(object : SearchView.OnQueryTextListener,
                androidx.appcompat.widget.SearchView.OnQueryTextListener {

                override fun onQueryTextChange(newText: String): Boolean {
                    if (::locationAdapter.isInitialized) {
                        locationAdapter.filter.filter(newText)
                    }
                    return false
                }

                override fun onQueryTextSubmit(query: String): Boolean {
                    return false
                }
            })
    }

    override fun updateData() {
        lifecycleScope.launch(Dispatchers.IO){
            val timestamp = App.sharedPreferencesManager[LOCATIONS_FRAGMENT_UPDATE_TIMESTAMP]
            if(timestamp  != null)
            {
                val diff = System.currentTimeMillis() - timestamp.toLong()
                if(diff > THIRTY_MINUTES)
                {
                    val weatherConditions =
                        App.weatherConditionDao.getAllFavouriteWeatherConditions()
                            ?.toMutableList()
                    if(weatherConditions != null)
                    {
                        updateFavouriteLocations(weatherConditions)
                    }
                    App.sharedPreferencesManager[LOCATIONS_FRAGMENT_UPDATE_TIMESTAMP] =
                        System.currentTimeMillis().toString()
                }
            }
            else
            {
                val weatherConditions =
                    App.weatherConditionDao.getAllFavouriteWeatherConditions()
                        ?.toMutableList()
                if(weatherConditions != null)
                {
                    updateFavouriteLocations(weatherConditions)
                }

                App.sharedPreferencesManager[LOCATIONS_FRAGMENT_UPDATE_TIMESTAMP] =
                    System.currentTimeMillis().toString()
            }
        }
    }

    private fun updateFavouriteLocations(favouriteLocations: MutableList<WeatherCondition>) {
        lifecycleScope.launch(Dispatchers.IO) {
            for (favouriteLocation in favouriteLocations) {
                oneDayWeatherViewModel.getLocationWeather(
                    favouriteLocation.address,
                    TODAY,
                    favouriteLocation.unitGroup.toString(),
                    favouriteLocation.isFavourite
                )
            }
        }
    }

    private fun observeOneDayWeatherViewModel() {
        oneDayWeatherViewModel.oneDayWeatherCondition.observe(this) { weatherCondition ->
            lifecycleScope.launch(Dispatchers.IO)
            {
                App.weatherConditionDao.update(weatherCondition.latitude, weatherCondition.longitude, weatherCondition.resolvedAddress,
                                                                        weatherCondition.address, weatherCondition.days, weatherCondition.currentConditions, weatherCondition.description,
                                                                        weatherCondition.unitGroup, weatherCondition.isFavourite, weatherCondition.isCurrentLocation) > 0
                Log.d(TAG, "Updated favourite location: $weatherCondition")
            }
        }

        oneDayWeatherViewModel.oneDayWeatherConditionError.observe(this) { e ->
            errorProvider.errorHandler(e)
        }
    }
}