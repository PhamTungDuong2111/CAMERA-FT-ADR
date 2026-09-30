package com.snaplab.cameraft.ui.camera

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snaplab.cameraft.models.VerificationRecord
import com.snaplab.cameraft.models.WatermarkBadgeStyle
import com.snaplab.cameraft.models.WatermarkPosition
import com.snaplab.cameraft.models.WatermarkTemplate
import com.snaplab.cameraft.services.LocalizationManager
import com.snaplab.cameraft.services.LocationState
import com.snaplab.cameraft.services.LocationWeatherManager
import kotlinx.coroutines.delay

@Composable
fun WatermarkOverlayView(
    template: WatermarkTemplate,
    locationState: LocationState,
    verificationRecord: VerificationRecord?,
    onEditTapped: () -> Unit,
    modifier: Modifier = Modifier
) {
    val loc = LocalizationManager.shared
    val isVi = loc.isVietnamese()

    // Live clock ticker
    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = System.currentTimeMillis()
            delay(1000)
        }
    }

    // Determine Box alignment based on template position
    val boxAlignment = when (template.position) {
        WatermarkPosition.BOTTOM_LEFT -> Alignment.BottomStart
        WatermarkPosition.BOTTOM_RIGHT -> Alignment.BottomEnd
        WatermarkPosition.TOP_LEFT -> Alignment.TopStart
        WatermarkPosition.TOP_RIGHT -> Alignment.TopEnd
        WatermarkPosition.CENTER_BOTTOM -> Alignment.BottomCenter
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(
                start = 14.dp,
                end = 14.dp,
                top = if (template.position in listOf(WatermarkPosition.TOP_LEFT, WatermarkPosition.TOP_RIGHT)) 80.dp else 16.dp,
                bottom = if (template.position in listOf(WatermarkPosition.BOTTOM_LEFT, WatermarkPosition.BOTTOM_RIGHT, WatermarkPosition.CENTER_BOTTOM)) 140.dp else 16.dp
            ),
        contentAlignment = boxAlignment
    ) {
        // Badge Card
        val accentColor = template.colorTheme.composeColor

        val bgModifier = when (template.badgeStyle) {
            WatermarkBadgeStyle.GLASSMORPHISM -> {
                Modifier
                    .background(Color(0xE0121316), RoundedCornerShape(12.dp))
                    .border(1.5.dp, accentColor.copy(alpha = 0.65f), RoundedCornerShape(12.dp))
            }
            WatermarkBadgeStyle.DARK_CARD -> {
                Modifier
                    .background(Color(0xF0181A20), RoundedCornerShape(12.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            }
            WatermarkBadgeStyle.BORDERED_STAMP -> {
                Modifier
                    .background(Color(0xEE0A0A0C), RoundedCornerShape(10.dp))
                    .border(2.5.dp, accentColor, RoundedCornerShape(10.dp))
            }
            WatermarkBadgeStyle.MINIMAL_TRANSPARENT -> {
                Modifier
                    .background(Color(0x77000000), RoundedCornerShape(10.dp))
            }
        }

        Column(
            modifier = Modifier
                .widthIn(max = 310.dp)
                .clip(RoundedCornerShape(12.dp))
                .then(bgModifier)
                .clickable { onEditTapped() }
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            // Header Row: Accent Bar + Title + Category Pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .width(3.5.dp)
                        .height(16.dp)
                        .background(accentColor, RoundedCornerShape(2.dp))
                )
                Spacer(modifier = Modifier.width(6.dp))

                val title = if (template.titleText.isNotBlank()) {
                    template.titleText.uppercase()
                } else {
                    template.category.getDisplayName(isVi).uppercase()
                }

                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = "● " + template.category.getDisplayName(isVi),
                    color = accentColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.8.dp)
                    .background(Color.White.copy(alpha = 0.2f))
            )
            Spacer(modifier = Modifier.height(6.dp))

            // Body Row: Text lines on the left, QR code preview on the right
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    // Time
                    if (template.showTime) {
                        val timeStr = if (template.showSeconds) {
                            VerificationRecord.formatFullDateTime(currentTime)
                        } else {
                            "${VerificationRecord.formatDisplayDate(currentTime)} ${VerificationRecord.formatDisplayTime(currentTime).take(5)}"
                        }
                        WatermarkTextItem(
                            label = loc.t("timeLabel"),
                            value = timeStr,
                            color = accentColor,
                            isBold = true
                        )
                    }

                    // Project & Tasks
                    if (template.projectName.isNotBlank()) {
                        WatermarkTextItem(label = loc.t("projectLabel"), value = template.projectName)
                    }
                    if (template.workItem.isNotBlank()) {
                        WatermarkTextItem(label = loc.t("workItemLabel"), value = template.workItem)
                    }
                    if (template.contractorName.isNotBlank()) {
                        WatermarkTextItem(label = loc.t("contractorLabel"), value = template.contractorName)
                    }
                    if (template.inspectorName.isNotBlank()) {
                        WatermarkTextItem(label = loc.t("inspectorLabel"), value = template.inspectorName)
                    }

                    // Location & GPS
                    if (template.showLocation && locationState.fullAddress.isNotBlank()) {
                        WatermarkTextItem(label = loc.t("locationLabel"), value = locationState.fullAddress)
                    }
                    if (template.showCoordinates) {
                        val coords = LocationWeatherManager.shared.formattedCoordinates(locationState.latitude, locationState.longitude)
                        WatermarkTextItem(label = loc.t("coordinatesLabel"), value = coords)
                    }
                    if (template.showAltitude) {
                        WatermarkTextItem(label = loc.t("altitudeLabel"), value = "${String.format(java.util.Locale.US, "%.1f", locationState.altitude)} m")
                    }

                    // Weather & Compass
                    val envParts = mutableListOf<String>()
                    if (template.showWeather) {
                        envParts.add("${locationState.weatherCondition} ${locationState.temperatureCelsius}°C")
                    }
                    if (template.showCompass) {
                        envParts.add(locationState.compassDirection)
                    }
                    if (envParts.isNotEmpty()) {
                        WatermarkTextItem(label = "", value = envParts.joinToString(" • "))
                    }

                    // Notes
                    if (template.customNotes.isNotBlank()) {
                        WatermarkTextItem(label = loc.t("notesLabel"), value = template.customNotes)
                    }

                    // Verification Record Code
                    if (template.showAntiCounterfeitQR && verificationRecord != null) {
                        WatermarkTextItem(
                            label = loc.t("verifyCodeLabel"),
                            value = verificationRecord.id,
                            color = accentColor,
                            isBold = true
                        )
                    }
                }

                // QR Code icon preview on right side
                if (template.showAntiCounterfeitQR) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(bottom = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color.White, RoundedCornerShape(4.dp))
                                .padding(3.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCode2,
                                contentDescription = "QR Code",
                                tint = Color.Black,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = loc.t("scanToVerify"),
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 6.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WatermarkTextItem(
    label: String,
    value: String,
    color: Color = Color.White.copy(alpha = 0.92f),
    isBold: Boolean = false
) {
    Text(
        text = "$label$value",
        color = color,
        fontSize = 9.5.sp,
        fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
        lineHeight = 13.sp
    )
}
