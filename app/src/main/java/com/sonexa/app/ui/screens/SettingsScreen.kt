package com.sonexa.app.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sonexa.app.data.local.AudioCacheManager
import com.sonexa.app.ui.components.SonexaGradientButton
import com.sonexa.app.ui.theme.SonexaBgDark
import com.sonexa.app.ui.theme.SonexaCardDark
import com.sonexa.app.ui.theme.SonexaInputBg
import com.sonexa.app.ui.theme.SonexaInputBorder
import com.sonexa.app.ui.theme.SonexaPurpleLight
import com.sonexa.app.ui.theme.SonexaPurplePrimary
import com.sonexa.app.ui.theme.SonexaTextMuted
import com.sonexa.app.ui.theme.SonexaTextSubtle
import com.sonexa.app.ui.theme.SonexaTextWhite
import com.sonexa.app.ui.viewmodel.CatalogUiState
import com.sonexa.app.ui.viewmodel.SettingsViewModel

private enum class SettingsPanel {
    NONE, ACCOUNT, AI, AUDIO, PROVIDERS, DOWNLOADS, NOTIFICATIONS, THEME, LANGUAGE, PRIVACY, DEVICES, ABOUT
}

private data class SettingRow(
    val panel: SettingsPanel,
    val name: String,
    val subtitle: String,
    val icon: ImageVector
)

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var activePanel by remember { mutableStateOf(SettingsPanel.NONE) }
    var showLogoutConfirm by remember { mutableStateOf(false) }

    BackHandler {
        when {
            activePanel != SettingsPanel.NONE -> activePanel = SettingsPanel.NONE
            showLogoutConfirm -> showLogoutConfirm = false
            else -> onNavigateBack()
        }
    }

    LaunchedEffect(uiState) {
        val msg = (uiState as? CatalogUiState.Ready)?.data?.message
        if (!msg.isNullOrBlank()) {
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SonexaBgDark)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(SonexaInputBg)
                    .clickable(onClick = onNavigateBack),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = SonexaTextWhite,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Settings & Preferences",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = SonexaTextWhite
            )
        }

        when (val state = uiState) {
            is CatalogUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = SonexaPurpleLight, modifier = Modifier.size(28.dp))
                }
            }
            is CatalogUiState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(state.message, color = SonexaTextMuted)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(onClick = { viewModel.load() }) {
                        Text("Retry", color = SonexaPurpleLight)
                    }
                }
            }
            is CatalogUiState.Ready -> {
                val model = state.data
                val settings = model.settings
                val audioQuality = settings["audioQuality"]?.toString() ?: "High"
                val theme = settings["theme"]?.toString() ?: "Dark"
                val language = settings["language"]?.toString()
                    ?: model.profile?.let { "English (India)" }
                    ?: "English (India)"
                val cacheLabel = AudioCacheManager.formatMb(model.cacheBytes)
                val aboutVersion = settings["appVersion"]?.toString()?.takeIf { it.isNotBlank() }
                    ?: model.appVersion
                val profileSubtitle = model.profile?.let { p ->
                    listOfNotNull(
                        p.email.takeIf { it.isNotBlank() },
                        if (p.isPremium) "Premium" else "Free plan"
                    ).joinToString(" • ").ifBlank { "Email, phone & plan details" }
                } ?: "Email, phone & plan details"

                val rows = listOf(
                    SettingRow(SettingsPanel.ACCOUNT, "Account Settings", profileSubtitle, Icons.Default.AccountCircle),
                    SettingRow(
                        SettingsPanel.AI,
                        "AI Features & DJ",
                        "${settings["aiSensitivity"] ?: "High"} • ${settings["aiVoiceModel"] ?: "Sonexa Voice v2.4"}",
                        Icons.Default.AutoAwesome
                    ),
                    SettingRow(
                        SettingsPanel.AUDIO,
                        "Audio Quality & Playback",
                        "$audioQuality • crossfade & equalizer",
                        Icons.Default.Tune
                    ),
                    SettingRow(
                        SettingsPanel.PROVIDERS,
                        "Music Discovery & Providers",
                        "YouTube • Sonexa Catalog • Audiomack",
                        Icons.Default.PlayArrow
                    ),
                    SettingRow(
                        SettingsPanel.DOWNLOADS,
                        "Downloads & Storage",
                        "${settings["downloadQuality"] ?: "High"} • Wi‑Fi only: ${settings["downloadOverWifiOnly"] ?: true}",
                        Icons.Default.Download
                    ),
                    SettingRow(
                        SettingsPanel.NOTIFICATIONS,
                        "Notifications",
                        "Push, friend activity & new releases",
                        Icons.Default.Notifications
                    ),
                    SettingRow(
                        SettingsPanel.THEME,
                        "Theme & Appearance",
                        "$theme • ${settings["accentStyle"] ?: "Glassmorphism"}",
                        Icons.Default.Palette
                    ),
                    SettingRow(SettingsPanel.LANGUAGE, "Language & Region", language, Icons.Default.Language),
                    SettingRow(
                        SettingsPanel.PRIVACY,
                        "Privacy & Security",
                        "Data sharing, sessions & 2FA",
                        Icons.Default.Security
                    ),
                    SettingRow(
                        SettingsPanel.DEVICES,
                        "Connected Devices",
                        viewModel.settingStringList("connectedDevices").take(2).joinToString(" • ")
                            .ifBlank { "Chromecast, Bluetooth & AirPlay" },
                        Icons.Default.Devices
                    ),
                    SettingRow(
                        SettingsPanel.ABOUT,
                        "About & Support",
                        "Sonexa v$aboutVersion • Terms & Help Center",
                        Icons.Default.Info
                    )
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        CacheCard(
                            cacheLabel = cacheLabel,
                            clearing = model.saving,
                            onClear = { viewModel.clearCache() }
                        )
                    }

                    items(rows) { row ->
                        SettingsCategoryCard(
                            name = row.name,
                            subtitle = row.subtitle,
                            icon = row.icon,
                            onClick = { activePanel = row.panel }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(6.dp))
                        SonexaGradientButton(
                            text = "Log Out of Sonexa",
                            onClick = { showLogoutConfirm = true }
                        )
                    }
                }

                if (activePanel != SettingsPanel.NONE) {
                    SettingsDetailDialog(
                        panel = activePanel,
                        viewModel = viewModel,
                        model = model,
                        onDismiss = { activePanel = SettingsPanel.NONE }
                    )
                }

                if (showLogoutConfirm) {
                    AlertDialog(
                        onDismissRequest = { showLogoutConfirm = false },
                        containerColor = SonexaCardDark,
                        title = {
                            Text("Log out?", color = SonexaTextWhite, fontWeight = FontWeight.Bold)
                        },
                        text = {
                            Text(
                                "You’ll need to sign in again to access your library and Premium.",
                                color = SonexaTextMuted,
                                fontSize = 13.sp
                            )
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                showLogoutConfirm = false
                                onLogout()
                            }) {
                                Text("Log Out", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showLogoutConfirm = false }) {
                                Text("Cancel", color = SonexaTextMuted)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CacheCard(
    cacheLabel: String,
    clearing: Boolean,
    onClear: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(SonexaInputBg)
            .border(1.dp, SonexaInputBorder, RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Temporary Audio Cache",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = SonexaTextWhite
                )
                Text(
                    text = cacheLabel,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = SonexaPurpleLight
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedButton(
                onClick = onClear,
                enabled = !clearing,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Clear Cache ($cacheLabel)", color = Color(0xFFEF4444))
            }
        }
    }
}

@Composable
private fun SettingsCategoryCard(
    name: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SonexaInputBg)
            .border(1.dp, SonexaInputBorder, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = SonexaPurpleLight,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(text = name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SonexaTextWhite)
                    Text(text = subtitle, fontSize = 12.sp, color = SonexaTextMuted)
                }
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = SonexaTextMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun SettingsDetailDialog(
    panel: SettingsPanel,
    viewModel: SettingsViewModel,
    model: SettingsViewModel.UiModel,
    onDismiss: () -> Unit
) {
    when (panel) {
        SettingsPanel.ACCOUNT -> AccountDialog(viewModel, model, onDismiss)
        SettingsPanel.AI -> AiDialog(viewModel, onDismiss)
        SettingsPanel.AUDIO -> AudioDialog(viewModel, onDismiss)
        SettingsPanel.PROVIDERS -> ProvidersDialog(onDismiss)
        SettingsPanel.DOWNLOADS -> DownloadsDialog(viewModel, model, onDismiss)
        SettingsPanel.NOTIFICATIONS -> NotificationsDialog(viewModel, onDismiss)
        SettingsPanel.THEME -> ThemeDialog(viewModel, onDismiss)
        SettingsPanel.LANGUAGE -> LanguageDialog(viewModel, model, onDismiss)
        SettingsPanel.PRIVACY -> PrivacyDialog(viewModel, onDismiss)
        SettingsPanel.DEVICES -> DevicesDialog(viewModel, onDismiss)
        SettingsPanel.ABOUT -> AboutDialog(model, onDismiss)
        SettingsPanel.NONE -> Unit
    }
}

@Composable
private fun SettingsSheetScaffold(
    title: String,
    onDismiss: () -> Unit,
    confirmLabel: String = "Done",
    onConfirm: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SonexaCardDark,
        title = { Text(title, color = SonexaTextWhite, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(360.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                content()
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm?.invoke(); onDismiss() }) {
                Text(confirmLabel)
            }
        },
        dismissButton = {
            if (onConfirm != null) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = SonexaTextMuted)
                }
            }
        }
    )
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = SonexaTextWhite, fontSize = 13.sp, modifier = Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedTrackColor = SonexaPurplePrimary,
                checkedThumbColor = Color.White
            )
        )
    }
}

