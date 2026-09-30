package com.snaplab.cameraft.ui.camera

import android.graphics.Bitmap
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.snaplab.cameraft.models.VerificationRecord
import com.snaplab.cameraft.models.WatermarkTemplate
import com.snaplab.cameraft.services.*
import com.snaplab.cameraft.ui.anticounterfeit.AntiCounterfeitQueryScreen
import com.snaplab.cameraft.ui.gallery.GalleryScreen
import com.snaplab.cameraft.ui.settings.SettingsScreen
import com.snaplab.cameraft.ui.templates.TemplateEditorDialog
import com.snaplab.cameraft.ui.templates.TemplateSelectorDrawer
import com.snaplab.cameraft.ui.theme.SnapDangerRed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun CameraScreen() {
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    val cameraManager = CameraManager.shared
    val locationWeatherManager = LocationWeatherManager.shared
    val antiManager = AntiCounterfeitingManager.shared
    val photoLibraryManager = PhotoLibraryManager.shared

    val flashMode by cameraManager.flashMode.collectAsState()
    val aspectRatio by cameraManager.aspectRatio.collectAsState()
    val zoomRatio by cameraManager.zoomRatio.collectAsState()
    val isRecordingVideo by cameraManager.isRecordingVideo.collectAsState()
    val recordingDurationSec by cameraManager.recordingDurationSec.collectAsState()
    val showGrid by cameraManager.showGrid.collectAsState()
    val isSyntheticMode by cameraManager.isSyntheticMode.collectAsState()

    val locationState by locationWeatherManager.locationState.collectAsState()

    var currentMode by remember { mutableStateOf(CameraUiMode.PHOTO) }
    var selectedTemplate by remember { mutableStateOf(WatermarkTemplate.presets[0]) }
    var allTemplates by remember { mutableStateOf(WatermarkTemplate.presets) }

    // Dialog / Sheet states
    var showingTemplateDrawer by remember { mutableStateOf(false) }
    var showingTemplateCustomizer by remember { mutableStateOf(false) }
    var showingVerificationView by remember { mutableStateOf(false) }
    var showingGallery by remember { mutableStateOf(false) }
    var showingSettings by remember { mutableStateOf(false) }

    // Shutter flash effect
    var flashAlpha by remember { mutableStateOf(0f) }
    val animatedFlashAlpha by animateFloatAsState(
        targetValue = flashAlpha,
        animationSpec = tween(durationMillis = 120),
        label = "flash"
    )

    // PreviewView holder
    var previewViewInstance by remember { mutableStateOf<PreviewView?>(null) }

    // Live preview verification record (for QR rendering)
    val liveRecord = remember(selectedTemplate, locationState) {
        VerificationRecord(
            id = "SL-LIVE-" + (1000..9999).random(),
            latitude = locationState.latitude,
            longitude = locationState.longitude,
            altitude = locationState.altitude,
            addressString = locationState.fullAddress,
            projectName = selectedTemplate.projectName,
            inspectorName = selectedTemplate.inspectorName
        )
    }

    // Process captured photo
    val processCapturedPhoto: (Bitmap) -> Unit = { rawBitmap ->
        scope.launch(Dispatchers.IO) {
            // 1. Register authoritative record with SHA-256
            val record = antiManager.createAndRegisterRecord(
                bitmap = rawBitmap,
                latitude = locationState.latitude,
                longitude = locationState.longitude,
                altitude = locationState.altitude,
                address = locationState.fullAddress,
                projectName = selectedTemplate.projectName,
                inspectorName = selectedTemplate.inspectorName
            )

            // 2. Render watermark card onto bitmap
            val watermarked = WatermarkRenderer.shared.renderWatermark(
                originalBitmap = rawBitmap,
                template = selectedTemplate,
                verificationRecord = record,
                timestamp = record.timestamp,
                locationState = locationState
            )

            // 3. Save to photo library
            photoLibraryManager.saveWatermarkedPhoto(
                bitmap = watermarked,
                template = selectedTemplate,
                record = record
            )
        }
    }

    // Set callbacks
    DisposableEffect(lifecycleOwner) {
        cameraManager.onPhotoCaptured = { bmp ->
            processCapturedPhoto(bmp)
        }
        locationWeatherManager.startTracking()
        onDispose {
            locationWeatherManager.stopTracking()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // 1. VIEWFINDER LAYER (CameraX PreviewView or Synthetic Fallback)
        if (isSyntheticMode) {
            // Simulated Camera for Emulators without webcam
            val syntheticBitmap = remember { ImageUtils.generateSyntheticScene() }
            Image(
                bitmap = syntheticBitmap.asImageBitmap(),
                contentDescription = "Simulated Camera",
                colorFilter = selectedTemplate.activeFilter.getComposeColorFilter(),
                modifier = Modifier.fillMaxSize()
            )
        } else {
            AndroidView(
                factory = { ctx ->
                    PreviewView(ctx).apply {
                        previewViewInstance = this
                        cameraManager.startCamera(lifecycleOwner, this)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // 2. GRID OVERLAY LAYER
        if (showGrid) {
            GridOverlay()
        }

        // 3. WATERMARK OVERLAY LAYER
        WatermarkOverlayView(
            template = selectedTemplate,
            locationState = locationState,
            verificationRecord = liveRecord,
            onEditTapped = { showingTemplateCustomizer = true }
        )

        // 4. SHUTTER FLASH OVERLAY
        if (animatedFlashAlpha > 0.01f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = animatedFlashAlpha))
            )
        }

        // 5. VIDEO RECORDING HEADER INDICATOR
        if (isRecordingVideo) {
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 56.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.75f))
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(SnapDangerRed)
                )
                val minutes = recordingDurationSec / 60
                val seconds = recordingDurationSec % 60
                Text(
                    text = String.format(java.util.Locale.US, "%02d:%02d", minutes, seconds),
                    color = Color.White,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // 6. CAMERA CONTROLS OVERLAY DECK
        CameraControlsOverlay(
            flashMode = flashMode,
            aspectRatio = aspectRatio,
            showGrid = showGrid,
            zoomRatio = zoomRatio,
            isRecordingVideo = isRecordingVideo,
            currentMode = currentMode,
            activeFilter = selectedTemplate.activeFilter,
            onSelectFilter = { selectedTemplate = selectedTemplate.copy(activeFilter = it) },
            activeSticker = selectedTemplate.activeSticker,
            onSelectSticker = { selectedTemplate = selectedTemplate.copy(activeSticker = it) },
            onModeChange = { currentMode = it },
            onToggleFlash = { cameraManager.toggleFlash() },
            onCycleAspectRatio = {
                previewViewInstance?.let { cameraManager.cycleAspectRatio(lifecycleOwner, it) }
            },
            onToggleGrid = { cameraManager.toggleGrid() },
            onSetZoom = { cameraManager.setZoom(it) },
            onCapturePhoto = {
                // Trigger visual flash
                scope.launch {
                    flashAlpha = 0.85f
                    delay(120)
                    flashAlpha = 0f
                }
                cameraManager.capturePhoto()
            },
            onToggleVideo = { cameraManager.toggleVideoRecording() },
            onFlipCamera = {
                previewViewInstance?.let { cameraManager.toggleCameraFacing(lifecycleOwner, it) }
            },
            onOpenGallery = { showingGallery = true },
            onOpenTemplates = { showingTemplateDrawer = true },
            onOpenVerification = { showingVerificationView = true },
            onOpenSettings = { showingSettings = true }
        )

        // 7. SLIDING TEMPLATE DRAWER
        AnimatedVisibility(
            visible = showingTemplateDrawer,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            TemplateSelectorDrawer(
                selectedTemplate = selectedTemplate,
                templates = allTemplates,
                onSelectTemplate = {
                    selectedTemplate = it
                    showingTemplateDrawer = false
                },
                onCustomizeTemplate = {
                    selectedTemplate = it
                    showingTemplateDrawer = false
                    showingTemplateCustomizer = true
                },
                onDismiss = { showingTemplateDrawer = false }
            )
        }
    }

    // DIALOGS & SHEETS
    if (showingTemplateCustomizer) {
        TemplateEditorDialog(
            initialTemplate = selectedTemplate,
            onSave = { updated ->
                selectedTemplate = updated
            },
            onDismiss = { showingTemplateCustomizer = false }
        )
    }

    if (showingVerificationView) {
        AntiCounterfeitQueryScreen(onDismiss = { showingVerificationView = false })
    }

    if (showingGallery) {
        GalleryScreen(onDismiss = { showingGallery = false })
    }

    if (showingSettings) {
        SettingsScreen(onDismiss = { showingSettings = false })
    }
}
