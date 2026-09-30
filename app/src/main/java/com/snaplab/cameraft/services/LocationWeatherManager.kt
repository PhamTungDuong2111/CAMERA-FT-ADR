package com.snaplab.cameraft.services

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import com.google.android.gms.location.*
import com.snaplab.cameraft.SnapLabApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.roundToInt

data class LocationState(
    val latitude: Double = 10.776889,
    val longitude: Double = 106.700806,
    val altitude: Double = 16.5,
    val fullAddress: String = "Bến Nghé, Quận 1, TP. Hồ Chí Minh",
    val compassDegrees: Float = 45f,
    val compassDirection: String = "Đông Bắc (NE 45°)",
    val temperatureCelsius: Int = 31,
    val humidityPercent: Int = 68,
    val weatherCondition: String = "Nắng nhẹ / Quang đãng ☀️"
)

class LocationWeatherManager private constructor() : SensorEventListener {

    private val context = SnapLabApplication.instance
    private val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val _locationState = MutableStateFlow(LocationState())
    val locationState: StateFlow<LocationState> = _locationState.asStateFlow()

    private var accelerometerReading = FloatArray(3)
    private var magnetometerReading = FloatArray(3)
    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    private var isTracking = false

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let { updateLocation(it) }
        }
    }

    fun startTracking() {
        if (isTracking) return
        isTracking = true

        // 1. Request GPS Location
        try {
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
                .setMinUpdateIntervalMillis(2000)
                .build()

            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
            fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                if (loc != null) updateLocation(loc)
            }
        } catch (e: SecurityException) {
            // Permissions not granted yet - fallback gracefully
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 2. Start Compass Sensor
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
        }
        if (magneticField != null) {
            sensorManager.registerListener(this, magneticField, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stopTracking() {
        if (!isTracking) return
        isTracking = false
        fusedLocationClient.removeLocationUpdates(locationCallback)
        sensorManager.unregisterListener(this)
    }

    private fun updateLocation(loc: Location) {
        val lat = loc.latitude
        val lon = loc.longitude
        val alt = if (loc.hasAltitude()) loc.altitude else 16.5

        CoroutineScope(Dispatchers.IO).launch {
            var addr = _locationState.value.fullAddress
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = geocoder.getFromLocation(lat, lon, 1)
                if (!addresses.isNullOrEmpty()) {
                    val a = addresses[0]
                    val parts = listOfNotNull(
                        a.thoroughfare ?: a.subLocality,
                        a.locality ?: a.subAdminArea,
                        a.adminArea,
                        a.countryName
                    ).filter { it.isNotBlank() }
                    if (parts.isNotEmpty()) {
                        addr = parts.joinToString(", ")
                    }
                }
            } catch (e: Exception) {
                // Keep existing or simulated address
            }

            _locationState.value = _locationState.value.copy(
                latitude = lat,
                longitude = lon,
                altitude = alt,
                fullAddress = addr
            )
        }
    }

    // SensorEventListener for Compass
    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, accelerometerReading, 0, accelerometerReading.size)
        } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magnetometerReading, 0, magnetometerReading.size)
        }

        SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerReading, magnetometerReading)
        SensorManager.getOrientation(rotationMatrix, orientationAngles)

        val azimuthRadians = orientationAngles[0]
        var azimuthDegrees = Math.toDegrees(azimuthRadians.toDouble()).toFloat()
        if (azimuthDegrees < 0) {
            azimuthDegrees += 360f
        }

        val dirText = degreesToCompassDirection(azimuthDegrees, LocalizationManager.shared.isVietnamese())
        _locationState.value = _locationState.value.copy(
            compassDegrees = azimuthDegrees,
            compassDirection = dirText
        )
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun degreesToCompassDirection(degrees: Float, isVi: Boolean): String {
        val deg = degrees.roundToInt()
        val sector = ((degrees + 22.5f) / 45).toInt() % 8
        return when (sector) {
            0 -> if (isVi) "Bắc (N $deg°)" else "North (N $deg°)"
            1 -> if (isVi) "Đông Bắc (NE $deg°)" else "North-East (NE $deg°)"
            2 -> if (isVi) "Đông (E $deg°)" else "East (E $deg°)"
            3 -> if (isVi) "Đông Nam (SE $deg°)" else "South-East (SE $deg°)"
            4 -> if (isVi) "Nam (S $deg°)" else "South (S $deg°)"
            5 -> if (isVi) "Tây Nam (SW $deg°)" else "South-West (SW $deg°)"
            6 -> if (isVi) "Tây (W $deg°)" else "West (W $deg°)"
            7 -> if (isVi) "Tây Bắc (NW $deg°)" else "North-West (NW $deg°)"
            else -> "$deg°"
        }
    }

    fun formattedCoordinates(lat: Double, lon: Double): String {
        val latDir = if (lat >= 0) "N" else "S"
        val lonDir = if (lon >= 0) "E" else "W"
        val latDMS = toDMS(Math.abs(lat))
        val lonDMS = toDMS(Math.abs(lon))
        return "$latDMS $latDir, $lonDMS $lonDir"
    }

    private fun toDMS(coord: Double): String {
        val d = coord.toInt()
        val m = ((coord - d) * 60).toInt()
        val s = ((coord - d - m / 60.0) * 3600).roundToInt()
        return "$d°$m'$s\""
    }

    companion object {
        val shared by lazy { LocationWeatherManager() }
    }
}