@Composable
private fun ChoiceRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(label, color = SonexaTextWhite, fontSize = 13.sp)
    }
}

@Composable
private fun AccountDialog(
    viewModel: SettingsViewModel,
    model: SettingsViewModel.UiModel,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(model.profile?.name.orEmpty()) }
    SettingsSheetScaffold(
        title = "Account Settings",
        onDismiss = onDismiss,
        confirmLabel = "Save",
        onConfirm = { viewModel.updateProfile(name.trim().ifBlank { model.profile?.name.orEmpty() }) }
    ) {
        Text("Email", color = SonexaTextMuted, fontSize = 12.sp)
        Text(model.profile?.email ?: "—", color = SonexaTextWhite, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(12.dp))
        Text("Plan", color = SonexaTextMuted, fontSize = 12.sp)
        Text(
            if (model.profile?.isPremium == true) "Sonexa Premium" else "Free",
            color = SonexaPurpleLight,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Display name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun AiDialog(viewModel: SettingsViewModel, onDismiss: () -> Unit) {
    val sensitivities = listOf("Low", "Medium", "High")
    val voices = listOf("Sonexa Voice v2.4", "Sonexa Voice Studio", "Natural Soft")
    SettingsSheetScaffold(title = "AI DJ & Assistant", onDismiss = onDismiss) {
        Text("Curation sensitivity", color = SonexaTextMuted, fontSize = 12.sp)
        sensitivities.forEach { option ->
            ChoiceRow(
                label = option,
                selected = viewModel.settingString("aiSensitivity", "High") == option,
                onClick = { viewModel.updateString("aiSensitivity", option) }
            )
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = SonexaInputBorder)
        Text("Voice model", color = SonexaTextMuted, fontSize = 12.sp)
        voices.forEach { option ->
            ChoiceRow(
                label = option,
                selected = viewModel.settingString("aiVoiceModel", "Sonexa Voice v2.4") == option,
                onClick = { viewModel.updateString("aiVoiceModel", option) }
            )
        }
        ToggleRow(
            label = "Smart lyrics translator",
            checked = viewModel.settingBool("smartLyrics", true),
            onCheckedChange = { viewModel.updateToggle("smartLyrics", it) }
        )
    }
}

@Composable
private fun AudioDialog(viewModel: SettingsViewModel, onDismiss: () -> Unit) {
    val qualities = listOf("Normal", "High", "Very High", "Lossless")
    SettingsSheetScaffold(title = "Audio Quality & Playback", onDismiss = onDismiss) {
        Text("Streaming quality", color = SonexaTextMuted, fontSize = 12.sp)
        qualities.forEach { option ->
            ChoiceRow(
                label = option,
                selected = viewModel.settingString("audioQuality", "High") == option,
                onClick = { viewModel.updateString("audioQuality", option) }
            )
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = SonexaInputBorder)
        ToggleRow(
            "Crossfade",
            viewModel.settingBool("crossfade"),
            { viewModel.updateToggle("crossfade", it) }
        )
        ToggleRow(
            "Normalize volume",
            viewModel.settingBool("normalizeVolume", true),
            { viewModel.updateToggle("normalizeVolume", it) }
        )
        ToggleRow(
            "Gapless playback",
            viewModel.settingBool("gaplessPlayback", true),
            { viewModel.updateToggle("gaplessPlayback", it) }
        )
        ToggleRow(
            "Allow explicit content",
            viewModel.settingBool("explicitContent"),
            { viewModel.updateToggle("explicitContent", it) }
        )
    }
}

@Composable
private fun DownloadsDialog(
    viewModel: SettingsViewModel,
    model: SettingsViewModel.UiModel,
    onDismiss: () -> Unit
) {
    val qualities = listOf("Normal", "High", "Very High")
    SettingsSheetScaffold(title = "Downloads & Storage", onDismiss = onDismiss) {
        Text("Download quality", color = SonexaTextMuted, fontSize = 12.sp)
        qualities.forEach { option ->
            ChoiceRow(
                label = option,
                selected = viewModel.settingString("downloadQuality", "High") == option,
                onClick = { viewModel.updateString("downloadQuality", option) }
            )
        }
        ToggleRow(
            "Download over Wi‑Fi only",
            viewModel.settingBool("downloadOverWifiOnly", true),
            { viewModel.updateToggle("downloadOverWifiOnly", it) }
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Cache on device: ${AudioCacheManager.formatMb(model.cacheBytes)}",
            color = SonexaTextMuted,
            fontSize = 12.sp
        )
        OutlinedButton(onClick = { viewModel.clearCache() }, modifier = Modifier.fillMaxWidth()) {
            Text("Clear audio cache", color = Color(0xFFEF4444))
        }
    }
}

@Composable
private fun NotificationsDialog(viewModel: SettingsViewModel, onDismiss: () -> Unit) {
    SettingsSheetScaffold(title = "Notifications", onDismiss = onDismiss) {
        ToggleRow(
            "Push notifications",
            viewModel.settingBool("pushNotifications", true),
            { viewModel.updateToggle("pushNotifications", it) }
        )
        ToggleRow(
            "Friend activity",
            viewModel.settingBool("friendActivity", true),
            { viewModel.updateToggle("friendActivity", it) }
        )
        ToggleRow(
            "New release alerts",
            viewModel.settingBool("newReleaseAlerts", true),
            { viewModel.updateToggle("newReleaseAlerts", it) }
        )
    }
}

@Composable
private fun ThemeDialog(viewModel: SettingsViewModel, onDismiss: () -> Unit) {
    val themes = listOf("Dark", "Amoled", "System")
    val accents = listOf("Glassmorphism", "Neon", "Minimal")
    SettingsSheetScaffold(title = "Theme & Appearance", onDismiss = onDismiss) {
        Text("Theme", color = SonexaTextMuted, fontSize = 12.sp)
        themes.forEach { option ->
            ChoiceRow(
                label = option,
                selected = viewModel.settingString("theme", "Dark") == option,
                onClick = { viewModel.updateString("theme", option) }
            )
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = SonexaInputBorder)
        Text("Accent style", color = SonexaTextMuted, fontSize = 12.sp)
        accents.forEach { option ->
            ChoiceRow(
                label = option,
                selected = viewModel.settingString("accentStyle", "Glassmorphism") == option,
                onClick = { viewModel.updateString("accentStyle", option) }
            )
        }
    }
}

@Composable
private fun LanguageDialog(
    viewModel: SettingsViewModel,
    model: SettingsViewModel.UiModel,
    onDismiss: () -> Unit
) {
    var selected by remember {
        mutableStateOf(
            viewModel.settingStringList("languages").ifEmpty {
                model.availableLanguages.take(3)
            }.toSet()
        )
    }
    SettingsSheetScaffold(
        title = "Language & Region",
        onDismiss = onDismiss,
        confirmLabel = "Save",
        onConfirm = { viewModel.saveLanguages(selected.toList()) }
    ) {
        Text("Music languages", color = SonexaTextMuted, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(8.dp))
        model.availableLanguages.forEach { lang ->
            FilterChip(
                selected = lang in selected,
                onClick = {
                    selected = if (lang in selected) selected - lang else selected + lang
                },
                label = { Text(lang) },
                modifier = Modifier.padding(end = 6.dp, bottom = 6.dp)
            )
        }
    }
}

@Composable
private fun PrivacyDialog(viewModel: SettingsViewModel, onDismiss: () -> Unit) {
    SettingsSheetScaffold(title = "Privacy & Security", onDismiss = onDismiss) {
        ToggleRow(
            "Share listening data for recommendations",
            viewModel.settingBool("dataSharing"),
            { viewModel.updateToggle("dataSharing", it) }
        )
        ToggleRow(
            "Show active sessions",
            viewModel.settingBool("showActiveSessions", true),
            { viewModel.updateToggle("showActiveSessions", it) }
        )
        ToggleRow(
            "Two-factor authentication",
            viewModel.settingBool("twoFactorEnabled"),
            { viewModel.updateToggle("twoFactorEnabled", it) }
        )
    }
}

@Composable
private fun DevicesDialog(viewModel: SettingsViewModel, onDismiss: () -> Unit) {
    val devices = viewModel.settingStringList("connectedDevices")
        .ifEmpty { listOf("This Android phone", "Bluetooth earbuds") }
    SettingsSheetScaffold(title = "Connected Devices", onDismiss = onDismiss) {
        devices.forEach { device ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(device, color = SonexaTextWhite, fontSize = 13.sp, modifier = Modifier.weight(1f))
                TextButton(
                    onClick = {
                        val next = devices.filterNot { it == device }
                        viewModel.updateSettings(
                            mapOf("connectedDevices" to next),
                            successMessage = "Device removed"
                        )
                    }
                ) {
                    Text("Remove", color = Color(0xFFEF4444), fontSize = 12.sp)
                }
            }
            HorizontalDivider(color = SonexaInputBorder)
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            onClick = {
                val next = (devices + "Chromecast Living Room").distinct()
                viewModel.updateSettings(
                    mapOf("connectedDevices" to next),
                    successMessage = "Device linked"
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Link Chromecast", color = SonexaPurpleLight)
        }
    }
}

@Composable
private fun AboutDialog(model: SettingsViewModel.UiModel, onDismiss: () -> Unit) {
    SettingsSheetScaffold(title = "About & Support", onDismiss = onDismiss) {
        Text("Sonexa", color = SonexaTextWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text("Version ${model.appVersion}", color = SonexaPurpleLight, fontSize = 13.sp)
        if (model.latestVersion.isNotBlank() && model.latestVersion != model.appVersion) {
            Text("Latest available: ${model.latestVersion}", color = SonexaTextMuted, fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text("Terms of Service", color = SonexaTextWhite, fontSize = 13.sp)
        Text("Help Center", color = SonexaTextWhite, fontSize = 13.sp)
        Text("Privacy Policy", color = SonexaTextWhite, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Made for listeners who want studio-quality streaming with an AI DJ.",
            color = SonexaTextMuted,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun ProvidersDialog(onDismiss: () -> Unit) {
    val ytConfigured = com.sonexa.app.BuildConfig.YOUTUBE_API_KEY.isNotBlank()

    SettingsSheetScaffold(title = "Music Discovery Providers", onDismiss = onDismiss) {
        Text(
            "Connected Streaming & Discovery Services",
            color = SonexaTextWhite,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Sonexa aggregates music across authorized APIs and plays using official player mechanisms.",
            color = SonexaTextMuted,
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(14.dp))

        // 1. YouTube
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(SonexaInputBg)
                .border(1.dp, SonexaInputBorder, RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("YouTube Music", color = SonexaTextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (ytConfigured) Color(0xFF16A34A).copy(alpha = 0.2f) else Color(0xFF38BDF8).copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (ytConfigured) "Configured & Active" else "Discovery Active",
                            color = if (ytConfigured) Color(0xFF4ADE80) else Color(0xFF38BDF8),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text(
                    text = "Player: Official YouTube IFrame Player (Embeddable)",
                    color = SonexaTextSubtle,
                    fontSize = 11.sp
                )
                Text(
                    text = "Search Filters: Official Audio/Video, Verified Channels, 15m Cache TTL",
                    color = SonexaTextSubtle,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 2. Sonexa Core Catalog
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(SonexaInputBg)
                .border(1.dp, SonexaInputBorder, RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Sonexa Core Catalog", color = SonexaTextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF16A34A).copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("Connected", color = Color(0xFF4ADE80), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Text(
                    text = "Player: Native ExoPlayer (Studio Lossless & Background)",
                    color = SonexaTextSubtle,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 3. Open Stream Providers (Audiomack & Jamendo)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(SonexaInputBg)
                .border(1.dp, SonexaInputBorder, RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Audiomack & Jamendo", color = SonexaTextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF16A34A).copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("Active", color = Color(0xFF4ADE80), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Text(
                    text = "Player: Native Audio Gateway",
                    color = SonexaTextSubtle,
                    fontSize = 11.sp
                )
            }
        }
    }
}
