package com.bogdan801.weatheraggregator.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/**
 * Function to save Int values to DataStore
 */
suspend fun Context.saveIntToDataStore(key: String, value: Int) {
    val dataStoreKey = intPreferencesKey(key)
    dataStore.edit { settings ->
        settings[dataStoreKey] = value
    }
}

/**
 * Function to read Int values from DataStore
 */
suspend fun Context.readIntFromDataStore(key: String): Int? {
    val dataStoreKey = intPreferencesKey(key)
    val preferences = dataStore.data.first()
    return preferences[dataStoreKey]
}

/**
 * Function to save String values to DataStore
 */
suspend fun Context.saveStringToDataStore(key: String, value: String) {
    val dataStoreKey = stringPreferencesKey(key)
    dataStore.edit { settings ->
        settings[dataStoreKey] = value
    }
}

/**
 * Function to read String values from DataStore
 */
suspend fun Context.readStringFromDataStore(key: String): String? {
    val dataStoreKey = stringPreferencesKey(key)
    val preferences = dataStore.data.first()
    return preferences[dataStoreKey]
}