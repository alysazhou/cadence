package com.cs407.cadence.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import checkEmail
import checkPassword
import com.cs407.cadence.data.models.User
import com.cs407.cadence.data.models.UserSettings
import com.cs407.cadence.data.models.UserState
import com.cs407.cadence.data.repository.UserRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import createAccount
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import updateName
import signIn
import reauthenticateUser

class UserViewModel : ViewModel() {
    private val _userState = MutableStateFlow<UserState?>(null)
    val userState = _userState.asStateFlow()

    private val _emailError = MutableStateFlow<String?>(null)
    val emailError = _emailError.asStateFlow()

    private val _reauthError = MutableStateFlow<String?>(null)
    val reauthError = _reauthError.asStateFlow()

    private val _passwordError = MutableStateFlow<String?>(null)
    val passwordError = _passwordError.asStateFlow()

    private val repository = UserRepository()

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _settings = MutableStateFlow(UserSettings())
    val settings: StateFlow<UserSettings> = _settings.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val auth: FirebaseAuth = Firebase.auth

    init {
        auth.addAuthStateListener { firebaseAuth ->
            val firebaseUser = firebaseAuth.currentUser
            _userState.update {
                if (firebaseUser == null) {
                    null
                } else {
                    UserState(
                        uid = firebaseUser.uid,
                        name = firebaseUser.displayName ?: ""
                    )
                }
            }

            if (firebaseUser != null) {
                loadUser(firebaseUser.uid)
            } else {
                _user.value = null
                _settings.value = UserSettings()
            }
        }
    }

    fun clearErrors() {
        _emailError.value = null
        _passwordError.value = null
        _reauthError.value = null
    }

    fun signIn(email: String, password: String) {
        clearErrors()
        if (checkEmail(email) != EmailResult.Valid) {
            _emailError.value = "Please enter a valid email."
            return
        }
        if (password.isEmpty()) {
            _passwordError.value = "Password cannot be empty."
            return
        }

        signIn(email, password,
            onSuccess = {
                println("Sign-in successful for user: ${it.uid}")
            },
            onFailure = {
                _passwordError.value = "Incorrect email or password."
                println("Sign-in failed: ${it.message}")
            }
        )
    }

    fun register(email: String, password: String) {
        var hasError = false
        clearErrors()
        when (checkEmail(email)) {
            EmailResult.Empty -> {
                _emailError.value = "Email cannot be empty."
                hasError = true
            }
            EmailResult.Invalid -> {
                _emailError.value = "Please enter a valid email."
                hasError = true
            }
            EmailResult.Valid -> {}
        }

        when (checkPassword(password)) {
            PasswordResult.Empty -> {
                _passwordError.value = "Password cannot be empty."
                hasError = true
            }
            PasswordResult.Short -> {
                _passwordError.value = "Password must be at least 5 characters."
                hasError = true
            }
            PasswordResult.Invalid -> {
                _passwordError.value = "Password needs an uppercase, lowercase, and number."
                hasError = true
            }
            PasswordResult.Valid -> {}
        }

        if(hasError) return

        createAccount(email, password,
            onSuccess = {
                println("Registration successful for user: ${it.uid}")
            },
            onFailure = {
                _emailError.value = "Email already in use!"
                println("Registration failed: ${it.message}")
            }
        )
    }

    fun setDisplayName(name: String) {
        updateName(name)
        _userState.update { it?.copy(name = name) }

        viewModelScope.launch {
            repository.updateUserName(name)
        }
    }

    fun logout() {
        auth.signOut()
    }

    fun deleteAccount(password: String) {
        clearErrors()

        if (password.isEmpty()) {
            _reauthError.value = "Password is required."
            return
        }

        reauthenticateUser(password,
            onSuccess = {
                val user = auth.currentUser
                user?.delete()?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        println("Account deleted successfully.")
                    } else {
                        _reauthError.value = "Deletion failed. Please try again."
                        println("Account deletion failed after re-auth: ${task.exception?.message}")
                    }
                }
            },
            onFailure = {
                _reauthError.value = "Incorrect password."
                println("Re-authentication failed: ${it.message}")
            }
        )
    }

    fun loadUser(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getUser(userId).onSuccess { user ->
                _user.value = user
                _settings.value = user?.settings ?: UserSettings()
            }.onFailure { e ->
                println("Error loading user: ${e.message}")
            }
            _isLoading.value = false
        }
    }

    fun saveUser(user: User) {
        viewModelScope.launch {
            repository.saveUser(user).onSuccess {
                _user.value = user
                _settings.value = user.settings
            }.onFailure { e ->
                println("Error saving user: ${e.message}")
            }
        }
    }

    fun updateSettings(
        autoSkipEnabled: Boolean? = null,
        pauseMusicOnStop: Boolean? = null,
        targetPace: Double? = null,
        preferredGenre: String? = null
    ) {
        viewModelScope.launch {
            val currentSettings = _settings.value
            val newSettings = currentSettings.copy(
                autoSkipEnabled = autoSkipEnabled ?: currentSettings.autoSkipEnabled,
                pauseMusicOnStop = pauseMusicOnStop ?: currentSettings.pauseMusicOnStop,
                targetPace = targetPace ?: currentSettings.targetPace,
                preferredGenre = preferredGenre ?: currentSettings.preferredGenre
            )

            repository.updateSettings(newSettings).onSuccess {
                _settings.value = newSettings
                println("Settings updated successfully")
            }.onFailure { e ->
                println("Error updating settings: ${e.message}")
            }
        }
    }
}