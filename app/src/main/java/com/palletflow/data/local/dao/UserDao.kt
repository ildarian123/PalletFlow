package com.palletflow.data.local.dao

import androidx.room.*
import com.palletflow.data.local.entity.UserProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: UserProfile)

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserProfile?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: String): UserProfile?

    @Query("SELECT * FROM users WHERE email = :email AND passwordHash = :passwordHash LIMIT 1")
    suspend fun getUserByCredentials(email: String, passwordHash: String): UserProfile?

    @Query("SELECT COUNT(*) FROM users WHERE email = :email")
    suspend fun isEmailTaken(email: String): Int

    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<UserProfile>>
}