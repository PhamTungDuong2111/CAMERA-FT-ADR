package com.snaplab.cameraft.ui.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.snaplab.cameraft.models.CapturedMedia
import com.snaplab.cameraft.services.LocalizationManager
import com.snaplab.cameraft.services.PhotoLibraryManager
import com.snaplab.cameraft.ui.theme.SnapBackgroundDark
import com.snaplab.cameraft.ui.theme.SnapCardDark
import com.snaplab.cameraft.ui.theme.SnapSafetyOrange
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(
    onDismiss: () -> Unit
) {
    val loc = LocalizationManager.shared
    val captures by PhotoLibraryManager.shared.capturesFlow.collectAsState()

    var filterType by remember { mutableStateOf(0) } // 0: All, 1: Photos, 2: Videos
    var selectedMedia by remember { mutableStateOf<CapturedMedia?>(null) }
    var showAlbumEditor by remember { mutableStateOf(false) }

    val filteredList = remember(captures, filterType) {
        when (filterType) {
            1 -> captures.filter { !it.isVideo }
            2 -> captures.filter { it.isVideo }
            else -> captures
        }
    }

    if (selectedMedia != null) {
        MediaDetailScreen(
            media = selectedMedia!!,
            onBack = { selectedMedia = null },
            onDeleted = { selectedMedia = null }
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = loc.t("galleryTitle"),
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
                    // Button: Stamp photo from device album
                    Button(
                        onClick = { showAlbumEditor = true },
                        colors = ButtonDefaults.buttonColors(containerColor = SnapSafetyOrange),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.AddPhotoAlternate, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Đóng dấu", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
        ) {
            // Filter Tabs
            TabRow(
                selectedTabIndex = filterType,
                containerColor = SnapCardDark,
                contentColor = SnapSafetyOrange
            ) {
                Tab(
                    selected = filterType == 0,
                    onClick = { filterType = 0 },
                    text = { Text(loc.t("all"), color = if (filterType == 0) SnapSafetyOrange else Color.White.copy(alpha = 0.6f)) }
                )
                Tab(
                    selected = filterType == 1,
                    onClick = { filterType = 1 },
                    text = { Text(loc.t("photo"), color = if (filterType == 1) SnapSafetyOrange else Color.White.copy(alpha = 0.6f)) }
                )
                Tab(
                    selected = filterType == 2,
                    onClick = { filterType = 2 },
                    text = { Text(loc.t("video"), color = if (filterType == 2) SnapSafetyOrange else Color.White.copy(alpha = 0.6f)) }
                )
            }

            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.3f),
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = loc.t("noMediaYet"),
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(filteredList) { item ->
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .background(Color.DarkGray)
                                .clickable { selectedMedia = item }
                        ) {
                            AsyncImage(
                                model = File(item.filePath),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )

                            if (item.isVideo) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(4.dp)
                                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Videocam,
                                        contentDescription = "Video",
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showAlbumEditor) {
            AlbumWatermarkEditorScreen(onDismiss = { showAlbumEditor = false })
        }
    }
}
