package com.snaplab.cameraft.ui.gallery

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
    var showingFilterDrawer by remember { mutableStateOf(false) }
    var showingStickerDrawer by remember { mutableStateOf(false) }
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
                        colorFilter = currentTemplate.activeFilter.getComposeColorFilter(),
                        modifier = Modifier.fillMaxSize()
                    )

                    WatermarkOverlayView(
                        template = currentTemplate,
                        locationState = LocationWeatherManager.shared.locationState.collectAsState().value,
                        verificationRecord = null,
                        onEditTapped = { showingTemplateEditor = true }
                    )

                    // Bottom Floating Action Button Bar
                    androidx.compose.foundation.lazy.LazyRow(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 20.dp, start = 12.dp, end = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            Button(
                                onClick = { photoPickerLauncher.launch("image/*") },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Black.copy(alpha = 0.75f)),
                                shape = RoundedCornerShape(16.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(imageVector = Icons.Default.SwapHoriz, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Đổi ảnh", color = Color.White, fontSize = 12.sp)
                            }
                        }

                        item {
                            Button(
                                onClick = {
                                    showingTemplateDrawer = true
                                    showingFilterDrawer = false
                                    showingStickerDrawer = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SnapSafetyOrange),
                                shape = RoundedCornerShape(16.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Layers, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(loc.t("templates"), color = Color.White, fontSize = 12.sp)
                            }
                        }

                        item {
                            Button(
                                onClick = {
                                    showingFilterDrawer = true
                                    showingTemplateDrawer = false
                                    showingStickerDrawer = false
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (currentTemplate.activeFilter != com.snaplab.cameraft.models.PhotoFilter.ORIGINAL) SnapSafetyOrange else Color.Black.copy(alpha = 0.75f)
                                ),
                                shape = RoundedCornerShape(16.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(imageVector = Icons.Default.AutoFixHigh, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(currentTemplate.activeFilter.getDisplayName(loc.isVietnamese()), color = Color.White, fontSize = 12.sp)
                            }
                        }

                        item {
                            Button(
                                onClick = {
                                    showingStickerDrawer = true
                                    showingTemplateDrawer = false
                                    showingFilterDrawer = false
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (currentTemplate.activeSticker != null) com.snaplab.cameraft.ui.theme.SnapNeonGreen else Color.Black.copy(alpha = 0.75f)
                                ),
                                shape = RoundedCornerShape(16.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Stars, contentDescription = null, tint = if (currentTemplate.activeSticker != null) Color.Black else Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = currentTemplate.activeSticker?.title ?: "Nhãn dán",
                                    color = if (currentTemplate.activeSticker != null) Color.Black else Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        item {
                            Button(
                                onClick = { showingTemplateEditor = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Black.copy(alpha = 0.75f)),
                                shape = RoundedCornerShape(16.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Tune, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Tùy biến", color = Color.White, fontSize = 12.sp)
                            }
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

            // Filter Drawer
            if (showingFilterDrawer) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                            .background(Color(0xF5181A20))
                            .padding(18.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "BỘ LỌC NGHỆ THUẬT & AI",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = { showingFilterDrawer = false }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = Color.White)
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        androidx.compose.foundation.lazy.LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(com.snaplab.cameraft.models.PhotoFilter.values().size) { idx ->
                                val f = com.snaplab.cameraft.models.PhotoFilter.values()[idx]
                                val isSelected = f == currentTemplate.activeFilter
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) SnapSafetyOrange else Color.White.copy(alpha = 0.1f))
                                        .border(1.dp, if (isSelected) Color.White else Color.Transparent, RoundedCornerShape(12.dp))
                                        .clickable {
                                            currentTemplate = currentTemplate.copy(activeFilter = f)
                                            showingFilterDrawer = false
                                        }
                                        .padding(horizontal = 14.dp, vertical = 10.dp)
                                ) {
                                    Text(
                                        text = f.getDisplayName(loc.isVietnamese()),
                                        color = Color.White,
                                        fontSize = 12.5.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }

            // Sticker Drawer
            if (showingStickerDrawer) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                            .background(Color(0xF5181A20))
                            .padding(18.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "NHÃN DÁN TRANG TRÍ (STICKERS)",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = { showingStickerDrawer = false }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = Color.White)
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        androidx.compose.foundation.lazy.LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            item {
                                val isNone = currentTemplate.activeSticker == null
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isNone) SnapSafetyOrange else Color.White.copy(alpha = 0.1f))
                                        .clickable {
                                            currentTemplate = currentTemplate.copy(activeSticker = null)
                                            showingStickerDrawer = false
                                        }
                                        .padding(horizontal = 14.dp, vertical = 10.dp)
                                ) {
                                    Text(
                                        text = if (loc.isVietnamese()) "Không nhãn" else "No Sticker",
                                        color = Color.White,
                                        fontSize = 12.5.sp
                                    )
                                }
                            }
                            items(com.snaplab.cameraft.models.PhotoSticker.values().size) { idx ->
                                val st = com.snaplab.cameraft.models.PhotoSticker.values()[idx]
                                val isSelected = st == currentTemplate.activeSticker
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) st.composeColor.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.1f))
                                        .border(1.5.dp, if (isSelected) st.composeColor else Color.Transparent, RoundedCornerShape(12.dp))
                                        .clickable {
                                            currentTemplate = currentTemplate.copy(activeSticker = st)
                                            showingStickerDrawer = false
                                        }
                                        .padding(horizontal = 14.dp, vertical = 10.dp)
                                ) {
                                    Text(
                                        text = "★ ${st.title}",
                                        color = if (isSelected) st.composeColor else Color.White,
                                        fontSize = 12.5.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
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
