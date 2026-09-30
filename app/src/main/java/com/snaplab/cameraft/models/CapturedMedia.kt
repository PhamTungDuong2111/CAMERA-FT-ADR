package com.snaplab.cameraft.models

import java.io.File

data class CapturedMedia(
    val id: String,
    val filePath: String,
    val uriString: String,
    val isVideo: Boolean = false,
    val durationSeconds: Long = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val recordId: String = "",
    val projectName: String = "",
    val location: String = ""
) {
    val file: File
        get() = File(filePath)

    val exists: Boolean
        get() = file.exists()
}
