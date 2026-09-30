package com.snaplab.cameraft.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snaplab.cameraft.services.AppLanguage
import com.snaplab.cameraft.services.CameraManager
import com.snaplab.cameraft.services.LocalizationManager
import com.snaplab.cameraft.ui.theme.SnapBackgroundDark
import com.snaplab.cameraft.ui.theme.SnapCardDark
import com.snaplab.cameraft.ui.theme.SnapNeonGreen
import com.snaplab.cameraft.ui.theme.SnapSafetyOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onDismiss: () -> Unit
) {
    val loc = LocalizationManager.shared
    val currentLang by loc.currentLanguage.collectAsState()
    val showGrid by CameraManager.shared.showGrid.collectAsState()

    var autoSaveToGallery by remember { mutableStateOf(true) }
    var ultraHdExport by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = loc.t("settingsTitle"),
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SnapBackgroundDark)
            )
        },
        containerColor = SnapBackgroundDark
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Section 1: Language Switcher
            SettingsSectionHeader(loc.t("languageSection"))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SnapCardDark)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = loc.t("languageDesc"),
                        color = Color.White.copy(alpha = 0.65f),
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    AppLanguage.values().forEach { lang ->
                        val isSelected = currentLang == lang
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) SnapSafetyOrange.copy(alpha = 0.15f) else Color.Transparent)
                                .clickable { loc.setLanguage(lang) }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { loc.setLanguage(lang) },
                                colors = RadioButtonDefaults.colors(selectedColor = SnapSafetyOrange)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = lang.displayName,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            // Section 2: Camera Options
            SettingsSectionHeader(loc.t("cameraSection"))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SnapCardDark)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SettingsToggleRow(
                        title = loc.t("gridlines"),
                        checked = showGrid,
                        onCheckedChange = { CameraManager.shared.toggleGrid() }
                    )
                    Divider(color = Color.White.copy(alpha = 0.1f))
                    SettingsToggleRow(
                        title = loc.t("saveToGallery"),
                        checked = autoSaveToGallery,
                        onCheckedChange = { autoSaveToGallery = it }
                    )
                    Divider(color = Color.White.copy(alpha = 0.1f))
                    SettingsToggleRow(
                        title = loc.t("highQualityRender"),
                        checked = ultraHdExport,
                        onCheckedChange = { ultraHdExport = it }
                    )
                }
            }

            // Section 3: Security & Anti-Counterfeiting Info
            SettingsSectionHeader(loc.t("securitySection"))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SnapCardDark)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = SnapNeonGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SHA-256 Cryptographic Engine",
                            color = SnapNeonGreen,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = loc.t("securityDesc"),
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            }

            // Section 4: App Info
            SettingsSectionHeader(loc.t("appInfoSection"))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SnapCardDark)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(loc.t("version"), color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp)
                        Text("2.4.0", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Divider(color = Color.White.copy(alpha = 0.1f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(loc.t("build"), color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp)
                        Text("6751682117 (Android Release)", color = Color.White, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsSectionHeader(text: String) {
    Text(
        text = text,
        color = SnapSafetyOrange,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun SettingsToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, color = Color.White, fontSize = 13.5.sp)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = SnapSafetyOrange
            )
        )
    }
}
