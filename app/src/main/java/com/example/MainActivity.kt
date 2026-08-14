package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.DutyRecord
import com.example.ui.screens.ArchiveScreen
import com.example.ui.screens.DutyDetailDialog
import com.example.ui.screens.DutyFormScreen
import com.example.ui.screens.FormMode
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ProfileDialog
import com.example.ui.theme.ForestGoldDark
import com.example.ui.theme.ForestGreenDark
import com.example.ui.theme.ForestGreenPale
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.DutyViewModel
import com.example.ui.viewmodel.UiEvent
import com.example.util.PdfReportGenerator
import kotlinx.coroutines.flow.collectLatest

sealed interface Screen {
    object Home : Screen
    data class DutyForm(val mode: FormMode, val existingDuty: DutyRecord? = null) : Screen
    object Archive : Screen
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var isDarkTheme by remember { mutableStateOf(true) } // Default to user-requested green dark theme
            MyApplicationTheme(darkTheme = isDarkTheme) {
                MainAppContent(
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = { isDarkTheme = !isDarkTheme }
                )
            }
        }
    }
}

@Composable
fun MainAppContent(
    dutyViewModel: DutyViewModel = viewModel(),
    isDarkTheme: Boolean = true,
    onToggleTheme: () -> Unit = {}
) {
    val context = LocalContext.current

    val activeDuty by dutyViewModel.activeDuty.collectAsStateWithLifecycle()
    val driverProfile by dutyViewModel.driverProfile.collectAsStateWithLifecycle()
    val completedDuties by dutyViewModel.allCompletedDuties.collectAsStateWithLifecycle()
    val filteredDuties by dutyViewModel.filteredDuties.collectAsStateWithLifecycle()
    val prefillData by dutyViewModel.prefillState.collectAsStateWithLifecycle()
    val selectedYear by dutyViewModel.selectedYear.collectAsStateWithLifecycle()
    val selectedMonth by dutyViewModel.selectedMonth.collectAsStateWithLifecycle()

    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
    var showProfileDialog by remember { mutableStateOf(false) }
    var selectedDutyForDetail by remember { mutableStateOf<DutyRecord?>(null) }
    var completedDutyPrompt by remember { mutableStateOf<DutyRecord?>(null) }

    // Listen to ViewModel events (Toasts & PDF Prompt)
    LaunchedEffect(Unit) {
        dutyViewModel.eventFlow.collectLatest { event ->
            when (event) {
                is UiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                is UiEvent.DutyCompleted -> {
                    completedDutyPrompt = event.duty
                }
                is UiEvent.DutyStarted -> {
                    // Handled
                }
            }
        }
    }

    // Back handler
    BackHandler(enabled = currentScreen !is Screen.Home) {
        currentScreen = Screen.Home
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        when (val screen = currentScreen) {
            is Screen.Home -> {
                HomeScreen(
                    driverProfile = driverProfile,
                    activeDuty = activeDuty,
                    completedDuties = completedDuties,
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = onToggleTheme,
                    onOpenNewDutyForm = {
                        dutyViewModel.refreshPrefillData()
                        currentScreen = Screen.DutyForm(FormMode.NEW_DUTY)
                    },
                    onOpenEditActiveDuty = { duty ->
                        currentScreen = Screen.DutyForm(FormMode.RETURN_ACTIVE, duty)
                    },
                    onOpenArchive = {
                        currentScreen = Screen.Archive
                    },
                    onOpenProfileSettings = {
                        showProfileDialog = true
                    },
                    onViewDutyDetails = { duty ->
                        selectedDutyForDetail = duty
                    },
                    onDeleteActiveDuty = { duty ->
                        dutyViewModel.deleteDuty(duty)
                    }
                )
            }

            is Screen.DutyForm -> {
                DutyFormScreen(
                    formMode = screen.mode,
                    existingDuty = screen.existingDuty,
                    prefillData = prefillData,
                    driverProfile = driverProfile,
                    onNavigateBack = { currentScreen = Screen.Home },
                    onSaveNewDuty = { date, startTime, startKm, dutyType, destination, p1Name, p1Title, p2Name, p2Title, p3Name, p3Title, notes ->
                        dutyViewModel.startNewDuty(
                            date, startTime, startKm, dutyType, destination,
                            p1Name, p1Title, p2Name, p2Title, p3Name, p3Title, notes
                        )
                    },
                    onFinishActiveDuty = { duty, endKm, endTime, notes ->
                        dutyViewModel.finishActiveDuty(duty, endKm, endTime, notes)
                    },
                    onUpdateExistingDuty = { duty ->
                        dutyViewModel.updateDuty(duty)
                    },
                    onDeleteDuty = { duty ->
                        dutyViewModel.deleteDuty(duty)
                    }
                )
            }

            is Screen.Archive -> {
                ArchiveScreen(
                    duties = filteredDuties,
                    selectedYear = selectedYear,
                    selectedMonth = selectedMonth,
                    driverProfile = driverProfile,
                    onSelectMonth = { month -> dutyViewModel.setMonthFilter(month) },
                    onSelectYear = { year -> dutyViewModel.setYearFilter(year) },
                    onDutyClick = { duty -> selectedDutyForDetail = duty },
                    onNavigateBack = { currentScreen = Screen.Home }
                )
            }
        }
    }

    // Driver & Vehicle Profile Edit Dialog
    if (showProfileDialog) {
        ProfileDialog(
            currentProfile = driverProfile,
            onDismiss = { showProfileDialog = false },
            onSave = { driverName, plate, model, photoUri ->
                dutyViewModel.updateDriverProfile(
                    driverName = driverName,
                    plate = plate,
                    model = model,
                    driverTitle = "",
                    chiefEngineer = "",
                    photoUri = photoUri,
                    keepExistingPhoto = false
                )
            }
        )
    }

    // Duty Detail / PDF Sharing Dialog
    selectedDutyForDetail?.let { duty ->
        DutyDetailDialog(
            duty = duty,
            profile = driverProfile,
            onDismiss = { selectedDutyForDetail = null },
            onEdit = { dutyToEdit ->
                currentScreen = Screen.DutyForm(FormMode.EDIT_EXISTING, dutyToEdit)
            },
            onDelete = { dutyToDelete ->
                dutyViewModel.deleteDuty(dutyToDelete)
            }
        )
    }

    // Instant PDF export prompt upon duty completion
    completedDutyPrompt?.let { duty ->
        AlertDialog(
            onDismissRequest = { completedDutyPrompt = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ForestGreenPrimary, modifier = Modifier.size(24.dp))
                    Text("Görev Tamamlandı!", fontWeight = FontWeight.Bold, color = ForestGreenDark)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Taşıt görevi başarıyla kaydedildi ve arşivlendi.")
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ForestGreenPale,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Tarih: ${duty.date}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("Güzergah: ${duty.destination}", fontSize = 13.sp)
                            val net = duty.netKm ?: if (duty.endKm != null) (duty.endKm - duty.startKm) else 0
                            Text("Yapılan Mesafe: $net KM (${duty.startKm} -> ${duty.endKm})", fontWeight = FontWeight.Bold, color = ForestGreenDark, fontSize = 13.sp)
                        }
                    }
                    Text("Resmi Taşıt Görev Fişini PDF olarak indirmek veya paylaşmak ister misiniz?", fontSize = 13.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val completed = completedDutyPrompt
                        completedDutyPrompt = null
                        if (completed != null) {
                            val pdf = PdfReportGenerator.generateDutySlipPdf(context, completed, driverProfile)
                            if (pdf != null) {
                                PdfReportGenerator.sharePdfFile(context, pdf)
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("PDF Fişini Paylaş / Yazdır")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { completedDutyPrompt = null },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Daha Sonra")
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}
