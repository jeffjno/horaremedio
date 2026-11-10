package br.com.remediohoracerta

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import br.com.remediohoracerta.data.MedicationStorage
import br.com.remediohoracerta.core.i18n.SupportedLanguages
import br.com.remediohoracerta.domain.model.Medication
import br.com.remediohoracerta.domain.model.MedicationInputValidator
import br.com.remediohoracerta.ui.theme.RemedioHoraCertaTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RemedioHoraCertaTheme {
                RemedioApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RemedioApp() {
    val context = LocalContext.current
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var showLanguagePicker by rememberSaveable { mutableStateOf(false) }
    var showAddMedication by rememberSaveable { mutableStateOf(false) }
    var medications by remember { mutableStateOf(MedicationStorage.load(context)) }

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
            onSave = { name, dosage, time ->
                medications = MedicationStorage.add(context, name, dosage, time)
                showAddMedication = false
            },
        )
    }
}

@Composable
private fun TodayScreen(
    contentPadding: PaddingValues,
    medications: List<Medication>,
    onAddMedication: () -> Unit,
) {
    val locale = LocalConfiguration.current.locales[0]
    val formattedDate = remember(locale) {
        LocalDate.now().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(locale))
    }

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
                    text = stringResource(R.string.medications_today),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            items(medications, key = Medication::id) { medication ->
                MedicationCard(medication)
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
private fun MedicationCard(medication: Medication) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = medication.time,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Column {
                Text(
                    text = medication.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = medication.dosage.ifBlank {
                        stringResource(R.string.dosage_not_informed)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun AddMedicationDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, dosage: String, time: String) -> Unit,
) {
    var name by rememberSaveable { mutableStateOf("") }
    var dosage by rememberSaveable { mutableStateOf("") }
    var time by rememberSaveable { mutableStateOf("") }
    var attemptedSave by rememberSaveable { mutableStateOf(false) }
    val nameInvalid = attemptedSave && !MedicationInputValidator.isNameValid(name)
    val timeInvalid = attemptedSave && !MedicationInputValidator.isTimeValid(time)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_medication_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                OutlinedTextField(
                    value = time,
                    onValueChange = { time = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.schedule_time)) },
                    placeholder = { Text(stringResource(R.string.time_hint)) },
                    singleLine = true,
                    isError = timeInvalid,
                    supportingText = if (timeInvalid) {
                        { Text(stringResource(R.string.invalid_time)) }
                    } else {
                        null
                    },
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    attemptedSave = true
                    if (
                        MedicationInputValidator.isNameValid(name) &&
                        MedicationInputValidator.isTimeValid(time)
                    ) {
                        onSave(name, dosage, time)
                    }
                },
            ) {
                Text(stringResource(R.string.save))
            }
        },
    )
}

@Composable
private fun SettingsScreen(
    contentPadding: PaddingValues,
    onChooseLanguage: () -> Unit,
) {
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

