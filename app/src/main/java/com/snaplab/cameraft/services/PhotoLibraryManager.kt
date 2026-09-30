package com.snaplab.cameraft.services

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
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
    private val _capturesFlow = MutableStateFlow<List<CapturedMedia>>(emptyMap<String, CapturedMedia>().values.toList())
    val capturesFlow: StateFlow<List<CapturedMedia>> = _capturesFlow.asStateFlow()

    private val capturesList = mutableListOf<CapturedMedia>()

    init {
        loadExistingCaptures()
    }

    private fun loadExistingCaptures() {
        CoroutineScope(Dispatchers.IO).launch {
            val dir = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "SnapLab")
            if (dir.exists()) {
                val files = dir.listFiles { file -> file.extension.lowercase() in listOf("jpg", "jpeg", "png", "mp4") }
                files?.sortedByDescending { it.lastModified() }?.forEach { f ->
                    val isVid = f.extension.lowercase() == "mp4"
                    capturesList.add(
                        CapturedMedia(
                            id = f.nameWithoutExtension,
                            filePath = f.absolutePath,
                            uriString = Uri.fromFile(f).toString(),
                            isVideo = isVid,
                            createdAt = f.lastModified(),
                            projectName = "SnapLab Capture"
                        )
                    )
                }
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

        // 1. Save to App private Pictures directory
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
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/SnapLab")
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
            }

            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            if (uri != null) {
                resolver.openOutputStream(uri)?.use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    resolver.update(uri, contentValues, null, null)
                }
                publicUriString = uri.toString()
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
            projectName = template.projectName,
            location = record?.addressString ?: ""
        )

        capturesList.add(0, captured)
        _capturesFlow.value = capturesList.toList()

        return captured
    }

    fun deleteMedia(item: CapturedMedia) {
        val file = File(item.filePath)
        if (file.exists()) {
            file.delete()
        }
        capturesList.removeAll { it.filePath == item.filePath || it.id == item.id }
        _capturesFlow.value = capturesList.toList()
    }

    companion object {
        val shared by lazy { PhotoLibraryManager() }
    }
}
