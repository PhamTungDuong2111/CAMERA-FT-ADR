package com.snaplab.cameraft.services

import android.graphics.*
import com.snaplab.cameraft.models.VerificationRecord
import com.snaplab.cameraft.models.WatermarkBadgeStyle
import com.snaplab.cameraft.models.WatermarkPosition
import com.snaplab.cameraft.models.WatermarkTemplate
import java.util.Date
import kotlin.math.max
import kotlin.math.min

class WatermarkRenderer private constructor() {

    fun renderWatermark(
        originalBitmap: Bitmap,
        template: WatermarkTemplate,
        verificationRecord: VerificationRecord?,
        timestamp: Long = System.currentTimeMillis(),
        locationState: LocationState = LocationWeatherManager.shared.locationState.value
    ): Bitmap {
        // Create a mutable copy with ARGB_8888 for high quality drawing
        val outputBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(outputBitmap)

        val imageWidth = outputBitmap.width.toFloat()
        val imageHeight = outputBitmap.height.toFloat()

        val baseDimension = min(imageWidth, imageHeight)
        val scaleFactor = (baseDimension / 1080f) * template.scale

        val loc = LocalizationManager.shared
        val isVi = loc.isVietnamese()

        // 1. Prepare Line Items
        data class LineItem(val text: String, val isHighlight: Boolean)
        val lines = mutableListOf<LineItem>()

        // Title
        val title = if (template.titleText.isNotBlank()) {
            template.titleText.uppercase()
        } else {
            template.category.getDisplayName(isVi).uppercase()
        }

        // Time
        if (template.showTime) {
            val timeStr = if (template.showSeconds) {
                VerificationRecord.formatFullDateTime(timestamp)
            } else {
                "${VerificationRecord.formatDisplayDate(timestamp)} ${VerificationRecord.formatDisplayTime(timestamp).take(5)}"
            }
            lines.add(LineItem(loc.t("timeLabel") + timeStr, true))
        }

        // Project & Personnel
        if (template.projectName.isNotBlank()) {
            lines.add(LineItem(loc.t("projectLabel") + template.projectName, false))
        }
        if (template.workItem.isNotBlank()) {
            lines.add(LineItem(loc.t("workItemLabel") + template.workItem, false))
        }
        if (template.contractorName.isNotBlank()) {
            lines.add(LineItem(loc.t("contractorLabel") + template.contractorName, false))
        }
        if (template.inspectorName.isNotBlank()) {
            lines.add(LineItem(loc.t("inspectorLabel") + template.inspectorName, false))
        }

        // Location & GPS
        if (template.showLocation && locationState.fullAddress.isNotBlank()) {
            lines.add(LineItem(loc.t("locationLabel") + locationState.fullAddress, false))
        }
        if (template.showCoordinates) {
            val coordStr = LocationWeatherManager.shared.formattedCoordinates(locationState.latitude, locationState.longitude)
            lines.add(LineItem(loc.t("coordinatesLabel") + coordStr, false))
        }
        if (template.showAltitude) {
            val altStr = String.format(java.util.Locale.US, "%.1f m", locationState.altitude)
            lines.add(LineItem(loc.t("altitudeLabel") + altStr, false))
        }

        // Weather & Compass
        val envParts = mutableListOf<String>()
        if (template.showWeather) {
            envParts.add("${locationState.weatherCondition} ${locationState.temperatureCelsius}°C (Độ ẩm ${locationState.humidityPercent}%)")
        }
        if (template.showCompass) {
            envParts.add(loc.t("compassLabel") + locationState.compassDirection)
        }
        if (envParts.isNotEmpty()) {
            lines.add(LineItem(envParts.joinToString(" • "), false))
        }

        // Notes
        if (template.customNotes.isNotBlank()) {
            lines.add(LineItem(loc.t("notesLabel") + template.customNotes, false))
        }

        // Verification Code
        if (template.showAntiCounterfeitQR && verificationRecord != null) {
            lines.add(LineItem(loc.t("verifyCodeLabel") + verificationRecord.id, true))
        }

        // 2. Measure & Layout
        val cardWidth = min(imageWidth * 0.88f, 620f * scaleFactor)
        val padding = 20f * scaleFactor
        val qrSize = if (template.showAntiCounterfeitQR) 105f * scaleFactor else 0f
        val textWidth = cardWidth - (padding * 2) - (if (template.showAntiCounterfeitQR) qrSize + 16f * scaleFactor else 0f)

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 21f * scaleFactor
            color = Color.WHITE
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val catPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 14f * scaleFactor
            color = template.colorTheme.androidColor
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 13.5f * scaleFactor
            color = Color.argb(235, 255, 255, 255)
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }

