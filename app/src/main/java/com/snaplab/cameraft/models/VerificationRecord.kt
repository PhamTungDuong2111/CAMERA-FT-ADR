package com.snaplab.cameraft.models

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class VerificationRecord(
    val id: String = generateDefaultId(),
    val timestamp: Long = System.currentTimeMillis(),
    val formattedDateString: String = formatFullDateTime(timestamp),
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val altitude: Double = 0.0,
    val addressString: String = "",
    val deviceModel: String = "Android Device",
    val systemVersion: String = "Android",
    val appVersion: String = "SnapLab v2.4 (Android)",
    val projectName: String = "",
    val inspectorName: String = "",
    val sha256Checksum: String = "",
    val digitalSignature: String = ""
) {
    fun toQRCodePayload(): String {
        val t = timestamp / 1000
        val latStr = String.format(Locale.US, "%.5f", latitude)
        val lonStr = String.format(Locale.US, "%.5f", longitude)
        val hashPrefix = if (sha256Checksum.length >= 12) sha256Checksum.substring(0, 12) else sha256Checksum
        return "snaplab://verify?id=$id&t=$t&lat=$latStr&lon=$lonStr&hash=$hashPrefix"
    }

    companion object {
        fun generateDefaultId(): String {
            val dateStr = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
            val randomNum = (1000..9999).random()
            return "SL-$dateStr-$randomNum"
        }

        fun formatFullDateTime(timestamp: Long): String {
            return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(timestamp))
        }

        fun formatDisplayDate(timestamp: Long): String {
            return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(timestamp))
        }

        fun formatDisplayTime(timestamp: Long): String {
            return SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(timestamp))
        }
    }
}
