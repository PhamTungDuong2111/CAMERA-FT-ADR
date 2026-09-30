package com.snaplab.cameraft.services

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Shader
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.util.EnumMap

object ImageUtils {

    // MARK: - Generate QR Code Bitmap using ZXing
    fun generateQRCode(text: String, sizePx: Int): Bitmap? {
        return try {
            val hints = EnumMap<EncodeHintType, Any>(EncodeHintType::class.java)
            hints[EncodeHintType.CHARACTER_SET] = "UTF-8"
            hints[EncodeHintType.ERROR_CORRECTION] = ErrorCorrectionLevel.M
            hints[EncodeHintType.MARGIN] = 1

            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, sizePx, sizePx, hints)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

            for (x in 0 until width) {
                for (y in 0 until height) {
                    bmp.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            bmp
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // MARK: - Fix Orientation
    fun rotateBitmap(source: Bitmap, angle: Float): Bitmap {
        if (angle == 0f) return source
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    // MARK: - Generate Synthetic Scene for Simulator / Emulator without Webcam
    fun generateSyntheticScene(
        width: Int = 1920,
        height: Int = 1440,
        sceneTitle: String = "SnapLab HD Live View"
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Gradient Background (Sky to Ground Architectural Scene)
        val skyPaint = Paint().apply {
            shader = LinearGradient(
                0f, 0f, 0f, height.toFloat(),
                intArrayOf(
                    Color.rgb(31, 89, 166),   // Deep Blue Sky
                    Color.rgb(115, 166, 217), // Soft Sky
                    Color.rgb(217, 191, 153), // Horizon Warmth
                    Color.rgb(77, 82, 89)     // Ground / Concrete
                ),
                floatArrayOf(0f, 0.45f, 0.70f, 1.0f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), skyPaint)

        // Draw Simulated Construction Crane & Grid Silhouettes
        val cranePaint = Paint().apply {
            color = Color.argb(120, 255, 255, 255)
            strokeWidth = 3f * (width / 1080f)
            style = Paint.Style.STROKE
        }
        val w = width.toFloat()
        val h = height.toFloat()

        // Tower crane vertical mast
        val mastX = w * 0.75f
        canvas.drawLine(mastX, h * 0.2f, mastX, h * 0.85f, cranePaint)
        canvas.drawLine(mastX + 30f, h * 0.2f, mastX + 30f, h * 0.85f, cranePaint)
        // Jib
        canvas.drawLine(mastX - w * 0.45f, h * 0.25f, mastX + w * 0.15f, h * 0.25f, cranePaint)

        // Grid guide in center
        val gridPaint = Paint().apply {
            color = Color.argb(40, 255, 255, 255)
            strokeWidth = 1.5f
            style = Paint.Style.STROKE
        }
        canvas.drawCircle(w / 2f, h / 2f, 120f * (width / 1080f), gridPaint)
        canvas.drawLine(w / 2f - 180f, h / 2f, w / 2f + 180f, h / 2f, gridPaint)
        canvas.drawLine(w / 2f, h / 2f - 180f, w / 2f, h / 2f + 180f, gridPaint)

        // Subtitle badge
        val textPaint = Paint().apply {
            color = Color.argb(220, 255, 255, 255)
            textSize = 28f * (width / 1080f)
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(sceneTitle, w / 2f, h * 0.12f, textPaint)

        return bitmap
    }
}
