package com.example.weatherapi.activities

import android.content.*
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.TransitionDrawable
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.weatherapi.R
import com.example.weatherapi.activities.MainActivity.MainActivityConstants.NAVIGATION_BUTTON_ANIMATION_DURATION
import com.example.weatherapi.application.App
import com.example.weatherapi.databinding.ActivityMainBinding
import com.example.weatherapi.extensionfunctions.ExtensionFunctions.createAlertDialog
import com.example.weatherapi.extensionfunctions.ExtensionFunctions.getAddress
import com.example.weatherapi.extensionfunctions.ExtensionFunctions.showSnackBar
import com.example.weatherapi.fragments.LocationDetailsFragment
import com.example.weatherapi.fragments.LocationSearchFragment
import com.example.weatherapi.fragments.LocationsFragment
import com.example.weatherapi.interfaces.*
import com.example.weatherapi.repository.Repository
import com.example.weatherapi.utils.*
import com.example.weatherapi.utils.Constants.METRIC_UNIT_GROUP
import com.example.weatherapi.utils.Utility.setBackground
import com.example.weatherapi.viewmodels.CurrentDayWeatherViewModel
import com.example.weatherapi.viewmodels.CurrentDayWeatherViewModelFactory
import com.example.weatherapi.viewmodels.MainActivityViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainActivity : BaseGPSActivity(), IReplaceFragment, IAccessActivityUI, IUpdateDataListener, IGpsStatusChange {

    object MainActivityConstants {
        const val NAVIGATION_BUTTON_ANIMATION_DURATION = 300
    }
    private val TAG = "MainActivity"
    private lateinit var binding: ActivityMainBinding

    private lateinit var locationsButtonTransition: TransitionDrawable
    private lateinit var currentLocationButtonTransition: TransitionDrawable
    private lateinit var currentDayWeatherViewModel: CurrentDayWeatherViewModel
    private lateinit var loadingDialog : AlertDialog
    private lateinit var networkConnection : NetworkConnection
    private lateinit var gpsReceiver : GpsReceiver
    private val mainActivityViewModel by viewModels<MainActivityViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeLoadingDialog()
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root

        binding.locationsTbtn.setOnClickListener {
            locationsButtonClicked()
        }

        binding.currentLocationTbtn.setOnClickListener {
            currentLocationButtonClicked()
        }

        binding.locationSearchFab.setOnClickListener {
            locationSearchButtonCLicked()
        }

        App.addToUpdateListeners(this)
        initializeButtonTransitions()
        initializeCurrentDayWeatherViewModelWithFactory()
        observeCurrentDayWeatherViewModel()
        initializeNetworkObserver()
        if(savedInstanceState == null) {
            mainActivityViewModel.background = AppCompatResources.getDrawable(this, R.drawable.default_bg)
            locationsButtonClicked()
            searchForCurrentLocation()
        }
        binding.mainContainerIv.background = mainActivityViewModel.background

        registerGpsReceiver()
        setContentView(view)
    }

    override fun onStart() {
        super.onStart()
        setSupportActionBar(binding.toolbar)
    }

    private fun registerGpsReceiver() {
        val mIntentFilter = IntentFilter()
        mIntentFilter.addAction(LocationManager.PROVIDERS_CHANGED_ACTION)
        gpsReceiver = GpsReceiver(this)
        registerReceiver(gpsReceiver, mIntentFilter)
    }

    override fun onStop() {
        super.onStop()
        loadingDialog.dismiss()
    }

    override fun onDestroy() {
        super.onDestroy()
        mainActivityViewModel.wasUpdated = false

        App.removeFromUpdateListeners(this)

        try{
            unregisterReceiver(gpsReceiver)
        }
        catch (e : Exception){
            Log.i(TAG, "Gps receiver is already unregistered!")
        }

        currentDayWeatherViewModel.currentDayWeatherCondition.value = null
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.menu_toolbar_favorite -> showSnackBar("Not available!")
            R.id.menu_toolbar_reload -> showSnackBar("Not available!")
            else -> {
                showSnackBar("Not available!")
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (supportFragmentManager.findFragmentByTag("LOCATIONSFRAGMENT") !is LocationsFragment) {
            locationsButtonClicked()
        } else {
            createAlertDialog("Press OK to close the application")
        }
    }
    override fun replaceFragment(fragment: Fragment, tag: String) {
        supportFragmentManager.beginTransaction()
            .replace(binding.fragmentContainerFcw.id, fragment, tag).commit()
    }

    override fun updateData() {
        updateCurrentData()
        for (fragment in supportFragmentManager.fragments) {
            if (fragment is IFragmentUpdateData) {
                fragment.updateData()
            }
        }
    }

    override fun gpsStatusChanged(isActivated: Boolean) {
        if(isActivated && !mainActivityViewModel.wasUpdated)
        {
            mainActivityViewModel.wasUpdated = true
            searchForCurrentLocation()
        }
        Log.d(TAG, "gpsStatusChanged: $isActivated")
    }
    override fun currentLocationChanged(location: Location) {
        binding.currentLocationTbtn.isEnabled = true
        try{
            mainActivityViewModel.currentAddress =
                App.geocoder.getAddress(location.latitude, location.longitude)
        }
        catch(e : Exception){
            ErrorProvider(this, this.findViewById(android.R.id.content)).errorHandler(e)
        }
        finally {
            hideLoadingDialog()
        }

        updateCurrentData()
    }

    override fun currentLocationErrorChanged(locationError: String) {
        hideLoadingDialog()
    }

    override fun hideLocationSearchButton() {
        binding.locationSearchFab.hide()
    }

    override fun showLocationSearchButton() {
        binding.locationSearchFab.show()
    }

    override fun hideTopNavigationLayout() {
        binding.topNavigationLl.visibility = GONE
    }

    override fun showTopNavigationLayout() {
        binding.topNavigationLl.visibility = VISIBLE
    }

    override fun hideSearchView() {
        binding.currentLocationSv.visibility = GONE
    }

    override fun showSearchView() {
        binding.currentLocationSv.visibility = VISIBLE
    }

    override fun hideToolbar() {
        binding.toolbar.visibility = GONE
    }

    override fun showToolbar() {
        binding.toolbar.visibility = VISIBLE
    }

    override fun getToolbar(): androidx.appcompat.widget.Toolbar {
        return binding.toolbar
    }

    override fun getSearchView(): SearchView {
        return binding.currentLocationSv
    }

    override fun showLoadingDialog() {
        loadingDialog.show()
    }

    override fun hideLoadingDialog() {
        loadingDialog.hide()
    }

    private fun initializeNetworkObserver()
    {
        networkConnection = NetworkConnection(applicationContext)
        networkConnection.observe(this@MainActivity) { isConnected ->
            if (isConnected) {
                binding.connectionModeTv.visibility = GONE
                if(App.isGPSEnabled)
                {
                    searchForCurrentLocation()
                    mainActivityViewModel.wasUpdated = true
                }
            } else {
                binding.connectionModeTv.visibility = VISIBLE
            }
            App.isOnline = isConnected

            lifecycleScope.launch(Dispatchers.Default){
                for (fragment in supportFragmentManager.fragments) {
                    if (fragment is IInternetStatusChange) {
                        fragment.internetStatusChanged(isConnected)
                    }
                }
            }
        }
    }

    private fun initializeCurrentDayWeatherViewModelWithFactory() {
        val currentDayWeatherViewModelFactory = CurrentDayWeatherViewModelFactory(Repository())
        currentDayWeatherViewModel = ViewModelProvider(
            this,
            currentDayWeatherViewModelFactory
        )[CurrentDayWeatherViewModel::class.java]
    }

    private fun initializeLoadingDialog()
    {
        val builder = AlertDialog.Builder(this)
        builder.setCancelable(false)
        builder.setView(R.layout.layout_loading_dialog)
        loadingDialog = builder.create()
        loadingDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        mainActivityViewModel.loadingDialog = loadingDialog
    }

    private fun initializeButtonTransitions() {
        locationsButtonTransition = binding.locationsTbtn.background as TransitionDrawable
        currentLocationButtonTransition =
            binding.currentLocationTbtn.background as TransitionDrawable
    }


    private fun locationsButtonClicked() {
        binding.locationsTbtn.isChecked = true
        binding.currentLocationTbtn.isChecked = false

        locationsButtonTransition.startTransition(NAVIGATION_BUTTON_ANIMATION_DURATION)
        currentLocationButtonTransition.resetTransition()

        binding.currentLocationTbtn.isEnabled = true
        binding.locationsTbtn.isEnabled = false

        replaceFragment(LocationsFragment(), "LOCATIONSFRAGMENT")
    }

    private fun currentLocationButtonClicked() {
        loadingDialog.show()

        binding.locationsTbtn.isChecked = false
        binding.currentLocationTbtn.isChecked = true
        locationsButtonTransition.resetTransition()
        currentLocationButtonTransition.startTransition(NAVIGATION_BUTTON_ANIMATION_DURATION)

        binding.currentLocationTbtn.isEnabled = false
        binding.locationsTbtn.isEnabled = true

        replaceFragment(LocationDetailsFragment())
    }

    private fun locationSearchButtonCLicked() {
        replaceFragment(LocationSearchFragment())
    }

    private fun updateCurrentData()
    {
        lifecycleScope.launch(Dispatchers.IO)
        {
            var unitGroup = METRIC_UNIT_GROUP
            var isFavourite = false

            val weatherCondition = App.weatherConditionDao.getWeatherCondition(mainActivityViewModel.currentAddress)
            if (weatherCondition != null) {
                unitGroup = weatherCondition.unitGroup.toString()
                isFavourite = weatherCondition.isFavourite
            }

            getCurrentDayWeather(mainActivityViewModel.currentAddress, Constants.TODAY, unitGroup, isFavourite)
        }
    }

    private fun getCurrentDayWeather(
        currentPosition : String,
        date: String,
        unitGroup: String,
        isFavourite: Boolean = false,
    ) {
        lifecycleScope.launch(Dispatchers.IO) {
            currentDayWeatherViewModel.getLocationWeather(
                currentPosition,
                date,
                unitGroup,
                isFavourite,
            )
        }
    }

    private fun observeCurrentDayWeatherViewModel() {
        currentDayWeatherViewModel.currentDayWeatherCondition.observe(this) { weatherCondition ->
            lifecycleScope.launch(Dispatchers.IO){
                if(weatherCondition != null) {
                    App.weatherConditionDao.insertOrUpdate(weatherCondition)

                    val icon: String = weatherCondition.currentConditions?.icon.toString()

                    val newBackground = Utility.getBackground(weatherCondition)

                    withContext(Dispatchers.Main) {
                        for (fragment in supportFragmentManager.fragments) {
                            if (fragment is ICurrentLocationWeatherDataObserver) {
                                fragment.currentWeatherCondition(weatherCondition)
                            }
                        }

                        mainActivityViewModel.imageViewDescription = binding.mainContainerIv.contentDescription.toString()

                        if(setBackground(icon, binding.mainContainerIv, newBackground))
                        {
                            mainActivityViewModel.background = newBackground
                        }

                        binding.mainContainerIv.contentDescription = icon


                        Log.d(TAG, "Changed background with location: $weatherCondition")

                        hideLoadingDialog()
                        mainActivityViewModel.wasUpdated = false
                    }
                }
            }
        }
        currentDayWeatherViewModel.currentDayWeatherConditionError.observe(this) { e ->
            ErrorProvider(this, this.findViewById(android.R.id.content)).errorHandler(e)
            mainActivityViewModel.wasUpdated = false
        }
    }
}