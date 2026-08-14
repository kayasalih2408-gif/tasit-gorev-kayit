package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.DriverProfile
import com.example.data.model.DutyRecord
import com.example.data.repository.DutyRepository
import com.example.util.DateUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PrefillData(
    val suggestedStartKm: Int = 0,
    val suggestedDate: String = "",
    val suggestedStartTime: String = "",
    val suggestedDutyType: String = "",
    val suggestedDestination: String = "",
    val suggestedP1Name: String = "",
    val suggestedP1Title: String = "",
    val suggestedP2Name: String = "",
    val suggestedP2Title: String = "",
    val suggestedP3Name: String = "",
    val suggestedP3Title: String = ""
)

sealed class UiEvent {
    data class ShowToast(val message: String) : UiEvent()
    data class DutyCompleted(val duty: DutyRecord) : UiEvent()
    data class DutyStarted(val duty: DutyRecord) : UiEvent()
}

@OptIn(ExperimentalCoroutinesApi::class)
class DutyViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: DutyRepository

    init {
        val db = AppDatabase.getDatabase(application)
        repository = DutyRepository(db.dutyDao(), db.driverProfileDao())

        // Ensure default profile is synchronized with user preference
        viewModelScope.launch {
            val profile = repository.getProfileDirect()
            if (profile == null) {
                repository.updateDriverProfile(
                    DriverProfile(
                        id = 1,
                        driverName = "Salih Kaya",
                        driverTitle = "",
                        vehiclePlate = "41 SN 561",
                        vehicleModel = "Mitsubishi L200",
                        chiefEngineerName = "",
                        photoUri = null
                    )
                )
            } else if (profile.vehiclePlate == "57 OG 057" || profile.vehicleModel == "Dacia Duster 4x4" || profile.driverTitle == "Taşıt Şoförü" || profile.chiefEngineerName == "Orman Kadastro Başmühendisi") {
                repository.updateDriverProfile(
                    profile.copy(
                        vehiclePlate = if (profile.vehiclePlate == "57 OG 057") "41 SN 561" else profile.vehiclePlate,
                        vehicleModel = if (profile.vehicleModel == "Dacia Duster 4x4") "Mitsubishi L200" else profile.vehicleModel,
                        driverTitle = if (profile.driverTitle == "Taşıt Şoförü") "" else profile.driverTitle,
                        chiefEngineerName = if (profile.chiefEngineerName == "Orman Kadastro Başmühendisi") "" else profile.chiefEngineerName
                    )
                )
            }
        }
    }

    // Active duty flow
    val activeDuty: StateFlow<DutyRecord?> = repository.activeDuty.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    // Driver & vehicle profile
    val driverProfile: StateFlow<DriverProfile> = repository.driverProfile.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DriverProfile()
    ).let { flow ->
        // Transform nullable to non-null with fallback
        MutableStateFlow(DriverProfile()).also { fallbackFlow ->
            viewModelScope.launch {
                repository.driverProfile.collect { profile ->
                    fallbackFlow.value = profile ?: DriverProfile()
                }
            }
        }
    }

    // Monthly filter state
    val selectedYear = MutableStateFlow(DateUtils.getCurrentYear())
    val selectedMonth = MutableStateFlow(DateUtils.getCurrentMonth()) // 1-12, or 0 for all

    // Filtered duties for Archive Screen
    val filteredDuties: StateFlow<List<DutyRecord>> = combine(
        selectedYear,
        selectedMonth
    ) { year, month ->
        Pair(year, month)
    }.flatMapLatest { (year, month) ->
        repository.getDutiesByMonth(year, month)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // All completed duties for dashboard stats
    val allCompletedDuties: StateFlow<List<DutyRecord>> = repository.allCompletedDuties.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Event bus for notifications / PDF prompts
    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow: SharedFlow<UiEvent> = _eventFlow.asSharedFlow()

    // Form prefill state
    private val _prefillState = MutableStateFlow(PrefillData())
    val prefillState: StateFlow<PrefillData> = _prefillState.asStateFlow()

    fun refreshPrefillData() {
        viewModelScope.launch {
            val lastDuty = repository.getLatestCompletedDuty()
            val currentDate = DateUtils.getCurrentDateString()
            val currentTime = DateUtils.getCurrentTimeString()

            _prefillState.value = PrefillData(
                suggestedStartKm = lastDuty?.endKm ?: 0,
                suggestedDate = currentDate,
                suggestedStartTime = currentTime,
                suggestedDutyType = lastDuty?.dutyType ?: "Orman Kadastro Sahası İnceleme",
                suggestedDestination = lastDuty?.destination ?: "",
                suggestedP1Name = lastDuty?.personnel1Name ?: "",
                suggestedP1Title = lastDuty?.personnel1Title ?: "Mühendis",
                suggestedP2Name = lastDuty?.personnel2Name ?: "",
                suggestedP2Title = lastDuty?.personnel2Title ?: "Kadastro Teknisyeni",
                suggestedP3Name = lastDuty?.personnel3Name ?: "",
                suggestedP3Title = lastDuty?.personnel3Title ?: "Orman Muhafaza Memuru"
            )
        }
    }

    /**
     * Start / Save a new Duty (Puts vehicle in Active Duty status).
     */
    fun startNewDuty(
        date: String,
        startTime: String,
        startKm: Int,
        dutyType: String,
        destination: String,
        p1Name: String,
        p1Title: String,
        p2Name: String,
        p2Title: String,
        p3Name: String,
        p3Title: String,
        notes: String
    ) {
        viewModelScope.launch {
            val profile = repository.getProfileDirect() ?: DriverProfile()
            val year = DateUtils.getCurrentYear()
            val month = DateUtils.getCurrentMonth()
            val count = repository.getMonthlyDutyCount(year, month) + 1
            val recordNumber = DateUtils.generateDutyNumber(year, month, count)

            val newDuty = DutyRecord(
                recordNumber = recordNumber,
                date = date.ifBlank { DateUtils.getCurrentDateString() },
                dateTimestamp = DateUtils.parseDateToTimestamp(date),
                year = year,
                month = month,
                driverName = profile.driverName,
                vehiclePlate = profile.vehiclePlate,
                vehicleModel = profile.vehicleModel,
                institutionName = profile.institutionName,
                departmentName = profile.departmentName,
                dutyType = dutyType.ifBlank { "Orman Kadastro Görevi" },
                destination = destination.ifBlank { "Kadastro Sahası" },
                personnel1Name = p1Name,
                personnel1Title = p1Title,
                personnel2Name = p2Name,
                personnel2Title = p2Title,
                personnel3Name = p3Name,
                personnel3Title = p3Title,
                startKm = startKm,
                startTime = startTime.ifBlank { DateUtils.getCurrentTimeString() },
                endKm = null,
                endTime = null,
                netKm = null,
                isCompleted = false,
                notes = notes,
                createdAt = System.currentTimeMillis()
            )

            val id = repository.insertDuty(newDuty)
            val savedDuty = newDuty.copy(id = id)
            _eventFlow.emit(UiEvent.DutyStarted(savedDuty))
            _eventFlow.emit(UiEvent.ShowToast("Taşıt görevi başlatıldı. Araç aktif görevde."))
        }
    }

    /**
     * Complete an Active Duty (Return procedure).
     */
    fun finishActiveDuty(
        activeRecord: DutyRecord,
        endKm: Int,
        endTime: String,
        notes: String = activeRecord.notes
    ) {
        viewModelScope.launch {
            val calculatedNet = (endKm - activeRecord.startKm).coerceAtLeast(0)
            val completedRecord = activeRecord.copy(
                endKm = endKm,
                endTime = endTime.ifBlank { DateUtils.getCurrentTimeString() },
                netKm = calculatedNet,
                isCompleted = true,
                notes = notes,
                completedAt = System.currentTimeMillis()
            )

            repository.updateDuty(completedRecord)
            _eventFlow.emit(UiEvent.DutyCompleted(completedRecord))
            _eventFlow.emit(UiEvent.ShowToast("Görev başarıyla tamamlandı. Net KM: $calculatedNet"))
        }
    }

    /**
     * Edit / Update an existing duty record.
     */
    fun updateDuty(duty: DutyRecord) {
        viewModelScope.launch {
            val net = if (duty.endKm != null) (duty.endKm - duty.startKm).coerceAtLeast(0) else null
            val updated = duty.copy(
                netKm = net,
                dateTimestamp = DateUtils.parseDateToTimestamp(duty.date)
            )
            repository.updateDuty(updated)
            _eventFlow.emit(UiEvent.ShowToast("Görev kaydı güncellendi."))
        }
    }

    /**
     * Delete a duty record.
     */
    fun deleteDuty(duty: DutyRecord) {
        viewModelScope.launch {
            repository.deleteDuty(duty)
            _eventFlow.emit(UiEvent.ShowToast("Görev kaydı silindi."))
        }
    }

    /**
     * Update Driver & Vehicle profile settings.
     */
    fun updateDriverProfile(
        driverName: String,
        plate: String,
        model: String,
        driverTitle: String = "",
        chiefEngineer: String = "",
        photoUri: String? = null,
        keepExistingPhoto: Boolean = true
    ) {
        viewModelScope.launch {
            val current = repository.getProfileDirect() ?: DriverProfile()
            val targetPhoto = if (keepExistingPhoto && photoUri == null) current.photoUri else photoUri
            val updated = current.copy(
                driverName = driverName.ifBlank { current.driverName },
                driverTitle = driverTitle,
                vehiclePlate = plate.ifBlank { current.vehiclePlate },
                vehicleModel = model.ifBlank { current.vehicleModel },
                chiefEngineerName = chiefEngineer,
                photoUri = targetPhoto
            )
            repository.updateDriverProfile(updated)
            _eventFlow.emit(UiEvent.ShowToast("Şoför ve araç bilgileri güncellendi."))
        }
    }

    fun setMonthFilter(month: Int) {
        selectedMonth.value = month
    }

    fun setYearFilter(year: Int) {
        selectedYear.value = year
    }
}
