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
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.internal.toImmutableList
import javax.inject.Inject

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
        readTrustLevelsForDomains(domains)
    }

    //trust levels
    private val _trustLevels = mutableStateOf(listOf(1.0/3, 1.0/3, 1.0/3))
    val trustLevels: State<List<Double>> = _trustLevels
    fun setTrustLevels(newTrustLevels: List<Double>){
        _trustLevels.value = newTrustLevels
    }

    fun saveTrustLevels(){
        viewModelScope.launch {
            context.saveStringToDataStore(
                "trustLevels",
                buildString {
                    _trustLevels.value.forEachIndexed {id, it ->
                        append(
                            "${dataListState[id].data.domain.ordinal}:${it}" +
                            if(id != _trustLevels.value.lastIndex) "_" else ""
                        )
                    }
                }
            )
        }
    }

    //average
    val averageData by derivedStateOf {
        val dataList = _dataListState.map { it.data }
        val someAreLoading = _dataListState.map { it.isLoading }.contains(true)
        val isErrorPresent = _dataListState.map { it.error != null }.contains(true)

        if(isErrorPresent) {
            return@derivedStateOf WeatherDataState.Error(
                d = getAverageUseCase(
                    dataList = dataList,
                    trustLevels = _trustLevels.value
                ),
                message = "Average error"
            )
        }

        if(someAreLoading){
            return@derivedStateOf WeatherDataState.IsLoading(
                d = getAverageUseCase(
                    dataList = dataList,
                    trustLevels = _trustLevels.value
                )
            )
        }

        return@derivedStateOf WeatherDataState.Data(getAverageUseCase(dataList = dataList, trustLevels = _trustLevels.value))
    }

    private val _selectedDataIndexState = mutableStateOf(0)
    val selectedDataIndexState: State<Int> = _selectedDataIndexState

    val currentDataState by derivedStateOf {
        if(_selectedDataIndexState.value == 0) averageData
        else _dataListState[_selectedDataIndexState.value - 1]
    }

    fun setSelectedData(index: Int){
        _selectedDataIndexState.value = index
    }

    //day selection
    private val _selectedDayState = mutableStateOf(0)
    val selectedDayState: State<Int> = _selectedDayState
    val selectedDay get() = if(currentDataState.data.weatherByDates.isNotEmpty()) currentDataState.data.weatherByDates[_selectedDayState.value] else DayWeatherCondition()

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

    private val _isDayPanelExpanded = mutableStateOf(false)
    val isDayPanelExpanded: State<Boolean> = _isDayPanelExpanded

    fun setDayPanelExpansion(state: Boolean){
        _isDayPanelExpanded.value = state
    }

    //loading
    private val _isRefreshingState = mutableStateOf(false)
    val isRefreshingState: State<Boolean> = _isRefreshingState

    fun refreshAllWeatherData(){
        setDayPanelExpansion(false)
        viewModelScope.launch {
            _isRefreshingState.value = true
            setupDataFlows(_selectedLocationState.value, _dataListState.map { it.data.domain })
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
        val remainingDomains = _dataListState
            .filterIndexed { index, _ ->
                !_selectedCards.contains(index)
            }
            .map{
                it.data.domain
            }
        clearSelection()
        setupDataFlows(location = _selectedLocationState.value, domains = remainingDomains, clearCache = true)
    }

    private fun readTrustLevelsForDomains(domains: List<WeatherSourceDomain>){
        viewModelScope.launch {
            val cachedTrustLevels = context.readStringFromDataStore("trustLevels")
            if(cachedTrustLevels != null && cachedTrustLevels.isNotBlank()){
                val split = cachedTrustLevels.split("_")
                val trustLevelsMap = buildMap {
                    split.forEach {
                        val pair = it.split(":")
                        put(pair[0].toInt(), pair[1].toDouble())
                    }
                }
                val newTrustLevels = buildList {
                    val equalPartSize = 1.0/domains.size
                    val sizeDifference = domains.size - split.size

                    var sumOfDomainsLeft = 0.0
                    trustLevelsMap.forEach { (domainID, value) ->
                        sumOfDomainsLeft += if(domains.contains(WeatherSourceDomain.values()[domainID])) value else 0.0
                    }

                    domains.forEach { domain ->
                        if(trustLevelsMap[domain.ordinal] != null){
                            val level = if(domains.size > split.size){
                                trustLevelsMap[domain.ordinal]!! * (1.0 - (equalPartSize * sizeDifference))
                            }
                            else if(domains.size < split.size){
                                trustLevelsMap[domain.ordinal]!! * (1.0/sumOfDomainsLeft)
                            }
                            else trustLevelsMap[domain.ordinal]!!
                            add(level)
                        }
                        else {
                            add(equalPartSize)
                        }
                    }
                }
                setTrustLevels(formatTrustLevels(newTrustLevels))
            }
            else{
                setTrustLevels(List(domains.size) { 1.0/domains.size })
            }
            //saveTrustLevels()
        }
    }

    private fun formatTrustLevels(levels: List<Double>) = if (levels.any { it < 0.1 }) {
        val indices = levels.indices.filter { levels[it] < 0.1 }
        val maxIndex = levels.indexOf(levels.max())
        val newLevels = levels.toMutableList().apply {
            indices.forEach { index ->
                val difference = 0.1 - levels[index]
                this[index] = 0.1
                this[maxIndex] -= difference
            }
        }
        newLevels
    } else levels


    init {
        viewModelScope.launch {
            val cachedLocation = Location.fromString(context.readStringFromDataStore("location"))
            if(cachedLocation != null){
                setupDataFlows(location = cachedLocation, domains = repository.getCachedDomains())
            }
            readTrustLevelsForDomains(dataListState.map { it.data.domain })
        }
    }
}