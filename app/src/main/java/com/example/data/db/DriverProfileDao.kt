package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.DriverProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface DriverProfileDao {

    @Query("SELECT * FROM driver_profile WHERE id = 1 LIMIT 1")
    fun getProfile(): Flow<DriverProfile?>

    @Query("SELECT * FROM driver_profile WHERE id = 1 LIMIT 1")
    suspend fun getProfileDirect(): DriverProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: DriverProfile)
}
