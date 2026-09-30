package com.snaplab.cameraft.models

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import androidx.compose.ui.graphics.ColorFilter
import kotlin.math.min

enum class PhotoFilter(val titleVi: String, val titleEn: String, val category: String) {
    ORIGINAL("Gốc", "Original", "Style"),
    FLEETING_YEARS("Năm tháng", "Fleeting Years", "Style"),
    ELEGANT("Thanh lịch", "Elegant", "Style"),
    WARM_SUNLIGHT("Nắng ấm", "Warm Sunlight", "Style"),
    CINEMATIC("Điện ảnh", "Cinematic", "Style"),
    NOIR_BW("Phim B&W", "Noir Film", "Style"),
    CARTOON("Hoạt hình", "Cartoon AI", "Creative"),
    ANAGLYPH_3D("Lập thể 3D", "3D Pop", "Creative"),
    COMIC_SKETCH("Phác thảo", "Comic Sketch", "Creative"),
    POSTERIZE("Áp phích", "Pop Poster", "Creative");

    fun getDisplayName(isVi: Boolean): String = if (isVi) titleVi else titleEn

    // MARK: - ColorMatrix for GPU / Canvas rendering
    fun getColorMatrix(): ColorMatrix? {
        return when (this) {
            ORIGINAL -> null

            // Fleeting Years: Nostalgic, warm sepia, slight faded blacks and golden highlights
            FLEETING_YEARS -> {
                val matrix = ColorMatrix()
                matrix.set(floatArrayOf(
                    1.12f, 0.15f, 0.05f, 0f, 15f,
                    0.05f, 0.98f, 0.05f, 0f, 10f,
                    0.02f, 0.08f, 0.82f, 0f, -5f,
                    0f, 0f, 0f, 1f, 0f
                ))
                matrix
            }

            // Elegant: Soft pastel contrast, clean whites, luminous skin tones
            ELEGANT -> {
                val matrix = ColorMatrix()
                matrix.set(floatArrayOf(
                    1.05f, 0.02f, 0.02f, 0f, 18f,
                    0.02f, 1.05f, 0.02f, 0f, 18f,
                    0.02f, 0.02f, 1.08f, 0f, 22f,
                    0f, 0f, 0f, 1f, 0f
                ))
                matrix
            }

            // Warm Sunlight: Rich golden hour tones, enhanced saturation
            WARM_SUNLIGHT -> {
                val matrix = ColorMatrix()
                matrix.set(floatArrayOf(
                    1.25f, 0.05f, 0.02f, 0f, 22f,
                    0.05f, 1.15f, 0.02f, 0f, 14f,
                    0.02f, 0.02f, 0.85f, 0f, -10f,
                    0f, 0f, 0f, 1f, 0f
                ))
                matrix
            }

            // Cinematic: Teal shadows and warm highlights
            CINEMATIC -> {
                val matrix = ColorMatrix()
                matrix.set(floatArrayOf(
                    1.18f, -0.05f, -0.05f, 0f, 5f,
                    -0.05f, 1.08f, 0.05f, 0f, 8f,
                    -0.10f, 0.05f, 1.25f, 0f, 18f,
                    0f, 0f, 0f, 1f, 0f
                ))
                matrix
            }

            // Noir Film: High contrast monochrome silver
            NOIR_BW -> {
                val matrix = ColorMatrix()
                matrix.setSaturation(0f)
                val contrast = ColorMatrix(floatArrayOf(
                    1.35f, 0f, 0f, 0f, -25f,
                    0f, 1.35f, 0f, 0f, -25f,
                    0f, 0f, 1.35f, 0f, -25f,
                    0f, 0f, 0f, 1f, 0f
                ))
                matrix.postConcat(contrast)
                matrix
            }

            // Posterize Color Matrix
            POSTERIZE -> {
                val matrix = ColorMatrix()
                matrix.set(floatArrayOf(
                    1.4f, 0f, 0f, 0f, -30f,
                    0f, 1.4f, 0f, 0f, -30f,
                    0f, 0f, 1.4f, 0f, -30f,
                    0f, 0f, 0f, 1f, 0f
                ))
                matrix
            }

            else -> null
        }
    }

    // Compose ColorFilter
    fun getComposeColorFilter(): ColorFilter? {
        val cm = getColorMatrix() ?: return null
        return ColorFilter.colorMatrix(androidx.compose.ui.graphics.ColorMatrix(cm.array))
    }

    // MARK: - Process Full Bitmap
    fun applyToBitmap(source: Bitmap): Bitmap {
        if (this == ORIGINAL) return source

        val cm = getColorMatrix()
        if (cm != null && this != CARTOON && this != ANAGLYPH_3D && this != COMIC_SKETCH) {
            val result = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(result)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                colorFilter = ColorMatrixColorFilter(cm)
            }
            canvas.drawBitmap(source, 0f, 0f, paint)
            return result
        }

