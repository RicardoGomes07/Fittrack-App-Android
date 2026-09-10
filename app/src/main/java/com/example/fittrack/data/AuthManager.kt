package com.example.fittrack.data

import com.example.fittrack.data.model.UserRepository
import com.example.fittrack.model.Gender
import com.example.fittrack.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import java.time.LocalDate

class AuthManager(private val userRepository: UserRepository) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    init {
        userRepository.loggedInUser
            .onEach { user ->
                _currentUser.value = user
                _isLoggedIn.value = user != null
                _isInitialized.value = true
            }
            .launchIn(scope)
    }

    suspend fun login(nickname: String, password: String): Boolean {
        val user = userRepository.login(nickname, password)
        if (user != null) {
            userRepository.signUp(user) // Mark as logged in in our local DB
            return true
        }
        return false
    }

    suspend fun signUp(
        name: String,
        nickname: String,
        password: String,
        weight: Double = 0.0,
        height: Int = 0,
        gender: Gender = Gender.OTHER
    ) {
        val newUser = User(
            name = name,
            nickname = nickname,
            password = password,
            weight = weight,
            height = height,
            gender = gender,
            memberSince = LocalDate.now()
        )
        userRepository.signUp(newUser)
    }

    suspend fun logout() {
        userRepository.logout()
    }
}
