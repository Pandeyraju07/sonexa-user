package com.sonexa.app.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.DeleteForever
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
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sonexa.app.data.local.AudioCacheManager
import com.sonexa.app.data.local.SessionManager
import com.sonexa.app.data.model.AudioQuality
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
        // ── Premium gradient header ──────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF12082A), Color(0xFF0C0520))
                    )
                )
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(SonexaInputBg)
                        .border(1.dp, SonexaInputBorder, CircleShape)
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
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "Settings & Preferences",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SonexaTextWhite
                    )
                    Text(
                        text = "Manage your Zynera experience",
                        fontSize = 12.sp,
                        color = SonexaTextSubtle
                    )
                }
            }
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
                        "${settings["aiSensitivity"] ?: "High"} • ${settings["aiVoiceModel"] ?: "Zynera Voice v2.4"}",
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
                        "YouTube • Zynera Catalog • Audiomack",
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
                        "Zynera v$aboutVersion • Terms & Help Center",
                        Icons.Default.Info
                    )
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // ── Premium Profile Card ─────────────────────────────────────
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF2D1B6B), Color(0xFF6B3CE9), Color(0xFF1A0D3D))
                                    )
                                )
                                .border(1.dp, SonexaPurplePrimary.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                                .padding(18.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Avatar
                                Box(
                                    modifier = Modifier
                                        .size(58.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.linearGradient(
                                                listOf(Color(0xFFE534B2), Color(0xFF6B3CE9))
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = model.profile?.name?.take(1)?.uppercase() ?: "S",
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = model.profile?.name?.ifBlank { "Zynera Listener" } ?: "Zynera Listener",
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = model.profile?.email ?: "listener@zynera.app",
                                        fontSize = 12.sp,
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (model.profile?.isPremium == true)
                                                    Color(0xFFFFD700).copy(alpha = 0.20f)
                                                else Color.White.copy(alpha = 0.10f)
                                            )
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = if (model.profile?.isPremium == true) "⭐  Zynera Premium" else "Free Plan  →  Upgrade",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (model.profile?.isPremium == true) Color(0xFFFFD700) else Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        CacheCard(
                            cacheLabel = cacheLabel,
                            clearing = model.saving,
                            onClear = { viewModel.clearCache() }
                        )
                    }

                    items(rows) { row ->
                        PremiumSettingsCategoryCard(
                            name = row.name,
                            subtitle = row.subtitle,
                            icon = row.icon,
                            onClick = { activePanel = row.panel }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(6.dp))
                        SonexaGradientButton(
                            text = "Log Out of Zynera",
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
                    com.sonexa.app.ui.components.LogoutConfirmationDialog(
                        onConfirmLogout = {
                            showLogoutConfirm = false
                            onLogout()
                        },
                        onDismiss = { showLogoutConfirm = false }
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
private fun PremiumSettingsCategoryCard(
    name: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    val panelColor = when {
        name.contains("Account") -> Color(0xFF3B82F6)
        name.contains("AI") -> Color(0xFF8B5CF6)
        name.contains("Audio") -> Color(0xFF10B981)
        name.contains("Download") -> Color(0xFFF59E0B)
        name.contains("Notification") -> Color(0xFFE534B2)
        name.contains("Theme") -> Color(0xFF06B6D4)
        name.contains("Language") -> Color(0xFF84CC16)
        name.contains("Privacy") -> Color(0xFFEF4444)
        name.contains("Device") -> Color(0xFFFF7849)
        name.contains("Music Discovery") -> Color(0xFF22D3EE)
        else -> SonexaPurpleLight
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(SonexaInputBg)
            .border(1.dp, SonexaInputBorder, RoundedCornerShape(18.dp))
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
                // Gradient icon container
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    panelColor.copy(alpha = 0.30f),
                                    panelColor.copy(alpha = 0.10f)
                                )
                            )
                        )
                        .border(1.dp, panelColor.copy(alpha = 0.22f), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = panelColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(text = name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SonexaTextWhite)
                    Text(text = subtitle, fontSize = 11.sp, color = SonexaTextMuted, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
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
        SettingsPanel.AI -> AiDialog(model, viewModel, onDismiss)
        SettingsPanel.AUDIO -> AudioDialog(model, viewModel, onDismiss)
        SettingsPanel.PROVIDERS -> ProvidersDialog(onDismiss)
        SettingsPanel.DOWNLOADS -> DownloadsDialog(model, viewModel, onDismiss)
        SettingsPanel.NOTIFICATIONS -> NotificationsDialog(model, viewModel, onDismiss)
        SettingsPanel.THEME -> ThemeDialog(model, viewModel, onDismiss)
        SettingsPanel.LANGUAGE -> LanguageDialog(viewModel, model, onDismiss)
        SettingsPanel.PRIVACY -> PrivacyDialog(model, viewModel, onDismiss)
        SettingsPanel.DEVICES -> DevicesDialog(model, viewModel, onDismiss)
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
            .clip(RoundedCornerShape(10.dp))
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 4.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = SonexaTextWhite,
            fontSize = 13.sp,
            modifier = Modifier
                .weight(1f)
                .padding(end = 12.dp)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedTrackColor = SonexaPurplePrimary,
                checkedThumbColor = Color.White,
                uncheckedTrackColor = SonexaInputBg,
                uncheckedThumbColor = SonexaTextMuted
            )
        )
    }
}

@Composable
private fun ChoiceRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) SonexaPurplePrimary.copy(alpha = 0.15f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else SonexaTextWhite.copy(alpha = 0.85f),
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 14.sp
        )
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = androidx.compose.material3.RadioButtonDefaults.colors(
                selectedColor = SonexaPurpleLight,
                unselectedColor = SonexaTextMuted
            )
        )
    }
}