        val highlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 13.5f * scaleFactor
            color = template.colorTheme.androidColor
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        // Calculate card height dynamically
        var contentHeight = padding + 34f * scaleFactor // title + header space
        for (item in lines) {
            val paint = if (item.isHighlight) highlightPaint else bodyPaint
            val itemHeight = measureWrappedTextHeight(item.text, paint, textWidth)
            contentHeight += itemHeight + 6f * scaleFactor
        }
        contentHeight += padding

        if (template.showAntiCounterfeitQR) {
            contentHeight = max(contentHeight, qrSize + padding * 2 + 18f * scaleFactor)
        }

        // Card Position
        val margin = 36f * scaleFactor
        var cardX = margin
        var cardY = imageHeight - contentHeight - margin

        when (template.position) {
            WatermarkPosition.BOTTOM_LEFT -> {
                cardX = margin
                cardY = imageHeight - contentHeight - margin
            }
            WatermarkPosition.BOTTOM_RIGHT -> {
                cardX = imageWidth - cardWidth - margin
                cardY = imageHeight - contentHeight - margin
            }
            WatermarkPosition.TOP_LEFT -> {
                cardX = margin
                cardY = margin + 20f * scaleFactor
            }
            WatermarkPosition.TOP_RIGHT -> {
                cardX = imageWidth - cardWidth - margin
                cardY = margin + 20f * scaleFactor
            }
            WatermarkPosition.CENTER_BOTTOM -> {
                cardX = (imageWidth - cardWidth) / 2f
                cardY = imageHeight - contentHeight - margin
            }
        }

        val cardRect = RectF(cardX, cardY, cardX + cardWidth, cardY + contentHeight)
        val cornerRadius = 16f * scaleFactor

