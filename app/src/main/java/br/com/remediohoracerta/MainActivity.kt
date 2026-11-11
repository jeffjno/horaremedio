package br.com.remediohoracerta

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.app.AlarmManager
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.os.LocaleListCompat
import br.com.remediohoracerta.attachments.PhotoCapture
import br.com.remediohoracerta.attachments.PhotoKind
import br.com.remediohoracerta.data.MedicationStorage
import br.com.remediohoracerta.core.i18n.SupportedLanguages
import br.com.remediohoracerta.domain.model.Medication
import br.com.remediohoracerta.domain.model.MedicationInputValidator
import br.com.remediohoracerta.reminder.AlarmScheduler
import br.com.remediohoracerta.reminder.ReminderNotifications
import br.com.remediohoracerta.ui.theme.RemedioHoraCertaTheme
import java.io.File
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ReminderNotifications.createChannel(this)
        MedicationStorage.load(this).forEach { medication ->
            AlarmScheduler.scheduleNext(this, medication)
        }
        enableEdgeToEdge()
        setContent {
            RemedioHoraCertaTheme {
                RemedioApp()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        MedicationStorage.load(this).forEach { medication ->
            AlarmScheduler.scheduleNext(this, medication)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RemedioApp() {
    val context = LocalContext.current
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { }
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var showLanguagePicker by rememberSaveable { mutableStateOf(false) }
    var showAddMedication by rememberSaveable { mutableStateOf(false) }
    var medications by remember { mutableStateOf(MedicationStorage.load(context)) }
    var medicationToDelete by remember { mutableStateOf<Medication?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (selectedTab == 0) {
                            stringResource(R.string.nav_today)
                        } else {
                            stringResource(R.string.nav_settings)
                        },
                        fontWeight = FontWeight.SemiBold,
                    )
                },
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_today)) },
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_settings)) },
                )
            }
        },
    ) { innerPadding ->
        if (selectedTab == 0) {
            TodayScreen(
                contentPadding = innerPadding,
                medications = medications,
                onAddMedication = { showAddMedication = true },
                onToggleActive = { medication ->
                    val activating = !medication.isActive
                    medications = MedicationStorage.setActive(
                        context,
                        medication.id,
                        activating,
                    )
                    if (activating) {
                        AlarmScheduler.scheduleNext(context, medication.copy(isActive = true))
                    } else {
                        AlarmScheduler.cancel(context, medication.id)
                        ReminderNotifications.cancel(context, medication.id)
                    }
                },
                onDelete = { medicationToDelete = it },
            )
        } else {
            SettingsScreen(innerPadding, onChooseLanguage = { showLanguagePicker = true })
        }
    }

    if (showLanguagePicker) {
        LanguagePicker(onDismiss = { showLanguagePicker = false })
    }

    if (showAddMedication) {
        AddMedicationDialog(
            onDismiss = { showAddMedication = false },
            onSave = { name, dosage, time, days, medicationPhoto, prescriptionPhoto ->
                val (medication, updated) = MedicationStorage.add(
                    context = context,
                    name = name,
                    dosage = dosage,
                    time = time,
                    daysOfWeek = days,
                    medicationPhotoPath = medicationPhoto,
                    prescriptionPhotoPath = prescriptionPhoto,
                )
                medications = updated
                AlarmScheduler.scheduleNext(context, medication)
                if (
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS,
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                showAddMedication = false
            },
        )
    }

    medicationToDelete?.let { medication ->
        DeleteMedicationDialog(
            medication = medication,
            onDismiss = { medicationToDelete = null },
            onConfirm = {
                val (removed, updated) = MedicationStorage.delete(context, medication.id)
                AlarmScheduler.cancel(context, medication.id)
                ReminderNotifications.cancel(context, medication.id)
                removed?.medicationPhotoPath?.let { File(it).delete() }
                removed?.prescriptionPhotoPath?.let { File(it).delete() }
                medications = updated
                medicationToDelete = null
            },
        )
    }
}

