package com.snaplab.cameraft.services

import android.content.Context
import android.os.SystemClock
import com.snaplab.cameraft.SnapLabApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class RealTimeInfo(
    val currentTimeMillis: Long = System.currentTimeMillis(),
    val isAntiTamperActive: Boolean = true,
    val isTamperDetected: Boolean = false,
    val timeSource: String = "GPS/NTP Bảo mật",
    val formattedDisplay: String = ""
)

/**
 * RealTimeManager: Cung cấp thời gian thực chống tua ngược giờ máy (Anti-Clock-Tampering Engine).
 * Tự động đồng bộ với đồng hồ nguyên tử vệ tinh GPS và máy chủ thời gian mạng (Network Time).
 * Bảo vệ tính toàn vẹn của con dấu và ngăn chặn gian lận lùi giờ hệ thống trên thiết bị.
 */
class RealTimeManager private constructor() {

    private val context = SnapLabApplication.instance
    private val prefs = context.getSharedPreferences("snaplab_anti_tamper_time", Context.MODE_PRIVATE)
    private val scope = CoroutineScope(Dispatchers.IO)

    private var tickerJob: Job? = null

    // Anchor: Network or GPS satellite reference
    private var anchorRealTimeMs: Long = 0L
    private var anchorElapsedRealtimeMs: Long = 0L
    private var isSynchronizedWithExternalSource: Boolean = false
    private var currentSourceDescription: String = "Thiết bị an toàn"
    private var tamperDetected: Boolean = false

    private val _realTimeState = MutableStateFlow(RealTimeInfo())
    val realTimeState: StateFlow<RealTimeInfo> = _realTimeState.asStateFlow()

    init {
        // Load persistent highest timestamp ever recorded to prevent rollback
        val savedHighest = prefs.getLong(KEY_HIGHEST_TRUSTED_TIME, 0L)
        val savedElapsed = prefs.getLong(KEY_LAST_SAVED_ELAPSED, 0L)
        val currentDeviceTime = System.currentTimeMillis()

        if (savedHighest > 0L && currentDeviceTime < savedHighest) {
            // Tamper detected: System clock has been set backward!
            tamperDetected = true
            val elapsedDelta = (SystemClock.elapsedRealtime() - savedElapsed).coerceAtLeast(0L)
            anchorRealTimeMs = savedHighest + elapsedDelta
            anchorElapsedRealtimeMs = SystemClock.elapsedRealtime()
            currentSourceDescription = "Phát hiện tua ngược - Đã tự bảo vệ"
        } else {
            anchorRealTimeMs = currentDeviceTime
            anchorElapsedRealtimeMs = SystemClock.elapsedRealtime()
            if (savedHighest > 0L) {
                currentSourceDescription = "Chống tua ngược hoạt động"
            }
        }

        // Trigger network time sync in background
        syncNetworkTime()

        // Start 1-second ticker for live camera UI
        startTicker()
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = scope.launch {
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            while (isActive) {
                val nowMs = getVerifiedTimestamp()
                _realTimeState.value = RealTimeInfo(
                    currentTimeMillis = nowMs,
                    isAntiTamperActive = true,
                    isTamperDetected = tamperDetected,
                    timeSource = currentSourceDescription,
                    formattedDisplay = formatter.format(Date(nowMs))
                )
                delay(1000)
            }
        }
    }

    /**
     * Đồng bộ thời gian với đồng hồ nguyên tử vệ tinh GPS khi nhận được tín hiệu định vị thực tế.
     */
    fun syncWithGpsTime(gpsTimeMs: Long) {
        if (gpsTimeMs <= 0L) return
        scope.launch {
            val savedHighest = prefs.getLong(KEY_HIGHEST_TRUSTED_TIME, 0L)
            val currentDeviceTime = System.currentTimeMillis()

            // If device clock differs significantly (> 60s) from GPS atomic time
            if (Math.abs(currentDeviceTime - gpsTimeMs) > 60_000L) {
                tamperDetected = true
            }

            val validGpsTime = if (savedHighest > gpsTimeMs) {
                savedHighest + 1000L
            } else {
                gpsTimeMs
            }

            anchorRealTimeMs = validGpsTime
            anchorElapsedRealtimeMs = SystemClock.elapsedRealtime()
            isSynchronizedWithExternalSource = true
            currentSourceDescription = "Vệ tinh GPS (Chuẩn nguyên tử)"

            persistHighestTime(validGpsTime)
        }
    }

