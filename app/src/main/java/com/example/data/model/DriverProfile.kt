package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Driver and vehicle default profile settings.
 */
@Entity(tableName = "driver_profile")
data class DriverProfile(
    @PrimaryKey
    val id: Int = 1,
    val driverName: String = "Salih Kaya",
    val driverTitle: String = "",
    val vehiclePlate: String = "41 SN 561",
    val vehicleModel: String = "Mitsubishi L200",
    val institutionName: String = "T.C. Orman Genel Müdürlüğü",
    val departmentName: String = "57 Nolu Orman Kadastro Başmühendisliği",
    val chiefEngineerName: String = "",
    val photoUri: String? = null
)