@Composable
private fun AccountDialog(
    viewModel: SettingsViewModel,
    model: SettingsViewModel.UiModel,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(model.profile?.name.orEmpty()) }
    var showAccountSwitcher by remember { mutableStateOf(false) }

    if (showAccountSwitcher) {
        com.sonexa.app.ui.components.AccountSwitcherDialog(
            onDismiss = { showAccountSwitcher = false },
            onAccountSwitched = {
                viewModel.load()
            }
        )
    }

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
            if (model.profile?.isPremium == true) "Zynera Premium" else "Free",
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
        Spacer(modifier = Modifier.height(14.dp))
        OutlinedButton(
            onClick = { showAccountSwitcher = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Switch or Add Account", color = SonexaPurpleLight)
        }
    }
}

@Composable
private fun AiDialog(
    model: SettingsViewModel.UiModel,
    viewModel: SettingsViewModel,
    onDismiss: () -> Unit
) {
    val sensitivities = listOf("Low", "Medium", "High")
    val voices = listOf("Zynera Voice v2.4", "Zynera Voice Studio", "Natural Soft")
    val currentSensitivity = model.settings["aiSensitivity"]?.toString() ?: "High"
    val currentVoice = model.settings["aiVoiceModel"]?.toString() ?: "Zynera Voice v2.4"
    val smartLyrics = (model.settings["smartLyrics"] as? Boolean) ?: true

    SettingsSheetScaffold(title = "AI DJ & Assistant", onDismiss = onDismiss) {
        Text("Curation sensitivity", color = SonexaTextMuted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(4.dp))
        sensitivities.forEach { option ->
            ChoiceRow(
                label = option,
                selected = currentSensitivity.equals(option, ignoreCase = true),
                onClick = { viewModel.updateString("aiSensitivity", option) }
            )
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = SonexaInputBorder)
        Text("Voice model", color = SonexaTextMuted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(4.dp))
        voices.forEach { option ->
            ChoiceRow(
                label = option,
                selected = currentVoice.equals(option, ignoreCase = true),
                onClick = { viewModel.updateString("aiVoiceModel", option) }
            )
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = SonexaInputBorder)
        ToggleRow(
            label = "Smart lyrics translator",
            checked = smartLyrics,
            onCheckedChange = { viewModel.updateToggle("smartLyrics", it) }
        )
    }
}