@Composable
private fun TodayScreen(
    contentPadding: PaddingValues,
    medications: List<Medication>,
    onAddMedication: () -> Unit,
    onToggleActive: (Medication) -> Unit,
    onDelete: (Medication) -> Unit,
) {
    val locale = LocalConfiguration.current.locales[0]
    val formattedDate = remember(locale) {
        LocalDate.now().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(locale))
    }
    val activeMedications = medications.filter(Medication::isActive)
    val inactiveMedications = medications.filterNot(Medication::isActive)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 20.dp,
            top = contentPadding.calculateTopPadding() + 12.dp,
            end = 20.dp,
            bottom = contentPadding.calculateBottomPadding() + 24.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Text(
                text = formattedDate.replaceFirstChar { it.titlecase(locale) },
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (medications.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                    ),
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Column {
                            Text(
                                text = stringResource(R.string.no_medications_title),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.no_medications_body),
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                    }
                }
            }
        } else {
            item {
                Text(
                    text = stringResource(R.string.active_medications),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            items(activeMedications, key = Medication::id) { medication ->
                MedicationCard(
                    medication = medication,
                    onToggleActive = { onToggleActive(medication) },
                    onDelete = { onDelete(medication) },
                )
            }
            if (inactiveMedications.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = stringResource(R.string.inactive_medications),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = stringResource(R.string.inactive_medications_description),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                items(inactiveMedications, key = Medication::id) { medication ->
                    MedicationCard(
                        medication = medication,
                        onToggleActive = { onToggleActive(medication) },
                        onDelete = { onDelete(medication) },
                    )
                }
            }
        }
        item {
            Button(
                onClick = onAddMedication,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.add_medication))
            }
        }
        item {
            Text(
                text = stringResource(R.string.medical_disclaimer),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun MedicationCard(
    medication: Medication,
    onToggleActive: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (medication.isActive) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
        ),
    ) {
        Column {
        Row(
            modifier = Modifier.padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            medication.medicationPhotoPath?.let { path ->
                PhotoThumbnail(
                    path = path,
                    contentDescription = stringResource(R.string.medication_photo),
                )
            }
            Text(
                text = medication.time,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Column(Modifier.weight(1f)) {
                Text(
                    text = medication.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = stringResource(
                        if (medication.isActive) {
                            R.string.medication_status_active
                        } else {
                            R.string.medication_status_inactive
                        },
                    ),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (medication.isActive) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    },
                )
                Text(
                    text = medication.dosage.ifBlank {
                        stringResource(R.string.dosage_not_informed)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = periodicityDescription(medication),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (medication.prescriptionPhotoPath != null) {
                    Text(
                        text = stringResource(R.string.prescription_attached),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
            HorizontalDivider()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onDelete) {
                    Text(
                        text = stringResource(R.string.delete),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                Spacer(Modifier.width(8.dp))
                OutlinedButton(onClick = onToggleActive) {
                    Text(
                        stringResource(
                            if (medication.isActive) {
                                R.string.deactivate
                            } else {
                                R.string.reactivate
                            },
                        ),
                    )
                }
            }
        }
    }
}

@Composable
private fun DeleteMedicationDialog(
    medication: Medication,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.delete_medication_title)) },
        text = {
            Text(stringResource(R.string.delete_medication_message, medication.name))
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(stringResource(R.string.delete))
            }
        },
    )
}

@Composable
private fun periodicityDescription(medication: Medication): String {
    val locale = LocalConfiguration.current.locales[0]
    return when (medication.daysOfWeek) {
        (1..7).toSet() -> stringResource(R.string.periodicity_daily)
        (1..5).toSet() -> stringResource(R.string.periodicity_weekdays)
        else -> medication.daysOfWeek.sorted().joinToString(", ") { day ->
            DayOfWeek.of(day).getDisplayName(TextStyle.SHORT, locale)
        }
    }
}

@Composable
private fun PhotoThumbnail(path: String, contentDescription: String) {
    val bitmap = remember(path) { android.graphics.BitmapFactory.decodeFile(path) }
    bitmap?.let {
        androidx.compose.foundation.Image(
            bitmap = it.asImageBitmap(),
            contentDescription = contentDescription,
            modifier = Modifier.size(56.dp),
            contentScale = ContentScale.Crop,
        )
    }
}

private enum class PeriodicityOption {
    DAILY,
    WEEKDAYS,
    CUSTOM,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddMedicationDialog(
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        dosage: String,
        time: String,
        daysOfWeek: Set<Int>,
        medicationPhotoPath: String?,
        prescriptionPhotoPath: String?,
    ) -> Unit,
) {
    val context = LocalContext.current
    val now = remember { LocalTime.now() }
    var name by rememberSaveable { mutableStateOf("") }
    var dosage by rememberSaveable { mutableStateOf("") }
    var hour by rememberSaveable { mutableIntStateOf(now.hour) }
    var minute by rememberSaveable { mutableIntStateOf(now.minute) }
    var periodicity by rememberSaveable { mutableStateOf(PeriodicityOption.DAILY.name) }
    var customDaysCsv by rememberSaveable { mutableStateOf("") }
    var showTimePicker by rememberSaveable { mutableStateOf(false) }
    var attemptedSave by rememberSaveable { mutableStateOf(false) }
    var medicationPhotoPath by rememberSaveable { mutableStateOf<String?>(null) }
    var prescriptionPhotoPath by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingMedicationPhoto by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingPrescriptionPhoto by rememberSaveable { mutableStateOf<String?>(null) }
    var cameraError by rememberSaveable { mutableStateOf(false) }

    val medicationCamera = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture(),
    ) { captured ->
        val path = pendingMedicationPhoto
        if (captured) medicationPhotoPath = path else path?.let { File(it).delete() }
        pendingMedicationPhoto = null
    }
    val prescriptionCamera = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture(),
    ) { captured ->
        val path = pendingPrescriptionPhoto
        if (captured) prescriptionPhotoPath = path else path?.let { File(it).delete() }
        pendingPrescriptionPhoto = null
    }

    val time = "%02d:%02d".format(hour, minute)
    val customDays = customDaysCsv.split(',').mapNotNull(String::toIntOrNull).toSet()
    val selectedDays = when (PeriodicityOption.valueOf(periodicity)) {
        PeriodicityOption.DAILY -> (1..7).toSet()
        PeriodicityOption.WEEKDAYS -> (1..5).toSet()
        PeriodicityOption.CUSTOM -> customDays
    }
    val nameInvalid = attemptedSave && !MedicationInputValidator.isNameValid(name)
    val daysInvalid = attemptedSave && selectedDays.isEmpty()

    fun capture(kind: PhotoKind) {
        runCatching {
            val file = PhotoCapture.createFile(context, kind)
            when (kind) {
                PhotoKind.MEDICATION -> {
                    pendingMedicationPhoto = file.absolutePath
                    medicationCamera.launch(PhotoCapture.contentUri(context, file))
                }
                PhotoKind.PRESCRIPTION -> {
                    pendingPrescriptionPhoto = file.absolutePath
                    prescriptionCamera.launch(PhotoCapture.contentUri(context, file))
                }
            }
        }.onFailure { cameraError = true }
    }

    fun dismissAndDiscardPhotos() {
        medicationPhotoPath?.let { File(it).delete() }
        prescriptionPhotoPath?.let { File(it).delete() }
        pendingMedicationPhoto?.let { File(it).delete() }
        pendingPrescriptionPhoto?.let { File(it).delete() }
        onDismiss()
    }

    AlertDialog(
        onDismissRequest = ::dismissAndDiscardPhotos,
        title = { Text(stringResource(R.string.add_medication_title)) },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 560.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.medication_name)) },
                    singleLine = true,
                    isError = nameInvalid,
                    supportingText = if (nameInvalid) {
                        { Text(stringResource(R.string.required_field)) }
                    } else {
                        null
                    },
                )
                OutlinedTextField(
                    value = dosage,
                    onValueChange = { dosage = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.dosage)) },
                    placeholder = { Text(stringResource(R.string.dosage_hint)) },
                    singleLine = true,
                )

                Text(stringResource(R.string.schedule_time), fontWeight = FontWeight.SemiBold)
                Button(onClick = { showTimePicker = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.choose_time, time))
                }

                Text(stringResource(R.string.periodicity), fontWeight = FontWeight.SemiBold)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    PeriodicityOption.entries.forEach { option ->
                        val label = when (option) {
                            PeriodicityOption.DAILY -> stringResource(R.string.periodicity_daily)
                            PeriodicityOption.WEEKDAYS -> stringResource(R.string.periodicity_weekdays)
                            PeriodicityOption.CUSTOM -> stringResource(R.string.periodicity_custom)
                        }
                        FilterChip(
                            selected = periodicity == option.name,
                            onClick = { periodicity = option.name },
                            label = { Text(label) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }

                if (periodicity == PeriodicityOption.CUSTOM.name) {
                    val locale = LocalConfiguration.current.locales[0]
                    Text(stringResource(R.string.select_days))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf((1..4).toList(), (5..7).toList()).forEach { rowDays ->
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                rowDays.forEach { day ->
                                    FilterChip(
                                        selected = day in customDays,
                                        onClick = {
                                            val updated = if (day in customDays) {
                                                customDays - day
                                            } else {
                                                customDays + day
                                            }
                                            customDaysCsv = updated.sorted().joinToString(",")
                                        },
                                        label = {
                                            Text(
                                                DayOfWeek.of(day).getDisplayName(
                                                    TextStyle.SHORT,
                                                    locale,
                                                ),
                                            )
                                        },
                                    )
                                }
                            }
                        }
                    }
                    if (daysInvalid) {
                        Text(
                            stringResource(R.string.select_at_least_one_day),
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }

                Text(stringResource(R.string.photos), fontWeight = FontWeight.SemiBold)
                PhotoCaptureRow(
                    label = stringResource(R.string.take_medication_photo),
                    path = medicationPhotoPath,
                    contentDescription = stringResource(R.string.medication_photo),
                    onCapture = { capture(PhotoKind.MEDICATION) },
                )
                PhotoCaptureRow(
                    label = stringResource(R.string.take_prescription_photo),
                    path = prescriptionPhotoPath,
                    contentDescription = stringResource(R.string.prescription_photo),
                    onCapture = { capture(PhotoKind.PRESCRIPTION) },
                )
                if (cameraError) {
                    Text(
                        stringResource(R.string.camera_unavailable),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        },
        dismissButton = {
            TextButton(onClick = ::dismissAndDiscardPhotos) {
                Text(stringResource(R.string.cancel))
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    attemptedSave = true
                    if (MedicationInputValidator.isNameValid(name) && selectedDays.isNotEmpty()) {
                        onSave(
                            name,
                            dosage,
                            time,
                            selectedDays,
                            medicationPhotoPath,
                            prescriptionPhotoPath,
                        )
                    }
                },
            ) {
                Text(stringResource(R.string.save))
            }
        },
    )

    if (showTimePicker) {
        MedicationTimePicker(
            initialHour = hour,
            initialMinute = minute,
            onDismiss = { showTimePicker = false },
            onConfirm = { selectedHour, selectedMinute ->
                hour = selectedHour
                minute = selectedMinute
                showTimePicker = false
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MedicationTimePicker(
    initialHour: Int,
    initialMinute: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit,
) {
    val state = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true,
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.schedule_time)) },
        text = { TimePicker(state = state) },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        },
        confirmButton = {
            Button(onClick = { onConfirm(state.hour, state.minute) }) {
                Text(stringResource(R.string.save))
            }
        },
    )
}

