package com.bogdan801.weatheraggregator.presentation.screens.home

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bogdan801.weatheraggregator.domain.repository.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
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


    init {
        viewModelScope.launch {
            _selectionDisplayList.value = repository.getOblastList()
        }
    }
}