@Composable
private fun AudioDialog(
    model: SettingsViewModel.UiModel,
    viewModel: SettingsViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val currentQuality = model.settings["audioQuality"]?.toString() ?: "Lossless"
    val crossfade = (model.settings["crossfade"] as? Boolean) ?: true
    val normalizeVolume = (model.settings["normalizeVolume"] as? Boolean) ?: true
    val gaplessPlayback = (model.settings["gaplessPlayback"] as? Boolean) ?: true
    val explicitContent = (model.settings["explicitContent"] as? Boolean) ?: true

    SettingsSheetScaffold(title = "Audio Quality & Playback", onDismiss = onDismiss) {
        Text("Streaming Quality (Real-Time Dynamic)", color = SonexaTextMuted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(4.dp))
        AudioQuality.values().forEach { quality ->
            ChoiceRow(
                label = "${quality.displayName} (${quality.description})",
                selected = currentQuality.equals(quality.key, ignoreCase = true) ||
                        currentQuality.equals(quality.name, ignoreCase = true),
                onClick = {
                    viewModel.updateString("audioQuality", quality.key)
                    SessionManager.getInstance(context).audioQuality = quality.key
                }
            )
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = SonexaInputBorder)
        ToggleRow(
            "Crossfade (Smooth transitions)",
            crossfade,
            { viewModel.updateToggle("crossfade", it) }
        )
        ToggleRow(
            "Normalize volume",
            normalizeVolume,
            { viewModel.updateToggle("normalizeVolume", it) }
        )
        ToggleRow(
            "Gapless playback",
            gaplessPlayback,
            { viewModel.updateToggle("gaplessPlayback", it) }
        )
        ToggleRow(
            "Allow explicit content",
            explicitContent,
            { viewModel.updateToggle("explicitContent", it) }
        )
    }
}

