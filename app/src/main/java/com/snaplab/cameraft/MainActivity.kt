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

class MainActivity : ComponentActivity() {

    private var deepLinkQuery: String? = null

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Permissions handled
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

                    CameraScreen()

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
        }

        val ungranted = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (ungranted.isNotEmpty()) {
            permissionLauncher.launch(ungranted.toTypedArray())
        }
    }
}
