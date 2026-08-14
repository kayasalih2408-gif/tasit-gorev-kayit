package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.DutyRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface DutyDao {

    @Query("SELECT * FROM duty_records ORDER BY dateTimestamp DESC, id DESC")
    fun getAllDuties(): Flow<List<DutyRecord>>

    @Query("SELECT * FROM duty_records WHERE isCompleted = 1 ORDER BY dateTimestamp DESC, id DESC")
    fun getAllCompletedDuties(): Flow<List<DutyRecord>>

    @Query("SELECT * FROM duty_records WHERE isCompleted = 0 LIMIT 1")
    fun getActiveDuty(): Flow<DutyRecord?>

    @Query("SELECT * FROM duty_records WHERE isCompleted = 1 ORDER BY dateTimestamp DESC, id DESC LIMIT 1")
    suspend fun getLatestCompletedDuty(): DutyRecord?

    @Query("SELECT * FROM duty_records WHERE id = :id LIMIT 1")
    suspend fun getDutyById(id: Long): DutyRecord?

    @Query("SELECT * FROM duty_records WHERE isCompleted = 1 AND year = :year AND month = :month ORDER BY dateTimestamp DESC, id DESC")
    fun getDutiesByMonth(year: Int, month: Int): Flow<List<DutyRecord>>

    @Query("SELECT * FROM duty_records WHERE isCompleted = 1 AND year = :year ORDER BY dateTimestamp DESC, id DESC")
    fun getDutiesByYear(year: Int): Flow<List<DutyRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDuty(duty: DutyRecord): Long

    @Update
    suspend fun updateDuty(duty: DutyRecord)

    @Delete
    suspend fun deleteDuty(duty: DutyRecord)

    @Query("DELETE FROM duty_records WHERE id = :id")
    suspend fun deleteDutyById(id: Long)

    @Query("SELECT COUNT(*) FROM duty_records WHERE year = :year AND month = :month")
    suspend fun getCountByMonth(year: Int, month: Int): Int
}
