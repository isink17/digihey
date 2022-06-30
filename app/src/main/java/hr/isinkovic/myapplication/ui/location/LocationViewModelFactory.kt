package hr.isinkovic.myapplication.ui.location

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import hr.isinkovic.myapplication.db.CardDAO

class LocationViewModelFactory(
    private val preferences: SharedPreferences,
    private val database: CardDAO
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LocationViewModel::class.java)) {
            return LocationViewModel(preferences, database) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}