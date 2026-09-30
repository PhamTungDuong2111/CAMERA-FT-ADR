package com.snaplab.cameraft.models

import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.Color as AndroidColor
import androidx.compose.ui.graphics.Color as ComposeColor
import com.snaplab.cameraft.ui.theme.*

enum class PhotoSticker(
    val id: String,
    val title: String,
    val subtitle: String,
    val primaryColorHex: Long,
    val secondaryColorHex: Long
) {
    APPROVED(
        id = "approved",
        title = "ĐÃ DUYỆT",
        subtitle = "OFFICIALLY APPROVED",
        primaryColorHex = 0xFF00E676,
        secondaryColorHex = 0xFF00B0FF
    ),
    PASSED(
        id = "passed",
        title = "NGHIỆM THU ĐẠT",
        subtitle = "QUALITY CONTROL PASSED",
        primaryColorHex = 0xFF0066FF,
        secondaryColorHex = 0xFF00E5FF
    ),
    SAFETY_FIRST(
        id = "safety_first",
        title = "SAFETY FIRST",
        subtitle = "AN TOÀN LÀ TRÊN HẾT",
        primaryColorHex = 0xFFFF6B00,
        secondaryColorHex = 0xFFFFD600
    ),
    URGENT(
        id = "urgent",
        title = "KHẨN CẤP",
        subtitle = "PRIORITY DISPATCH",
        primaryColorHex = 0xFFFF1744,
        secondaryColorHex = 0xFFFF9100
    ),
    HANDOVER(
        id = "handover",
        title = "BÀN GIAO HỒ SƠ",
        subtitle = "OFFICIAL HANDOVER",
        primaryColorHex = 0xFF7C4DFF,
        secondaryColorHex = 0xFFE040FB
    ),
    VIP_SEAL(
        id = "vip_seal",
        title = "VIP SEAL",
        subtitle = "VERIFIED EXCELLENCE",
        primaryColorHex = 0xFFFFD700,
        secondaryColorHex = 0xFFFFAB00
    );

    val composeColor: ComposeColor
        get() = ComposeColor(primaryColorHex)

    val androidColor: Int
        get() = primaryColorHex.toInt()

    // MARK: - Draw Sticker onto Bitmap Canvas
    fun draw(canvas: Canvas, x: Float, y: Float, size: Float) {
        val radius = size / 2f
        val cx = x + radius
        val cy = y + radius

        // Outer Ring Stamp
        val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = androidColor
            style = Paint.Style.STROKE
            strokeWidth = size * 0.04f
        }
        canvas.drawCircle(cx, cy, radius * 0.92f, ringPaint)

        // Dashed / Inset Circle
        val insetPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = androidColor
            style = Paint.Style.STROKE
            strokeWidth = size * 0.018f
            pathEffect = DashPathEffect(floatArrayOf(size * 0.04f, size * 0.03f), 0f)
        }
        canvas.drawCircle(cx, cy, radius * 0.82f, insetPaint)

        // Semi-transparent center fill
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.argb(45, (androidColor shr 16) and 0xFF, (androidColor shr 8) and 0xFF, androidColor and 0xFF)
            style = Paint.Style.FILL
        }
        canvas.drawCircle(cx, cy, radius * 0.82f, fillPaint)

        // Main Title (Curved or centered banner)
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.WHITE
            textSize = size * 0.16f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            setShadowLayer(size * 0.02f, 0f, 0f, AndroidColor.BLACK)
        }
        canvas.drawText(title, cx, cy - size * 0.03f, titlePaint)

        // Subtitle text
        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = androidColor
            textSize = size * 0.09f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(subtitle, cx, cy + size * 0.16f, subPaint)

        // Little stars decoration
        val starPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = androidColor
            textSize = size * 0.12f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("★ ★ ★", cx, cy - size * 0.22f, starPaint)
    }
}