    /**
     * Đồng bộ thời gian qua HTTP Head Request tới máy chủ quốc tế (Google / Cloudflare / NTP).
     */
    fun syncNetworkTime() {
        scope.launch {
            val endpoints = listOf(
                "https://www.google.com",
                "https://cloudflare.com",
                "https://www.apple.com"
            )

            for (urlStr in endpoints) {
                try {
                    val url = URL(urlStr)
                    val conn = (url.openConnection() as HttpURLConnection).apply {
                        requestMethod = "HEAD"
                        connectTimeout = 4000
                        readTimeout = 4000
                        instanceFollowRedirects = false
                    }
                    conn.connect()
                    val serverDate = conn.date
                    conn.disconnect()

                    if (serverDate > 1_600_000_000_000L) { // Valid timestamp
                        val currentSysTime = System.currentTimeMillis()
                        if (Math.abs(currentSysTime - serverDate) > 60_000L) {
                            tamperDetected = true
                        }

                        val savedHighest = prefs.getLong(KEY_HIGHEST_TRUSTED_TIME, 0L)
                        val validTime = if (savedHighest > serverDate) savedHighest + 1000L else serverDate

                        anchorRealTimeMs = validTime
                        anchorElapsedRealtimeMs = SystemClock.elapsedRealtime()
                        isSynchronizedWithExternalSource = true
                        currentSourceDescription = "Máy chủ thời gian chuẩn (NTP Network)"

                        persistHighestTime(validTime)
                        break
                    }
                } catch (_: Exception) {
                    // Try next endpoint
                }
            }
        }
    }

    /**
     * Lấy mốc thời gian thực hiện tại đã được kiểm chứng và khóa chống tua ngược giờ máy.
     */
    fun getVerifiedTimestamp(): Long {
        val calculated = if (anchorRealTimeMs > 0L) {
            val elapsedDelta = (SystemClock.elapsedRealtime() - anchorElapsedRealtimeMs).coerceAtLeast(0L)
            anchorRealTimeMs + elapsedDelta
        } else {
            System.currentTimeMillis()
        }

        // Monotonic check against persistent highest time
        val savedHighest = prefs.getLong(KEY_HIGHEST_TRUSTED_TIME, 0L)
        val finalTime = if (calculated < savedHighest) {
            tamperDetected = true
            val savedElapsed = prefs.getLong(KEY_LAST_SAVED_ELAPSED, 0L)
            val elapsedDelta = (SystemClock.elapsedRealtime() - savedElapsed).coerceAtLeast(0L)
            savedHighest + elapsedDelta
        } else {
            calculated
        }

        persistHighestTime(finalTime)
        return finalTime
    }

    private fun persistHighestTime(timeMs: Long) {
        val currentHighest = prefs.getLong(KEY_HIGHEST_TRUSTED_TIME, 0L)
        if (timeMs > currentHighest) {
            prefs.edit()
                .putLong(KEY_HIGHEST_TRUSTED_TIME, timeMs)
                .putLong(KEY_LAST_SAVED_ELAPSED, SystemClock.elapsedRealtime())
                .apply()
        }
    }

    fun isClockTampered(): Boolean = tamperDetected

    companion object {
        private const val KEY_HIGHEST_TRUSTED_TIME = "key_highest_trusted_time_millis"
        private const val KEY_LAST_SAVED_ELAPSED = "key_last_saved_elapsed_millis"

        val shared: RealTimeManager by lazy { RealTimeManager() }
    }
}