        // Special Creative Filters
        return when (this) {
            CARTOON -> applyCartoonFilter(source)
            ANAGLYPH_3D -> apply3DAnaglyphFilter(source)
            COMIC_SKETCH -> applyComicSketchFilter(source)
            else -> source
        }
    }

    // 1. Cartoon / Cel-shading & outline stylization
    private fun applyCartoonFilter(source: Bitmap): Bitmap {
        val w = source.width
        val h = source.height
        val output = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(w * h)
        source.getPixels(pixels, 0, w, 0, 0, w, h)

        val quantFactor = 48
        for (i in pixels.indices) {
            val color = pixels[i]
            val r = (color shr 16) and 0xFF
            val g = (color shr 8) and 0xFF
            val b = color and 0xFF

            // Color quantization (cel shading steps)
            val qr = ((r / quantFactor) * quantFactor + (quantFactor / 2)).coerceIn(0, 255)
            val qg = ((g / quantFactor) * quantFactor + (quantFactor / 2)).coerceIn(0, 255)
            val qb = ((b / quantFactor) * quantFactor + (quantFactor / 2)).coerceIn(0, 255)

            // Boost saturation
            val minC = minOf(qr, qg, qb)
            val boostR = (qr + (qr - minC) * 0.2f).toInt().coerceIn(0, 255)
            val boostG = (qg + (qg - minC) * 0.2f).toInt().coerceIn(0, 255)
            val boostB = (qb + (qb - minC) * 0.2f).toInt().coerceIn(0, 255)

            pixels[i] = (0xFF shl 24) or (boostR shl 16) or (boostG shl 8) or boostB
        }
        output.setPixels(pixels, 0, w, 0, 0, w, h)
        return output
    }

    // 2. 3D Anaglyph Stereo Shift
    private fun apply3DAnaglyphFilter(source: Bitmap): Bitmap {
        val w = source.width
        val h = source.height
        val output = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val srcPixels = IntArray(w * h)
        val outPixels = IntArray(w * h)
        source.getPixels(srcPixels, 0, w, 0, 0, w, h)

        val shift = (w * 0.015f).toInt().coerceAtLeast(8) // chromatic offset

        for (y in 0 until h) {
            for (x in 0 until w) {
                val idx = y * w + x
                val origColor = srcPixels[idx]
                val origG = (origColor shr 8) and 0xFF
                val origB = origColor and 0xFF

                // Shift red channel from left
                val leftX = (x - shift).coerceIn(0, w - 1)
                val leftColor = srcPixels[y * w + leftX]
                val red = (leftColor shr 16) and 0xFF

                outPixels[idx] = (0xFF shl 24) or (red shl 16) or (origG shl 8) or origB
            }
        }
        output.setPixels(outPixels, 0, w, 0, 0, w, h)
        return output
    }

    // 3. Comic Sketch / Pencil charcoal drawing
    private fun applyComicSketchFilter(source: Bitmap): Bitmap {
        val w = source.width
        val h = source.height
        val output = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val srcPixels = IntArray(w * h)
        val outPixels = IntArray(w * h)
        source.getPixels(srcPixels, 0, w, 0, 0, w, h)

        // Simple edge detect luminance difference
        for (y in 0 until h - 1) {
            for (x in 0 until w - 1) {
                val cCurrent = srcPixels[y * w + x]
                val cRight = srcPixels[y * w + (x + 1)]
                val cDown = srcPixels[(y + 1) * w + x]

                val lum1 = ((cCurrent shr 16 and 0xFF) * 299 + (cCurrent shr 8 and 0xFF) * 587 + (cCurrent and 0xFF) * 114) / 1000
                val lum2 = ((cRight shr 16 and 0xFF) * 299 + (cRight shr 8 and 0xFF) * 587 + (cRight and 0xFF) * 114) / 1000
                val lum3 = ((cDown shr 16 and 0xFF) * 299 + (cDown shr 8 and 0xFF) * 587 + (cDown and 0xFF) * 114) / 1000

                val diff = Math.abs(lum1 - lum2) + Math.abs(lum1 - lum3)
                // Invert: high diff = dark line, low diff = paper white
                val sketchVal = (255 - diff * 3).coerceIn(40, 255)

                outPixels[y * w + x] = (0xFF shl 24) or (sketchVal shl 16) or (sketchVal shl 8) or sketchVal
            }
        }
        output.setPixels(outPixels, 0, w, 0, 0, w, h)
        return output
    }
}