@Composable
private fun DownloadsDialog(
    model: SettingsViewModel.UiModel,
    viewModel: SettingsViewModel,
    onDismiss: () -> Unit
) {
    val qualities = listOf(
        "Normal (96 kbps)",
        "High (160 kbps)",
        "Very High (320 kbps)",
        "Lossless (320 kbps Master)"
    )
    val currentQuality = model.settings["downloadQuality"]?.toString() ?: "Very High"
    val wifiOnly = (model.settings["downloadOverWifiOnly"] as? Boolean) ?: true

    SettingsSheetScaffold(title = "Downloads & Storage", onDismiss = onDismiss) {
        Text("Download Quality", color = SonexaTextMuted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(4.dp))
        qualities.forEach { option ->
            val key = when {
                option.contains("Lossless", true) -> "Lossless"
                option.contains("Very High", true) -> "Very High"
                option.contains("High", true) -> "High"
                else -> "Normal"
            }
            ChoiceRow(
                label = option,
                selected = currentQuality.equals(key, ignoreCase = true) || currentQuality.equals(option, ignoreCase = true),
                onClick = { viewModel.updateString("downloadQuality", key) }
            )
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = SonexaInputBorder)
        ToggleRow(
            "Download over Wi‑Fi only",
            wifiOnly,
            { viewModel.updateToggle("downloadOverWifiOnly", it) }
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "Cache on device: ${AudioCacheManager.formatMb(model.cacheBytes)}",
            color = SonexaTextMuted,
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(onClick = { viewModel.clearCache() }, modifier = Modifier.fillMaxWidth()) {
            Text("Clear audio cache", color = Color(0xFFEF4444))
        }
    }
}

@Composable
private fun NotificationsDialog(
    model: SettingsViewModel.UiModel,
    viewModel: SettingsViewModel,
    onDismiss: () -> Unit
) {
    val push = (model.settings["pushNotifications"] as? Boolean) ?: true
    val friend = (model.settings["friendActivity"] as? Boolean) ?: true
    val releases = (model.settings["newReleaseAlerts"] as? Boolean) ?: true

    SettingsSheetScaffold(title = "Notifications", onDismiss = onDismiss) {
        ToggleRow(
            "Push notifications",
            push,
            { viewModel.updateToggle("pushNotifications", it) }
        )
        ToggleRow(
            "Friend activity",
            friend,
            { viewModel.updateToggle("friendActivity", it) }
        )
        ToggleRow(
            "New release alerts",
            releases,
            { viewModel.updateToggle("newReleaseAlerts", it) }
        )
    }
}

@Composable
private fun ThemeDialog(
    model: SettingsViewModel.UiModel,
    viewModel: SettingsViewModel,
    onDismiss: () -> Unit
) {
    val themes = listOf("Dark", "Amoled", "System")
    val accents = listOf("Glassmorphism", "Neon", "Minimal")
    val currentTheme = model.settings["theme"]?.toString() ?: "Dark"
    val currentAccent = model.settings["accentStyle"]?.toString() ?: "Glassmorphism"

    SettingsSheetScaffold(title = "Theme & Appearance", onDismiss = onDismiss) {
        Text("Theme", color = SonexaTextMuted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(4.dp))
        themes.forEach { option ->
            ChoiceRow(
                label = option,
                selected = currentTheme.equals(option, ignoreCase = true),
                onClick = { viewModel.updateString("theme", option) }
            )
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = SonexaInputBorder)
        Text("Accent style", color = SonexaTextMuted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(4.dp))
        accents.forEach { option ->
            ChoiceRow(
                label = option,
                selected = currentAccent.equals(option, ignoreCase = true),
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
private fun PrivacyDialog(
    model: SettingsViewModel.UiModel,
    viewModel: SettingsViewModel,
    onDismiss: () -> Unit
) {
    val dataSharing = (model.settings["dataSharing"] as? Boolean) ?: true
    val showSessions = (model.settings["showActiveSessions"] as? Boolean) ?: true
    val twoFa = (model.settings["twoFactorEnabled"] as? Boolean) ?: false
    val personalizedAds = (model.settings["personalizedAds"] as? Boolean) ?: true
    val shareListeningHistory = (model.settings["shareListeningHistory"] as? Boolean) ?: false
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor = SonexaCardDark,
            title = { Text("Delete Account?", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        "This will permanently delete your Zynera account, all playlists, and listening history. This cannot be undone.",
                        color = SonexaTextMuted,
                        fontSize = 13.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showDeleteConfirm = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Delete Account", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel", color = SonexaTextMuted)
                }
            }
        )
    }

    SettingsSheetScaffold(title = "Privacy & Security", onDismiss = onDismiss) {
        // Security section
        Text("Security", color = SonexaPurpleLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        ToggleRow(
            "Two-factor authentication (2FA)",
            twoFa,
            { viewModel.updateToggle("twoFactorEnabled", it) }
        )
        ToggleRow(
            "Show active sessions on other devices",
            showSessions,
            { viewModel.updateToggle("showActiveSessions", it) }
        )
        HorizontalDivider(color = SonexaInputBorder, modifier = Modifier.padding(vertical = 8.dp))

        // Data Controls
        Text("Data & Privacy", color = SonexaPurpleLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        ToggleRow(
            "Share listening data for recommendations",
            dataSharing,
            { viewModel.updateToggle("dataSharing", it) }
        )
        ToggleRow(
            "Personalized ads & discovery",
            personalizedAds,
            { viewModel.updateToggle("personalizedAds", it) }
        )
        ToggleRow(
            "Share listening history with friends",
            shareListeningHistory,
            { viewModel.updateToggle("shareListeningHistory", it) }
        )
        HorizontalDivider(color = SonexaInputBorder, modifier = Modifier.padding(vertical = 8.dp))

        // Danger Zone
        Text("Danger Zone", color = Color(0xFFEF4444), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedButton(
            onClick = { showDeleteConfirm = true },
            modifier = Modifier.fillMaxWidth(),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                Icons.Default.DeleteForever,
                contentDescription = null,
                tint = Color(0xFFEF4444),
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text("Delete My Account", color = Color(0xFFEF4444), fontSize = 13.sp)
        }
    }
}

@Composable
private fun DevicesDialog(
    model: SettingsViewModel.UiModel,
    viewModel: SettingsViewModel,
    onDismiss: () -> Unit
) {
    val rawDevices = model.settings["connectedDevices"]
    val devices = when (rawDevices) {
        is List<*> -> rawDevices.mapNotNull { it?.toString() }
        is String -> rawDevices.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        else -> listOf("This Android phone", "Bluetooth earbuds")
    }
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
        Spacer(modifier = Modifier.height(12.dp))
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
        Text("Zynera", color = SonexaTextWhite, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
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
            "Your mood. Your music. Studio-quality streaming with AI DJ intelligence.",
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
            "Zynera aggregates music across authorized APIs and plays using official player mechanisms.",
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

        // 2. Zynera Core Catalog
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
                    Text("Zynera Core Catalog", color = SonexaTextWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
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
