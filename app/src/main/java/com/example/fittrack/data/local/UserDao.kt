package com.example.fittrack.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.fittrack.model.User
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE isLoggedIn = 1 LIMIT 1")
    fun getLoggedInUser(): Flow<User?>

    @Query("SELECT * FROM users WHERE nickname = :nickname LIMIT 1")
    suspend fun getByNickname(nickname: String): User?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: User)

    /** Marks [id] as the logged-in user and every other account as logged out, in one statement. */
    @Query("UPDATE users SET isLoggedIn = (id = :id)")
    suspend fun setLoggedInUser(id: UUID)

    @Query("UPDATE users SET isLoggedIn = 0")
    suspend fun logout()
}
