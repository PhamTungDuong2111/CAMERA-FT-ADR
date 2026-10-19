package com.snaplab.cameraft.services

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.snaplab.cameraft.SnapLabApplication
import com.snaplab.cameraft.models.CapturedMedia
import com.snaplab.cameraft.models.VerificationRecord
import com.snaplab.cameraft.models.WatermarkTemplate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PhotoLibraryManager private constructor() {

    private val context = SnapLabApplication.instance
    private val _capturesFlow = MutableStateFlow<List<CapturedMedia>>(emptyList())
    val capturesFlow: StateFlow<List<CapturedMedia>> = _capturesFlow.asStateFlow()

    private val capturesList = mutableListOf<CapturedMedia>()

    init {
        loadExistingCaptures()
    }

    private fun loadExistingCaptures() {
        CoroutineScope(Dispatchers.IO).launch {
            val loadedItems = mutableListOf<CapturedMedia>()

            // Load Pictures
            val picDir = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "SnapLab")
            if (picDir.exists()) {
                val files = picDir.listFiles { file -> file.extension.lowercase() in listOf("jpg", "jpeg", "png") }
                files?.forEach { f ->
                    loadedItems.add(
                        CapturedMedia(
                            id = f.nameWithoutExtension,
                            filePath = f.absolutePath,
                            uriString = Uri.fromFile(f).toString(),
                            isVideo = false,
                            createdAt = f.lastModified(),
                            projectName = "SnapLab Photo"
                        )
                    )
                }
            }

            // Load Movies
            val movieDir = File(context.getExternalFilesDir(Environment.DIRECTORY_MOVIES), "SnapLab")
            if (movieDir.exists()) {
                val files = movieDir.listFiles { file -> file.extension.lowercase() in listOf("mp4", "mkv", "3gp") }
                files?.forEach { f ->
                    loadedItems.add(
                        CapturedMedia(
                            id = f.nameWithoutExtension,
                            filePath = f.absolutePath,
                            uriString = Uri.fromFile(f).toString(),
                            isVideo = true,
                            createdAt = f.lastModified(),
                            projectName = "SnapLab Video"
                        )
                    )
                }
            }

            synchronized(capturesList) {
                capturesList.clear()
                capturesList.addAll(loadedItems.sortedByDescending { it.createdAt })
                _capturesFlow.value = capturesList.toList()
            }
        }
    }

    fun saveWatermarkedPhoto(
        bitmap: Bitmap,
        template: WatermarkTemplate,
        record: VerificationRecord?
    ): CapturedMedia? {
        val timestamp = System.currentTimeMillis()
        val dateStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date(timestamp))
        val filename = "SnapLab_$dateStr.jpg"

        // 1. Save to App private Pictures directory (always reliable)
        val privateDir = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "SnapLab")
        if (!privateDir.exists()) privateDir.mkdirs()
        val privateFile = File(privateDir, filename)

        try {
            FileOutputStream(privateFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }

        // 2. Also export to public MediaStore so user sees it in Android Photo Gallery app
        var publicUriString = Uri.fromFile(privateFile).toString()
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/SnapLab")
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }

                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { out ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
                    }
                    contentValues.clear()
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    resolver.update(uri, contentValues, null, null)
                    publicUriString = uri.toString()
                }
            } else {
                // Android 9 and lower: write to public storage and notify MediaScanner
                try {
                    val publicDir = File(
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                        "SnapLab"
                    )
                    if (!publicDir.exists()) publicDir.mkdirs()
                    val publicFile = File(publicDir, filename)
                    FileOutputStream(publicFile).use { out ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
                    }
                    MediaScannerConnection.scanFile(
                        context,
                        arrayOf(publicFile.absolutePath),
                        arrayOf("image/jpeg")
                    ) { _, uri ->
                        if (uri != null) {
                            publicUriString = uri.toString()
                        }
                    }
                    publicUriString = Uri.fromFile(publicFile).toString()
                } catch (e: Exception) {
                    e.printStackTrace()
                    // Fallback to scanning private file
                    MediaScannerConnection.scanFile(
                        context,
                        arrayOf(privateFile.absolutePath),
                        arrayOf("image/jpeg"),
                        null
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val captured = CapturedMedia(
            id = record?.id ?: filename,
            filePath = privateFile.absolutePath,
            uriString = publicUriString,
            isVideo = false,
            createdAt = timestamp,
            recordId = record?.id ?: "",
            projectName = record?.projectName ?: template.getProjectName(LocalizationManager.shared.isVietnamese()),
            location = record?.addressString ?: ""
        )

        synchronized(capturesList) {
            capturesList.add(0, captured)
            _capturesFlow.value = capturesList.toList()
        }

        return captured
    }

    fun saveRecordedVideo(
        videoFile: File,
        projectName: String = "SnapLab Video"
    ): CapturedMedia? {
        val timestamp = System.currentTimeMillis()
        val dateStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date(timestamp))
        val filename = "SnapLab_$dateStr.mp4"

        // 1. Copy to private Movies / SnapLab
        val privateDir = File(context.getExternalFilesDir(Environment.DIRECTORY_MOVIES), "SnapLab")
        if (!privateDir.exists()) privateDir.mkdirs()
        val privateDestFile = File(privateDir, filename)

        try {
            videoFile.copyTo(privateDestFile, overwrite = true)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }

        var publicUriString = Uri.fromFile(privateDestFile).toString()
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.Video.Media.DISPLAY_NAME, filename)
                    put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                    put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES + "/SnapLab")
                    put(MediaStore.Video.Media.IS_PENDING, 1)
                }
                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { out ->
                        privateDestFile.inputStream().use { input ->
                            input.copyTo(out)
                        }
                    }
                    contentValues.clear()
                    contentValues.put(MediaStore.Video.Media.IS_PENDING, 0)
                    resolver.update(uri, contentValues, null, null)
                    publicUriString = uri.toString()
                }
            } else {
                try {
                    val publicDir = File(
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
                        "SnapLab"
                    )
                    if (!publicDir.exists()) publicDir.mkdirs()
                    val publicFile = File(publicDir, filename)
                    privateDestFile.copyTo(publicFile, overwrite = true)
                    MediaScannerConnection.scanFile(
                        context,
                        arrayOf(publicFile.absolutePath),
                        arrayOf("video/mp4")
                    ) { _, uri ->
                        if (uri != null) publicUriString = uri.toString()
                    }
                    publicUriString = Uri.fromFile(publicFile).toString()
                } catch (e: Exception) {
                    e.printStackTrace()
                    MediaScannerConnection.scanFile(
                        context,
                        arrayOf(privateDestFile.absolutePath),
                        arrayOf("video/mp4"),
                        null
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Delete temporary video file
        try {
            videoFile.delete()
        } catch (_: Exception) {}

        val captured = CapturedMedia(
            id = "VID-$timestamp",
            filePath = privateDestFile.absolutePath,
            uriString = publicUriString,
            isVideo = true,
            createdAt = timestamp,
            recordId = "VID-$timestamp",
            projectName = projectName,
            location = LocationWeatherManager.shared.locationState.value.fullAddress
        )

        synchronized(capturesList) {
            capturesList.add(0, captured)
            _capturesFlow.value = capturesList.toList()
        }

        return captured
    }

    fun deleteMedia(item: CapturedMedia) {
        val file = File(item.filePath)
        if (file.exists()) {
            file.delete()
        }
        synchronized(capturesList) {
            capturesList.removeAll { it.filePath == item.filePath || it.id == item.id }
            _capturesFlow.value = capturesList.toList()
        }
    }

    companion object {
        val shared by lazy { PhotoLibraryManager() }
    }
}
