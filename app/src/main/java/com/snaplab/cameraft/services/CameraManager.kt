package com.snaplab.cameraft.services

import android.annotation.SuppressLint
import android.content.Context
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
    private val sound = MediaActionSound().apply { load(MediaActionSound.SHUTTER_CLICK) }

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

    // Callbacks
    var onPhotoCaptured: ((Bitmap) -> Unit)? = null
    var onVideoRecorded: ((File) -> Unit)? = null

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
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                bindCameraUseCases(lifecycleOwner, previewView)
                _isSyntheticMode.value = false
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

        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            .setTargetAspectRatio(targetRatio)
            .build()

        val recorder = Recorder.Builder()
            .setQualitySelector(QualitySelector.from(Quality.HIGHEST))
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
            applyFlashSettings()
            camera?.cameraControl?.setZoomRatio(_zoomRatio.value)
        } catch (e: Exception) {
            e.printStackTrace()
            _isSyntheticMode.value = true
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
        sound.play(MediaActionSound.SHUTTER_CLICK)

        if (_isSyntheticMode.value || imageCapture == null) {
            // Synthetic scene fallback
            val dummyBitmap = ImageUtils.generateSyntheticScene()
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
                        val bitmap = BitmapFactory.decodeFile(tempFile.absolutePath)
                        // If front camera, mirror horizontally
                        val finalBitmap = if (_isFrontCamera.value) {
                            val matrix = Matrix().apply { preScale(-1.0f, 1.0f) }
                            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                        } else {
                            bitmap
                        }
                        tempFile.delete()
                        onPhotoCaptured?.invoke(finalBitmap)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    exception.printStackTrace()
                    // Fallback to synthetic
                    val dummy = ImageUtils.generateSyntheticScene()
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

        activeRecording = vid.output
            .prepareRecording(context, outputOptions)
            .start(ContextCompat.getMainExecutor(context)) { recordEvent ->
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
