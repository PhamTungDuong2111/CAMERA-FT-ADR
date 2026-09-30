package com.snaplab.cameraft.ui.templates

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.snaplab.cameraft.models.WatermarkBadgeStyle
import com.snaplab.cameraft.models.WatermarkColorTheme
import com.snaplab.cameraft.models.WatermarkPosition
import com.snaplab.cameraft.models.WatermarkTemplate
import com.snaplab.cameraft.services.LocalizationManager
import com.snaplab.cameraft.ui.theme.SnapBackgroundDark
import com.snaplab.cameraft.ui.theme.SnapCardDark
import com.snaplab.cameraft.ui.theme.SnapSafetyOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateEditorDialog(
    initialTemplate: WatermarkTemplate,
    onSave: (WatermarkTemplate) -> Unit,
    onDismiss: () -> Unit
) {
    val loc = LocalizationManager.shared
    val isVi = loc.isVietnamese()

    var name by remember { mutableStateOf(initialTemplate.name) }
    var titleText by remember { mutableStateOf(initialTemplate.titleText) }
    var projectName by remember { mutableStateOf(initialTemplate.projectName) }
    var workItem by remember { mutableStateOf(initialTemplate.workItem) }
    var contractorName by remember { mutableStateOf(initialTemplate.contractorName) }
    var inspectorName by remember { mutableStateOf(initialTemplate.inspectorName) }
    var customNotes by remember { mutableStateOf(initialTemplate.customNotes) }

    var badgeStyle by remember { mutableStateOf(initialTemplate.badgeStyle) }
    var colorTheme by remember { mutableStateOf(initialTemplate.colorTheme) }
    var position by remember { mutableStateOf(initialTemplate.position) }

    var showTime by remember { mutableStateOf(initialTemplate.showTime) }
    var showSeconds by remember { mutableStateOf(initialTemplate.showSeconds) }
    var showLocation by remember { mutableStateOf(initialTemplate.showLocation) }
    var showCoordinates by remember { mutableStateOf(initialTemplate.showCoordinates) }
    var showAltitude by remember { mutableStateOf(initialTemplate.showAltitude) }
    var showWeather by remember { mutableStateOf(initialTemplate.showWeather) }
    var showCompass by remember { mutableStateOf(initialTemplate.showCompass) }
    var showDeviceInfo by remember { mutableStateOf(initialTemplate.showDeviceInfo) }
    var showAntiCounterfeitQR by remember { mutableStateOf(initialTemplate.showAntiCounterfeitQR) }

    var opacity by remember { mutableStateOf(initialTemplate.opacity) }
    var scale by remember { mutableStateOf(initialTemplate.scale) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = loc.t("customizeTemplate"),
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
                    actions = {
                        Button(
                            onClick = {
                                val updated = initialTemplate.copy(
                                    name = name,
                                    titleText = titleText,
                                    projectName = projectName,
                                    workItem = workItem,
                                    contractorName = contractorName,
                                    inspectorName = inspectorName,
                                    customNotes = customNotes,
                                    badgeStyle = badgeStyle,
                                    colorTheme = colorTheme,
                                    position = position,
                                    showTime = showTime,
                                    showSeconds = showSeconds,
                                    showLocation = showLocation,
                                    showCoordinates = showCoordinates,
                                    showAltitude = showAltitude,
                                    showWeather = showWeather,
                                    showCompass = showCompass,
                                    showDeviceInfo = showDeviceInfo,
                                    showAntiCounterfeitQR = showAntiCounterfeitQR,
                                    opacity = opacity,
                                    scale = scale
                                )
                                onSave(updated)
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SnapSafetyOrange)
                        ) {
                            Text(text = loc.t("save"), color = Color.White, fontWeight = FontWeight.Bold)
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
                // Section: Color Theme
                EditorSectionHeader(loc.t("colorTheme"))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    WatermarkColorTheme.values().forEach { theme ->
                        val isSelected = theme == colorTheme
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(theme.composeColor)
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) Color.White else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { colorTheme = theme }
                        )
                    }
                }

                // Section: Badge Style
                EditorSectionHeader(loc.t("badgeStyle"))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    WatermarkBadgeStyle.values().forEach { style ->
                        val isSelected = style == badgeStyle
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) SnapSafetyOrange.copy(alpha = 0.2f) else SnapCardDark)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) SnapSafetyOrange else Color.White.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { badgeStyle = style }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { badgeStyle = style },
                                colors = RadioButtonDefaults.colors(selectedColor = SnapSafetyOrange)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = style.getTitle(isVi),
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                // Section: Position
                EditorSectionHeader(loc.t("position"))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    WatermarkPosition.values().forEach { pos ->
                        val isSelected = pos == position
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) SnapSafetyOrange.copy(alpha = 0.2f) else SnapCardDark)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) SnapSafetyOrange else Color.White.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { position = pos }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { position = pos },
                                colors = RadioButtonDefaults.colors(selectedColor = SnapSafetyOrange)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = pos.getTitle(isVi),
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                // Section: Content Fields
                EditorSectionHeader(loc.t("contentInformation"))
                CustomTextField(label = loc.t("templateTitle"), value = titleText, onValueChange = { titleText = it })
                CustomTextField(label = loc.t("projectNameField"), value = projectName, onValueChange = { projectName = it })
                CustomTextField(label = loc.t("workItemField"), value = workItem, onValueChange = { workItem = it })
                CustomTextField(label = loc.t("contractorField"), value = contractorName, onValueChange = { contractorName = it })
                CustomTextField(label = loc.t("inspectorField"), value = inspectorName, onValueChange = { inspectorName = it })
                CustomTextField(label = loc.t("notesField"), value = customNotes, onValueChange = { customNotes = it })

                // Section: Display Toggles
                EditorSectionHeader(loc.t("displayOptions"))
                ToggleItem(loc.t("showTimeToggle"), showTime) { showTime = it }
                ToggleItem(loc.t("showSecondsToggle"), showSeconds) { showSeconds = it }
                ToggleItem(loc.t("showLocationToggle"), showLocation) { showLocation = it }
                ToggleItem(loc.t("showCoordinatesToggle"), showCoordinates) { showCoordinates = it }
                ToggleItem(loc.t("showAltitudeToggle"), showAltitude) { showAltitude = it }
                ToggleItem(loc.t("showWeatherToggle"), showWeather) { showWeather = it }
                ToggleItem(loc.t("showCompassToggle"), showCompass) { showCompass = it }
                ToggleItem(loc.t("showAntiCounterfeitQRToggle"), showAntiCounterfeitQR) { showAntiCounterfeitQR = it }

                // Section: Sliders
                EditorSectionHeader(loc.t("opacitySlider") + " (${(opacity * 100).toInt()}%)")
                Slider(
                    value = opacity,
                    onValueChange = { opacity = it },
                    valueRange = 0.3f..1.0f,
                    colors = SliderDefaults.colors(thumbColor = SnapSafetyOrange, activeTrackColor = SnapSafetyOrange)
                )

                EditorSectionHeader(loc.t("scaleSlider") + " (${String.format(java.util.Locale.US, "%.1fx", scale)})")
                Slider(
                    value = scale,
                    onValueChange = { scale = it },
                    valueRange = 0.7f..1.4f,
                    colors = SliderDefaults.colors(thumbColor = SnapSafetyOrange, activeTrackColor = SnapSafetyOrange)
                )

                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

@Composable
private fun EditorSectionHeader(text: String) {
    Text(
        text = text,
        color = SnapSafetyOrange,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun CustomTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = Color.White.copy(alpha = 0.6f)) },
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedBorderColor = SnapSafetyOrange,
            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
            focusedContainerColor = SnapCardDark,
            unfocusedContainerColor = SnapCardDark
        ),
        shape = RoundedCornerShape(10.dp)
    )
}

@Composable
private fun ToggleItem(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(SnapCardDark)
            .padding(horizontal = 14.dp, vertical = 6.dp),
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
