package com.example.fittrack.data.model

import com.example.fittrack.data.local.UserDao
import com.example.fittrack.model.User
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class UserRepository(private val userDao: UserDao) {
    val loggedInUser: Flow<User?> = userDao.getLoggedInUser()

    suspend fun findByNickname(nickname: String): User? = userDao.getByNickname(nickname)

    /** Stores a new account and logs it in. */
    suspend fun signUp(user: User) {
        userDao.insertUser(user.copy(isLoggedIn = false))
        userDao.setLoggedInUser(user.id)
    }

    suspend fun setLoggedIn(id: UUID) {
        userDao.setLoggedInUser(id)
    }

    suspend fun logout() {
        userDao.logout()
    }
}
