package com.snaplab.cameraft.services

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.MediaActionSound
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.snaplab.cameraft.SnapLabApplication
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import android.view.OrientationEventListener
import android.view.Surface
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

enum class FlashMode(val iconRes: String, val titleVi: String, val titleEn: String) {
    OFF("flash_off", "Tắt", "Off"),
    ON("flash_on", "Bật", "On"),
    AUTO("flash_auto", "Tự động", "Auto"),
    TORCH("highlight", "Đèn pin", "Torch");
}

enum class CameraAspectRatio(val title: String, val ratio: Int) {
    RATIO_4_3("4:3", AspectRatio.RATIO_4_3),
    RATIO_16_9("16:9", AspectRatio.RATIO_16_9);
}

class CameraManager private constructor() {

    private val context = SnapLabApplication.instance
    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private val sound = MediaActionSound().apply {
        try { load(MediaActionSound.SHUTTER_CLICK) } catch (_: Exception) {}
    }

    private var cameraProvider: ProcessCameraProvider? = null
    private var camera: Camera? = null
    private var imageCapture: ImageCapture? = null
    private var videoCapture: VideoCapture<Recorder>? = null
    private var activeRecording: Recording? = null

    // State Flows
    private val _isFrontCamera = MutableStateFlow(false)
    val isFrontCamera: StateFlow<Boolean> = _isFrontCamera.asStateFlow()

    private val _flashMode = MutableStateFlow(FlashMode.AUTO)
    val flashMode: StateFlow<FlashMode> = _flashMode.asStateFlow()

    private val _aspectRatio = MutableStateFlow(CameraAspectRatio.RATIO_4_3)
    val aspectRatio: StateFlow<CameraAspectRatio> = _aspectRatio.asStateFlow()

    private val _zoomRatio = MutableStateFlow(1.0f)
    val zoomRatio: StateFlow<Float> = _zoomRatio.asStateFlow()

    private val _isRecordingVideo = MutableStateFlow(false)
    val isRecordingVideo: StateFlow<Boolean> = _isRecordingVideo.asStateFlow()

    private val _recordingDurationSec = MutableStateFlow(0L)
    val recordingDurationSec: StateFlow<Long> = _recordingDurationSec.asStateFlow()

    private val _isSyntheticMode = MutableStateFlow(false)
    val isSyntheticMode: StateFlow<Boolean> = _isSyntheticMode.asStateFlow()

    private val _showGrid = MutableStateFlow(true)
    val showGrid: StateFlow<Boolean> = _showGrid.asStateFlow()

    private var orientationEventListener: OrientationEventListener? = null

    private val _deviceRotation = MutableStateFlow(Surface.ROTATION_0)
    val deviceRotation: StateFlow<Int> = _deviceRotation.asStateFlow()

    private val _deviceOrientationDegrees = MutableStateFlow(0)
    val deviceOrientationDegrees: StateFlow<Int> = _deviceOrientationDegrees.asStateFlow()

    // Callbacks
    var onPhotoCaptured: ((Bitmap) -> Unit)? = null
    var onVideoRecorded: ((File) -> Unit)? = null

    fun startOrientationListener() {
        if (orientationEventListener == null) {
            orientationEventListener = object : OrientationEventListener(context) {
                override fun onOrientationChanged(orientation: Int) {
                    if (orientation == ORIENTATION_UNKNOWN) return

                    val roundedAngle = when (orientation) {
                        in 45 until 135 -> 270
                        in 135 until 225 -> 180
                        in 225 until 315 -> 90
                        else -> 0
                    }
                    _deviceOrientationDegrees.value = roundedAngle

                    val rotation = when (orientation) {
                        in 45 until 135 -> Surface.ROTATION_270
                        in 135 until 225 -> Surface.ROTATION_180
                        in 225 until 315 -> Surface.ROTATION_90
                        else -> Surface.ROTATION_0
                    }

                    if (_deviceRotation.value != rotation) {
                        _deviceRotation.value = rotation
                        imageCapture?.targetRotation = rotation
                        try {
                            videoCapture?.targetRotation = rotation
                        } catch (_: Exception) {}
                    }
                }
            }
        }
        if (orientationEventListener?.canDetectOrientation() == true) {
            orientationEventListener?.enable()
        }
    }

