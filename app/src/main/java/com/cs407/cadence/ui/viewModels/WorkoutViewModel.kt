package com.cs407.cadence.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cs407.cadence.data.models.RoutePoint
import com.cs407.cadence.data.models.SessionStatus
import com.cs407.cadence.data.models.WorkoutSession
import com.cs407.cadence.data.repository.WorkoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class WorkoutViewModel : ViewModel() {

    private val repository = WorkoutRepository()

    private val _currentSession = MutableStateFlow<WorkoutSession?>(null)
    val currentSession: StateFlow<WorkoutSession?> = _currentSession

    private val _lastSession = MutableStateFlow<WorkoutSession?>(null)
    val lastSession: StateFlow<WorkoutSession?> = _lastSession

    private val _workoutHistory = MutableStateFlow<List<WorkoutSession>>(emptyList())
    val workoutHistory: StateFlow<List<WorkoutSession>> = _workoutHistory

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun startWorkout() {
        viewModelScope.launch {
            _isLoading.value = true

            repository
                    .startSession()
                    .onSuccess { session -> _currentSession.value = session }
                    .onFailure { e -> _error.value = e.message }

            _isLoading.value = false
        }
    }

    fun updateWorkout(distance: Double, routePoints: List<RoutePoint>, songsPlayed: List<String>) {
        viewModelScope.launch {
            val session = _currentSession.value ?: return@launch

            repository.updateSession(
                            sessionId = session.sessionId,
                            distance = distance,
                            routePoints = routePoints,
                            songsPlayed = songsPlayed
                    )
                    .onSuccess {
                        _currentSession.value =
                                session.copy(
                                        distance = distance,
                                        routePoints = routePoints,
                                        songsPlayed = songsPlayed
                                )
                    }
        }
    }

    fun pauseWorkout() {
        viewModelScope.launch {
            val session = _currentSession.value ?: return@launch

            repository.pauseSession(session.sessionId).onSuccess {
                _currentSession.value = session.copy(status = SessionStatus.PAUSED.name)
            }
        }
    }

    fun resumeWorkout() {
        viewModelScope.launch {
            val session = _currentSession.value ?: return@launch

            repository.resumeSession(session.sessionId).onSuccess {
                _currentSession.value = session.copy(status = SessionStatus.ACTIVE.name)
            }
        }
    }

    fun endWorkout(
            time: Int,
            distance: Double,
            averagePace: Double,
            calories: Int,
            bpm: Int,
            playedSongsDetails: List<Map<String, Any?>> = emptyList()
    ) {
        viewModelScope.launch {
            val session =
                    _currentSession.value
                            ?: run {
                                return@launch
                            }

            repository
                    .endSession(
                            sessionId = session.sessionId,
                            time = time,
                            distance = distance,
                            averagePace = averagePace,
                            calories = calories,
                            bpm = bpm,
                            playedSongsDetails = playedSongsDetails
                    )
                    .onSuccess {
                        _currentSession.value = null

                        loadLastSession()
                        loadWorkoutHistory()
                    }
                    .onFailure { e -> _error.value = e.message }
        }
    }

    fun loadWorkoutHistory() {
        viewModelScope.launch {
            repository.getAllSessions().onSuccess { sessions ->
                val sorted =
                        sessions.sortedByDescending { session ->
                            session.endTime?.seconds ?: session.startTime.seconds
                        }
                _workoutHistory.value = sorted
            }
        }
    }

    fun loadLastSession() {
        viewModelScope.launch { repository.getLastSession().onSuccess { _lastSession.value = it } }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.deleteAllSessions().onSuccess {
                _workoutHistory.value = emptyList()
                _lastSession.value = null
            }
        }
    }

    fun deleteSessions(ids: List<String>) {
        viewModelScope.launch {
            repository.deleteSessions(ids)
            loadWorkoutHistory()
        }
    }
}
