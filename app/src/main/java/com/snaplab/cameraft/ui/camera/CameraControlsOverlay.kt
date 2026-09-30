package com.snaplab.cameraft.ui.camera

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snaplab.cameraft.services.CameraAspectRatio
import com.snaplab.cameraft.services.FlashMode
import com.snaplab.cameraft.services.LocalizationManager
import com.snaplab.cameraft.ui.theme.SnapDangerRed
import com.snaplab.cameraft.ui.theme.SnapGold
import com.snaplab.cameraft.ui.theme.SnapNeonGreen
import com.snaplab.cameraft.ui.theme.SnapSafetyOrange

enum class CameraUiMode {
    PHOTO, VIDEO
}

@Composable
fun CameraControlsOverlay(
    flashMode: FlashMode,
    aspectRatio: CameraAspectRatio,
    showGrid: Boolean,
    zoomRatio: Float,
    isRecordingVideo: Boolean,
    currentMode: CameraUiMode,
    onModeChange: (CameraUiMode) -> Unit,
    onToggleFlash: () -> Unit,
    onCycleAspectRatio: () -> Unit,
    onToggleGrid: () -> Unit,
    onSetZoom: (Float) -> Unit,
    onCapturePhoto: () -> Unit,
    onToggleVideo: () -> Unit,
    onFlipCamera: () -> Unit,
    onOpenGallery: () -> Unit,
    onOpenTemplates: () -> Unit,
    onOpenVerification: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val loc = LocalizationManager.shared
    val currentLang by loc.currentLanguage.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // TOP CONTROLS BAR
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Flash button
            TopBarIconButton(
                onClick = onToggleFlash,
                icon = when (flashMode) {
                    FlashMode.OFF -> Icons.Filled.FlashOff
                    FlashMode.ON -> Icons.Filled.FlashOn
                    FlashMode.AUTO -> Icons.Filled.FlashAuto
                    FlashMode.TORCH -> Icons.Filled.Highlight
                },
                tint = if (flashMode == FlashMode.OFF) Color.White else SnapGold
            )

            // Aspect ratio button
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.45f))
                    .clickable { onCycleAspectRatio() }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = aspectRatio.title,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Grid button
            TopBarIconButton(
                onClick = onToggleGrid,
                icon = Icons.Default.GridOn,
                tint = if (showGrid) SnapGold else Color.White.copy(alpha = 0.5f)
            )

            // Language Switcher Button (1-Tap VI/EN)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black.copy(alpha = 0.45f))
                    .clickable { loc.toggleLanguage() }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (loc.isVietnamese()) "🇻🇳 VI" else "🇺🇸 EN",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Settings button
            TopBarIconButton(
                onClick = onOpenSettings,
                icon = Icons.Default.Settings,
                tint = Color.White
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // CENTER-BOTTOM CONTROLS: Zoom Pill + Mode Switcher + Shutter Deck
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Zoom Selector Pill
            Row(
                modifier = Modifier
                    .clip(CapsuleShape)
                    .background(Color.Black.copy(alpha = 0.45f))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(0.5f, 1.0f, 2.0f, 5.0f).forEach { zoom ->
                    val isSelected = Math.abs(zoomRatio - zoom) < 0.2f
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (isSelected) SnapGold else Color.Transparent)
                            .clickable { onSetZoom(zoom) }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (zoom == 0.5f) ".5" else "${zoom.toInt()}x",
                            color = if (isSelected) Color.Black else Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Mode Switcher (ẢNH / VIDEO)
            Row(
                modifier = Modifier.padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = loc.t("photo"),
                    color = if (currentMode == CameraUiMode.PHOTO) SnapGold else Color.White.copy(alpha = 0.6f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onModeChange(CameraUiMode.PHOTO) }
                )
                Text(
                    text = loc.t("video"),
                    color = if (currentMode == CameraUiMode.VIDEO) SnapGold else Color.White.copy(alpha = 0.6f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onModeChange(CameraUiMode.VIDEO) }
                )
            }

            // Bottom Shutter & Utility Deck
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Gallery Button
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { onOpenGallery() }
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.15f))
                            .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = "Gallery",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = loc.t("gallery"),
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // 2. Templates Drawer Button
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { onOpenTemplates() }
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(SnapSafetyOrange.copy(alpha = 0.25f))
                            .border(1.dp, SnapSafetyOrange.copy(alpha = 0.8f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = "Templates",
                            tint = SnapSafetyOrange,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = loc.t("templates"),
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // 3. Shutter Button
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .border(3.5.dp, Color.White, CircleShape)
                        .padding(5.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            if (currentMode == CameraUiMode.PHOTO) {
                                onCapturePhoto()
                            } else {
                                onToggleVideo()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (currentMode == CameraUiMode.PHOTO) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(Color.White)
                        )
                    } else {
                        // Video button morphing
                        val innerSize = if (isRecordingVideo) 28.dp else 56.dp
                        val shape = if (isRecordingVideo) RoundedCornerShape(6.dp) else CircleShape
                        Box(
                            modifier = Modifier
                                .size(innerSize)
                                .clip(shape)
                                .background(SnapDangerRed)
                        )
                    }
                }

                // 4. Anti-Counterfeit Verification Button
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { onOpenVerification() }
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(SnapNeonGreen.copy(alpha = 0.2f))
                            .border(1.dp, SnapNeonGreen.copy(alpha = 0.7f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = "Verify",
                            tint = SnapNeonGreen,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = loc.t("verify"),
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // 5. Flip Camera Button
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { onFlipCamera() }
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f))
                            .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FlipCameraAndroid,
                            contentDescription = "Flip",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Lật",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

private val CapsuleShape = RoundedCornerShape(20.dp)

@Composable
private fun TopBarIconButton(
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.45f))
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )
    }
}
