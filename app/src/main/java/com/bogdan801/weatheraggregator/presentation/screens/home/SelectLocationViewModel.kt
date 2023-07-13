package com.bogdan801.weatheraggregator.presentation.screens.home

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bogdan801.weatheraggregator.R
import com.bogdan801.weatheraggregator.domain.model.Location
import com.bogdan801.weatheraggregator.domain.repository.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import javax.inject.Inject

@HiltViewModel
class SelectLocationViewModel
@Inject
constructor(
    val repository: Repository
): ViewModel() {
    //SEARCH
    private val _searchBarText = mutableStateOf("")
    val searchBarText: State<String> = _searchBarText

    private val _foundOblasts = mutableStateOf(listOf<Location>())
    val foundOblasts: State<List<Location>> = _foundOblasts

    private val _foundRegions = mutableStateOf(listOf<Location>())
    val foundRegions: State<List<Location>> = _foundRegions

    private val _foundLocations = mutableStateOf(listOf<Location>())
    val foundLocations: State<List<Location>> = _foundLocations

    private fun searchOblasts(prompt: String){
        viewModelScope.launch {
            _foundOblasts.value = repository.searchOblasts(prompt.trim())
        }
    }

    private fun searchRegions(prompt: String){
        viewModelScope.launch {
            _foundRegions.value = repository.searchRegions(prompt.trim())
        }
    }

    private fun searchLocations(prompt: String){
        viewModelScope.launch {
            _foundLocations.value = repository.searchLocations(prompt.trim())
        }
    }

    fun searchBarTextChanged(newText: String){
        _searchBarText.value = newText

        _foundOblasts.value = listOf()
        _foundRegions.value = listOf()
        _foundLocations.value = listOf()

        if(newText.isNotBlank()){
            searchOblasts(newText)
            if(newText.length > 1) searchRegions(newText)
            if(newText.length > 2) searchLocations(newText)
        }
    }

    //SELECTION
    val lazyColumnState = LazyListState()
    val lazyRowState = LazyListState()
    private val _path = mutableStateOf(listOf("Україна"))
    val path: State<List<String>> = _path

    private val _selectionDisplayList = mutableStateOf(listOf(""))
    val selectionDisplayList: State<List<String>> = _selectionDisplayList

    fun displayOblastList(){
        _path.value = listOf(_path.value[0])
        _searchBarText.value = ""
        viewModelScope.launch {
            _selectionDisplayList.value = repository.getOblastList()
            lazyColumnState.scrollToItem(0)
            delay(100)
            lazyRowState.scrollToItem(_path.value.lastIndex)
        }
    }

    fun selectOblast(oblastName: String){
        viewModelScope.launch {
            delay(200)
            _searchBarText.value = ""
            _path.value = listOf(_path.value[0], oblastName)
            _selectionDisplayList.value = repository.getOblastRegionList(oblastName)
            lazyColumnState.scrollToItem(0)
            delay(100)
            lazyRowState.scrollToItem(_path.value.lastIndex)
        }
    }

    fun selectRegion(oblastName: String, regionName:String){
        viewModelScope.launch {
            delay(200)
            _searchBarText.value = ""
            _path.value = listOf(_path.value[0], oblastName, regionName)
            _selectionDisplayList.value = repository.getLocationsList(oblastName, regionName)
            lazyColumnState.scrollToItem(0)
            delay(100)
            lazyRowState.scrollToItem(_path.value.lastIndex)
        }
    }

    fun selectLocation(oblastName: String, regionName:String, name: String) = runBlocking {
        delay(200)
        return@runBlocking repository.getLocation(oblastName, regionName, name)[0]
    }


    var isGettingLocationLoading by mutableStateOf(false)
        private set

    @SuppressLint("MissingPermission")
    fun getGetDeviceLocation(context: Context, onLocationDetermined: (Location) -> Unit) {
        isGettingLocationLoading = true
        repository.getDeviceLocation(context){ location ->
            if (location == null) {
                Toast.makeText(context, context.getString(R.string.failedToGetLocation), Toast.LENGTH_SHORT).show()
            }
            else {
                viewModelScope.launch(Dispatchers.IO) {
                    val determinedLocation = async {
                        repository.getClosestLocation(location.latitude, location.longitude)
                    }
                    determinedLocation.await()
                        ?.let {
                            launch(Dispatchers.Main){
                                onLocationDetermined(it)
                            }
                        }
                        ?: Toast.makeText(context, context.getString(R.string.failedToGetLocation), Toast.LENGTH_SHORT).show()
                }
            }
            isGettingLocationLoading = false
        }
    }

    init {
        viewModelScope.launch {
            _selectionDisplayList.value = repository.getOblastList()
        }
    }
}