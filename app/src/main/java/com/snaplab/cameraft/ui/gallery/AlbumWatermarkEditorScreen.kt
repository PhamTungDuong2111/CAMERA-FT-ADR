package com.snaplab.cameraft.ui.gallery

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snaplab.cameraft.models.VerificationRecord
import com.snaplab.cameraft.models.WatermarkTemplate
import com.snaplab.cameraft.services.AntiCounterfeitingManager
import com.snaplab.cameraft.services.LocalizationManager
import com.snaplab.cameraft.services.LocationWeatherManager
import com.snaplab.cameraft.services.PhotoLibraryManager
import com.snaplab.cameraft.services.WatermarkRenderer
import com.snaplab.cameraft.ui.camera.WatermarkOverlayView
import com.snaplab.cameraft.ui.templates.TemplateEditorDialog
import com.snaplab.cameraft.ui.templates.TemplateSelectorDrawer
import com.snaplab.cameraft.ui.theme.SnapBackgroundDark
import com.snaplab.cameraft.ui.theme.SnapSafetyOrange
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumWatermarkEditorScreen(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val loc = LocalizationManager.shared

    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var currentTemplate by remember { mutableStateOf(WatermarkTemplate.presets[0]) }
    var allTemplates by remember { mutableStateOf(WatermarkTemplate.presets) }

    var showingTemplateDrawer by remember { mutableStateOf(false) }
    var showingTemplateEditor by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch(Dispatchers.IO) {
                try {
                    val stream = context.contentResolver.openInputStream(uri)
                    val bmp = BitmapFactory.decodeStream(stream)
                    withContext(Dispatchers.Main) {
                        selectedBitmap = bmp
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = loc.t("albumEditorTitle"),
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
                    if (selectedBitmap != null) {
                        Button(
                            onClick = {
                                val bmp = selectedBitmap ?: return@Button
                                isProcessing = true
                                scope.launch(Dispatchers.IO) {
                                    val locState = LocationWeatherManager.shared.locationState.value
                                    val record = AntiCounterfeitingManager.shared.createAndRegisterRecord(
                                        bitmap = bmp,
                                        latitude = locState.latitude,
                                        longitude = locState.longitude,
                                        altitude = locState.altitude,
                                        address = locState.fullAddress,
                                        projectName = currentTemplate.projectName,
                                        inspectorName = currentTemplate.inspectorName
                                    )
                                    val watermarked = WatermarkRenderer.shared.renderWatermark(
                                        originalBitmap = bmp,
                                        template = currentTemplate,
                                        verificationRecord = record,
                                        locationState = locState
                                    )
                                    PhotoLibraryManager.shared.saveWatermarkedPhoto(
                                        bitmap = watermarked,
                                        template = currentTemplate,
                                        record = record
                                    )
                                    withContext(Dispatchers.Main) {
                                        isProcessing = false
                                        Toast.makeText(context, loc.t("imageSavedSuccess"), Toast.LENGTH_LONG).show()
                                        onDismiss()
                                    }
                                }
                            },
                            enabled = !isProcessing,
                            colors = ButtonDefaults.buttonColors(containerColor = SnapSafetyOrange)
                        ) {
                            Text(text = loc.t("save"), color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SnapBackgroundDark)
            )
        },
        containerColor = SnapBackgroundDark
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (selectedBitmap == null) {
                // Empty state: prompt to choose photo
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = null,
                        tint = SnapSafetyOrange,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = loc.t("chooseFromAlbum"),
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { photoPickerLauncher.launch("image/*") },
                        colors = ButtonDefaults.buttonColors(containerColor = SnapSafetyOrange),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = loc.t("chooseFromAlbum"), fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                // Preview with Watermark Overlay
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = selectedBitmap!!.asImageBitmap(),
                        contentDescription = "Selected Photo",
                        modifier = Modifier.fillMaxSize()
                    )

                    WatermarkOverlayView(
                        template = currentTemplate,
                        locationState = LocationWeatherManager.shared.locationState.collectAsState().value,
                        verificationRecord = null,
                        onEditTapped = { showingTemplateEditor = true }
                    )

                    // Bottom Floating Action Button to choose another photo or change template
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = { photoPickerLauncher.launch("image/*") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Black.copy(alpha = 0.7f)),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Icon(imageVector = Icons.Default.SwapHoriz, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Đổi ảnh", color = Color.White)
                        }

                        Button(
                            onClick = { showingTemplateDrawer = true },
                            colors = ButtonDefaults.buttonColors(containerColor = SnapSafetyOrange),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Layers, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(loc.t("templates"), color = Color.White)
                        }
                    }
                }
            }

            // Template Drawer
            if (showingTemplateDrawer) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    TemplateSelectorDrawer(
                        selectedTemplate = currentTemplate,
                        templates = allTemplates,
                        onSelectTemplate = {
                            currentTemplate = it
                            showingTemplateDrawer = false
                        },
                        onCustomizeTemplate = {
                            currentTemplate = it
                            showingTemplateDrawer = false
                            showingTemplateEditor = true
                        },
                        onDismiss = { showingTemplateDrawer = false }
                    )
                }
            }

            // Template Customizer Dialog
            if (showingTemplateEditor) {
                TemplateEditorDialog(
                    initialTemplate = currentTemplate,
                    onSave = { updated -> currentTemplate = updated },
                    onDismiss = { showingTemplateEditor = false }
                )
            }

            // Processing indicator
            if (isProcessing) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = SnapSafetyOrange)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(loc.t("processing"), color = Color.White)
                    }
                }
            }
        }
    }
}
