package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.DriverProfile
import com.example.data.model.DutyRecord
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE driver_profile ADD COLUMN photoUri TEXT DEFAULT NULL")
    }
}

@Database(
    entities = [DutyRecord::class, DriverProfile::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun dutyDao(): DutyDao
    abstract fun driverProfileDao(): DriverProfileDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ogm_duty_database.db"
                )
                .addMigrations(MIGRATION_1_2)
                .fallbackToDestructiveMigration()
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Pre-populate default driver profile
                        CoroutineScope(Dispatchers.IO).launch {
                            getDatabase(context).driverProfileDao().insertOrUpdateProfile(
                                DriverProfile(
                                    id = 1,
                                    driverName = "Salih Kaya",
                                    driverTitle = "",
                                    vehiclePlate = "41 SN 561",
                                    vehicleModel = "Mitsubishi L200",
                                    institutionName = "T.C. Orman Genel Müdürlüğü",
                                    departmentName = "57 Nolu Orman Kadastro Başmühendisliği",
                                    chiefEngineerName = "",
                                    photoUri = null
                                )
                            )
                        }
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
