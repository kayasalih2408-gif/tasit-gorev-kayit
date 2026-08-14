package com.example.data.repository

import com.example.data.db.DriverProfileDao
import com.example.data.db.DutyDao
import com.example.data.model.DriverProfile
import com.example.data.model.DutyRecord
import kotlinx.coroutines.flow.Flow

class DutyRepository(
    private val dutyDao: DutyDao,
    private val driverProfileDao: DriverProfileDao
) {
    val allDuties: Flow<List<DutyRecord>> = dutyDao.getAllDuties()
    val allCompletedDuties: Flow<List<DutyRecord>> = dutyDao.getAllCompletedDuties()
    val activeDuty: Flow<DutyRecord?> = dutyDao.getActiveDuty()
    val driverProfile: Flow<DriverProfile?> = driverProfileDao.getProfile()

    fun getDutiesByMonth(year: Int, month: Int): Flow<List<DutyRecord>> {
        return if (month == 0) {
            dutyDao.getDutiesByYear(year)
        } else {
            dutyDao.getDutiesByMonth(year, month)
        }
    }

    suspend fun getLatestCompletedDuty(): DutyRecord? {
        return dutyDao.getLatestCompletedDuty()
    }

    suspend fun getDutyById(id: Long): DutyRecord? {
        return dutyDao.getDutyById(id)
    }

    suspend fun insertDuty(duty: DutyRecord): Long {
        return dutyDao.insertDuty(duty)
    }

    suspend fun updateDuty(duty: DutyRecord) {
        dutyDao.updateDuty(duty)
    }

    suspend fun deleteDuty(duty: DutyRecord) {
        dutyDao.deleteDuty(duty)
    }

    suspend fun deleteDutyById(id: Long) {
        dutyDao.deleteDutyById(id)
    }

    suspend fun getProfileDirect(): DriverProfile? {
        return driverProfileDao.getProfileDirect()
    }

    suspend fun updateDriverProfile(profile: DriverProfile) {
        driverProfileDao.insertOrUpdateProfile(profile)
    }

    suspend fun getMonthlyDutyCount(year: Int, month: Int): Int {
        return dutyDao.getCountByMonth(year, month)
    }
}
