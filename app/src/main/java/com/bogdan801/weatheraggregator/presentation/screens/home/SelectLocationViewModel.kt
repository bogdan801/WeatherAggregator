package com.bogdan801.weatheraggregator.presentation.screens.home

import android.location.Location
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bogdan801.weatheraggregator.domain.repository.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class SelectLocationViewModel
@Inject
constructor(
    val repository: Repository
): ViewModel() {
    private val _searchBarText = mutableStateOf("")
    val searchBarText: State<String> = _searchBarText

    fun searchBarTextChanged(newText: String){
        _searchBarText.value = newText
    }

    private val _path = mutableStateOf(listOf("Україна"))
    val path: State<List<String>> = _path

    private val _selectionDisplayList = mutableStateOf(listOf(""))
    val selectionDisplayList: State<List<String>> = _selectionDisplayList

    fun displayOblastList(){
        _path.value = listOf(_path.value[0])
        viewModelScope.launch {
            _selectionDisplayList.value = repository.getOblastList()
        }
    }

    fun selectOblast(oblastName: String){
        _path.value = listOf(_path.value[0], oblastName)
        viewModelScope.launch {
            _selectionDisplayList.value = repository.getOblastRegionList(oblastName)
        }
    }

    fun selectRegion(oblastName: String, regionName:String){
        _path.value = listOf(_path.value[0], oblastName, regionName)
        viewModelScope.launch {
            _selectionDisplayList.value = repository.getLocationsList(oblastName, regionName)
        }
    }

    fun selectLocation(name: String) = runBlocking { return@runBlocking repository.getLocation(path.value[1], path.value[2], name)[0] }

    init {
        viewModelScope.launch {
            _selectionDisplayList.value = repository.getOblastList()
        }
    }
}