        // 3. Draw Background Card according to BadgeStyle
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
        }

        when (template.badgeStyle) {
            WatermarkBadgeStyle.GLASSMORPHISM -> {
                val alpha = (template.opacity * 200).toInt().coerceIn(40, 255)
                bgPaint.color = Color.argb(alpha, 14, 16, 20)
                borderPaint.color = Color.argb(160, Color.red(template.colorTheme.androidColor), Color.green(template.colorTheme.androidColor), Color.blue(template.colorTheme.androidColor))
                borderPaint.strokeWidth = 2.5f * scaleFactor
            }
            WatermarkBadgeStyle.DARK_CARD -> {
                val alpha = (template.opacity * 240).toInt().coerceIn(60, 255)
                bgPaint.color = Color.argb(alpha, 24, 26, 32)
                borderPaint.color = Color.argb(60, 255, 255, 255)
                borderPaint.strokeWidth = 1.5f * scaleFactor
            }
            WatermarkBadgeStyle.BORDERED_STAMP -> {
                val alpha = (template.opacity * 220).toInt().coerceIn(60, 255)
                bgPaint.color = Color.argb(alpha, 10, 10, 12)
                borderPaint.color = template.colorTheme.androidColor
                borderPaint.strokeWidth = 3.5f * scaleFactor
            }
            WatermarkBadgeStyle.MINIMAL_TRANSPARENT -> {
                val alpha = (template.opacity * 110).toInt().coerceIn(20, 255)
                bgPaint.color = Color.argb(alpha, 0, 0, 0)
                borderPaint.color = Color.TRANSPARENT
                borderPaint.strokeWidth = 0f
            }
        }

        canvas.drawRoundRect(cardRect, cornerRadius, cornerRadius, bgPaint)
        if (borderPaint.strokeWidth > 0) {
            canvas.drawRoundRect(cardRect, cornerRadius, cornerRadius, borderPaint)
        }

        // 4. Header: Accent Bar + Title + Category Pill
        val accentBarPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = template.colorTheme.androidColor
            style = Paint.Style.FILL
        }
        val accentBarRect = RectF(
            cardX + padding,
            cardY + padding,
            cardX + padding + 4f * scaleFactor,
            cardY + padding + 22f * scaleFactor
        )
        canvas.drawRoundRect(accentBarRect, 2f * scaleFactor, 2f * scaleFactor, accentBarPaint)

        canvas.drawText(
            title,
            cardX + padding + 12f * scaleFactor,
            cardY + padding + 18f * scaleFactor,
            titlePaint
        )

        val catText = "● " + template.category.getDisplayName(isVi)
        val catTextWidth = catPaint.measureText(catText)
        canvas.drawText(
            catText,
            cardX + cardWidth - padding - catTextWidth,
            cardY + padding + 17f * scaleFactor,
            catPaint
        )

        // Divider
        val dividerY = cardY + padding + 28f * scaleFactor
        val dividerPaint = Paint().apply {
            color = Color.argb(45, 255, 255, 255)
            strokeWidth = 1f * scaleFactor
        }
        canvas.drawLine(cardX + padding, dividerY, cardX + cardWidth - padding, dividerY, dividerPaint)

        // 5. Draw Content Lines
        var currentY = dividerY + 14f * scaleFactor
        for (item in lines) {
            val paint = if (item.isHighlight) highlightPaint else bodyPaint
            val drawnHeight = drawWrappedText(canvas, item.text, cardX + padding, currentY, textWidth, paint)
            currentY += drawnHeight + 5f * scaleFactor
        }

        // 6. Draw Anti-Counterfeiting QR Code
        if (template.showAntiCounterfeitQR && verificationRecord != null) {
            val qrBitmap = ImageUtils.generateQRCode(verificationRecord.toQRCodePayload(), qrSize.toInt())
            if (qrBitmap != null) {
                val qrX = cardX + cardWidth - padding - qrSize
                val qrY = cardY + contentHeight - padding - qrSize - 12f * scaleFactor
                val qrRect = RectF(qrX, qrY, qrX + qrSize, qrY + qrSize)

                // White backing plate for crisp scanning
                val qrPlatePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.WHITE
                    style = Paint.Style.FILL
                }
                val qrPlateRect = RectF(
                    qrX - 4f * scaleFactor,
                    qrY - 4f * scaleFactor,
                    qrX + qrSize + 4f * scaleFactor,
                    qrY + qrSize + 4f * scaleFactor
                )
                canvas.drawRoundRect(qrPlateRect, 6f * scaleFactor, 6f * scaleFactor, qrPlatePaint)

                canvas.drawBitmap(qrBitmap, null, qrRect, null)

                // Label under QR code
                val qrLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.argb(210, 255, 255, 255)
                    textSize = 8.5f * scaleFactor
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    textAlign = Paint.Align.CENTER
                }
                canvas.drawText(
                    loc.t("scanToVerify"),
                    qrX + qrSize / 2f,
                    qrY + qrSize + 11f * scaleFactor,
                    qrLabelPaint
                )
            }
        }

        return outputBitmap
    }

    private fun measureWrappedTextHeight(text: String, paint: Paint, maxWidth: Float): Float {
        val words = text.split(" ")
        var currentLine = ""
        var lineCount = 0

        for (word in words) {
            val candidate = if (currentLine.isEmpty()) word else "$currentLine $word"
            if (paint.measureText(candidate) <= maxWidth) {
                currentLine = candidate
            } else {
                lineCount++
                currentLine = word
            }
        }
        if (currentLine.isNotEmpty()) lineCount++

        val fontSpacing = paint.fontSpacing
        return max(fontSpacing, lineCount * fontSpacing)
    }

    private fun drawWrappedText(canvas: Canvas, text: String, x: Float, y: Float, maxWidth: Float, paint: Paint): Float {
        val words = text.split(" ")
        var currentLine = ""
        var currentY = y
        val fontSpacing = paint.fontSpacing
        var totalHeight = 0f

        for (word in words) {
            val candidate = if (currentLine.isEmpty()) word else "$currentLine $word"
            if (paint.measureText(candidate) <= maxWidth) {
                currentLine = candidate
            } else {
                canvas.drawText(currentLine, x, currentY, paint)
                currentY += fontSpacing
                totalHeight += fontSpacing
                currentLine = word
            }
        }
        if (currentLine.isNotEmpty()) {
            canvas.drawText(currentLine, x, currentY, paint)
            totalHeight += fontSpacing
        }

        return totalHeight
    }

    companion object {
        val shared by lazy { WatermarkRenderer() }
    }
}
