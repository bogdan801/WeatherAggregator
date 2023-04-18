package com.bogdan801.weatheraggregator.presentation.screens.home

import android.app.Application
import android.content.Context
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bogdan801.weatheraggregator.data.datastore.readStringFromDataStore
import com.bogdan801.weatheraggregator.data.datastore.saveIntToDataStore
import com.bogdan801.weatheraggregator.data.datastore.saveStringToDataStore
import com.bogdan801.weatheraggregator.domain.model.*
import com.bogdan801.weatheraggregator.domain.repository.Repository
import com.bogdan801.weatheraggregator.domain.usecase.GetAverageWeatherDataUseCase
import com.bogdan801.weatheraggregator.domain.usecase.GetWeatherDataUseCase
import com.bogdan801.weatheraggregator.presentation.theme.Theme
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.internal.toImmutableList
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class HomeScreenViewModel
@Inject
constructor(
    private val repository: Repository,
    private val theme: MutableState<Theme>,
    val getDataUseCase: GetWeatherDataUseCase,
    val getAverageUseCase: GetAverageWeatherDataUseCase,
    application: Application
): AndroidViewModel(application) {
    //context
    private val context
        get() = getApplication<Application>()

    //theme handling
    val themeState: State<Theme> = theme
    fun setTheme(ordinal: Int, context: Context){
        theme.value = Theme.values()[ordinal]
        viewModelScope.launch {
            context.saveIntToDataStore("THEME", ordinal)
        }
    }

    //bottom sheet control and location
    private val _tempLocation = mutableStateOf(Location("", "", "", "", "", 0.0, 0.0))
    val tempLocation: State<Location>  = _tempLocation

    private val _blockBackPressOnLocationsSelection = mutableStateOf(true)
    val blockBackPressOnLocationsSelection: State<Boolean>  = _blockBackPressOnLocationsSelection

    fun setBlockBackPressOnLocationsSelection(value: Boolean){
        _blockBackPressOnLocationsSelection.value = value
    }

    fun setTemporaryLocation(location: Location){
        _tempLocation.value = location
    }

    private val _showSelectLocationSheet = mutableStateOf(true)
    val showSelectLocationSheet: State<Boolean>  = _showSelectLocationSheet

    fun openSelectLocationSheet(value: Boolean){
        _showSelectLocationSheet.value = value
    }

    private val _selectedLocationState = mutableStateOf(Location("", "", "", "", "", 0.0, 0.0))
    val selectedLocation: State<Location>  = _selectedLocationState


    //data selection
    private val jobs = mutableListOf<Job>()

    private val _dataListState = mutableStateListOf<WeatherDataState>()
    val dataListState: List<WeatherDataState>  = _dataListState

    fun setupDataFlows(location: Location, domains: List<WeatherSourceDomain>, clearCache: Boolean = false) {
        if(clearCache) runBlocking { repository.deleteAllWeatherData() }

        _selectedLocationState.value = location
        viewModelScope.launch { context.saveStringToDataStore("location", location.toString()) }

        jobs.forEach{ job ->
            job.cancel()
        }
        jobs.clear()
        _dataListState.clear()

        setTrustLevels(List(domains.size) { 1 / domains.size.toDouble() })

        domains.forEachIndexed{ id, domain ->
            _dataListState.add(WeatherDataState.IsLoading(d = WeatherData(domain = domain, url = domain.domain)))

            jobs.add(
                viewModelScope.launch {
                    getDataUseCase(location, domain).collect { newDataState ->
                        _dataListState[id] = newDataState
                    }
                }
            )
        }
    }

    //trust levels
    private val _trustLevels = mutableStateOf(listOf(1.0/3, 1.0/3, 1.0/3))
    val trustLevels: State<List<Double>> = _trustLevels
    fun setTrustLevels(newTrustLevels: List<Double>){
        _trustLevels.value = newTrustLevels
    }

    //average
    val averageData by derivedStateOf {
        WeatherDataState.Data(getAverageUseCase(dataList = _dataListState.map { it.data }, trustLevels = _trustLevels.value))
    }

    private val _selectedDataIndexState = mutableStateOf(0)
    val selectedDataIndexState: State<Int> = _selectedDataIndexState

    val currentData by derivedStateOf {
        if(_selectedDataIndexState.value == 0) averageData.data
        else _dataListState[_selectedDataIndexState.value - 1].data
    }

    fun setSelectedData(index: Int){
        setSelectedDay(0)
        _selectedDataIndexState.value = index
    }

    //day selection
    private val _selectedDayState = mutableStateOf(0)
    val selectedDayState: State<Int> = _selectedDayState
    val selectedDay get() = if(currentData.weatherByDates.isNotEmpty()) currentData.weatherByDates[_selectedDayState.value] else DayWeatherCondition()

    fun setSelectedDay(index: Int, slideRight: (Boolean) -> Unit = {}){
        if(selectedDayState.value > index){
            slideRight(true)
            _selectedDayState.value = index
        }
        else if(selectedDayState.value < index){
            slideRight(false)
            _selectedDayState.value = index
        }
    }

    //loading
    private val _isLoadingState = mutableStateOf(false)
    val isLoadingState: State<Boolean> = _isLoadingState

    private val _isRefreshingState = mutableStateOf(false)
    val isRefreshingState: State<Boolean> = _isRefreshingState

    fun refreshAllWeatherData(){
        viewModelScope.launch {
            _isRefreshingState.value = true
            setupDataFlows(_selectedLocationState.value, _dataListState.map { it.data.domain })
            //delay(1.seconds)
            _isRefreshingState.value = false
        }
    }

    //selected data cards
    private val _selectedCards = mutableStateListOf<Int>()
    val selectedCards by derivedStateOf {
        _selectedCards.toImmutableList()
    }

    val cardsSelected by derivedStateOf {
        _selectedCards.isNotEmpty()
    }

    fun setDataCardSelection(index: Int, value: Boolean){
        if(_selectedCards.contains(index)){
            if(!value) _selectedCards.remove(index)
        }
        else {
            if(value) _selectedCards.add(index)
        }
    }

    fun clearSelection(){
        _selectedCards.clear()
    }

    fun deleteSelectedData(){

    }


    init {
        viewModelScope.launch {
            val cachedLocation = Location.fromString(context.readStringFromDataStore("location"))
            if(cachedLocation != null){
                setupDataFlows(location = cachedLocation, domains = repository.getCachedDomains())
            }
        }
    }
}