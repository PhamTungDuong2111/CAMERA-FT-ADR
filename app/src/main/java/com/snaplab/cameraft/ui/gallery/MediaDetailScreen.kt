package com.snaplab.cameraft.ui.gallery

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.snaplab.cameraft.models.CapturedMedia
import com.snaplab.cameraft.models.VerificationRecord
import com.snaplab.cameraft.services.AntiCounterfeitingManager
import com.snaplab.cameraft.services.LocalizationManager
import com.snaplab.cameraft.services.PhotoLibraryManager
import com.snaplab.cameraft.ui.anticounterfeit.AntiCounterfeitQueryScreen
import com.snaplab.cameraft.ui.theme.SnapBackgroundDark
import com.snaplab.cameraft.ui.theme.SnapCardDark
import com.snaplab.cameraft.ui.theme.SnapDangerRed
import com.snaplab.cameraft.ui.theme.SnapNeonGreen
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaDetailScreen(
    media: CapturedMedia,
    onBack: () -> Unit,
    onDeleted: () -> Unit
) {
    val context = LocalContext.current
    val loc = LocalizationManager.shared
    var showVerifyQuery by remember { mutableStateOf(false) }

    val record = remember(media) {
        AntiCounterfeitingManager.shared.recordsFlow.value[media.recordId]
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (media.projectName.isNotBlank()) media.projectName else "SnapLab Media",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    // Share
                    IconButton(onClick = {
                        try {
                            val file = File(media.filePath)
                            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = if (media.isVideo) "video/mp4" else "image/jpeg"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, loc.t("share")))
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Share", tint = Color.White)
                    }

                    // Delete
                    IconButton(onClick = {
                        PhotoLibraryManager.shared.deleteMedia(media)
                        onDeleted()
                    }) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = SnapDangerRed)
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
        ) {
            // Main Media Preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp)
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = File(media.filePath),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )

                if (media.isVideo) {
                    Icon(
                        imageVector = Icons.Default.PlayCircle,
                        contentDescription = "Play Video",
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(64.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Metadata Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SnapCardDark)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "THÔNG TIN CHI TIẾT",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )

                    DetailRow("Tập tin:", File(media.filePath).name)
                    DetailRow("Thời gian:", VerificationRecord.formatFullDateTime(media.createdAt))
                    if (media.location.isNotBlank()) {
                        DetailRow("Vị trí:", media.location)
                    }
                    if (media.recordId.isNotBlank()) {
                        DetailRow("Mã định danh:", media.recordId, isMono = true)
                    }
                    if (record != null) {
                        DetailRow("Thiết bị:", record.deviceModel)
                        DetailRow("Mã băm SHA-256:", record.sha256Checksum, isMono = true)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Button: Verify Signature
                    Button(
                        onClick = { showVerifyQuery = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SnapNeonGreen)
                    ) {
                        Icon(imageVector = Icons.Default.VerifiedUser, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Đối Soát Chống Giả Mạo", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        if (showVerifyQuery) {
            AntiCounterfeitQueryScreen(onDismiss = { showVerifyQuery = false })
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String, isMono: Boolean = false) {
    Column {
        Text(text = label, color = Color.White.copy(alpha = 0.5f), fontSize = 10.5.sp)
        Text(
            text = value,
            color = Color.White,
            fontSize = if (isMono) 10.5.sp else 12.5.sp,
            fontFamily = if (isMono) FontFamily.Monospace else FontFamily.Default,
            fontWeight = FontWeight.Medium
        )
    }
}