    fun stopOrientationListener() {
        orientationEventListener?.disable()
    }

    fun toggleGrid() {
        _showGrid.value = !_showGrid.value
    }

    fun toggleFlash() {
        _flashMode.value = when (_flashMode.value) {
            FlashMode.AUTO -> FlashMode.ON
            FlashMode.ON -> FlashMode.TORCH
            FlashMode.TORCH -> FlashMode.OFF
            FlashMode.OFF -> FlashMode.AUTO
        }
        applyFlashSettings()
    }

    fun toggleCameraFacing(lifecycleOwner: LifecycleOwner, previewView: PreviewView) {
        _isFrontCamera.value = !_isFrontCamera.value
        startCamera(lifecycleOwner, previewView)
    }

    fun cycleAspectRatio(lifecycleOwner: LifecycleOwner, previewView: PreviewView) {
        _aspectRatio.value = if (_aspectRatio.value == CameraAspectRatio.RATIO_4_3) {
            CameraAspectRatio.RATIO_16_9
        } else {
            CameraAspectRatio.RATIO_4_3
        }
        startCamera(lifecycleOwner, previewView)
    }

    fun setZoom(ratio: Float) {
        _zoomRatio.value = ratio
        camera?.cameraControl?.setZoomRatio(ratio)
    }

