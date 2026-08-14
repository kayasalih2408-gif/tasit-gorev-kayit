package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing an official Vehicle Duty Record (Taşıt Görev Kaydı)
 * for Orman Genel Müdürlüğü 57 Nolu Orman Kadastro Başmühendisliği.
 */
@Entity(tableName = "duty_records")
data class DutyRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val recordNumber: String = "", // e.g. "2026/08-001"
    val date: String, // "DD.MM.YYYY" e.g. "14.08.2026"
    val dateTimestamp: Long, // timestamp for sorting and date range queries
    val year: Int,
    val month: Int, // 1 to 12
    
    // Vehicle & Driver snapshot at time of duty
    val driverName: String,
    val vehiclePlate: String,
    val vehicleModel: String,
    val institutionName: String = "T.C. Orman Genel Müdürlüğü",
    val departmentName: String = "57 Nolu Orman Kadastro Başmühendisliği",
    
    // Mission details
    val dutyType: String, // Görev Türü / Konusu (örn: Kadastro Sınır Tespiti)
    val destination: String, // Görev Yeri / Güzergah (örn: Boyabat Orman İşletmesi)
    
    // Delegation / 3 Personnel
    val personnel1Name: String = "",
    val personnel1Title: String = "",
    val personnel2Name: String = "",
    val personnel2Title: String = "",
    val personnel3Name: String = "",
    val personnel3Title: String = "",
    
    // Departure & Return details
    val startKm: Int, // Çıkış Kilometresi
    val startTime: String, // Çıkış Saati "HH:mm"
    val endKm: Int? = null, // Dönüş Kilometresi (null when ongoing)
    val endTime: String? = null, // Dönüş Saati (null when ongoing)
    val netKm: Int? = null, // Hesaplanan net km
    
    // Status & Notes
    val isCompleted: Boolean = false, // false = Aktif Görevde, true = Tamamlandı
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)
