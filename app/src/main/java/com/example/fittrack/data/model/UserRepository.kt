package com.example.fittrack.data.model

import com.example.fittrack.data.local.UserDao
import com.example.fittrack.model.User
import kotlinx.coroutines.flow.Flow

class UserRepository(private val userDao: UserDao) {
    val loggedInUser: Flow<User?> = userDao.getLoggedInUser()

    suspend fun login(nickname: String, password: String): User? {
        return userDao.login(nickname, password)
    }

    suspend fun signUp(user: User) {
        userDao.insertUser(user)
    }

    suspend fun logout() {
        userDao.logout()
    }
}
