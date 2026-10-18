package com.snaplab.cameraft

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.snaplab.cameraft.ui.anticounterfeit.AntiCounterfeitQueryScreen
import com.snaplab.cameraft.ui.camera.CameraScreen
import com.snaplab.cameraft.ui.theme.SnapBackgroundDark
import com.snaplab.cameraft.ui.theme.SnapLabTheme

import android.view.WindowManager

class MainActivity : ComponentActivity() {

    private var deepLinkQuery: String? = null
    private var hasCameraPermission by mutableStateOf(false)

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val cameraGranted = permissions[Manifest.permission.CAMERA] == true ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        hasCameraPermission = cameraGranted
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Keep screen on while user is in camera mode
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Check if opened via deep link snaplab://verify?...
        intent?.data?.let { uri ->
            if (uri.scheme == "snaplab" && uri.host == "verify") {
                deepLinkQuery = uri.toString()
            }
        }

        checkAndRequestPermissions()

        setContent {
            SnapLabTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = SnapBackgroundDark
                ) {
                    var showDeepLinkVerify by remember { mutableStateOf(deepLinkQuery != null) }

                    CameraScreen(
                        hasCameraPermission = hasCameraPermission,
                        onRequestPermission = { checkAndRequestPermissions() }
                    )

                    if (showDeepLinkVerify) {
                        AntiCounterfeitQueryScreen(
                            onDismiss = {
                                showDeepLinkVerify = false
                                deepLinkQuery = null
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val cameraGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        if (cameraGranted != hasCameraPermission) {
            hasCameraPermission = cameraGranted
        }
    }

    private fun checkAndRequestPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
            permissions.add(Manifest.permission.READ_MEDIA_VIDEO)
        } else {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }

        val ungranted = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (ungranted.isNotEmpty()) {
            permissionLauncher.launch(ungranted.toTypedArray())
        } else {
            hasCameraPermission = true
        }
    }
}
