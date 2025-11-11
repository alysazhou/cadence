package com.cs407.cadence.ui.viewModels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cs407.cadence.data.ActivityRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate

// ACTIVITY TYPE TO GENRES
val allActivities = ActivityRepository.getActivityNames()

data class HomeScreenUiState (
    val selectedActivity: String = allActivities.first(),
    val selectedGenre: String = ActivityRepository.findActivityByName(allActivities.first())!!.compatibleGenres.first(),
    val showActivitySelector: Boolean = false,
    val showGenreSelector: Boolean = false
)
class HomeScreenViewModel(application: Application): ViewModel() {

    private val _uiState = MutableStateFlow(HomeScreenUiState())
    val uiState: StateFlow<HomeScreenUiState> = _uiState.asStateFlow()

    fun onActivitySelected(activityName: String) {
        val selectedActivity = ActivityRepository.findActivityByName(activityName) ?: return

        // select activity and close dialog
        _uiState.value = _uiState.value.copy(selectedActivity = activityName, showActivitySelector = false)

        // if selected genre is not a valid genre for the new activity, select the first valid genre
        if (uiState.value.selectedActivity !in selectedActivity.compatibleGenres) {
            _uiState.value = _uiState.value.copy(selectedGenre = selectedActivity.compatibleGenres.first())
        }
    }

    fun onGenreSelected(genre: String) {
        _uiState.value = _uiState.value.copy(selectedGenre = genre, showGenreSelector = false)
    }

    fun onActivitySelectorDismiss() {
        _uiState.value = _uiState.value.copy(showActivitySelector = false)
    }

    fun onGenreSelectorDismiss() {
        _uiState.value = _uiState.value.copy(showGenreSelector = false)
    }

    fun onActivityButtonClick() {
        _uiState.value = _uiState.value.copy(showActivitySelector = true)
    }

    fun onGenreButtonClick() {
        _uiState.value = _uiState.value.copy(showGenreSelector = true)
    }
}

class HomeScreenViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeScreenViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeScreenViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
