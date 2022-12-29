package com.bogdan801.weatheraggregator.presentation.screens.home

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bogdan801.weatheraggregator.data.datastore.saveIntToDataStore
import com.bogdan801.weatheraggregator.domain.repository.Repository
import com.bogdan801.weatheraggregator.presentation.theme.Theme
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeScreenViewModel
@Inject
constructor(
    private val repository: Repository,
    private val theme: MutableState<Theme>,
    handle: SavedStateHandle
): ViewModel() {
    //theme handling
    val themeState: State<Theme> = theme
    fun setTheme(ordinal: Int, context: Context){
        theme.value = Theme.values()[ordinal]
        viewModelScope.launch {
            context.saveIntToDataStore("THEME", ordinal)
        }
    }

    private val _isLoadingState = mutableStateOf(false)
    val isLoadingState: State<Boolean> = _isLoadingState
    fun updateWeatherData(){
        viewModelScope.launch {
            _isLoadingState.value = true
            delay(3000)
            _isLoadingState.value = false
        }
    }
}