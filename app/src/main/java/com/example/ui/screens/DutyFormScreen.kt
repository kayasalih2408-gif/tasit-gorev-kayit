package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DriverProfile
import com.example.data.model.DutyRecord
import com.example.ui.theme.SleekBorder
import com.example.ui.theme.SleekGreenDark
import com.example.ui.theme.SleekGreenPale
import com.example.ui.theme.SleekGreenPrimary
import com.example.ui.theme.SleekGreenSurface
import com.example.ui.theme.SleekSlate100
import com.example.ui.theme.SleekSlate200
import com.example.ui.theme.SleekSlate50
import com.example.ui.theme.SleekSlate500
import com.example.ui.theme.SleekSlate600
import com.example.ui.theme.SleekSlate700
import com.example.ui.theme.SleekSlate800
import com.example.ui.theme.SleekSlate900
import com.example.ui.theme.SleekTextDark
import com.example.ui.viewmodel.PrefillData
import com.example.util.DateUtils

enum class FormMode {
    NEW_DUTY,          // Start a new duty
    RETURN_ACTIVE,     // Return / finish an active ongoing duty
    EDIT_EXISTING      // Edit an existing completed duty record
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DutyFormScreen(
    formMode: FormMode,
    existingDuty: DutyRecord? = null,
    prefillData: PrefillData,
    driverProfile: DriverProfile,
    onNavigateBack: () -> Unit,
    onSaveNewDuty: (
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
    ) -> Unit,
    onFinishActiveDuty: (
        duty: DutyRecord,
        endKm: Int,
        endTime: String,
        notes: String
    ) -> Unit,
    onUpdateExistingDuty: (DutyRecord) -> Unit,
    onDeleteDuty: ((DutyRecord) -> Unit)? = null
) {
    val context = LocalContext.current
    var showDeleteConfirm by remember { mutableStateOf(false) }

    // State initialization based on mode
    var date by remember {
        mutableStateOf(
            existingDuty?.date
                ?: prefillData.suggestedDate.ifBlank { DateUtils.getCurrentDateString() }
        )
    }
    var startTime by remember {
        mutableStateOf(
            existingDuty?.startTime
                ?: prefillData.suggestedStartTime.ifBlank { DateUtils.getCurrentTimeString() }
        )
    }
    var startKmText by remember {
        mutableStateOf(
            existingDuty?.startKm?.toString()
                ?: if (prefillData.suggestedStartKm > 0) prefillData.suggestedStartKm.toString() else ""
        )
    }

    var endTime by remember {
        mutableStateOf(
            existingDuty?.endTime
                ?: if (formMode == FormMode.RETURN_ACTIVE) DateUtils.getCurrentTimeString() else ""
        )
    }
    var endKmText by remember {
        mutableStateOf(
            existingDuty?.endKm?.toString() ?: ""
        )
    }

    var dutyType by remember {
        mutableStateOf(
            existingDuty?.dutyType
                ?: prefillData.suggestedDutyType.ifBlank { "Orman Kadastro Sahası İnceleme" }
        )
    }
    var destination by remember {
        mutableStateOf(
            existingDuty?.destination
                ?: prefillData.suggestedDestination
        )
    }

    // 3 Personnels
    var p1Name by remember {
        mutableStateOf(existingDuty?.personnel1Name ?: prefillData.suggestedP1Name)
    }
    var p1Title by remember {
        mutableStateOf(
            existingDuty?.personnel1Title
                ?: prefillData.suggestedP1Title.ifBlank { "Mühendis" }
        )
    }

    var p2Name by remember {
        mutableStateOf(existingDuty?.personnel2Name ?: prefillData.suggestedP2Name)
    }
    var p2Title by remember {
        mutableStateOf(
            existingDuty?.personnel2Title
                ?: prefillData.suggestedP2Title.ifBlank { "Kadastro Teknisyeni" }
        )
    }

    var p3Name by remember {
        mutableStateOf(existingDuty?.personnel3Name ?: prefillData.suggestedP3Name)
    }
    var p3Title by remember {
        mutableStateOf(
            existingDuty?.personnel3Title
                ?: prefillData.suggestedP3Title.ifBlank { "Orman Muhafaza Memuru" }
        )
    }

    var notes by remember {
        mutableStateOf(existingDuty?.notes ?: "")
    }

    // Live Net KM Calculation
    val startKmVal = startKmText.toIntOrNull() ?: 0
    val endKmVal = endKmText.toIntOrNull()
    val calculatedNetKm by remember(startKmVal, endKmVal) {
        derivedStateOf {
            if (endKmVal != null && endKmVal >= startKmVal) {
                endKmVal - startKmVal
            } else {
                null
            }
        }
    }

    val quickDutyTypes = listOf(
        "Orman Kadastro Sınır Tespiti",
        "Kadastro Parsel Ölçümü",
        "Bilirkişi / Keşif İncelemesi",
        "Orman İçi Yol Kontrolü",
        "İdari / Resmi Hizmet Görevi"
    )

    // Delete Confirmation Dialog
    if (showDeleteConfirm && existingDuty != null && onDeleteDuty != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = Color(0xFFEF4444))
                    Text("Görevi İptal Et / Sil", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp)
                }
            },
            text = {
                Text(
                    "Bu görev kaydı (${existingDuty.date} - ${existingDuty.destination}) kalıcı olarak silinecektir. Emin misiniz?",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteDuty(existingDuty)
                        showDeleteConfirm = false
                        Toast.makeText(context, "Görev kaydı silindi.", Toast.LENGTH_SHORT).show()
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("confirm_delete_duty_form_button")
                ) {
                    Text("Evet, Görevi Sil", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Vazgeç", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = when (formMode) {
                                FormMode.NEW_DUTY -> "Yeni Görev Başlat (Çıkış)"
                                FormMode.RETURN_ACTIVE -> "Görevi Tamamla (Dönüş)"
                                FormMode.EDIT_EXISTING -> "Görev Kaydını Düzenle"
                            },
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "57 Nolu Orman Kadastro",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Geri",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    if (existingDuty != null && onDeleteDuty != null) {
                        IconButton(
                            onClick = { showDeleteConfirm = true },
                            modifier = Modifier.testTag("delete_duty_top_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Görevi İptal Et / Sil",
                                tint = Color(0xFFEF4444)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    when (formMode) {
                        FormMode.NEW_DUTY -> {
                            Button(
                                onClick = {
                                    val startKm = startKmText.toIntOrNull()
                                    if (startKm == null || startKm <= 0) {
                                        Toast.makeText(context, "Lütfen geçerli bir Çıkış Kilometresi giriniz.", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    if (destination.isBlank()) {
                                        Toast.makeText(context, "Lütfen Gidilecek Yer / Güzergah belirtiniz.", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }

                                    onSaveNewDuty(
                                        date,
                                        startTime,
                                        startKm,
                                        dutyType,
                                        destination,
                                        p1Name,
                                        p1Title,
                                        p2Name,
                                        p2Title,
                                        p3Name,
                                        p3Title,
                                        notes
                                    )
                                    onNavigateBack()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("start_duty_submit_button")
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Görevi Başlat / Kaydet",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        FormMode.RETURN_ACTIVE -> {
                            Button(
                                onClick = {
                                    val endKm = endKmText.toIntOrNull()
                                    if (endKm == null || endKm <= 0) {
                                        Toast.makeText(context, "Lütfen geçerli bir Dönüş Kilometresi giriniz.", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    if (existingDuty != null && endKm < existingDuty.startKm) {
                                        Toast.makeText(context, "Dönüş kilometresi çıkış kilometresinden (${existingDuty.startKm} km) küçük olamaz!", Toast.LENGTH_LONG).show()
                                        return@Button
                                    }

                                    if (existingDuty != null) {
                                        onFinishActiveDuty(
                                            existingDuty,
                                            endKm,
                                            endTime.ifBlank { DateUtils.getCurrentTimeString() },
                                            notes
                                        )
                                        onNavigateBack()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("finish_duty_submit_button")
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Sona Eren Görevi Kaydet & Arşivle",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        FormMode.EDIT_EXISTING -> {
                            Button(
                                onClick = {
                                    val startKm = startKmText.toIntOrNull() ?: 0
                                    val endKm = endKmText.toIntOrNull()

                                    if (existingDuty != null) {
                                        val updated = existingDuty.copy(
                                            date = date,
                                            startTime = startTime,
                                            startKm = startKm,
                                            endTime = if (endTime.isNotBlank()) endTime else existingDuty.endTime,
                                            endKm = endKm ?: existingDuty.endKm,
                                            netKm = if (endKm != null) (endKm - startKm).coerceAtLeast(0) else existingDuty.netKm,
                                            dutyType = dutyType,
                                            destination = destination,
                                            personnel1Name = p1Name,
                                            personnel1Title = p1Title,
                                            personnel2Name = p2Name,
                                            personnel2Title = p2Title,
                                            personnel3Name = p3Name,
                                            personnel3Title = p3Title,
                                            notes = notes
                                        )
                                        onUpdateExistingDuty(updated)
                                        onNavigateBack()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("update_duty_submit_button")
                            ) {
                                Icon(Icons.Default.Save, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Değişiklikleri Kaydet", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (existingDuty != null && onDeleteDuty != null) {
                        TextButton(
                            onClick = { showDeleteConfirm = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("cancel_duty_form_text_button")
                        ) {
                            Icon(
                                Icons.Default.DeleteOutline,
                                contentDescription = null,
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Yanlışlıkla mı Eklendi? Bu Görevi İptal Et / Sil",
                                color = Color(0xFFEF4444),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Vehicle & Driver Active Banner
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.DirectionsCar,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = driverProfile.vehiclePlate,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${driverProfile.vehicleModel} • ${driverProfile.driverName}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (formMode == FormMode.NEW_DUTY && prefillData.suggestedStartKm > 0) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "Son KM: ${prefillData.suggestedStartKm}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // SECTION: DÖNÜŞ İŞLEMİ ÖZEL ALANI (Eğer Aktif Görevi Dönüş Olarak Kapatıyorsak En Üstte Vurgula)
            if (formMode == FormMode.RETURN_ACTIVE) {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Speed, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text(
                                text = "Görev Dönüş Bilgileri",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Text(
                            text = "Çıkış Kilometresi: ${existingDuty?.startKm ?: startKmVal} km (Çıkış Saati: ${existingDuty?.startTime ?: startTime})",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = endKmText,
                                onValueChange = { endKmText = it.filter { ch -> ch.isDigit() } },
                                label = { Text("Dönüş Kilometresi *") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .weight(1.3f)
                                    .testTag("end_km_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )

                            OutlinedTextField(
                                value = endTime,
                                onValueChange = { endTime = it },
                                label = { Text("Dönüş Saati") },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("end_time_input"),
                                trailingIcon = {
                                    IconButton(onClick = { endTime = DateUtils.getCurrentTimeString() }) {
                                        Icon(Icons.Default.AccessTime, contentDescription = "Şimdi", tint = MaterialTheme.colorScheme.primary)
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )
                        }

                        // Live Net KM Display Box
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Yapılan Net Mesafe:",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Text(
                                    text = if (calculatedNetKm != null) "$calculatedNetKm KM" else "Hesaplanıyor...",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }
                }
            }

            // SECTION 1: ÇIKIŞ KİLOMETRESİ VE ZAMAN
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "1. Tarih, Çıkış Saati ve Kilometresi",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = date,
                            onValueChange = { date = it },
                            label = { Text("Tarih (GG.AA.YYYY)") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1.2f)
                                .testTag("date_input"),
                            leadingIcon = {
                                Icon(Icons.Default.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                focusedLabelColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )

                        OutlinedTextField(
                            value = startTime,
                            onValueChange = { startTime = it },
                            label = { Text("Çıkış Saati") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("start_time_input"),
                            trailingIcon = {
                                IconButton(onClick = { startTime = DateUtils.getCurrentTimeString() }) {
                                    Icon(Icons.Default.AccessTime, contentDescription = "Şimdi", tint = MaterialTheme.colorScheme.primary)
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                focusedLabelColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )
                    }

                    OutlinedTextField(
                        value = startKmText,
                        onValueChange = { startKmText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Çıkış Kilometresi (Başlangıç KM) *") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        leadingIcon = {
                            Icon(Icons.Default.Speed, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        supportingText = {
                            if (formMode == FormMode.NEW_DUTY) {
                                Text("Önceki görevin bitiş kilometresinden otomatik aktarıldı.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("start_km_input")
                    )
                }
            }

            // SECTION 2: GÖREV KONUSU VE GÜZERGAH
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "2. Görev Konusu ve Gidilecek Yer",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = destination,
                        onValueChange = { destination = it },
                        label = { Text("Gidilecek Yer / Güzergah *") },
                        placeholder = { Text("Örn: Boyabat Orman İşletmesi / Çangal Sahası") },
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("destination_input")
                    )

                    OutlinedTextField(
                        value = dutyType,
                        onValueChange = { dutyType = it },
                        label = { Text("Görev Türü / Amacı") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("duty_type_input")
                    )

                    // Quick Selection Chips
                    Text(
                        text = "Hızlı Seçim:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        quickDutyTypes.forEach { quickType ->
                            SuggestionChip(
                                onClick = { dutyType = quickType },
                                label = { Text(quickType, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface) },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = if (dutyType == quickType) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                ),
                                border = SuggestionChipDefaults.suggestionChipBorder(
                                    borderColor = if (dutyType == quickType) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                    enabled = true
                                )
                            )
                        }
                    }
                }
            }

            // SECTION 3: GÖREVLİ HEYET (3 PERSONEL BİLGİSİ)
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "3. Görevli Heyet (3 Personel)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Son görevden aktarıldı",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Personel 1
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("1. Personel (Heyet Başkanı / Mühendis)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = p1Name,
                                onValueChange = { p1Name = it },
                                label = { Text("Adı Soyadı") },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1.3f)
                                    .testTag("p1_name_input"),
                                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )
                            OutlinedTextField(
                                value = p1Title,
                                onValueChange = { p1Title = it },
                                label = { Text("Unvanı") },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("p1_title_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    // Personel 2
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("2. Personel (Teknik Personel)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = p2Name,
                                onValueChange = { p2Name = it },
                                label = { Text("Adı Soyadı") },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1.3f)
                                    .testTag("p2_name_input"),
                                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )
                            OutlinedTextField(
                                value = p2Title,
                                onValueChange = { p2Title = it },
                                label = { Text("Unvanı") },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("p2_title_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    // Personel 3
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("3. Personel (Muhafaza Memuru / Üye)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = p3Name,
                                onValueChange = { p3Name = it },
                                label = { Text("Adı Soyadı") },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1.3f)
                                    .testTag("p3_name_input"),
                                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )
                            OutlinedTextField(
                                value = p3Title,
                                onValueChange = { p3Title = it },
                                label = { Text("Unvanı") },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("p3_title_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )
                        }
                    }
                }
            }

            // SECTION 4: NOTLAR
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "4. Notlar ve Açıklama (İsteğe Bağlı)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Açıklama / Yakıt / Güzergah Notu") },
                        placeholder = { Text("Örn: Arazi şartları çamurlu, 4x4 mod kullanıldı.") },
                        minLines = 2,
                        maxLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("notes_input")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