    fun startCamera(lifecycleOwner: LifecycleOwner, previewView: PreviewView) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                bindCameraUseCases(lifecycleOwner, previewView)
            } catch (e: Exception) {
                e.printStackTrace()
                // If on simulator with no camera hardware, fallback to synthetic camera
                _isSyntheticMode.value = true
            }
        }, ContextCompat.getMainExecutor(context))
    }

    private fun bindCameraUseCases(lifecycleOwner: LifecycleOwner, previewView: PreviewView) {
        val provider = cameraProvider ?: return

        val cameraSelector = if (_isFrontCamera.value) {
            CameraSelector.DEFAULT_FRONT_CAMERA
        } else {
            CameraSelector.DEFAULT_BACK_CAMERA
        }

        val targetRatio = _aspectRatio.value.ratio

        val preview = Preview.Builder()
            .setTargetAspectRatio(targetRatio)
            .build()
            .also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

        val currentRot = _deviceRotation.value

        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            .setTargetAspectRatio(targetRatio)
            .setTargetRotation(currentRot)
            .build()

        val qualitySelector = QualitySelector.fromOrderedList(
            listOf(Quality.FHD, Quality.HD, Quality.SD),
            FallbackStrategy.lowerQualityOrHigherThan(Quality.SD)
        )
        val recorder = Recorder.Builder()
            .setQualitySelector(qualitySelector)
            .build()
        videoCapture = VideoCapture.withOutput(recorder)

        try {
            provider.unbindAll()
            camera = provider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture,
                videoCapture
            )
            _isSyntheticMode.value = false
            applyFlashSettings()
            camera?.cameraControl?.setZoomRatio(_zoomRatio.value)
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback for budget/older devices that cannot bind 3 use cases concurrently
            try {
                videoCapture = null
                provider.unbindAll()
                camera = provider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
                _isSyntheticMode.value = false
                applyFlashSettings()
                camera?.cameraControl?.setZoomRatio(_zoomRatio.value)
            } catch (e2: Exception) {
                e2.printStackTrace()
                _isSyntheticMode.value = true
            }
        }
    }

    private fun applyFlashSettings() {
        val cap = imageCapture ?: return
        when (_flashMode.value) {
            FlashMode.AUTO -> {
                cap.flashMode = ImageCapture.FLASH_MODE_AUTO
                camera?.cameraControl?.enableTorch(false)
            }
            FlashMode.ON -> {
                cap.flashMode = ImageCapture.FLASH_MODE_ON
                camera?.cameraControl?.enableTorch(false)
            }
            FlashMode.OFF -> {
                cap.flashMode = ImageCapture.FLASH_MODE_OFF
                camera?.cameraControl?.enableTorch(false)
            }
            FlashMode.TORCH -> {
                cap.flashMode = ImageCapture.FLASH_MODE_OFF
                camera?.cameraControl?.enableTorch(true)
            }
        }
    }

    // MARK: - Capture Photo
    fun capturePhoto() {
        try {
            sound.play(MediaActionSound.SHUTTER_CLICK)
        } catch (_: Exception) {}

        if (_isSyntheticMode.value || imageCapture == null) {
            // Synthetic scene fallback
            val isPortrait = (_deviceRotation.value == Surface.ROTATION_0 || _deviceRotation.value == Surface.ROTATION_180)
            val w = if (isPortrait) 1440 else 1920
            val h = if (isPortrait) 1920 else 1440
            val dummyBitmap = ImageUtils.generateSyntheticScene(width = w, height = h)
            onPhotoCaptured?.invoke(dummyBitmap)
            return
        }

        val capture = imageCapture ?: return
        val tempFile = File(context.cacheDir, "snap_temp_${System.currentTimeMillis()}.jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(tempFile).build()

        capture.takePicture(
            outputOptions,
            cameraExecutor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    try {
                        val finalBitmap = ImageUtils.decodeAndRotateBitmap(
                            filePath = tempFile.absolutePath,
                            isFrontCamera = _isFrontCamera.value,
                            fallbackDeviceRotation = _deviceRotation.value
                        )
                        if (finalBitmap != null) {
                            onPhotoCaptured?.invoke(finalBitmap)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        try { tempFile.delete() } catch (_: Exception) {}
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    exception.printStackTrace()
                    try { tempFile.delete() } catch (_: Exception) {}
                    // Fallback to synthetic
                    val isPortrait = (_deviceRotation.value == Surface.ROTATION_0 || _deviceRotation.value == Surface.ROTATION_180)
                    val w = if (isPortrait) 1440 else 1920
                    val h = if (isPortrait) 1920 else 1440
                    val dummy = ImageUtils.generateSyntheticScene(width = w, height = h)
                    onPhotoCaptured?.invoke(dummy)
                }
            }
        )
    }

    // MARK: - Video Recording
    @SuppressLint("MissingPermission")
    fun toggleVideoRecording() {
        if (_isRecordingVideo.value) {
            // Stop recording
            activeRecording?.stop()
            activeRecording = null
            _isRecordingVideo.value = false
            return
        }

        val vid = videoCapture ?: return
        val videoFile = File(
            context.cacheDir,
            "SnapLab_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.mp4"
        )
        val outputOptions = FileOutputOptions.Builder(videoFile).build()

        val pending = vid.output.prepareRecording(context, outputOptions)
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            pending.withAudioEnabled()
        }

        activeRecording = pending.start(ContextCompat.getMainExecutor(context)) { recordEvent ->
            when (recordEvent) {
                is VideoRecordEvent.Start -> {
                    _isRecordingVideo.value = true
                    _recordingDurationSec.value = 0
                }
                is VideoRecordEvent.Status -> {
                    _recordingDurationSec.value = recordEvent.recordingStats.recordedDurationNanos / 1_000_000_000L
                }
                is VideoRecordEvent.Finalize -> {
                    _isRecordingVideo.value = false
                    if (!recordEvent.hasError()) {
                        onVideoRecorded?.invoke(videoFile)
                    }
                }
            }
        }
    }

    companion object {
        val shared by lazy { CameraManager() }
    }
}