@Composable
private fun PhotoCaptureRow(
    label: String,
    path: String?,
    contentDescription: String,
    onCapture: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        path?.let {
            val bitmap = remember(path) { android.graphics.BitmapFactory.decodeFile(path) }
            bitmap?.let {
                androidx.compose.foundation.Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = contentDescription,
                    modifier = Modifier.size(72.dp),
                    contentScale = ContentScale.Crop,
                )
            }
        }
        Button(onClick = onCapture, modifier = Modifier.weight(1f)) {
            Text(if (path == null) label else stringResource(R.string.retake_photo))
        }
    }
}

@Composable
private fun SettingsScreen(
    contentPadding: PaddingValues,
    onChooseLanguage: () -> Unit,
) {
    val context = LocalContext.current
    val selectedTag = AppCompatDelegate.getApplicationLocales().get(0)?.toLanguageTag().orEmpty()
    val selectedName = SupportedLanguages.findByTag(selectedTag)?.autonym
        ?: stringResource(R.string.language_system)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 20.dp,
            top = contentPadding.calculateTopPadding() + 12.dp,
            end = 20.dp,
            bottom = contentPadding.calculateBottomPadding() + 24.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                text = stringResource(R.string.language_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
        item {
            Card(onClick = onChooseLanguage, modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "A/文",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = selectedName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                        )
                        Text(
                            text = stringResource(R.string.language_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            item {
                Card(
                    onClick = {
                        val intent = Intent(
                            Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                            Uri.parse("package:${context.packageName}"),
                        )
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Text(
                            text = stringResource(R.string.exact_alarm_permission_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = if (
                                context.getSystemService(AlarmManager::class.java)
                                    .canScheduleExactAlarms()
                            ) {
                                stringResource(R.string.exact_alarm_permission_ready)
                            } else {
                                stringResource(R.string.exact_alarm_permission_needed)
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LanguagePicker(onDismiss: () -> Unit) {
    val currentTag = AppCompatDelegate.getApplicationLocales().get(0)?.toLanguageTag().orEmpty()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.select_language)) },
        text = {
            LazyColumn(Modifier.heightIn(max = 480.dp)) {
                item {
                    LanguageOption(
                        name = stringResource(R.string.language_system),
                        selected = currentTag.isBlank(),
                        onClick = {
                            onDismiss()
                            AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
                        },
                    )
                    HorizontalDivider()
                }
                items(SupportedLanguages.all, key = { it.languageTag }) { language ->
                    LanguageOption(
                        name = language.autonym,
                        selected = SupportedLanguages.findByTag(currentTag) == language,
                        onClick = {
                            onDismiss()
                            AppCompatDelegate.setApplicationLocales(
                                LocaleListCompat.forLanguageTags(language.languageTag),
                            )
                        },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        },
    )
}

@Composable
private fun LanguageOption(
    name: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    ListItem(
        headlineContent = { Text(name) },
        leadingContent = {
            RadioButton(selected = selected, onClick = null)
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    )
}

