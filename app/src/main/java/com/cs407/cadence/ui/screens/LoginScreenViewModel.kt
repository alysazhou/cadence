package com.cs407.cadence.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cs407.cadence.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class AuthState {
    UNKNOWN,
    UNAUTHENTICATED,
    AUTHENTICATED
}

class LoginScreenViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _authState = MutableStateFlow(AuthState.UNKNOWN)
    val authState = _authState.asStateFlow()

    init {
        viewModelScope.launch {
            val user = userRepository.getUser()
            _authState.value = if (user != null) {
                AuthState.AUTHENTICATED
            } else {
                AuthState.UNAUTHENTICATED
            }
        }
    }

    fun registerUser(username: String) {
        viewModelScope.launch {
            userRepository.registerUser(username)
            _authState.value = AuthState.AUTHENTICATED
        }
    }

    fun deleteUser() {
        viewModelScope.launch {
            userRepository.deleteUser()
            _authState.value = AuthState.UNAUTHENTICATED
        }
    }
}

class LoginScreenViewModelFactory(private val userRepository: UserRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginScreenViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LoginScreenViewModel(userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
