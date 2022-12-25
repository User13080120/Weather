package com.example.weatherapi.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherapi.R
import com.example.weatherapi.adapter.DayAdapter
import com.example.weatherapi.adapter.HourAdapter
import com.example.weatherapi.api.models.weatherconditions.Day
import com.example.weatherapi.api.models.weatherconditions.WeatherCondition
import com.example.weatherapi.application.App
import com.example.weatherapi.databinding.FragmentDetailsBinding
import com.example.weatherapi.extensionfunctions.ExtensionFunctions.showSnackBar
import com.example.weatherapi.fragments.LocationDetailsFragment.LocationDetailsFragmentConstants.CURRENT_LOCATION
import com.example.weatherapi.fragments.LocationDetailsFragment.LocationDetailsFragmentConstants.SELECTED_LOCATION
import com.example.weatherapi.fragments.LocationSearchFragment.LocationSearchFragmentConstants.BUNDLE_IS_FAVOURITE
import com.example.weatherapi.fragments.LocationSearchFragment.LocationSearchFragmentConstants.BUNDLE_LOCATION
import com.example.weatherapi.fragments.LocationSearchFragment.LocationSearchFragmentConstants.BUNDLE_UNITGROUP
import com.example.weatherapi.interfaces.*
import com.example.weatherapi.repository.Repository
import com.example.weatherapi.utils.Constants.METRIC_UNIT_GROUP
import com.example.weatherapi.utils.Constants.TODAY
import com.example.weatherapi.utils.Constants.TOMORROW
import com.example.weatherapi.utils.Utility
import com.example.weatherapi.utils.Utility.collapse
import com.example.weatherapi.utils.Utility.expand
import com.example.weatherapi.viewmodels.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class LocationDetailsFragment(private val type: String = CURRENT_LOCATION) : BaseFragment(),
    IHideTopNavigationLayout, IHideSearchView, IFragmentUpdateData, ICurrentLocationWeatherDataObserver,
    HourAdapter.ItemClickListener, DayAdapter.ItemClickListener {

    object LocationDetailsFragmentConstants {
        const val CURRENT_LOCATION = "CurrentLocation"
        const val SELECTED_LOCATION = "SelectedLocation"
    }
    private val TAG = "LocationDetailsFragment"

    init {
        if (type == CURRENT_LOCATION) {
            super.hideTopNavigationLayout = false
        }
        if (type == SELECTED_LOCATION) {
            super.hideTopNavigationLayout = true
        }
    }
    private lateinit var binding: FragmentDetailsBinding

    private lateinit var hourAdapter: HourAdapter
    private lateinit var dayAdapter: DayAdapter

    private lateinit var dayRecyclerView: RecyclerView
    private lateinit var hourRecyclerView: RecyclerView
    private lateinit var nextSixteenDaysAndCurrentWeatherViewModel: NextSixteenDaysAndCurrentWeatherViewModel
    private lateinit var lastSixteenDaysWeatherViewModel: LastSixteenDaysWeatherViewModel

    private lateinit var locationName: String
    private var unitGroup: String = METRIC_UNIT_GROUP
    private var isFavourite: Boolean = false
    private lateinit var todayWeatherDetails : WeatherCondition
    private var detailsIsUp : Boolean = true


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDetailsBinding.inflate(layoutInflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeNextSixteenDaysAndCurrentWeatherViewModelWithFactory()
        initializeLastSixteenDaysWeatherViewModelWithFactory()

        binding.lastSixteenDaysBtn.setOnClickListener {
            onLastSixteenDaysBtnClicked()
        }
        binding.nextSixteenDaysBtn.setOnClickListener {
            onNextSixteenDaysBtnClicked()
        }
        binding.tomorrowBtn.setOnClickListener {
            tomorrowBtnClicked()
        }
        binding.detailsBtn.setOnClickListener {
            detailsBtnClicked()
        }

        observeOneDayWeatherViewModel()
        observeLastSixteenDaysWeatherViewModel()
        observeNextSixteenDaysAndCurrentWeatherViewModel()

        if(savedInstanceState == null)
        {
            initializeToolbarMenu()
        }

        loadData()
    }

    override fun onDestroy() {
        super.onDestroy()

        oneDayWeatherViewModel.oneDayWeatherCondition.value = null
        lastSixteenDaysWeatherViewModel.lastSixteenWeatherCondition.value = null
        nextSixteenDaysAndCurrentWeatherViewModel.nextSixteenDaysAndCurrentWeatherCondition.value = null
    }

    override fun onDayItemClick(position: Int) {
        Log.d(TAG, "Day: " + dayAdapter.getItemData(position).toString())
    }

    override fun onHourItemClick(position: Int) {

        setDetailsToUI(hourAdapter.getItemData(position), unitGroup, hourAdapter.sunriseEpoch,hourAdapter.sunsetEpoch)
        Log.d(TAG, "Hour: " + hourAdapter.getItemData(position).toString())
    }

    override fun updateData() {
        if(this::locationName.isInitialized)
        {
            if (binding.tomorrowBtn.text.equals(getString(R.string.show_tomorrow))) {
                getOneDayWeather(TODAY, unitGroup, isFavourite)
            } else {
                getOneDayWeather(TOMORROW, unitGroup, isFavourite)
            }
        }
    }

    override fun internetStatusChanged(isActivated: Boolean) {
        if(isActivated)
        {
            updateData()
        }
    }

    private fun initializeToolbarMenu()
    {
        accessActivityUI.getToolbar().menu[0].setIcon(R.drawable.ic_heart_white)
        accessActivityUI.getToolbar().menu[0].isEnabled = false
        accessActivityUI.getToolbar().menu[1].isEnabled = false

        accessActivityUI.getToolbar().setOnMenuItemClickListener{
            when (it.itemId) {
                R.id.menu_toolbar_favorite -> showSnackBar("Not available!")
                R.id.menu_toolbar_reload -> {
                    accessActivityUI.showLoadingDialog()
                    accessActivityLocation.searchForCurrentLocation()
                }
                else -> {
                    showSnackBar("Not available!")
                }
            }
            true
        }
    }

    private fun detailsBtnClicked()
    {
        if (detailsIsUp) {
            expand(binding.detailsCl, 1000, 550)
            val drawable = ContextCompat.getDrawable(requireActivity(), R.drawable.ic_collapse)
            binding.detailsBtn.setImageDrawable(drawable)
        } else {
            collapse(binding.detailsCl, 1000, 0)
            val drawable = ContextCompat.getDrawable(requireActivity(), R.drawable.ic_expand)
            binding.detailsBtn.setImageDrawable(drawable)
        }
        detailsIsUp = !detailsIsUp
    }

    private fun onLastSixteenDaysBtnClicked() {
        accessActivityUI.showLoadingDialog()

        if(App.isOnline)
        {
            if (locationName.isNotEmpty()) {
                getLastSixteenDaysWeather()
            }
        }
        else
        {
            lifecycleScope.launch(Dispatchers.IO){

                val weatherCondition = App.weatherConditionDao.getWeatherCondition(locationName)
                val days = weatherCondition?.lastDays?.toMutableList()
                withContext(Dispatchers.Main)
                {
                    if(days != null)
                    {
                        weatherCondition.unitGroup?.let { dayRecycleViewAndAdapterHandler(days, it, 0) }
                        binding.nextSixteenDaysBtn.isEnabled = true
                    }
                    else
                    {
                        showSnackBar("You are in offline mode!")
                    }
                    binding.lastSixteenDaysBtn.isEnabled = true
                    accessActivityUI.hideLoadingDialog()
                }
            }
        }

        binding.lastSixteenDaysBtn.isEnabled = false
    }

    private fun onNextSixteenDaysBtnClicked() {
        accessActivityUI.showLoadingDialog()
        if(App.isOnline)
        {
            if (locationName.isNotEmpty()) {
                getNextSixteenAndCurrentDayWeather()
            }
        }
        else
        {
            lifecycleScope.launch(Dispatchers.IO){
                val weatherCondition = App.weatherConditionDao.getWeatherCondition(locationName)
                val days = weatherCondition?.nextDays?.toMutableList()
                withContext(Dispatchers.Main)
                {
                    if(days != null)
                    {
                        weatherCondition.unitGroup?.let { dayRecycleViewAndAdapterHandler(days, it, 0) }
                    }
                    else
                    {
                        showSnackBar("You are in offline mode!")
                    }
                    binding.nextSixteenDaysBtn.isEnabled = true
                    accessActivityUI.hideLoadingDialog()
                }

            }
        }

        binding.nextSixteenDaysBtn.isEnabled = false
    }

    private fun tomorrowBtnClicked() {
        accessActivityUI.showLoadingDialog()
        if (locationName.isNotEmpty()) {
            if (binding.tomorrowBtn.text.equals(getString(R.string.show_tomorrow))) {
                binding.tomorrowBtn.text = getString(R.string.show_today)

                if(App.isOnline) {
                    updateData()
                }
                else {
                    lifecycleScope.launch(Dispatchers.IO){
                        val weatherCondition = App.weatherConditionDao.getWeatherCondition(locationName)
                        val weatherConditionTomorrow = weatherCondition?.tomorrow
                        withContext(Dispatchers.Main)
                        {
                            if(weatherConditionTomorrow != null)
                             {
                                updateUI(weatherConditionTomorrow)
                            }
                            else
                            {
                                showSnackBar("You are in offline mode!")
                            }
                            binding.tomorrowBtn.isEnabled = true
                            accessActivityUI.hideLoadingDialog()
                        }
                    }
                }
            } else if (binding.tomorrowBtn.text.equals(getString(R.string.show_today))) {
                binding.tomorrowBtn.text = getString(R.string.show_tomorrow)
                if(App.isOnline) {
                    updateData()
                }
                else {
                    lifecycleScope.launch(Dispatchers.IO) {
                        val weatherConditionToday =
                            App.weatherConditionDao.getWeatherCondition(
                                locationName
                            )
                        withContext(Dispatchers.Main)
                        {
                            if (weatherConditionToday != null) {
                                updateUI(weatherConditionToday)
                            } else {
                                showSnackBar("You are in offline mode!")
                            }
                            binding.tomorrowBtn.isEnabled = true
                            accessActivityUI.hideLoadingDialog()
                        }
                    }
                }

            }
        }

        binding.tomorrowBtn.isEnabled = false
    }


    private fun setLocationFromBundle() {
        val bundle = arguments
        if (bundle != null) {
            val selectedLocation = bundle.getString(BUNDLE_LOCATION)
            if (selectedLocation?.isNotEmpty() == true) {
                locationName = selectedLocation
            }
            unitGroup = bundle.getString(BUNDLE_UNITGROUP).toString()
            isFavourite = bundle.getBoolean(BUNDLE_IS_FAVOURITE)
        }
    }


    private fun initializeNextSixteenDaysAndCurrentWeatherViewModelWithFactory() {
        val nextSixteenDaysAndCurrentWeatherViewModelFactory =
            NextSixteenAndCurrentWeatherViewModelFactory(Repository())
        nextSixteenDaysAndCurrentWeatherViewModel = ViewModelProvider(
            this,
            nextSixteenDaysAndCurrentWeatherViewModelFactory
        )[NextSixteenDaysAndCurrentWeatherViewModel::class.java]
    }

    private fun initializeLastSixteenDaysWeatherViewModelWithFactory() {
        val lastSixteenDaysWeatherViewModelFactory =
            LastSixteenDaysWeatherViewModelFactory(Repository())
        lastSixteenDaysWeatherViewModel = ViewModelProvider(
            this,
            lastSixteenDaysWeatherViewModelFactory
        )[LastSixteenDaysWeatherViewModel::class.java]
    }

    private fun setDetailsToUI(weatherData : IWeatherData, unitGroup: String, sunriseEpoch : Long, sunsetEpoch : Long) {
        val degree = if (unitGroup == "metric") "C" else "F"
        val speed = if (unitGroup == "metric") "km/h" else "mph"
        val distance = if (unitGroup == "metric") "km" else "miles"

        try{
            if(weatherData.preciptype != null)
            {
                for(precip in weatherData.preciptype!!)
                {
                    when(precip){
                        "rain" -> binding.precipTypeRainIv.visibility = VISIBLE
                        "snow" -> binding.precipTypeSnowIv.visibility = VISIBLE
                        "freezingrain" -> binding.precipTypeFreezingRainIv.visibility = VISIBLE
                        "ice" -> binding.precipTypeIceIv.visibility = VISIBLE
                    }
                }
            }

        val img = weatherData.datetimeEpoch?.let {
            Utility.getImageIcon(weatherData.icon,
                sunriseEpoch, sunsetEpoch, it)
        }
        binding.weatherIv.setImageDrawable(img)

        if(weatherData.precipprob == null)
            weatherData.precipprob = 0.0
        if(weatherData.windgust == null)
            weatherData.windgust = 0.0

        binding.timeTv.text = weatherData.datetime
        binding.tempLikeTv.text = getString(
            R.string.temp,
            weatherData.temp.toString(),
            degree
        )
        binding.feelsLikeTv.text = getString(
            R.string.feels_like,
            weatherData.feelslike.toString(),
            degree
        )
        binding.precipProbTv.text =
            getString(R.string.precipprob, weatherData.precipprob.toString())
        binding.windTv.text =
            getString(R.string.wind, weatherData.windspeed.toString(), speed)
        binding.windGustTv.text =
            getString(R.string.wind_gust, weatherData.windgust.toString(), speed)
        binding.humidityTv.text =
            getString(R.string.humidity, weatherData.humidity.toString())
        binding.dewPointTv.text =
            getString(R.string.dew_point, weatherData.dew.toString(), degree)
        binding.cloudCoverTv.text =
            getString(R.string.cloud_cover, weatherData.cloudcover.toString())
        binding.visiblityTv.text =
            getString(R.string.visibility, weatherData.visibility.toString(), distance)
        }catch (e : Exception)
        {
            Log.e(TAG, "setDetailsToUI failed!")
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun hourRecycleViewAndAdapterHandler(
        day: Day,
        indexToJumpTo: Int
    ) {
        hourRecyclerView = binding.hoursRv

        try {
            val sunsetEpoch = day.sunsetEpoch!!
            val sunriseEpoch = day.sunriseEpoch!!
            hourAdapter =
                day.hours?.let {
                    HourAdapter(
                        this,
                        it.toMutableList(),
                        unitGroup,
                        sunriseEpoch,
                        sunsetEpoch
                    )
                }!!
        }catch (e : Exception)
        {
            Log.e(TAG, "sunsetEpoch or sunriseEpoch was null!")
        }

        val layoutManager: RecyclerView.LayoutManager =
            LinearLayoutManager(activity, RecyclerView.HORIZONTAL, false)

        layoutManager.scrollToPosition(indexToJumpTo)
        hourRecyclerView.layoutManager = layoutManager
        hourRecyclerView.itemAnimator = DefaultItemAnimator()
        hourRecyclerView.adapter = hourAdapter

        if (hourRecyclerView.itemDecorationCount < 1) {
            hourRecyclerView.addItemDecoration(
                DividerItemDecoration(
                    requireActivity(),
                    DividerItemDecoration.HORIZONTAL
                )
            )
        }

        hourAdapter.notifyDataSetChanged()
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun dayRecycleViewAndAdapterHandler(
        days: MutableList<Day>,
        unitGroup: String,
        indexToJumpTo: Int,
        reverseLayout: Boolean = false
    ) {
        dayRecyclerView = binding.daysRv

        dayAdapter = DayAdapter(this, days, unitGroup)

        val layoutManager: RecyclerView.LayoutManager =
            LinearLayoutManager(activity, RecyclerView.VERTICAL, reverseLayout)

        layoutManager.scrollToPosition(indexToJumpTo)
        dayRecyclerView.layoutManager = layoutManager
        dayRecyclerView.itemAnimator = DefaultItemAnimator()
        dayRecyclerView.adapter = dayAdapter

        if (dayRecyclerView.itemDecorationCount < 1) {
            dayRecyclerView.addItemDecoration(
                DividerItemDecoration(
                    requireActivity(),
                    DividerItemDecoration.VERTICAL
                )
            )
        }

        dayAdapter.notifyDataSetChanged()
    }


    private fun loadData() {
        accessActivityUI.getToolbar().title = "Currently not available!"
        if (type == CURRENT_LOCATION) {
            if(App.isOnline && App.isGPSEnabled)
            {
                accessActivityLocation.searchForCurrentLocation()
            }
            else
            {
                lifecycleScope.launch(Dispatchers.IO)
                {
                    val weatherCondition = App.weatherConditionDao.getCurrentWeatherCondition()
                    withContext(Dispatchers.Main){
                        if (weatherCondition != null) {
                            locationName = weatherCondition.address
                            updateUI(weatherCondition)
                            todayWeatherDetails = weatherCondition
                            setupToolbarMenuHandler(todayWeatherDetails)
                            binding.nextSixteenDaysBtn.isEnabled = true
                            binding.lastSixteenDaysBtn.isEnabled = true
                        }
                        accessActivityUI.hideLoadingDialog()
                    }
                }
            }

        }
        if (type == SELECTED_LOCATION) {
            setLocationFromBundle()
            if(App.isOnline)
            {
                updateData()
            }
            else
            {
                lifecycleScope.launch(Dispatchers.IO)
                {
                    val weatherCondition = App.weatherConditionDao.getWeatherCondition(locationName)
                    withContext(Dispatchers.Main){
                        if (weatherCondition != null) {
                            todayWeatherDetails = weatherCondition
                            updateUI(weatherCondition)
                            setupToolbarMenuHandler(weatherCondition)
                            binding.nextSixteenDaysBtn.isEnabled = true
                            binding.lastSixteenDaysBtn.isEnabled = true
                        }
                        accessActivityUI.hideLoadingDialog()
                    }
                }

            }

        }
    }

    private fun getNextSixteenAndCurrentDayWeather() {
        lifecycleScope.launch(Dispatchers.IO) {
            nextSixteenDaysAndCurrentWeatherViewModel.getLocationWeather(locationName, unitGroup, isFavourite)
        }
    }

    private fun observeNextSixteenDaysAndCurrentWeatherViewModel() {
        nextSixteenDaysAndCurrentWeatherViewModel.nextSixteenDaysAndCurrentWeatherCondition.observe(viewLifecycleOwner) { weatherConditions ->
            if(weatherConditions != null) {

                lifecycleScope.launch(Dispatchers.IO){
                    App.weatherConditionDao.updateNextDays(weatherConditions.days, weatherConditions.address)
                }

                dayRecycleViewAndAdapterHandler(weatherConditions.days.toMutableList(), unitGroup, 0)
                binding.nextSixteenDaysBtn.isEnabled = true
                accessActivityUI.hideLoadingDialog()
            }
        }

        nextSixteenDaysAndCurrentWeatherViewModel.nextSixteenDaysAndCurrentWeatherConditionError.observe(viewLifecycleOwner) { e ->
            accessActivityUI.hideLoadingDialog()
            errorProvider.errorHandler(e)
        }
    }


    private fun getLastSixteenDaysWeather() {
        lifecycleScope.launch(Dispatchers.IO) {
            lastSixteenDaysWeatherViewModel.getLocationWeather(locationName, unitGroup, isFavourite)
        }
    }

    private fun observeLastSixteenDaysWeatherViewModel() {
        lastSixteenDaysWeatherViewModel.lastSixteenWeatherCondition.observe(viewLifecycleOwner) { weatherConditions ->
            if(weatherConditions != null)
            {
                lifecycleScope.launch(Dispatchers.IO){
                    App.weatherConditionDao.updateLastDays(weatherConditions.days, weatherConditions.address)
                }

                dayRecycleViewAndAdapterHandler(weatherConditions.days.toMutableList(), unitGroup, 0, reverseLayout = true)
                binding.lastSixteenDaysBtn.isEnabled = true
                accessActivityUI.hideLoadingDialog()
            }
        }

        lastSixteenDaysWeatherViewModel.lastSixteenWeatherConditionError.observe(viewLifecycleOwner) { e ->
            accessActivityUI.hideLoadingDialog()
            errorProvider.errorHandler(e)
        }
    }


    override fun currentWeatherCondition(weatherCondition: WeatherCondition) {
        if(type == CURRENT_LOCATION)
        {
            locationName = weatherCondition.address
            updateUI(weatherCondition)

            lifecycleScope.launch(Dispatchers.IO) {
                App.weatherConditionDao.insertOrUpdateFavourite(weatherCondition)
            }

            setupToolbarMenuHandler(weatherCondition)
            todayWeatherDetails = weatherCondition

            binding.lastSixteenDaysBtn.isEnabled = true
            binding.nextSixteenDaysBtn.isEnabled = true
        }
        accessActivityUI.hideLoadingDialog()
    }

    private fun getOneDayWeather(
        date: String,
        unitGroup: String,
        isFavourite: Boolean = false,
    ) {
        lifecycleScope.launch(Dispatchers.IO) {
            oneDayWeatherViewModel.getLocationWeather(
                locationName,
                date,
                unitGroup,
                isFavourite,
            )
        }
    }

    private fun observeOneDayWeatherViewModel() {
        oneDayWeatherViewModel.oneDayWeatherCondition.observe(viewLifecycleOwner) { weatherCondition ->
            if(weatherCondition != null)
            {
                updateUI(weatherCondition)

                if (binding.tomorrowBtn.text.equals(getString(R.string.show_tomorrow))) {
                    lifecycleScope.launch(Dispatchers.IO)
                    {
                        App.weatherConditionDao.insertOrUpdateFavourite(weatherCondition)
                    }
                    todayWeatherDetails = weatherCondition
                    setupToolbarMenuHandler(weatherCondition)
                }
                else{

                    lifecycleScope.launch(Dispatchers.IO)
                    {
                        App.weatherConditionDao.updateTomorrow(weatherCondition, weatherCondition.address)
                    }
                    setupToolbarMenuHandler(todayWeatherDetails)
                }
                accessActivityUI.hideLoadingDialog()
            }
        }

        oneDayWeatherViewModel.oneDayWeatherConditionError.observe(viewLifecycleOwner) { e ->
            accessActivityUI.hideLoadingDialog()
            errorProvider.errorHandler(e)
        }
    }

    private fun updateUI(weatherCondition: WeatherCondition) {
        val location = weatherCondition.address
        val temp: String
        val conditions: String
        var currHour: String
        if (weatherCondition.currentConditions != null) {
            temp = weatherCondition.currentConditions?.temp.toString()
            conditions = weatherCondition.currentConditions?.conditions.toString()
            currHour = weatherCondition.currentConditions?.datetime?.take(2).toString()
            weatherCondition.unitGroup?.let { unitGroup ->
                weatherCondition.currentConditions!!.sunriseEpoch?.let { sunriseEpoch ->
                    weatherCondition.currentConditions!!.sunsetEpoch?.let { sunsetEpoch ->
                        setDetailsToUI(weatherCondition.currentConditions!!, unitGroup, sunriseEpoch, sunsetEpoch)
                    }
                }
            }
        } else {
            temp = weatherCondition.days[0].temp.toString()
            conditions = weatherCondition.days[0].conditions.toString()
            currHour = "00"
            weatherCondition.unitGroup?.let { unitGroup -> weatherCondition.days[0].sunriseEpoch?.let { sunriseEpoch ->
                weatherCondition.days[0].sunsetEpoch?.let { sunsetEpoch ->
                    setDetailsToUI(weatherCondition.days[0], unitGroup, sunriseEpoch, sunsetEpoch)
                }
            } }
        }
        if (currHour[0] == '0') {
            currHour = currHour[1].toString()
        }
        val hourIndex = currHour.toInt()

        handleButtonVisibilityAfterNewOneDayWeather()

        val symbol = if (weatherCondition.unitGroup == "metric") "C" else "F"
        accessActivityUI.getToolbar().title = "$location $temp°$symbol, $conditions"
        binding.sunriseTv.text = getString(R.string.sunrise, weatherCondition.days[0].sunrise)
        binding.sunsetTv.text = getString(R.string.sunset, weatherCondition.days[0].sunset)
        if (weatherCondition.isFavourite)
            accessActivityUI.getToolbar().menu[0].setIcon(R.drawable.ic_heart_red)
        accessActivityUI.getToolbar().menu[0].isEnabled = true
        accessActivityUI.getToolbar().menu[1].isEnabled = true

        hourRecycleViewAndAdapterHandler(weatherCondition.days[0], hourIndex)
    }

    private fun setupToolbarMenuHandler(weatherCondition: WeatherCondition) {
        accessActivityUI.getToolbar().setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.menu_toolbar_favorite ->

                    if (weatherCondition.isFavourite) {
                        lifecycleScope.launch(Dispatchers.IO){
                            App.weatherConditionDao.delete(weatherCondition)
                            weatherCondition.isFavourite = false
                            isFavourite = false
                        }
                        it.setIcon(R.drawable.ic_heart_white)
                        Log.i(
                            TAG,
                            "Today weather was deleted from favourite list!"
                        )
                        showSnackBar("Location: " + weatherCondition.address + " was successfully removed from the favourites!")
                    } else {
                        weatherCondition.isFavourite = true
                        isFavourite = true
                        lifecycleScope.launch(Dispatchers.IO) {
                            App.weatherConditionDao.insertOrUpdateFavourite(
                                weatherCondition
                            )
                        }
                        Log.i(
                            TAG,
                            "Today weather ${weatherCondition.address} was added to favourite list!"
                        )
                        it.setIcon(R.drawable.ic_heart_red)
                        showSnackBar("Location: " + weatherCondition.address + " was successfully added to the favourites!")
                    }

                R.id.menu_toolbar_reload ->
                    if (locationName.isNotEmpty()) {
                        accessActivityUI.showLoadingDialog()
                        updateData()
                        showSnackBar("Location: ${weatherCondition.address} was successfully reloaded!")
                        Log.i(TAG, "${weatherCondition.address} weather was reloaded!")
                    }
                else -> {
                    Log.e(TAG, "Undefined item")
                }
            }
            true
        }
    }

    private fun handleButtonVisibilityAfterNewOneDayWeather() {
        binding.lastSixteenDaysBtn.visibility = VISIBLE
        binding.nextSixteenDaysBtn.visibility = VISIBLE
        binding.tomorrowBtn.visibility = VISIBLE
        binding.tomorrowBtn.isEnabled = true
    }
}